package singleplayer.update.mixin;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.RangedAttackMob;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.mob.AbstractSkeletonEntity;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.entity.mob.StrayEntity;
import net.minecraft.entity.mob.WitherSkeletonEntity;
import net.minecraft.entity.projectile.ArrowEntity;
import net.minecraft.entity.projectile.PersistentProjectileEntity;
import net.minecraft.entity.projectile.ProjectileUtil;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(AbstractSkeletonEntity.class)
public class AbstractSkeletonMixin extends HostileEntity implements RangedAttackMob {

    public AbstractSkeletonMixin(EntityType<? extends AbstractSkeletonEntity> entityType, World world) {
        super(entityType, world);
    }

    AbstractSkeletonEntity skeleton = (AbstractSkeletonEntity)(Object)this;
    Vec3d targetPos;

    @Inject(at = @At("TAIL"), method = "tickMovement")
    public void tickMovement(CallbackInfo inf) {
        if (skeleton.getTarget() != null) {
            this.targetPos = skeleton.getTarget().getPos();
        }
    }
    
    @Override
    public void attack(LivingEntity target, float pullProgress) {
        ItemStack itemStack = new ItemStack(Items.ARROW);
        PersistentProjectileEntity persistentProjectileEntity = ProjectileUtil.createArrowProjectile(skeleton, itemStack, pullProgress);
        if (skeleton instanceof StrayEntity) {
            ((ArrowEntity)persistentProjectileEntity).addEffect(new StatusEffectInstance(StatusEffects.SLOWNESS, 600));
            ((ArrowEntity)persistentProjectileEntity).addEffect(new StatusEffectInstance(StatusEffects.MINING_FATIGUE, 600));
        } else if (skeleton instanceof WitherSkeletonEntity){
            persistentProjectileEntity.setOnFireFor(100);
            ((ArrowEntity)persistentProjectileEntity).addEffect(new StatusEffectInstance(StatusEffects.WITHER, 100));
        }
        Vec3d rPos = skeleton.getPos().relativize(target.getPos());
        double dt = rPos.length()/2;
        Vec3d pos2 = target.getVelocity().multiply(dt);
        if (pos2.getX() == 0 && pos2.getZ() == 0) {
            pos2 = this.targetPos.relativize(target.getPos()).multiply(dt);
        }
        double d = rPos.getX() + pos2.getX();
        double e = target.getBodyY(0.3333333333333333D) - persistentProjectileEntity.getY();
        double f = rPos.getZ() + pos2.getZ();
        double g = rPos.horizontalLength();
        persistentProjectileEntity.setVelocity(d, e + g * 0.20000000298023224D, f, 2F, (float)(14 - skeleton.world.getDifficulty().getId() * 4));
        skeleton.playSound(SoundEvents.ENTITY_SKELETON_SHOOT, 1.0F, 1.0F / (skeleton.getRandom().nextFloat() * 0.4F + 0.8F));
        skeleton.world.spawnEntity(persistentProjectileEntity);
    }
    
}
