package committee.nova.mods.avaritia.common.crafting.recipe;

import com.google.gson.JsonArray;
import com.google.gson.JsonSyntaxException;
import committee.nova.mods.avaritia.init.registry.ModItems;
import committee.nova.mods.avaritia.init.registry.ModRecipeSerializers;
import net.minecraft.core.NonNullList;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.ShapedRecipe;
import net.minecraftforge.items.IItemHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class NoConsumeCatalystShapedRecipe extends ShapedTableCraftingRecipe {

    public NoConsumeCatalystShapedRecipe(ResourceLocation recipeId, int width, int height,
                                         NonNullList<Ingredient> inputs, ItemStack output, int tier) {
        super(recipeId, width, height, inputs, output, tier, false);
    }

    @Override
    public @NotNull NonNullList<ItemStack> getRemainingItems(@NotNull IItemHandler inventory) {
        NonNullList<ItemStack> remaining = super.getRemainingItems(inventory);
        for (int i = 0; i < inventory.getSlots(); i++) {
            ItemStack stack = inventory.getStackInSlot(i);
            if (stack.is(ModItems.infinity_catalyst.get())) {
                remaining.set(i, stack.copyWithCount(1));
            }
        }
        return remaining;
    }

    @Override
    public @NotNull RecipeSerializer<?> getSerializer() {
        return ModRecipeSerializers.NO_CONSUME_CATALYST_SHAPED_SERIALIZER.get();
    }

    public static class Serializer implements RecipeSerializer<NoConsumeCatalystShapedRecipe> {
        @Override
        public @NotNull NoConsumeCatalystShapedRecipe fromJson(@NotNull ResourceLocation recipeId, com.google.gson.@NotNull JsonObject json) {
            var map = ShapedRecipe.keyFromJson(GsonHelper.getAsJsonObject(json, "key"));
            var pattern = patternFromJson(GsonHelper.getAsJsonArray(json, "pattern"));
            int width = pattern[0].length();
            int height = pattern.length;
            var inputs = ShapedRecipe.dissolvePattern(pattern, map, width, height);
            var output = ShapedRecipe.itemStackFromJson(GsonHelper.getAsJsonObject(json, "result"));
            int tier = GsonHelper.getAsInt(json, "tier", 0);
            return new NoConsumeCatalystShapedRecipe(recipeId, width, height, inputs, output, tier);
        }

        @Override
        public @Nullable NoConsumeCatalystShapedRecipe fromNetwork(@NotNull ResourceLocation recipeId, @NotNull FriendlyByteBuf buffer) {
            int width = buffer.readVarInt();
            int height = buffer.readVarInt();
            var inputs = NonNullList.withSize(width * height, Ingredient.EMPTY);

            inputs.replaceAll(ignored -> Ingredient.fromNetwork(buffer));

            var output = buffer.readItem();
            var tier = buffer.readVarInt();
            return new NoConsumeCatalystShapedRecipe(recipeId, width, height, inputs, output, tier);
        }

        @Override
        public void toNetwork(@NotNull FriendlyByteBuf buffer, @NotNull NoConsumeCatalystShapedRecipe recipe) {
            buffer.writeVarInt(recipe.getWidth());
            buffer.writeVarInt(recipe.getHeight());

            for (var ingredient : recipe.getIngredients()) {
                ingredient.toNetwork(buffer);
            }

            buffer.writeItem(recipe.output);
            buffer.writeVarInt(recipe.getTier());
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
