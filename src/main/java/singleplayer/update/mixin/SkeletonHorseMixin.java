package singleplayer.update.mixin;

import net.minecraft.entity.EntityData;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.mob.SkeletonHorseEntity;
import net.minecraft.entity.passive.AbstractHorseEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.world.EntityView;
import net.minecraft.world.LocalDifficulty;
import net.minecraft.world.ServerWorldAccess;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(SkeletonHorseEntity.class)
public class SkeletonHorseMixin extends AbstractHorseEntity{


    @Shadow
    private boolean trapped;

    protected SkeletonHorseMixin(EntityType<? extends AbstractHorseEntity> entityType, World world) {
        super(entityType, world);
    }

    @Override
    public EntityData initialize(ServerWorldAccess world, LocalDifficulty difficulty, SpawnReason spawnReason, @Nullable EntityData entityData, @Nullable NbtCompound entityNbt) {
        ((SkeletonHorseEntity)(Object)this).setTrapped(false);
        return super.initialize(world, difficulty, spawnReason, entityData, entityNbt);
    }

    @Inject(at = @At("HEAD"), method = "setTrapped", cancellable = true)
    public void resetTrapped(boolean trap, CallbackInfo cir) {
        if (trap) {return;}
        ((SkeletonHorseEntity)(Object)this).setTrapped(true);
        this.trapped = false;
        cir.cancel();
    }

    public EntityView method_48926() {
        return null;
    }
}
