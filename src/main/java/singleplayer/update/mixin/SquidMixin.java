package singleplayer.update.mixin;

import net.minecraft.block.Blocks;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.TargetPredicate;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.mob.WaterCreatureEntity;
import net.minecraft.entity.passive.SquidEntity;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;
import java.util.function.Predicate;

@Mixin(SquidEntity.class)
public class SquidMixin extends WaterCreatureEntity {

    Predicate<LivingEntity> BLIND_FILTER = entity -> entity.getType() != EntityType.WARDEN;
    TargetPredicate BLIND_PREDICATE = TargetPredicate.createNonAttackable().ignoreDistanceScalingFactor().ignoreVisibility().setPredicate(BLIND_FILTER);
    SquidEntity squid = (SquidEntity)(Object)this;
    Vec3d velocity = Vec3d.ZERO;


    public SquidMixin(EntityType<? extends SquidEntity> entityType, World world) {
        super(entityType, world);
    }

    @Inject(at = @At("RETURN"), method = "damage")
    public void damage(DamageSource source, float amount, CallbackInfoReturnable<Boolean> cir) {
        if (cir.getReturnValue()) {
            List<LivingEntity> list = squid.world.getEntitiesByClass(LivingEntity.class, squid.getBoundingBox().expand(2D), entity -> BLIND_PREDICATE.test(squid, entity));
            for (LivingEntity entity : list) {
                if (!entity.isAlive()) continue;
                entity.addStatusEffect(new StatusEffectInstance(StatusEffects.BLINDNESS, 100 , 0), squid);
            }
        }
    }

    @Inject(at=@At(value="INVOKE", target="net/minecraft/entity/mob/WaterCreatureEntity.tickMovement()V", shift = At.Shift.AFTER), method="tickMovement")
    public void getSpeed(CallbackInfo inf) {
        velocity = squid.getVelocity();
    }

    @Inject(at=@At("TAIL"), method="tickMovement")
    public void editSpeed(CallbackInfo inf) {
        if (!squid.isInsideWaterOrBubbleColumn()) {
            squid.addVelocity(velocity.multiply(new Vec3d(0.8D, 0.1D, 0.8D)));
        } else if (squid.getWorld().getBlockState(squid.getBlockPos()).isOf(Blocks.BUBBLE_COLUMN)) {
            velocity = velocity.multiply(new Vec3d(0D, 0.9D, 0D)).add(squid.getVelocity().multiply(new Vec3d(1D, 0D, 1D)));
            squid.setVelocity(velocity);
        } else {
            squid.addVelocity(velocity.multiply(new Vec3d(0D, 0.2D, 0D)));
        }

    }

}

