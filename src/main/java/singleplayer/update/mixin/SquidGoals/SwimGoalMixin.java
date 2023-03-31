package singleplayer.update.mixin.SquidGoals;

import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.passive.SquidEntity;
import net.minecraft.fluid.WaterFluid;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(targets = "net.minecraft.entity.passive.SquidEntity$SwimGoal")
public class SwimGoalMixin extends Goal {

    @Final
    @Shadow
    private SquidEntity squid;

    public boolean canStart(){return true;}

    boolean submerged(BlockPos blockPos) {
        return this.squid.getWorld().getBlockState(blockPos).getFluidState().getFluid() instanceof WaterFluid;
    }

    @Override
    public void tick() {
        int i = this.squid.getDespawnCounter();
        if (i > 100) {
            this.squid.setSwimmingVector(0.0f, 0.0f, 0.0f);
        } else if (this.squid.getRandom().nextInt(SwimGoalMixin.toGoalTicks(50)) == 0 || !submerged(this.squid.getBlockPos()) || !this.squid.hasSwimmingVector()) {
            float f = this.squid.getRandom().nextFloat() * ((float)Math.PI * 2);
            float g = MathHelper.cos(f) * 0.2f;
            float h = -0.1f + this.squid.getRandom().nextFloat() * 0.2f;
            float j = MathHelper.sin(f) * 0.2f;
            Vec3d targetPos = this.squid.getPos().add(new Vec3d(g, h, j).multiply(10));
            BlockPos blockPos = new BlockPos((int)targetPos.getX(), (int)targetPos.getY(), (int)targetPos.getZ());
            if (submerged(blockPos)) {
                this.squid.setSwimmingVector(g, h, j);
            } else {
                this.squid.setSwimmingVector(0.0f, 0.0f, 0.0f);
            }
        }
    }

}