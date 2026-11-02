package committee.nova.mods.avaritia.common.crafting.recipe;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import com.mojang.serialization.JsonOps;
import net.minecraft.core.Holder;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;

import java.util.Arrays;

/** Strict item+NBT ingredients with native serialization/matching semantics. */
public final class NbtIngredients {
    private NbtIngredients() {}

    public static Ingredient of(ItemStack stack) {
        Ingredient ingredient = Ingredient.of(stack.getItem());
        ((NbtIngredientAccess) (Object) ingredient).avaritia$setNbtStack(stack.copy());
        return ingredient;
    }

    public static Ingredient parseSpecial(JsonElement json) {
        if (json == null) return null;
        if (json.isJsonObject()) {
            JsonObject object = json.getAsJsonObject();
            if ("avaritia:nbt".equals(GsonHelper.getAsString(object, "type", ""))) {
                return of(RecipeIngredients.resultFromJson(object));
            }
            return null;
        }
        if (json.isJsonArray() && containsNbt(json)) {
            JsonArray array = json.getAsJsonArray();
            Ingredient[] alternatives = new Ingredient[array.size()];
            for (int i = 0; i < alternatives.length; i++) {
                alternatives[i] = parseIngredient(array.get(i));
            }
            Ingredient ingredient = Ingredient.of(Arrays.stream(alternatives).flatMap(value -> value.items()).map(Holder::value));
            ((NbtIngredientAccess) (Object) ingredient).avaritia$setAlternatives(alternatives);
            return ingredient;
        }
        return null;
    }

    private static Ingredient parseIngredient(JsonElement json) {
        Ingredient special = parseSpecial(json);
        if (special != null) return special;
        return Ingredient.CODEC.parse(JsonOps.INSTANCE, json)
                .getOrThrow(message -> new JsonSyntaxException("Invalid ingredient: " + message));
    }

    private static boolean containsNbt(JsonElement json) {
        if (json.isJsonObject()) return "avaritia:nbt".equals(GsonHelper.getAsString(json.getAsJsonObject(), "type", ""));
        if (json.isJsonArray()) {
            for (JsonElement child : json.getAsJsonArray()) if (containsNbt(child)) return true;
        }
        return false;
    }
}
