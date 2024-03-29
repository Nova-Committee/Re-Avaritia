package committee.nova.mods.avaritia.util;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.serialization.JsonOps;
import committee.nova.mods.avaritia.Static;
import committee.nova.mods.avaritia.common.item.singularity.Singularity;
import committee.nova.mods.avaritia.init.config.ModConfig;
import committee.nova.mods.avaritia.init.handler.SingularityRegistryHandler;
import committee.nova.mods.avaritia.init.registry.ModItems;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.neoforged.neoforge.common.conditions.ICondition;
import net.neoforged.neoforge.common.conditions.NotCondition;
import net.neoforged.neoforge.common.conditions.TagEmptyCondition;
import net.neoforged.neoforge.common.crafting.CraftingHelper;

import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.List;

/**
 * Description:
 * Author: cnlimiter
 * Date: 2022/4/2 12:39
 * Version: 1.0
 */
public class SingularityUtil {
    public static Singularity loadFromJson(ResourceLocation id, JsonObject json) {
        if (!ICondition.conditionsMatched(JsonOps.INSTANCE, json)) {
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
            var ingredient = Ingredient.fromJson(json.get("ingredient"), false);
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
            var tagEmpty = new TagEmptyCondition(singularity.getTag());
            var notCondition = new NotCondition(tagEmpty);
            var obj = new JsonObject();
            obj.addProperty("tag", singularity.getTag());
            ingredient = obj;
            ICondition.writeConditions(JsonOps.INSTANCE, json, List.of(notCondition));
        } else {
            ingredient = Ingredient.CODEC.encodeStart(JsonOps.INSTANCE, singularity.getIngredient()).result().orElse(null);
        }

        json.add("ingredient", ingredient);
        json.addProperty("enabled", singularity.isEnabled());
        json.addProperty("recipeDisabled", singularity.isRecipeDisabled());


        return json;
    }

    public static CompoundTag makeTag(Singularity singularity) {
        var nbt = new CompoundTag();

        nbt.putString("Id", singularity.getId().toString());

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
            return SingularityRegistryHandler.getInstance().getSingularityById(ResourceLocation.tryParse(id));
        }

        return null;
    }

}
