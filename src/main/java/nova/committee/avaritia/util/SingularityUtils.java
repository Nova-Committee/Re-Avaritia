package nova.committee.avaritia.util;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraftforge.common.crafting.CraftingHelper;
import net.minecraftforge.common.crafting.conditions.ICondition;
import nova.committee.avaritia.Static;
import nova.committee.avaritia.common.item.singularity.Singularity;
import nova.committee.avaritia.init.handler.SingularityRegistryHandler;
import nova.committee.avaritia.init.registry.ModItems;

/**
 * Description:
 * Author: cnlimiter
 * Date: 2022/4/2 12:39
 * Version: 1.0
 */
public class SingularityUtils {
    public static Singularity loadFromJson(ResourceLocation id, JsonObject json, ICondition.IContext context) {
        if (!CraftingHelper.processConditions(json, "conditions", context)) {
            Static.LOGGER.info("Skipping loading Singularity {} as its conditions were not met", id);
            return null;
        }
        var name = GsonHelper.getAsString(json, "name");
        var colors = GsonHelper.getAsJsonArray(json, "colors");
        int materialCount = GsonHelper.getAsInt(json, "materialCount", 1000);

        int overlayColor = Integer.parseInt(colors.get(0).getAsString(), 16);
        int underlayColor = Integer.parseInt(colors.get(1).getAsString(), 16);

        Singularity singularity;
        var ing = GsonHelper.getAsJsonObject(json, "ingredient", null);


        var time = GsonHelper.getAsInt(json, "timeRequired", 240);


        if (ing == null) {
            singularity = new Singularity(id, name, new int[]{overlayColor, underlayColor}, Ingredient.EMPTY, materialCount, time);
        } else if (ing.has("tag")) {
            var tag = ing.get("tag").getAsString();

            singularity = new Singularity(id, name, new int[]{overlayColor, underlayColor}, tag, materialCount, time);
        } else {
            var ingredient = Ingredient.fromJson(json.get("ingredient"));
            singularity = new Singularity(id, name, new int[]{overlayColor, underlayColor}, ingredient, materialCount, time);
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
        json.addProperty("timeRequired", singularity.getTimeRequired());

        JsonElement ingredient;
        if (singularity.getTag() != null) {
            var obj = new JsonObject();
            obj.addProperty("tag", singularity.getTag());
            ingredient = obj;

            var array = new JsonArray();
            var main = new JsonObject();

            var sub = new JsonObject();
            main.addProperty("type", "forge:not");

            sub.addProperty("tag", singularity.getTag());
            sub.addProperty("type", "forge:tag_empty");

            main.add("value", sub);
            array.add(main);
            json.add("conditions", array);

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
        var id = NBTUtil.getString(stack, "Id");
        if (!id.isEmpty()) {
            return SingularityRegistryHandler.getInstance().getSingularityById(ResourceLocation.tryParse(id));
        }

        return null;
    }
}
