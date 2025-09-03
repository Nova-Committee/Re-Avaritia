package committee.nova.mods.avaritia.common.crafting.recipe;

import com.google.gson.JsonArray;
import com.google.gson.JsonSyntaxException;
import committee.nova.mods.avaritia.init.registry.ModItems;
import committee.nova.mods.avaritia.init.registry.ModRecipeSerializers;
import net.minecraft.core.NonNullList;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.ShapedRecipe;
import net.minecraftforge.items.IItemHandler;

public class NoConsumeCatalystShapedRecipe extends ShapedTableCraftingRecipe {

    public NoConsumeCatalystShapedRecipe(ResourceLocation recipeId, int width, int height,
                                         NonNullList<Ingredient> inputs, ItemStack output, int tier) {
        super(recipeId, width, height, inputs, output, tier);
    }

    @Override
    public NonNullList<ItemStack> getRemainingItems(IItemHandler inventory) {
        NonNullList<ItemStack> remaining = super.getRemainingItems(inventory);
        int size = (int) Math.sqrt(inventory.getSlots());

        for (int i = 0; i < inventory.getSlots(); i++) {
            ItemStack stack = inventory.getStackInSlot(i);
            if (stack.is(ModItems.infinity_catalyst.get())) {
                remaining.set(i, stack.copy());
            }
        }
        return remaining;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return ModRecipeSerializers.NO_CONSUME_CATALYST_SHAPED_SERIALIZER.get();
    }

    public static class Serializer extends ShapedTableCraftingRecipe.Serializer {
        @Override
        public NoConsumeCatalystShapedRecipe fromJson(ResourceLocation recipeId, com.google.gson.JsonObject json) {
            var map = ShapedRecipe.keyFromJson(GsonHelper.getAsJsonObject(json, "key"));
            var pattern = patternFromJson(GsonHelper.getAsJsonArray(json, "pattern"));
            int width = pattern[0].length();
            int height = pattern.length;
            var inputs = ShapedRecipe.dissolvePattern(pattern, map, width, height);
            var output = ShapedRecipe.itemStackFromJson(GsonHelper.getAsJsonObject(json, "result"));
            int tier = GsonHelper.getAsInt(json, "tier", 0);
            return new NoConsumeCatalystShapedRecipe(recipeId, width, height, inputs, output, tier);
        }

        private static String[] patternFromJson(JsonArray jsonArr) {
            var astring = new String[jsonArr.size()];
            for (int i = 0; i < astring.length; ++i) {
                var s = GsonHelper.convertToString(jsonArr.get(i), "pattern[" + i + "]");
                if (i > 0 && astring[0].length() != s.length()) {
                    throw new JsonSyntaxException("Invalid pattern: each row must be the same width");
                }
                astring[i] = s;
            }
            return astring;
        }
    }
}