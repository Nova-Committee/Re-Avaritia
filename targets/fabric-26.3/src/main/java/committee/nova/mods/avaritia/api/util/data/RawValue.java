package committee.nova.mods.avaritia.api.util.data;

import com.google.gson.JsonObject;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.core.registries.BuiltInRegistries;
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
public class RawValue {
    private final Identifier item;

    public RawValue(Identifier pItem) {
        this.item = pItem;
    }

    @NotNull
    public Collection<ItemStack> getItems() {
        return Collections.singleton(new ItemStack(BuiltInRegistries.ITEM.getValue(this.item)));
    }

    @NotNull
    public JsonObject serialize() {
        JsonObject jsonobject = new JsonObject();
        jsonobject.addProperty("item", item.toString());
        return jsonobject;
    }
}
