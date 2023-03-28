package singleplayer.update.mixin;

import net.minecraft.entity.mob.WitchEntity;
import net.minecraft.potion.Potion;
import net.minecraft.potion.Potions;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(WitchEntity.class)
public class WitchMixin {

    @ModifyVariable(at=@At("STORE"), method="tickMovement")
    Potion heal(Potion potion) {
        if (potion == Potions.WATER_BREATHING) {return Potions.LONG_WATER_BREATHING;}
        if (potion == Potions.FIRE_RESISTANCE) {return Potions.LONG_FIRE_RESISTANCE;}
        if (potion == Potions.HEALING) {return Potions.STRONG_HEALING;}
        if (potion == Potions.SWIFTNESS) {
            if (!((WitchEntity)(Object)this).isInvisible()) {
                return Potions.LONG_INVISIBILITY;
            }
            return Potions.STRONG_SWIFTNESS;
        }
        return potion;
    }

    @ModifyVariable(at=@At("STORE"), method="attack")
    Potion harm(Potion potion) {
        if (potion == Potions.HARMING) {return Potions.STRONG_HARMING;}
        if (potion == Potions.HEALING) {return Potions.STRONG_HEALING;}
        if (potion == Potions.REGENERATION) {return Potions.STRONG_REGENERATION;}
        if (potion == Potions.SLOWNESS) {return Potions.STRONG_SLOWNESS;}
        if (potion == Potions.POISON) {return Potions.STRONG_POISON;}
        if (potion == Potions.WEAKNESS) {return Potions.LONG_WEAKNESS;}
        return potion;
    }
}
