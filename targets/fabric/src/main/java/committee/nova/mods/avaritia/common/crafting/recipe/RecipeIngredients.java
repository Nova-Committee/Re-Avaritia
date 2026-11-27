package committee.nova.mods.avaritia.common.crafting.recipe;

import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.nbt.TagParser;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.ShapedRecipe;

import java.util.Arrays;
import java.util.List;

/** Native matching and result decoding shared by the extended crafting serializers. */
public final class RecipeIngredients {
    private RecipeIngredients() {}

    public static boolean matches(List<ItemStack> stacks, List<Ingredient> ingredients) {
        if (stacks.size() != ingredients.size()) return false;
        int[] assigned = new int[ingredients.size()];
        Arrays.fill(assigned, -1);
        boolean[] visited = new boolean[ingredients.size()];
        for (int stack = 0; stack < stacks.size(); stack++) {
            Arrays.fill(visited, false);
            if (!assign(stack, stacks, ingredients, assigned, visited)) return false;
        }
        return true;
    }

    private static boolean assign(int stack, List<ItemStack> stacks, List<Ingredient> ingredients,
                                  int[] assigned, boolean[] visited) {
        for (int ingredient = 0; ingredient < ingredients.size(); ingredient++) {
            if (visited[ingredient] || !ingredients.get(ingredient).test(stacks.get(stack))) continue;
            visited[ingredient] = true;
            if (assigned[ingredient] == -1 || assign(assigned[ingredient], stacks, ingredients, assigned, visited)) {
                assigned[ingredient] = stack;
                return true;
            }
        }
        return false;
    }

    /** Retains the source datapack result's optional SNBT/object NBT extension. */
    public static ItemStack resultFromJson(JsonObject json) {
        ItemStack stack = ShapedRecipe.itemStackFromJson(json);
        if (json.has("nbt")) {
            String nbt = json.get("nbt").isJsonObject() ? json.get("nbt").toString()
                    : GsonHelper.getAsString(json, "nbt");
            try {
                stack.setTag(TagParser.parseTag(nbt));
            } catch (CommandSyntaxException exception) {
                throw new JsonSyntaxException("Invalid recipe result NBT", exception);
            }
        }
        return stack;
    }
}
