package singleplayer.update.mixin;

import singleplayer.update.SnowyAccessor;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.LeavesBlock;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.color.block.BlockColors;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.BlockRenderView;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(BlockColors.class)
public class BlockColorsMixin {

    public long Time = 0;
    public boolean isSnowing = false;

    public int updateColour(int colour, float gradient) {
        int r = colour >> 16;
        int g = colour >> 8 & 0xFF;
        int b = colour & 0xFF;
        r += (int)(gradient*(255-r));
        g += (int)(gradient*(255-g));
        b += (int)(gradient*(255-b));
        return r << 16 | g << 8 | b;
    }

    public float getGradient(ClientWorld world) {
        float gradient;
        float timeDelta = (float)(world.getTime()-Time);
        if (world.isRaining()) {
            gradient = timeDelta/300;
        } else {
            gradient = 1 - timeDelta/300;
        }
        return MathHelper.clamp(gradient, 0, 1);
    }

    @Inject(at = @At("RETURN"), method = "getColor", cancellable = true)
    public void getColour(BlockState state, @Nullable BlockRenderView w, @Nullable BlockPos pos, int tintIndex, CallbackInfoReturnable<Integer> cir) {
        if (pos == null) return;
        int colour = cir.getReturnValue();
        Block block = state.getBlock();
        ClientWorld world = MinecraftClient.getInstance().world;
        if (world == null) {
            return;
        }
        if ((block instanceof LeavesBlock) && ((SnowyAccessor)block).isSnowy(world, pos)) {
            if (world.isRaining() ^ isSnowing) {
                Time = world.getTime();
                isSnowing = world.isRaining();
                return;
            }
            float gradient = getGradient(world);
            colour = updateColour(colour, gradient);
            cir.setReturnValue(colour);
        }
    }

}
