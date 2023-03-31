package singleplayer.update.mixin;


import net.minecraft.block.BlockState;
import net.minecraft.entity.*;
import net.minecraft.entity.ai.RangedAttackMob;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.mob.IllusionerEntity;
import net.minecraft.entity.mob.SpellcastingIllagerEntity;
import net.minecraft.entity.projectile.PersistentProjectileEntity;
import net.minecraft.entity.projectile.ProjectileUtil;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.LocalDifficulty;
import net.minecraft.world.ServerWorldAccess;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import net.minecraft.util.math.random.Random;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(IllusionerEntity.class)
public class IllusionerMixin extends SpellcastingIllagerEntity implements RangedAttackMob {

    public IllusionerMixin(EntityType<? extends IllusionerEntity> entityType, World world) {
        super(entityType, world);
    }

    IllusionerEntity illusioner = (IllusionerEntity)(Object)this;

    @Shadow
    private int mirrorSpellTimer;

    Random random = Random.create();

    int spellCooldown = 0;
    int summonTimer = 0;
    int appearTimer;
    Vec3d targetPos;

    @Override
    public EntityData initialize(ServerWorldAccess world, LocalDifficulty difficulty, SpawnReason spawnReason, @Nullable EntityData entityData, @Nullable NbtCompound entityNbt) {
        if (spawnReason == SpawnReason.TRIGGERED){
            this.mirrorSpellTimer = 255;
        }
        this.equipStack(EquipmentSlot.MAINHAND, new ItemStack(Items.BOW));
        return super.initialize(world, difficulty, spawnReason, entityData, entityNbt);
    }

    @Override
    public boolean isSpellcasting() {
        if (this.mirrorSpellTimer == 255 || this.spellCooldown > 0) {
            StackTraceElement[] stack = Thread.currentThread().getStackTrace();
            String methodName = stack[2].getMethodName();
            return methodName.equals("canStart") || methodName.equals("method_6264");
        }
        return super.isSpellcasting();
    }

    @Override
    public boolean addStatusEffect(StatusEffectInstance effect, @Nullable Entity source) {
        if (effect.getEffectType() == StatusEffects.INVISIBILITY && effect.getDuration() == 1200 && source == null && this.mirrorSpellTimer != 255 && illusioner.getTarget() != null) {
            this.summonTimer = 40;
            this.appearTimer = 20 + this.random.nextInt(20);
            effect = new StatusEffectInstance(StatusEffects.INVISIBILITY, this.appearTimer);
            boolean bl = super.addStatusEffect(effect);
            illusioner.world.addParticle(ParticleTypes.CLOUD, illusioner.getParticleX(0.5), illusioner.getRandomBodyY(), illusioner.offsetZ(0.5), 0.0, 0.0, 0.0);
            for (int i = 0; i < 64; ++i) {
                if (this.teleportTo(illusioner, illusioner.getTarget())) {
                    return bl;
                }
            }
            return bl;
        }
        return super.addStatusEffect(effect, null);
    }

    @Override
    public void tickMovement() {
        if (illusioner.getTarget()!=null && !illusioner.getTarget().isAlive()) {illusioner.setTarget(null);}
        super.tickMovement();
        --this.spellCooldown;
        if (this.mirrorSpellTimer == 255 && illusioner.age >= 1200) {
            illusioner.discard();
        }
        if (this.spellCooldown < 0) {
            this.spellCooldown = 0;
        }
        if (this.summonTimer > 0) {
            --this.summonTimer;
            --this.appearTimer;
            if (this.appearTimer != 0 && this.summonTimer < 20 && illusioner.getTarget() != null) {
                summonFake(illusioner.getTarget());
                this.spellCooldown = 1200;
            }
        }
        if (illusioner.getTarget() != null) {
            this.targetPos = illusioner.getTarget().getPos();
        }
    }

    @Inject(at=@At("RETURN"), method="getMirrorCopyOffsets", cancellable = true)
    void removeOffsets(float tickDelta, CallbackInfoReturnable<Vec3d[]> cir) {
        cir.setReturnValue(new Vec3d[0]);
    }

