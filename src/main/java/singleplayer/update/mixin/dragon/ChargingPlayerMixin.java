package singleplayer.update.mixin.dragon;

import net.minecraft.entity.boss.dragon.EnderDragonEntity;
import net.minecraft.entity.boss.dragon.phase.AbstractPhase;
import net.minecraft.entity.boss.dragon.phase.ChargingPlayerPhase;
import net.minecraft.entity.boss.dragon.phase.Phase;
import net.minecraft.entity.boss.dragon.phase.PhaseType;
import net.minecraft.util.math.Vec3d;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(ChargingPlayerPhase.class)
public class ChargingPlayerMixin extends AbstractPhase {

    public ChargingPlayerMixin(EnderDragonEntity dragon) {
        super(dragon);
    }

    @Shadow
    private Vec3d pathTarget;

    @Shadow
    private int chargingTicks;

    @Override
    public void serverTick() {
        if (this.pathTarget == null) {
            this.dragon.getPhaseManager().setPhase(PhaseType.HOLDING_PATTERN);
            return;
        }
        if (this.chargingTicks > 0 && this.chargingTicks++ >= 10) {
            this.dragon.getPhaseManager().setPhase(PhaseType.HOLDING_PATTERN);
            return;
        }
        double d = this.pathTarget.squaredDistanceTo(this.dragon.getX(), this.dragon.getY(), this.dragon.getZ());
        if (d < 100.0 || d > 22500.0 || this.dragon.horizontalCollision || this.dragon.verticalCollision) {
            ++this.chargingTicks;
        }
    }

    @Override
    public float getMaxYAcceleration() {
        return 3.0f;
    }

    @Override
    public float getYawAcceleration() {
        return 3f;
    }

    @Override
    public PhaseType<? extends Phase> getType() {
        return PhaseType.CHARGING_PLAYER;
    }
}
