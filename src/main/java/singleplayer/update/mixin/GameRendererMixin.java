package singleplayer.update.mixin;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RotationAxis;
import net.minecraft.util.math.Vec3d;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GameRenderer.class)
public class GameRendererMixin {

    Vec3d playerPos;

    @Inject(at = @At("HEAD"), method = "renderWorld")
    private void render(float tickDelta, long limitTime, MatrixStack matrices, CallbackInfo ci) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client == null || client.player == null) {
            return;
        }
        LivingEntity player = client.player;
        if (player.isUsingRiptide()) {
            matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(((float)player.age + tickDelta) * -75.0f));
        } else if (player.isFallFlying()) {
            Vec3d vec3d = player.getRotationVec(tickDelta);
            Vec3d vec3d2 = player.getVelocity();
            if (tickDelta > 0 && playerPos != null && vec3d2.getX() == 0 && vec3d2.getZ() == 0) {
                vec3d2 = playerPos.relativize(player.getPos()).multiply(1/tickDelta);
            }
            double d = vec3d2.horizontalLengthSquared();
            double e = vec3d.horizontalLengthSquared();
            if (d > 0 && e > 0) {
                double l = (vec3d2.x * vec3d.x + vec3d2.z * vec3d.z) / Math.sqrt(d * e);
                double m = vec3d2.x * vec3d.z - vec3d2.z * vec3d.x;
                l = MathHelper.clamp(l, -1, 1);
                matrices.multiply(RotationAxis.POSITIVE_Z.rotation((float) (Math.signum(m) * Math.acos(l))));
            }
        }
        playerPos = player.getPos();
    }
}