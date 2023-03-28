package singleplayer.update.mixin;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.NoPenaltyTargeting;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.ai.goal.SkeletonHorseTrapTriggerGoal;
import net.minecraft.entity.ai.pathing.EntityNavigation;
import net.minecraft.entity.ai.pathing.Path;
import net.minecraft.entity.mob.SkeletonEntity;
import net.minecraft.entity.mob.SkeletonHorseEntity;
import net.minecraft.util.math.Vec3d;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(SkeletonHorseTrapTriggerGoal.class)
public class TrapTriggerMixin extends Goal {

    @Final
    @Shadow
    private SkeletonHorseEntity skeletonHorse;

    float fleeDistance = 5;
    float squaredMaxShootRange = 225;
    EntityNavigation fleeingEntityNavigation;
    LivingEntity targetEntity;
    LivingEntity classToFleeFrom;
    Path fleePath;
    boolean fleeing;



    @Override
    public boolean canStart() {
        if (this.skeletonHorse.getFirstPassenger() instanceof SkeletonEntity passenger) {
            this.classToFleeFrom = passenger.getTarget();
            if (passenger.getTarget() == null) {
                return false;
            }
            this.targetEntity = passenger.getTarget();
            this.fleeingEntityNavigation = this.skeletonHorse.getNavigation();
            double d = this.skeletonHorse.squaredDistanceTo(this.targetEntity.getX(), this.targetEntity.getY(), this.targetEntity.getZ());
            this.fleeing = !(d > (double)this.squaredMaxShootRange || !passenger.canSee(targetEntity));
            if (!fleeing) {
                this.fleePath = fleeingEntityNavigation.findPathTo(targetEntity, 0);
            } else if (d < (double)this.fleeDistance){
                Vec3d vec3d = NoPenaltyTargeting.findFrom(this.skeletonHorse, 16, 7, this.targetEntity.getPos());
                if (vec3d == null) {
                    return false;
                }
                if (this.targetEntity.squaredDistanceTo(vec3d.x, vec3d.y, vec3d.z) < this.targetEntity.squaredDistanceTo(this.skeletonHorse)) {
                    return false;
                }
                this.fleePath = this.fleeingEntityNavigation.findPathTo(vec3d.x, vec3d.y, vec3d.z, 0);
            } else {
                return false;
            }
            return this.fleePath != null;
        }
        return this.skeletonHorse.isTrapped() && this.skeletonHorse.world.isPlayerInRange(this.skeletonHorse.getX(), this.skeletonHorse.getY(), this.skeletonHorse.getZ(), 10.0);
    }

    @Override
    public boolean shouldContinue() {
        if (this.targetEntity != null) {
            return !this.fleeingEntityNavigation.isIdle() && (this.skeletonHorse.getFirstPassenger() instanceof SkeletonEntity);
        }
        return this.canStart();
    }

    @Override
    public void start() {
        if (this.targetEntity != null) {
            this.fleeingEntityNavigation.startMovingAlong(this.fleePath, fleeing? 1.0 : 2.0);
        }
    }

    @Override
    public void stop() {
        this.targetEntity = null;
    }
    
    @Inject(at=@At("HEAD"), method="tick", cancellable = true)
    public void tick(CallbackInfo cir) {
        if (this.targetEntity != null) {
            if (!(this.skeletonHorse.getFirstPassenger() instanceof SkeletonEntity skeleton)) {
                stop();
            } else {
                double d = this.skeletonHorse.squaredDistanceTo(this.targetEntity.getX(), this.targetEntity.getY(), this.targetEntity.getZ());
                if ((d > (double)this.squaredMaxShootRange || !skeleton.canSee(targetEntity)) && fleeing) {
                    stop();
                } else if (d < (double)this.squaredMaxShootRange && !fleeing) {
                    stop();
                }
            }
            cir.cancel();
        }
        
    }

}
