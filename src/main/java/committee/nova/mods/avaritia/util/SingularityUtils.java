package committee.nova.mods.avaritia.util;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import committee.nova.mods.avaritia.Static;
import committee.nova.mods.avaritia.common.item.singularity.Singularity;
import committee.nova.mods.avaritia.init.config.ModConfig;
import committee.nova.mods.avaritia.init.handler.SingularityRegistryHandler;
import committee.nova.mods.avaritia.init.registry.ModItems;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraftforge.common.crafting.CraftingHelper;
import net.minecraftforge.common.crafting.conditions.ICondition;

/**
 * Description:
 * Author: cnlimiter
 * Date: 2022/4/2 12:39
 * Version: 1.0
 */
public class SingularityUtils {
    public static Singularity loadFromJson(String id, JsonObject json, ICondition.IContext context) {
        if (!CraftingHelper.processConditions(json, "conditions", context)) {
            Static.LOGGER.info("Skipping loading Singularity {} as its conditions were not met!", id);
            return null;
        }
        var name = GsonHelper.getAsString(json, "name");
        var colors = GsonHelper.getAsJsonArray(json, "colors");
        int materialCount = GsonHelper.getAsInt(json, "materialCount", 1000);

        int overlayColor = Integer.parseInt(colors.get(0).getAsString(), 16);
        int underlayColor = Integer.parseInt(colors.get(1).getAsString(), 16);

        Singularity singularity;
        var ing = GsonHelper.getAsJsonObject(json, "ingredient", null);

        var time = GsonHelper.getAsInt(json, "timeRequired", ModConfig.singularityTimeRequired.get());

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
        var recipeDisabled = GsonHelper.getAsBoolean(json, "recipeDisabled", false);

        singularity.setEnabled(enabled);
        singularity.setRecipeDisabled(recipeDisabled);

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
        json.addProperty("enabled", singularity.isEnabled());
        json.addProperty("recipeDisabled", singularity.isRecipeDisabled());


        return json;
    }

    public static CompoundTag makeTag(Singularity singularity) {
        var nbt = new CompoundTag();

        nbt.putString("Id", singularity.getId());

        return nbt;
    }

    public static ItemStack getItemForSingularity(Singularity singularity) {
        var nbt = makeTag(singularity);
        var stack = new ItemStack(ModItems.singularity.get());

        stack.setTag(nbt);

        return stack;
    }

    public static Singularity getSingularity(ItemStack stack) {
        var id = NBTUtil.getString(stack, "Id");
        if (!id.isEmpty()) {
            return SingularityRegistryHandler.getInstance().getSingularityById(id);
        }

        return null;
    }
}
