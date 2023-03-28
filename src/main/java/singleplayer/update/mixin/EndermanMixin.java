package singleplayer.update.mixin;

import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.pathing.Path;
import net.minecraft.entity.boss.dragon.EnderDragonEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.thrown.PotionEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionUtil;
import net.minecraft.potion.Potions;
import net.minecraft.registry.tag.DamageTypeTags;
import net.minecraft.registry.tag.FluidTags;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraft.world.event.GameEvent;
import org.spongepowered.asm.mixin.Mixin;
import net.minecraft.entity.mob.EndermanEntity;
import net.minecraft.util.math.random.Random;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Arrays;
import java.util.List;

@Mixin(EndermanEntity.class)
public class EndermanMixin extends HostileEntity {

    public EndermanMixin(EntityType<? extends EndermanEntity> entityType, World world) {
        super(entityType, world);
    }

    @Shadow
    private int ageWhenTargetSet;
    EndermanEntity enderman = (EndermanEntity)(Object)this;
    Random random = Random.create();
    String[] avoidable = {"player", "mob", "fallingBlock", "anvil", "fallingStalactite", "sonic_boom"};

    private boolean teleportRandomly2() {
        if (!enderman.world.isClient() && enderman.isAlive()) {
            if (enderman.getTarget() != null) {
                LivingEntity target = enderman.getTarget();
                Path path = enderman.getNavigation().findPathTo(target, 1);
                if ((path == null || !path.reachesTarget()) && enderman.squaredDistanceTo(target.getX(), target.getY(), target.getZ()) > 1.5f) {
                    if (teleportTo2(target.getX(), target.getY(), target.getZ())) {
                        return true;
                    }
                } else {
                    if ((random.nextInt(1) == 0) && teleportTo2(target)) {
                        return true;
                    }
                }
            }
            double d = enderman.getX() + (random.nextDouble() - 0.5D) * 64.0D;
            double e = enderman.getY() + (double)(random.nextInt(64) - 32);
            double f = enderman.getZ() + (random.nextDouble() - 0.5D) * 64.0D;
            return teleportTo2(d, e, f);
        } else {
            return false;
        }
    }

    boolean teleportTo2(Entity entity) {
        Vec3d vec3d = new Vec3d(enderman.getX() - entity.getX(), enderman.getBodyY(0.5D) - entity.getEyeY(), enderman.getZ() - entity.getZ());
        vec3d = vec3d.normalize();
        double d = MathHelper.clamp(vec3d.length() + 2, 5D, 255.0D);
        if (d > 32) {
            d -= 5;
        }
        double e = enderman.getX() + (random.nextDouble() - 0.5D) * 6.0D - vec3d.x * d;
        double f = enderman.getY() + (double)(random.nextInt(16) - 8) - vec3d.y * d;
        double g = enderman.getZ() + (random.nextDouble() - 0.5D) * 6.0D - vec3d.z * d;
        if (teleportTo2(e, f, g)){
            enderman.lookAtEntity(entity, 200f, 200f);
            return true;
        }
        return false;
    }

    private boolean teleportTo2(double x, double y, double z) {
        BlockPos.Mutable mutable = new BlockPos.Mutable(x, y, z);

        while(mutable.getY() > enderman.world.getBottomY() && !enderman.world.getBlockState(mutable).getMaterial().blocksMovement()) {
            mutable.move(Direction.DOWN);
        }

        BlockState blockState = enderman.world.getBlockState(mutable);
        boolean bl = blockState.getMaterial().blocksMovement();
        boolean bl2 = blockState.getFluidState().isIn(FluidTags.WATER);
        if (bl && !bl2) {
            Vec3d vec3d = enderman.getPos();
            enderman.dismountVehicle();
            boolean bl3 = enderman.teleport(x, y, z, true);
            if (bl3) {
                enderman.setVelocity(Vec3d.ZERO);
                enderman.world.emitGameEvent(GameEvent.TELEPORT, vec3d, GameEvent.Emitter.of(enderman));
                if (!enderman.isSilent()) {
                    enderman.world.playSound(null, enderman.prevX, enderman.prevY, enderman.prevZ, SoundEvents.ENTITY_ENDERMAN_TELEPORT, enderman.getSoundCategory(), 1.0F, 1.0F);
                    enderman.playSound(SoundEvents.ENTITY_ENDERMAN_TELEPORT, 1.0F, 1.0F);
                }
            }

            return bl3;
        } else {
            return false;
        }
    }

    private boolean damageFromPotion2(DamageSource source, PotionEntity potion, float amount) {
        ItemStack itemStack = potion.getStack();
        Potion potion2 = PotionUtil.getPotion(itemStack);
        List<StatusEffectInstance> list = PotionUtil.getPotionEffects(itemStack);
        boolean bl = potion2 == Potions.WATER && list.isEmpty();
        if (!bl) {return false;}
        return super.damage(source, amount);
    }

    @Override
    public boolean damage(DamageSource source, float amount) {

        if (enderman.isInvulnerableTo(source)) {
            return false;
        } else {
            boolean bl = source.getSource() instanceof PotionEntity;
            boolean bl2;
            if (!source.isIn(DamageTypeTags.IS_PROJECTILE) && !(Arrays.asList(avoidable).contains(source.getType().msgId())) && !bl) {
                bl2 = super.damage(source, amount);
                if (!enderman.world.isClient() && !(source.getAttacker() instanceof LivingEntity) && random.nextInt(10) != 0) {
                    teleportRandomly2();
                }

                return bl2;
            } else {
                bl2 = bl && damageFromPotion2(source, (PotionEntity)source.getSource(), amount);
                if (source.getAttacker() instanceof LivingEntity) {
                    if (!source.isIn(DamageTypeTags.IS_PROJECTILE) && !source.isSourceCreativePlayer()) {
                        if (!(source.getAttacker() instanceof EnderDragonEntity)) {
                            if (enderman.getTarget() == null || (!(enderman.getTarget() instanceof PlayerEntity) && (source.getAttacker() instanceof PlayerEntity))) {
                                enderman.setTarget((LivingEntity) source.getAttacker());
                                enderman.playAngrySound();
                                enderman.playSound(SoundEvents.ENTITY_ENDERMAN_SCREAM, 4F, 1.0F);
                            } else if (enderman.getTarget() == source.getAttacker()) {
                                enderman.setTarget((LivingEntity) source.getAttacker());
                            }
                        }
                    }
                }
                for(int i = 0; i < 64; ++i) {
                    if (teleportRandomly2()) {
                        return true;
                    }
                }
                boolean bl3 = super.damage(source, amount);
                return bl3 || bl2;
            }
        }
    }

    @Inject(at = @At("HEAD"), method = "mobTick")
    public void mobTick(CallbackInfo cir) {
        if ((enderman.getVelocity().getY() < -0.7) || ((enderman.age - this.ageWhenTargetSet) % 100 == 0)) {
            for (int i = 0; i < 64; ++i) {
                if (teleportRandomly2()) {
                    break;
                }
            }
        }
    }

}
