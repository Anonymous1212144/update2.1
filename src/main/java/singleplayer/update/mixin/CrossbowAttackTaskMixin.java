package singleplayer.update.mixin;

import net.minecraft.entity.CrossbowUser;
import net.minecraft.entity.ai.brain.MemoryModuleState;
import net.minecraft.entity.ai.brain.MemoryModuleType;
import net.minecraft.entity.ai.brain.task.CrossbowAttackTask;
import net.minecraft.entity.ai.brain.task.MultiTickTask;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.item.Items;
import net.minecraft.server.world.ServerWorld;
import org.spongepowered.asm.mixin.Mixin;

import java.util.Map;

@Mixin(CrossbowAttackTask.class)
public class CrossbowAttackTaskMixin<E extends MobEntity> extends MultiTickTask<E> {

    public CrossbowAttackTaskMixin(Map<MemoryModuleType<?>, MemoryModuleState> requiredMemoryState) {
        super(requiredMemoryState);
    }
    
    @Override
    public void finishRunning(ServerWorld serverWorld, E mobEntity, long l) {
        mobEntity.clearActiveItem();
        if (mobEntity.isHolding(Items.CROSSBOW)) {
            ((CrossbowUser)mobEntity).setCharging(false);
        }
    }


    
}
