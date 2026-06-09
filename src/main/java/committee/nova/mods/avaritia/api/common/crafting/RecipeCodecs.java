package committee.nova.mods.avaritia.api.common.crafting;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSyntaxException;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.JsonOps;
import net.minecraft.core.NonNullList;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.crafting.Ingredient;

import java.util.List;

public final class RecipeCodecs {
    public static final Codec<ItemStack> STRICT_ITEM_STACK = ItemStack.CODEC.validate(ItemStack::validateStrict);
    public static final Codec<Ingredient> LEGACY_INGREDIENT = legacyIngredientCodec();
    public static final Codec<ItemStackTemplate> LEGACY_ITEM_STACK_TEMPLATE = legacyItemStackTemplateCodec();

    private RecipeCodecs() {
    }

    public static Codec<NonNullList<Ingredient>> ingredientList(int max, boolean allowEmpty, String recipeName) {
        return LEGACY_INGREDIENT
                .listOf()
                .flatXmap(
                        ingredients -> validateIngredients(ingredients, max, allowEmpty, recipeName),
                        ingredients -> DataResult.success(List.copyOf(ingredients))
                );
    }

    private static DataResult<NonNullList<Ingredient>> validateIngredients(List<Ingredient> ingredients, int max, boolean allowEmpty, String recipeName) {
        if (!allowEmpty && ingredients.isEmpty()) {
            return DataResult.error(() -> "No ingredients for " + recipeName);
        }
        if (ingredients.size() > max) {
            return DataResult.error(() -> "Too many ingredients for %s. The maximum is: %s".formatted(recipeName, max));
        }
        return DataResult.success(NonNullList.copyOf(ingredients));
    }

    private static Codec<Ingredient> legacyIngredientCodec() {
        return Codec.PASSTHROUGH.flatXmap(RecipeCodecs::decodeLegacyIngredient, RecipeCodecs::encodeIngredient);
    }

    private static Codec<ItemStackTemplate> legacyItemStackTemplateCodec() {
        return Codec.PASSTHROUGH.flatXmap(RecipeCodecs::decodeLegacyItemStackTemplate, RecipeCodecs::encodeItemStackTemplate);
    }

    private static DataResult<Ingredient> decodeLegacyIngredient(Dynamic<?> input) {
        if (input.getValue() instanceof JsonElement json) {
            @SuppressWarnings("unchecked")
            DynamicOps<JsonElement> ops = (DynamicOps<JsonElement>) input.getOps();
            return Ingredient.CODEC.parse(ops, normalizeLegacyIngredient(json));
        }
        return Ingredient.CODEC.parse(input);
    }

    private static DataResult<Dynamic<?>> encodeIngredient(Ingredient ingredient) {
        return Ingredient.CODEC
                .encodeStart(JsonOps.INSTANCE, ingredient)
                .map(json -> new Dynamic<>(JsonOps.INSTANCE, json));
    }

    private static DataResult<ItemStackTemplate> decodeLegacyItemStackTemplate(Dynamic<?> input) {
        if (input.getValue() instanceof JsonElement json) {
            @SuppressWarnings("unchecked")
            DynamicOps<JsonElement> ops = (DynamicOps<JsonElement>) input.getOps();
            return ItemStackTemplate.CODEC.parse(ops, normalizeLegacyItemStack(json));
        }
        return ItemStackTemplate.CODEC.parse(input);
    }

    private static DataResult<Dynamic<?>> encodeItemStackTemplate(ItemStackTemplate template) {
        return ItemStackTemplate.CODEC
                .encodeStart(JsonOps.INSTANCE, template)
                .map(json -> new Dynamic<>(JsonOps.INSTANCE, json));
    }

    private static JsonElement normalizeLegacyIngredient(JsonElement element) {
        if (element == null || element.isJsonNull()) {
            return element;
        }
        if (element.isJsonArray()) {
            JsonArray array = element.getAsJsonArray();
            if (array.size() == 1) {
                return normalizeLegacyIngredient(array.get(0));
            }

            JsonArray normalized = new JsonArray();
            array.forEach(entry -> normalized.add(normalizeLegacyIngredient(entry)));
            return normalized;
        }
        if (element.isJsonObject()) {
            JsonObject object = element.getAsJsonObject();
            if (object.has("item")) {
                return new JsonPrimitive(object.get("item").getAsString());
            }
            if (object.has("tag")) {
                JsonObject normalized = new JsonObject();
                normalized.addProperty("neoforge:ingredient_type", "avaritia:tag");
                normalized.add("tag", object.get("tag"));
                return normalized;
            }
        }
        return element;
    }

    private static JsonElement normalizeLegacyItemStack(JsonElement element) {
        if (!element.isJsonObject()) {
            return element;
        }

        JsonObject object = element.getAsJsonObject();
        if (!object.has("item")) {
            return element;
        }

        String item = object.get("item").getAsString();
        JsonObject normalized = object.deepCopy();
        normalized.remove("item");

        int componentStart = item.indexOf('[');
        if (componentStart >= 0 && item.endsWith("]")) {
            normalized.addProperty("id", item.substring(0, componentStart));
            mergeLegacyComponents(normalized, item.substring(componentStart + 1, item.length() - 1));
        } else {
            normalized.addProperty("id", item);
        }
        return normalized;
    }

    private static void mergeLegacyComponents(JsonObject stack, String componentsText) {
        JsonObject components = stack.has("components") && stack.get("components").isJsonObject()
                ? stack.getAsJsonObject("components")
                : new JsonObject();

        for (String entry : componentsText.split(",")) {
            int separator = entry.indexOf('=');
            if (separator <= 0) {
                continue;
            }

            String key = entry.substring(0, separator).trim();
            String value = entry.substring(separator + 1).trim();
            try {
                components.add(key, JsonParser.parseString(value));
            } catch (JsonSyntaxException ignored) {
                components.addProperty(key, value);
            }
        }
        stack.add("components", components);
    }
}
