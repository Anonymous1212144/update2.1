package singleplayer.update.mixin;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerInteractionManager;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ClientPlayerInteractionManager.class)
public class InteractionManagerMixin {

    @Final
    @Shadow
    private MinecraftClient client;

    @Shadow
    private int blockBreakingCooldown;

    @Inject(at=@At("HEAD"), method="updateBlockBreakingProgress")
    void update(BlockPos pos, Direction direction, CallbackInfoReturnable<Boolean> cir) {
        if (this.client.player == null || this.blockBreakingCooldown <= 0) {return;}
        if (this.client.player.input.movementForward != 0 || this.client.player.input.movementSideways != 0) {
            if (this.client.player.isOnGround() && !this.client.player.isSprinting()) {
                this.blockBreakingCooldown = Math.min(2, this.blockBreakingCooldown);
            } else {
                this.blockBreakingCooldown = 1;
            }
        } else if (this.client.player.getAbilities().flying) {
            if (this.client.player.input.jumping || this.client.player.input.sneaking) {
                this.blockBreakingCooldown = 1;
            }
        }
    }
}
