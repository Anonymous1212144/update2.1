package singleplayer.update.mixin;

import net.minecraft.block.BlockState;
import net.minecraft.entity.boss.WitherEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.GameRules;
import net.minecraft.world.World;
import net.minecraft.world.WorldEvents;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(WitherEntity.class)
public class WitherMixin {

    WitherEntity wither = (WitherEntity)(Object)this;
    Random random = Random.create();
    Vec3d targetPos;

    @Shadow
    private int blockBreakingCooldown;

    @Inject(at=@At("HEAD"), method="onSummoned")
    void randomSpawn(CallbackInfo inf) {
        Vec3d pos = wither.getPos();
        int x = (int)(pos.getX() + (random.nextDouble() - 0.5D) * 4.0D);
        int y = (int)(pos.getY() + (random.nextDouble()) * 2.0D);
        int z = (int)(pos.getZ() + (random.nextDouble() - 0.5D) * 4.0D);
        wither.setPos(x, y, z);
    }

    boolean checkValid(BlockPos.Mutable mutable, World world) {
        for (int j=0; j<3; j++) {
            BlockState blockState = world.getBlockState(mutable);
            if (!blockState.getMaterial().blocksMovement() || world.isOutOfHeightLimit(mutable)) {
                mutable.move(Direction.UP);
            } else {
                return false;
            }
        }
        return true;
    }

    @Inject(at=@At("HEAD"), method="tickMovement")
    void getUnstuck(CallbackInfo inf) {

        if (wither.getInvulnerableTimer() > 0) {

            Vec3d pos = wither.getPos();
            if (this.targetPos != null) {
                if (pos.relativize(this.targetPos).length() > 1) {
                    wither.setVelocity(pos.relativize(this.targetPos).normalize().multiply(0.3));
                    return;
                } else {
                    this.targetPos = null;
                }
            }

            wither.noClip = false;
            World world = wither.world;

            if (wither.isInsideWall() || !checkValid(new BlockPos.Mutable(pos.x, pos.y, pos.z), world)) {

                for (int i=0; i<64; i++) {
                    int x = (int)(pos.getX() + (random.nextDouble() - 0.5D) * 16.0D);
                    int y = (int)(pos.getY() + (random.nextDouble() - 0.5D) * 16.0D);
                    int z = (int)(pos.getZ() + (random.nextDouble() - 0.5D) * 16.0D);

                    if (checkValid(new BlockPos.Mutable(x, y, z), world)) {
                        wither.noClip = true;
                        this.targetPos = new Vec3d(x, y, z);
                        return;
                    }
                }
                return;
            }
            return;
        } else if (wither.noClip) {
            wither.noClip = false;
        }
        if (this.blockBreakingCooldown == 1 && wither.world.getGameRules().getBoolean(GameRules.DO_MOB_GRIEFING)) {
            int i = MathHelper.floor(wither.getX());
            int j = MathHelper.floor(wither.getY());
            int k = MathHelper.floor(wither.getZ());
            boolean bl = false;
            for (int l = -1; l <= 1; ++l) {
                for (int m = -1; m <= 1; ++m) {
                    int o = i + l;
                    int p = j + 4;
                    int q = k + m;
                    BlockPos blockPos = new BlockPos(o, p, q);
                    BlockState blockState = wither.world.getBlockState(blockPos);
                    if (!WitherEntity.canDestroy(blockState)) continue;
                    bl = wither.world.breakBlock(blockPos, true, wither) || bl;
                }
            }
            if (bl) {
                wither.world.syncWorldEvent(null, WorldEvents.WITHER_BREAKS_BLOCK, wither.getBlockPos(), 0);
            }
        }
    }
}
