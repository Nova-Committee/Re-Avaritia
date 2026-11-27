package committee.nova.mods.avaritia.init.mixins;

import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.client.renderer.item.ItemPropertyFunction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import java.util.Map;

@Mixin(ItemProperties.class)
public interface GameplayItemPropertiesAccessor {
    @Accessor("PROPERTIES")
    static Map<Item, Map<ResourceLocation, ItemPropertyFunction>> avaritia$properties() {
        throw new AssertionError("Mixin accessor was not applied");
    }
}
