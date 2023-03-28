package singleplayer.update.mixin;

import net.minecraft.client.MinecraftClient;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MinecraftClient.class)
public class MinecraftClientMixin {

    @Shadow
    private int itemUseCooldown;

    MinecraftClient client = (MinecraftClient)(Object)this;

    @Inject(at=@At("HEAD"), method="tick")
    void tick(CallbackInfo inf) {
        if (this.client.player == null || this.itemUseCooldown <= 0) {return;}
        if (this.client.player.input.movementForward != 0 || this.client.player.input.movementSideways != 0) {
            if (this.client.player.isOnGround() && !this.client.player.isSprinting()) {
                this.itemUseCooldown = Math.min(2, this.itemUseCooldown);
            } else {
                this.itemUseCooldown = 1;
            }
        } else if (this.client.player.getAbilities().flying) {
            if (this.client.player.input.jumping || this.client.player.input.sneaking) {
                this.itemUseCooldown = 1;
            }
        }
    }

}
