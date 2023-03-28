package singleplayer.update.mixin;

import net.minecraft.entity.Entity;
import net.minecraft.util.math.Vec3d;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(Entity.class)
public class EntityMixin {

    @ModifyVariable(at=@At("HEAD"), method="setVelocity(Lnet/minecraft/util/math/Vec3d;)V", ordinal=0, argsOnly = true)
    Vec3d setVelocity(Vec3d velocity) {
        StackTraceElement[] stack = Thread.currentThread().getStackTrace();
        for (StackTraceElement stackTraceElement : stack) {
            if (stackTraceElement.getClassName().equals("net.minecraft.entity.boss.dragon.EnderDragonEntity")) {
                Vec3d accel = velocity.subtract(((Entity)(Object)this).getVelocity());
                if (accel.getY()*velocity.getY() > 0) {
                    return velocity.add(accel.multiply(new Vec3d(1, 3, 1)));
                }
            }
        }
        return velocity;
    }

    @ModifyVariable(at=@At("HEAD"), method="damage", ordinal=0, argsOnly = true)
    float damage(float amount) {
        StackTraceElement[] stack = Thread.currentThread().getStackTrace();
        for (StackTraceElement stackTraceElement : stack) {
            if (stackTraceElement.getClassName().equals("net.minecraft.entity.boss.dragon.EnderDragonEntity")) {
                return amount * (float)Math.pow(2, ((Entity)(Object)this).getWorld().getDifficulty().getId()-1);
            }
        }
        return amount;
    }
}