    boolean teleportTo(IllusionerEntity illusionerEntity, LivingEntity target) {
        Vec3d pos = target.getPos();
        int x = (int)(pos.getX() + (random.nextDouble() - 0.5D) * 16.0D);
        int y = (int)(pos.getY() + (random.nextDouble() - 0.5D) * 16.0D);
        int z = (int)(pos.getZ() + (random.nextDouble() - 0.5D) * 16.0D);

        BlockPos.Mutable mutable = new BlockPos.Mutable(x, y, z);

        while(mutable.getY() > world.getBottomY() && !world.getBlockState(mutable).getMaterial().blocksMovement()) {
            mutable.move(Direction.DOWN);
        }
        BlockState blockState = world.getBlockState(mutable);
        boolean bl = false;
        if (blockState.getMaterial().blocksMovement()) {
            bl = illusionerEntity.teleport(x, y, z, false);
        }
        return bl;
    }

    void summonFake(LivingEntity target){
        World world = illusioner.world;
        IllusionerEntity illusionerEntity = EntityType.ILLUSIONER.create(world);
        if (illusionerEntity == null) {return;}
        LocalDifficulty localDifficulty = world.getLocalDifficulty(illusioner.getBlockPos());
        illusionerEntity.initialize((ServerWorld)world, localDifficulty, SpawnReason.TRIGGERED, null, null);
        if (!teleportTo(illusionerEntity, target) && !teleportTo(illusionerEntity, target)) {return;}
        illusionerEntity.setTarget(target);
        illusionerEntity.age = random.nextInt(200);
        ((ServerWorld)world).spawnEntityAndPassengers(illusionerEntity);
        illusionerEntity.playSound(getCastSpellSound(), 1.0f, 1.0f);
        illusionerEntity.lookAtEntity(target, 200, 200);
        world.addParticle(ParticleTypes.CLOUD, illusionerEntity.getParticleX(0.5), illusionerEntity.getRandomBodyY(), illusionerEntity.offsetZ(0.5), 0.0, 0.0, 0.0);
    }

    @Override
    public void attack(LivingEntity target, float pullProgress) {
        if (this.appearTimer > 0) {return ;}
        ItemStack itemStack = this.getProjectileType(this.getStackInHand(ProjectileUtil.getHandPossiblyHolding(this, Items.BOW)));
        PersistentProjectileEntity arrow;
        if (this.mirrorSpellTimer == 255) {
            arrow = ProjectileUtil.createArrowProjectile(this, itemStack, pullProgress*0.01f);
            arrow.setDamage(0.1);
        } else {
            arrow = ProjectileUtil.createArrowProjectile(this, itemStack, pullProgress*2f);
        }
        Vec3d rPos = illusioner.getPos().relativize(target.getPos());
        double dt = rPos.length()/2;
        Vec3d pos2 = target.getVelocity().multiply(dt);
        if (pos2.getX() == 0 && pos2.getZ() == 0) {
            pos2 = targetPos.relativize(target.getPos()).multiply(dt);
        }
        double d = rPos.getX() + pos2.getX();
        double e = target.getBodyY(0.3333333333333333D) - arrow.getY();
        double f = rPos.getZ() + pos2.getZ();
        double g = rPos.horizontalLength();
        arrow.setVelocity(d, e + g * (double)0.2f, f, 2f, 14 - this.world.getDifficulty().getId() * 4);
        this.playSound(SoundEvents.ENTITY_SKELETON_SHOOT, 1.0f, 1.0f / (this.getRandom().nextFloat() * 0.4f + 0.8f));
        this.world.spawnEntity(arrow);
    }

    @Override
    public boolean damage(DamageSource source, float amount) {
        if (source.getAttacker() instanceof IllusionerEntity && source.getSource() instanceof PersistentProjectileEntity) {
            PersistentProjectileEntity arrow = (PersistentProjectileEntity)(source.getSource());
            arrow.setPierceLevel((byte)(arrow.getPierceLevel() + 1));
            return true;
        }
        if (amount >= 1f && this.mirrorSpellTimer == 255) {
            illusioner.world.addParticle(ParticleTypes.CLOUD, illusioner.getParticleX(0.5), illusioner.getRandomBodyY(), illusioner.offsetZ(0.5), 0.0, 0.0, 0.0);
            illusioner.discard();
            return true;
        }
        return super.damage(source, amount);
    }

    public void addBonusForWave(int wave, boolean unused) {}

    public SoundEvent getCelebratingSound() {return SoundEvents.ENTITY_ILLUSIONER_AMBIENT;}

    public SoundEvent getCastSpellSound() {return SoundEvents.ENTITY_ILLUSIONER_CAST_SPELL;}
}
