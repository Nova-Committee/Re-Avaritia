package committee.nova.mods.avaritia.util;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.serialization.JsonOps;
import committee.nova.mods.avaritia.Const;
import committee.nova.mods.avaritia.api.client.util.color.ColorARGB;
import committee.nova.mods.avaritia.core.singularity.Singularity;
import committee.nova.mods.avaritia.core.singularity.SingularityDataManager;
import committee.nova.mods.avaritia.init.config.ModConfig;
import committee.nova.mods.avaritia.init.registry.ModDataComponents;
import committee.nova.mods.avaritia.init.registry.ModItems;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.neoforged.neoforge.common.conditions.ICondition;

/**
 * Description:
 * Author: cnlimiter
 * Date: 2022/4/2 12:39
 * Version: 1.0
 */
public class SingularityUtils {
    public static Singularity loadFromJson(ResourceLocation id, JsonObject json) {
        if (!ICondition.conditionsMatched(JsonOps.INSTANCE, json)) {
            Const.LOGGER.info("Skipping loading Singularity {} as its conditions were not met!", id);
            return null;
        }
        var name = GsonHelper.getAsString(json, "name");
        var colors = GsonHelper.getAsJsonArray(json, "colors");
        int materialCount = ModConfig.useModDifficulty.get()
                ? Const.isLoad("projecte")
                ? 10000
                : GsonHelper.getAsInt(json, "materialCount", 1000)
                : GsonHelper.getAsInt(json, "materialCount", 1000);
        int overlayColor = Integer.parseInt(colors.get(0).getAsString(), 16);
        int underlayColor = Integer.parseInt(colors.get(1).getAsString(), 16);

        var ing = GsonHelper.getAsJsonObject(json, "ingredient", null);
        var time = GsonHelper.getAsInt(json, "timeRequired", ModConfig.singularityTimeRequired.get());
        var enabled = GsonHelper.getAsBoolean(json, "enabled", true);
        var recipeEnabled = GsonHelper.getAsBoolean(json, "recipeEnabled", false);

        Singularity singularity = new Singularity(id).setDisplayName(name).setColors(overlayColor, underlayColor).setCount(materialCount).setTimeCost(time).setEnabled(enabled).setRecipeEnabled(recipeEnabled);

        if (ing != null) {
            if (ing.has("tag")) {
                var tag = ing.get("tag").getAsString();
                singularity.setTag(tag);
            } else {
                var ingredient = Ingredient.CODEC.decode(JsonOps.INSTANCE, json.get("ingredient")).getOrThrow().getFirst();
                singularity.setIngredient(ingredient);
            }
        }
        return singularity;
    }

    public static JsonObject writeToJson(Singularity singularity) {
        var json = new JsonObject();

        json.addProperty("name", singularity.getDisplayName());

        var colors = new JsonArray();

        colors.add(Integer.toString(singularity.getOverlayColor(), 16));
        colors.add(Integer.toString(singularity.getUnderlayColor(), 16));

        json.add("colors", colors);
        json.addProperty("timeRequired", singularity.getTimeCost());

        JsonElement ingredient;
        if (singularity.getTag() != null) {
            //var conditions = new ICondition[]{new NotCondition(new TagEmptyCondition(singularity.getTag()))};

            var obj = new JsonObject();
            obj.addProperty("tag", singularity.getTag());
            ingredient = obj;

            var array = new JsonArray();
            var main = new JsonObject();

            var sub = new JsonObject();
            main.addProperty("type", "neoforge:not");

            sub.addProperty("tag", singularity.getTag());
            sub.addProperty("type", "neoforge:tag_empty");

            main.add("value", sub);
            array.add(main);
            json.add("neoforge:conditions", array);

        } else {
            ingredient = Ingredient.CODEC.encodeStart(JsonOps.INSTANCE, singularity.getIngredient()).result().orElse(null);
        }

        json.add("ingredient", ingredient);
        json.addProperty("enabled", singularity.isEnabled());
        json.addProperty("recipeEnabled", singularity.isRecipeEnabled());

        return json;
    }

    public static ItemStack getItemForSingularity(Singularity singularity) {
        var stack = new ItemStack(ModItems.singularity.get());
        stack.set(ModDataComponents.SINGULARITY_ID, singularity.getRegistryName());
        return stack;
    }

    public static Singularity getSingularity(ItemStack stack) {
        var id = stack.get(ModDataComponents.SINGULARITY_ID);
        if (id != null) {
            var manager = SingularityDataManager.getInstance();
            if (manager != null && manager.isInitialized()) {
                return manager.getSingularity(id);
            }
        }
        return null;
    }

}
