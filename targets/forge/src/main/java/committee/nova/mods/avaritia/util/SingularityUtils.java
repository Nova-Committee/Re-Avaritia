package committee.nova.mods.avaritia.util;

import com.google.gson.JsonObject;
import committee.nova.mods.avaritia.Const;
import committee.nova.mods.avaritia.api.util.NBTUtils;
import committee.nova.mods.avaritia.api.util.ResourceUtil;
import committee.nova.mods.avaritia.core.singularity.Singularity;
import committee.nova.mods.avaritia.core.singularity.SingularityReloadListener;
import committee.nova.mods.avaritia.init.config.ModConfig;
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
 * @author cnlimiter
 * Date: 2022/4/2 12:39
 * Version: 1.0
 */
public class SingularityUtils {
    public static Singularity loadFromJson(JsonObject json) {
        return loadFromJson(json, null);
    }

    public static Singularity loadFromJson(JsonObject json, ResourceLocation sourceId) {
        var name = GsonHelper.getAsString(json, "name");
        var displayName = GsonHelper.getAsString(json, "displayName", name);
        int materialCount = GsonHelper.getAsInt(json, "count", 1000);
        int overlayColor = Integer.parseInt(GsonHelper.getAsString(json, "overlayColor"), 16);
        int underlayColor = Integer.parseInt(GsonHelper.getAsString(json, "underlayColor"), 16);

        var ing = GsonHelper.getAsJsonObject(json, "ingredient", null);
        int time;
        if (json.has("timeCost")) {
            time = GsonHelper.getAsInt(json, "timeCost");
        } else if (json.has("timeRequired")) {
            time = GsonHelper.getAsInt(json, "timeRequired");
            Const.LOGGER.warn("Singularity: {} uses deprecated field timeRequired; use timeCost", sourceId == null ? name : sourceId);
        } else {
            time = ModConfig.singularityTimeRequired.get();
        }
        var enabled = GsonHelper.getAsBoolean(json, "enabled", true);
        boolean recipeEnabled;
        if (json.has("recipeEnabled")) {
            recipeEnabled = GsonHelper.getAsBoolean(json, "recipeEnabled");
        } else if (json.has("recipeDisabled")) {
            // Historical 1.20 generated data wrote this value with recipeEnabled semantics.
            recipeEnabled = GsonHelper.getAsBoolean(json, "recipeDisabled");
            Const.LOGGER.warn("Singularity: {} uses deprecated field recipeDisabled; use recipeEnabled", sourceId == null ? name : sourceId);
        } else {
            recipeEnabled = true;
        }

        return Singularity.create(ResourceUtil.createInstanceWithColon(name), displayName, new int[]{overlayColor, underlayColor},  ing == null ? Ingredient.EMPTY : Ingredient.fromJson(ing))
                .setTimeCost(time).setCount(materialCount).setEnabled(enabled).setRecipeEnabled(recipeEnabled).validate();
    }

    public static JsonObject writeToJson(Singularity singularity) {
        singularity = singularity.copy();
        var json = new JsonObject();

        json.addProperty("name", singularity.getRegistryName().toString());
        json.addProperty("displayName", singularity.getDisplayName());
        json.addProperty("overlayColor", Integer.toString(singularity.getOverlayColor(), 16));
        json.addProperty("underlayColor", Integer.toString(singularity.getUnderlayColor(), 16));
        json.addProperty("count", singularity.getRealCount());
        json.addProperty("timeCost", singularity.getTimeCost());
        json.add("ingredient", singularity.getIngredient().toJson());
        json.addProperty("enabled", singularity.isEnabled());
        json.addProperty("recipeEnabled", singularity.isRecipeEnabled());
        json.add("conditions", CraftingHelper.serialize(singularity.getConditions().toArray(ICondition[]::new)));
        return json;
    }

    public static CompoundTag makeTag(Singularity singularity) {
        var nbt = new CompoundTag();

        nbt.putString("Id", singularity.getRegistryName().toString());

        return nbt;
    }

    public static ItemStack getItemForSingularity(Singularity singularity) {
        var nbt = makeTag(singularity);
        var stack = new ItemStack(ModItems.singularity.get());

        stack.setTag(nbt);

        return stack;
    }

    public static Singularity getSingularity(ItemStack stack) {
        var id = NBTUtils.getString(stack, "Id");
        if (!id.isEmpty()) {
            return SingularityReloadListener.INSTANCE.getSingularity(ResourceUtil.createInstanceWithColon(id));
        }
        return null;
    }

}
