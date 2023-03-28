package singleplayer.update.mixin.dragon;

import net.minecraft.entity.boss.dragon.EnderDragonEntity;
import net.minecraft.entity.boss.dragon.phase.AbstractPhase;
import net.minecraft.entity.boss.dragon.phase.LandingApproachPhase;
import net.minecraft.entity.boss.dragon.phase.Phase;
import net.minecraft.entity.boss.dragon.phase.PhaseType;
import net.minecraft.entity.projectile.DragonFireballEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.Heightmap;
import net.minecraft.world.gen.feature.EndPortalFeature;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LandingApproachPhase.class)
public class LandingApproachMixin extends AbstractPhase {

    public LandingApproachMixin(EnderDragonEntity dragon) {
        super(dragon);
    }

    @Inject(at=@At("HEAD"), method="beginPhase")
    void beginPhase(CallbackInfo inf) {
        Vec3d vec3d3 = this.dragon.getRotationVec(1.0f);
        BlockPos blockPos = this.dragon.world.getTopPosition(Heightmap.Type.MOTION_BLOCKING_NO_LEAVES, EndPortalFeature.ORIGIN);
        double l = this.dragon.head.getX() - vec3d3.x;
        double m = this.dragon.head.getBodyY(0.5) + 0.5;
        double n = this.dragon.head.getZ() - vec3d3.z;
        double o = -l;
        double p = blockPos.getY() - 3 - m;
        double q = -n;
        DragonFireballEntity dragonFireballEntity = new DragonFireballEntity(this.dragon.world, this.dragon, o, p, q);
        dragonFireballEntity.refreshPositionAndAngles(l, m, n, 0.0f, 0.0f);
        this.dragon.world.spawnEntity(dragonFireballEntity);
    }

    @Override
    public PhaseType<? extends Phase> getType() {
        return PhaseType.LANDING_APPROACH;
    }
}
