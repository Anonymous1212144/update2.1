package singleplayer.update.mixin.dragon;

import net.minecraft.entity.boss.dragon.EnderDragonEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.Heightmap;
import net.minecraft.world.gen.feature.EndPortalFeature;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(EnderDragonEntity.class)
public class EnderDragonMixin {

    EnderDragonEntity dragon = (EnderDragonEntity)(Object)this;

    @ModifyVariable(at = @At("HEAD"), method = "getNearestPathNodeIndex(DDD)I", ordinal = 1, argsOnly = true)
    public double getNearestPathNodeIndex(double y) {
        if (y != 105) {return y;}
        StackTraceElement[] stack = Thread.currentThread().getStackTrace();
        if (stack[3].getClassName().equals("net.minecraft.entity.boss.dragon.phase.LandingApproachPhase")) {
            BlockPos blockPos = this.dragon.world.getTopPosition(Heightmap.Type.MOTION_BLOCKING_NO_LEAVES, EndPortalFeature.ORIGIN);
            return blockPos.getY();
        }
        return y;
    }
}
