package com.avaritia.compat.kubejs;

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
import dev.latvian.mods.kubejs.recipe.RecipeKey;
import dev.latvian.mods.kubejs.recipe.component.ComponentRole;
import dev.latvian.mods.kubejs.recipe.component.IngredientComponent;
import dev.latvian.mods.kubejs.recipe.component.ItemStackComponent;
import dev.latvian.mods.kubejs.recipe.component.RecipeComponent;
import dev.latvian.mods.kubejs.util.IntBounds;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;

import java.util.List;

/**
 * KubeJS 配方 schema 的小型工具方法。
 */
public final class KjsUtils {
    public static final RecipeComponent<Ingredient> COMPAT_INGREDIENT =
            IngredientComponent.INGREDIENT.withCodec(legacyIngredientCodec());
    public static final RecipeComponent<ItemStack> COMPAT_ITEM_STACK =
            ItemStackComponent.ITEM_STACK.withCodec(legacyItemStackCodec());

    private KjsUtils() {
    }

    public static <T> RecipeKey<List<T>> optionalList(RecipeComponent<T> component, String name, ComponentRole role) {
        return component.asConditionalList()
                .orSelf()
                .withBounds(IntBounds.OPTIONAL)
                .key(name, role)
                .optional(List.of());
    }

    private static Codec<Ingredient> legacyIngredientCodec() {
        return Codec.PASSTHROUGH.flatXmap(KjsUtils::decodeLegacyIngredient, KjsUtils::encodeIngredient);
    }

    private static Codec<ItemStack> legacyItemStackCodec() {
        return Codec.PASSTHROUGH.flatXmap(KjsUtils::decodeLegacyItemStack, KjsUtils::encodeItemStack);
    }

    private static DataResult<Ingredient> decodeLegacyIngredient(Dynamic<?> input) {
        if (input.getValue() instanceof JsonElement json) {
            @SuppressWarnings("unchecked")
            DynamicOps<JsonElement> ops = (DynamicOps<JsonElement>) input.getOps();
            return IngredientComponent.INGREDIENT.codec().parse(ops, normalizeLegacyIngredient(json));
        }
        return IngredientComponent.INGREDIENT.codec().parse(input);
    }

    private static DataResult<Dynamic<?>> encodeIngredient(Ingredient ingredient) {
        return IngredientComponent.INGREDIENT.codec()
                .encodeStart(JsonOps.INSTANCE, ingredient)
                .map(json -> new Dynamic<>(JsonOps.INSTANCE, json));
    }

    private static DataResult<ItemStack> decodeLegacyItemStack(Dynamic<?> input) {
        if (input.getValue() instanceof JsonElement json) {
            @SuppressWarnings("unchecked")
            DynamicOps<JsonElement> ops = (DynamicOps<JsonElement>) input.getOps();
            JsonElement normalized = normalizeLegacyItemStack(json);
            DataResult<ItemStack> decoded = ItemStackComponent.ITEM_STACK.codec().parse(ops, normalized);

            if (json.isJsonObject() && normalized.isJsonPrimitive()) {
                JsonObject object = json.getAsJsonObject();
                if (object.has("count")) {
                    int count = object.get("count").getAsInt();
                    return decoded.map(stack -> stack.copyWithCount(count));
                }
            }
            return decoded;
        }
        return ItemStackComponent.ITEM_STACK.codec().parse(input);
    }

    private static DataResult<Dynamic<?>> encodeItemStack(ItemStack stack) {
        return ItemStackComponent.ITEM_STACK.codec()
                .encodeStart(JsonOps.INSTANCE, stack)
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
                return new JsonPrimitive("#" + object.get("tag").getAsString());
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

        String itemId = object.get("item").getAsString();
        JsonObject normalized = object.deepCopy();
        normalized.remove("item");

        int componentStart = itemId.indexOf('[');
        if (componentStart >= 0 && itemId.endsWith("]")) {
            normalized.add("id", new JsonPrimitive(itemId.substring(0, componentStart)));
            mergeLegacyComponents(normalized, itemId.substring(componentStart + 1, itemId.length() - 1));
        } else {
            normalized.add("id", new JsonPrimitive(itemId));
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
