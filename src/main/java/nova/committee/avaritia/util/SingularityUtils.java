package nova.committee.avaritia.util;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import nova.committee.avaritia.api.util.NBTHelper;
import nova.committee.avaritia.common.item.singularity.Singularity;
import nova.committee.avaritia.common.item.singularity.SingularityRegistry;
import nova.committee.avaritia.init.registry.ModItems;

/**
 * Description:
 * Author: cnlimiter
 * Date: 2022/4/2 12:39
 * Version: 1.0
 */
public class SingularityUtils {
    public static Singularity loadFromJson(ResourceLocation id, JsonObject json) {
        var name = GsonHelper.getAsString(json, "name");
        var colors = GsonHelper.getAsJsonArray(json, "colors");
        int materialCount = GsonHelper.getAsInt(json, "materialCount", 1000);

        int overlayColor = Integer.parseInt(colors.get(0).getAsString(), 16);
        int underlayColor = Integer.parseInt(colors.get(1).getAsString(), 16);

        var inUltimateSingularity = GsonHelper.getAsBoolean(json, "inUltimateSingularity", true);

        Singularity singularity;
        var ing = GsonHelper.getAsJsonObject(json, "ingredient", null);

        if (ing == null) {
            singularity = new Singularity(id, name, new int[]{overlayColor, underlayColor}, Ingredient.EMPTY, materialCount, inUltimateSingularity);
        } else if (ing.has("tag")) {
            var tag = ing.get("tag").getAsString();
            singularity = new Singularity(id, name, new int[]{overlayColor, underlayColor}, tag, materialCount, inUltimateSingularity);
        } else {
            var ingredient = Ingredient.fromJson(json.get("ingredient"));
            singularity = new Singularity(id, name, new int[]{overlayColor, underlayColor}, ingredient, materialCount, inUltimateSingularity);
        }

        var enabled = GsonHelper.getAsBoolean(json, "enabled", true);

        singularity.setEnabled(enabled);

        return singularity;
    }

    public static JsonObject writeToJson(Singularity singularity) {
        var json = new JsonObject();

        json.addProperty("name", singularity.getName());

        var colors = new JsonArray();

        colors.add(Integer.toString(singularity.getOverlayColor(), 16));
        colors.add(Integer.toString(singularity.getUnderlayColor(), 16));

        json.add("colors", colors);

        JsonElement ingredient;
        if (singularity.getTag() != null) {
            var obj = new JsonObject();
            obj.addProperty("tag", singularity.getTag());
            ingredient = obj;
        } else {
            ingredient = singularity.getIngredient().toJson();
        }

        json.add("ingredient", ingredient);

        if (!singularity.isEnabled()) {
            json.addProperty("enabled", false);
        }

        return json;
    }

    public static CompoundTag makeTag(Singularity singularity) {
        var nbt = new CompoundTag();

        nbt.putString("Id", singularity.getId().toString());

        return nbt;
    }

    public static ItemStack getItemForSingularity(Singularity singularity) {
        var nbt = makeTag(singularity);
        var stack = new ItemStack(ModItems.singularity);

        stack.setTag(nbt);

        return stack;
    }

    public static Singularity getSingularity(ItemStack stack) {
        var id = NBTHelper.getString(stack, "Id");
        if (!id.isEmpty()) {
            return SingularityRegistry.getInstance().getSingularityById(ResourceLocation.tryParse(id));
        }

        return null;
    }
}
