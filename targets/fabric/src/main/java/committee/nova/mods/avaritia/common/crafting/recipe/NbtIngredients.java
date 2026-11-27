package committee.nova.mods.avaritia.common.crafting.recipe;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;

import java.util.Arrays;

/** Strict item+NBT ingredients with native JSON/network semantics. */
public final class NbtIngredients {
    private NbtIngredients() {}

    public static Ingredient of(ItemStack stack) {
        Ingredient ingredient = Ingredient.of(stack);
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
        } else if (json.isJsonArray() && containsNbt(json)) {
            JsonArray array = json.getAsJsonArray();
            Ingredient[] alternatives = new Ingredient[array.size()];
            for (int i = 0; i < alternatives.length; i++) alternatives[i] = Ingredient.fromJson(array.get(i));
            Ingredient ingredient = Ingredient.of(Arrays.stream(alternatives).flatMap(value -> Arrays.stream(value.getItems())));
            ((NbtIngredientAccess) (Object) ingredient).avaritia$setAlternatives(alternatives);
            return ingredient;
        }
        return null;
    }

    private static boolean containsNbt(JsonElement json) {
        if (json.isJsonObject()) return "avaritia:nbt".equals(GsonHelper.getAsString(json.getAsJsonObject(), "type", ""));
        if (json.isJsonArray()) {
            for (JsonElement child : json.getAsJsonArray()) if (containsNbt(child)) return true;
        }
        return false;
    }
}
