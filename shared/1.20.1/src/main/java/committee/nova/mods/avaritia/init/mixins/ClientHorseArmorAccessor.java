package committee.nova.mods.avaritia.init.mixins;

import net.minecraft.world.entity.animal.horse.Horse;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.UUID;

/** Exposes the vanilla modifier used specifically for horse armor. */
@Mixin(Horse.class)
public interface ClientHorseArmorAccessor {
    @Accessor("ARMOR_MODIFIER_UUID")
    static UUID avaritia$armorModifierId() {
        throw new AssertionError("Mixin accessor was not applied");
    }
}
