package singleplayer.update.mixin;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.mob.IllusionerEntity;
import net.minecraft.entity.mob.WardenEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin {

    LivingEntity entity = ((LivingEntity)(Object)this);

    @Inject(at=@At("RETURN"), method="canTarget(Lnet/minecraft/entity/LivingEntity;)Z", cancellable = true)
    public void canTarget(LivingEntity target, CallbackInfoReturnable<Boolean> cir) {
        if (target.getName().equals(((LivingEntity)(Object)this).getName())) {
            cir.setReturnValue(false);
            return;
        }
        if (target instanceof IllusionerEntity && target.isInvisible()) {
            cir.setReturnValue(this.entity.distanceTo(target) < 2);
        }
        if (entity instanceof WardenEntity) {return;}
        if (this.entity.hasStatusEffect(StatusEffects.BLINDNESS) && this.entity.distanceTo(target) > 5) {
            cir.setReturnValue(false);
            return;
        }
        if (target.isInvisible() && target.getArmorVisibility() == 0 && this.entity.distanceTo(target) > 5) {
            cir.setReturnValue(false);
        }
    }
}
