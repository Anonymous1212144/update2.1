package singleplayer.update.mixin;

import net.minecraft.entity.CrossbowUser;
import net.minecraft.entity.ai.goal.CrossbowAttackGoal;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.item.Items;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(CrossbowAttackGoal.class)
public class CrossbowAttackGoalMixin<T extends HostileEntity & CrossbowUser> extends Goal {

    @Final
    @Shadow
    private T actor;

    public boolean canStart() {
        return hasAliveTarget() && isEntityHoldingCrossbow();
    }

    private boolean hasAliveTarget() {
        return this.actor.getTarget() != null && this.actor.getTarget().isAlive();
    }

    private boolean isEntityHoldingCrossbow() {
        return this.actor.isHolding(Items.CROSSBOW);
    }

    @Redirect(method="tick", at=@At(value="INVOKE", target="net/minecraft/entity/ai/goal/CrossbowAttackGoal.isUncharged()Z"))
    boolean isUncharged(CrossbowAttackGoal<T> goal) {
        return !this.actor.isUsingItem();
    }

}
