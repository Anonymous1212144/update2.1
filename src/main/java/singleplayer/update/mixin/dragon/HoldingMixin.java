package singleplayer.update.mixin.dragon;

import net.minecraft.entity.ai.TargetPredicate;
import net.minecraft.entity.boss.dragon.EnderDragonEntity;
import net.minecraft.entity.boss.dragon.phase.AbstractPhase;
import net.minecraft.entity.boss.dragon.phase.HoldingPatternPhase;
import net.minecraft.entity.boss.dragon.phase.Phase;
import net.minecraft.entity.boss.dragon.phase.PhaseType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.Heightmap;
import net.minecraft.world.gen.feature.EndPortalFeature;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(HoldingPatternPhase.class)
public class HoldingMixin extends AbstractPhase {

    public HoldingMixin(EnderDragonEntity dragon) {
        super(dragon);
    }

    private static final TargetPredicate PLAYERS_IN_RANGE_PREDICATE = TargetPredicate.createAttackable().ignoreVisibility();

    @Inject(at=@At("HEAD"), method="beginPhase")
    void beginPhase(CallbackInfo inf) {
        BlockPos blockPos = this.dragon.world.getTopPosition(Heightmap.Type.MOTION_BLOCKING_NO_LEAVES, EndPortalFeature.ORIGIN);
        PlayerEntity playerEntity = this.dragon.world.getClosestPlayer(PLAYERS_IN_RANGE_PREDICATE, this.dragon, this.dragon.getX(), this.dragon.getY(), this.dragon.getZ());
        if (playerEntity == null) {return;}
        if (playerEntity.getPos().getY() > blockPos.getY() || blockPos.getSquaredDistance(playerEntity.getPos()) < 100) {
            this.dragon.getPhaseManager().setPhase(PhaseType.CHARGING_PLAYER);
            this.dragon.getPhaseManager().create(PhaseType.CHARGING_PLAYER).setPathTarget(new Vec3d(MathHelper.clamp(playerEntity.getX()+1, -256, 256), Math.min(playerEntity.getY()-1,this.dragon.getPos().getY()+20), MathHelper.clamp(playerEntity.getZ()+1, -256, 256)));
        }
    }

    @Override
    public PhaseType<? extends Phase> getType() {
        return PhaseType.HOLDING_PATTERN;
    }
}
