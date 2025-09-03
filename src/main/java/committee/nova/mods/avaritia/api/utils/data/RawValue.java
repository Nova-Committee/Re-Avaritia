package committee.nova.mods.avaritia.api.utils.data;

import com.google.gson.JsonObject;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.Collections;

/**
 * RawValue
 *
 * @author cnlimiter
 * @version 1.0
 * @description
 * @date 2024/6/16 下午2:02
 */
public class RawValue implements Ingredient.Value {
    private final ResourceLocation item;

    public RawValue(ResourceLocation pItem) {
        this.item = pItem;
    }

    @NotNull
    @Override
    public Collection<ItemStack> getItems() {
        return Collections.singleton(new ItemStack(BuiltInRegistries.ITEM.get(this.item)));
    }

    @NotNull
    @Override
    public JsonObject serialize() {
        JsonObject jsonobject = new JsonObject();
        jsonobject.addProperty("item", item.toString());
        return jsonobject;
    }
}
