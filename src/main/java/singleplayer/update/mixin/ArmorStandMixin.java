package singleplayer.update.mixin;

import net.minecraft.entity.decoration.ArmorStandEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ArmorStandEntity.class)
public class ArmorStandMixin {

    boolean hasSetArm = false;

    @Inject(at=@At("HEAD"), method="tick")
    void tick(CallbackInfo inf){
        if (!this.hasSetArm) {
            ((ArmorStandEntity)(Object)this).setShowArms(true);
            this.hasSetArm = true;
        }
    }

}
