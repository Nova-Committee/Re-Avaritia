package committee.nova.mods.avaritia.util;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.mojang.serialization.JsonOps;
import committee.nova.mods.avaritia.Const;
import committee.nova.mods.avaritia.api.util.NBTUtils;
import committee.nova.mods.avaritia.api.util.ResourceUtil;
import committee.nova.mods.avaritia.core.singularity.Singularity;
import committee.nova.mods.avaritia.core.singularity.SingularityReloadListener;
import committee.nova.mods.avaritia.init.config.ModConfig;
import committee.nova.mods.avaritia.init.registry.ModItems;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.Identifier;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.crafting.Ingredient;

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

    public static Singularity loadFromJson(JsonObject json, Identifier sourceId) {
        var name = GsonHelper.getAsString(json, "name");
        var displayName = GsonHelper.getAsString(json, "displayName", name);
        int materialCount = GsonHelper.getAsInt(json, "count", 1000);
        int overlayColor = Integer.parseInt(GsonHelper.getAsString(json, "overlayColor"), 16);
        int underlayColor = Integer.parseInt(GsonHelper.getAsString(json, "underlayColor"), 16);

        JsonElement ing = normalizeIngredient(json.has("ingredient") ? json.get("ingredient") : null);
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

        Ingredient ingredient = ing == null ? null
                : Ingredient.CODEC.parse(JsonOps.INSTANCE, ing).getOrThrow(JsonParseException::new);
        Singularity singularity = Singularity.create(ResourceUtil.createInstanceWithColon(name), displayName,
                new int[]{overlayColor, underlayColor}, ingredient)
                .setTimeCost(time).setCount(materialCount).setEnabled(enabled).setRecipeEnabled(recipeEnabled).validate();
        if (json.has("conditions")) {
            GsonHelper.getAsJsonArray(json, "conditions").forEach(condition ->
                    singularity.addCondition(GsonHelper.convertToJsonObject(condition, "condition")));
        }
        return singularity;
    }

    /**
     * Converts legacy 1.20 ingredient JSON to the 26.3 codec form.
     * <p>26.3 accepts a plain item id string ("minecraft:emerald"), a tag string prefixed
     * with '#', or an array of those; the old object forms {"item":...}/{"tag":...} were removed.
     */
    private static JsonElement normalizeIngredient(JsonElement element) {
        if (element == null || element.isJsonNull()) {
            return null;
        }
        if (element.isJsonObject()) {
            JsonObject object = element.getAsJsonObject();
            if (object.has("item")) {
                return new JsonPrimitive(GsonHelper.getAsString(object, "item"));
            }
            if (object.has("tag")) {
                return new JsonPrimitive("#" + GsonHelper.getAsString(object, "tag"));
            }
            return element;
        }
        if (element.isJsonArray()) {
            JsonArray normalized = new JsonArray();
            for (JsonElement child : element.getAsJsonArray()) {
                normalized.add(normalizeIngredient(child));
            }
            return normalized;
        }
        return element;
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
        if (singularity.hasIngredient()) {
            json.add("ingredient", Ingredient.CODEC.encodeStart(JsonOps.INSTANCE, singularity.getIngredient())
                    .getOrThrow(IllegalArgumentException::new));
        }
        json.addProperty("enabled", singularity.isEnabled());
        json.addProperty("recipeEnabled", singularity.isRecipeEnabled());
        JsonArray conditions = new JsonArray();
        singularity.getConditions().forEach(conditions::add);
        json.add("conditions", conditions);
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

        stack.set(DataComponents.CUSTOM_DATA, CustomData.of(nbt));

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
