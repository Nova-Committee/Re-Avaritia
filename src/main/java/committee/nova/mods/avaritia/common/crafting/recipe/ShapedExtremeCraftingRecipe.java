package committee.nova.mods.avaritia.common.crafting.recipe;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import committee.nova.mods.avaritia.api.common.crafting.ISpecialRecipe;
import committee.nova.mods.avaritia.init.registry.ModRecipeSerializers;
import committee.nova.mods.avaritia.init.registry.ModRecipeTypes;
import committee.nova.mods.avaritia.util.java.TriFunction;
import net.minecraft.core.NonNullList;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.ShapedRecipe;
import net.minecraft.world.level.Level;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.wrapper.InvWrapper;
import org.jetbrains.annotations.NotNull;

/**
 * Description:
 * Author: cnlimiter
 * Date: 2022/4/2 9:16
 * Version: 1.0
 */
public class ShapedExtremeCraftingRecipe implements ISpecialRecipe{
    private final ResourceLocation recipeId;
    private final NonNullList<Ingredient> inputs;
    private final ItemStack output;
    private final int width;
    private final int height;
    private TriFunction<Integer, Integer, ItemStack, ItemStack> transformers;

    public ShapedExtremeCraftingRecipe(ResourceLocation recipeId, int width, int height, NonNullList<Ingredient> inputs, ItemStack output) {
        this.recipeId = recipeId;
        this.inputs = inputs;
        this.output = output;
        this.width = width;
        this.height = height;
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

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    @Override
    public @NotNull ItemStack getResultItem(@NotNull RegistryAccess p_267052_) {
        return this.output;
    }

    @Override
    public ItemStack assemble(IItemHandler inventory) {
        return this.output.copy();
    }

    @Override
    public @NotNull ItemStack assemble(@NotNull Container inv, @NotNull RegistryAccess p_267052_) {
        return this.output.copy();
    }

    @Override
    public boolean matches(IItemHandler inventory) {
        for (int i = 0; i <= 9 - this.width; i++) {
            for (int j = 0; j <= 9 - this.height; j++) {
                if (this.checkMatch(inventory, i, j, true)) {
                    return true;
                }

                if (this.checkMatch(inventory, i, j, false)) {
                    return true;
                }
            }
        }

        return false;
    }

    @Override
    public boolean matches(@NotNull Container inv, @NotNull Level level) {
        return this.matches(new InvWrapper(inv));
    }

    @Override
    public @NotNull NonNullList<Ingredient> getIngredients() {
        return this.inputs;
    }

    @Override
    public @NotNull ResourceLocation getId() {
        return this.recipeId;
    }

    @Override
    public @NotNull RecipeSerializer<?> getSerializer() {
        return ModRecipeSerializers.SHAPED_EXTREME_CRAFT_SERIALIZER.get();
    }

    @Override
    public @NotNull RecipeType<?> getType() {
        return ModRecipeTypes.EXTREME_CRAFT_RECIPE.get();
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return width >= this.width && height >= this.height;
    }

    @Override
    public @NotNull NonNullList<ItemStack> getRemainingItems(@NotNull IItemHandler inventory) {
        if (this.transformers != null) {
            var remaining = NonNullList.withSize(inventory.getSlots(), ItemStack.EMPTY);
            int size = (int) Math.sqrt(inventory.getSlots());

            for (int i = 0; i <= size - this.width; i++) {
                for (int j = 0; j <= size - this.height; j++) {
                    if (this.checkMatch(inventory, i, j, true)) {
                        for (int k = 0; k < this.height; k++) {
                            for (int l = 0; l < this.width; l++) {
                                int index = (this.width - 1 - l) + i + (k + j) * size;
                                var stack = inventory.getStackInSlot(index);

                                remaining.set(index, this.transformers.apply(l, k, stack));
                            }
                        }

                        break;
                    }

                    if (this.checkMatch(inventory, i, j, false)) {
                        for (int k = 0; k < this.height; k++) {
                            for (int l = 0; l < this.width; l++) {
                                int index = l + i + (k + j) * size;
                                var stack = inventory.getStackInSlot(index);

                                remaining.set(index, this.transformers.apply(l, k, stack));
                            }
                        }

                        break;
                    }
                }
            }

            return remaining;
        }
        return ISpecialRecipe.super.getRemainingItems(inventory);
    }


    private boolean checkMatch(IItemHandler inventory, int x, int y, boolean mirror) {
        for (int i = 0; i < 9; i++) {
            for (int j = 0; j < 9; j++) {
                int k = i - x;
                int l = j - y;
                var ingredient = Ingredient.EMPTY;

                if (k >= 0 && l >= 0 && k < this.width && l < this.height) {
                    if (mirror) {
                        ingredient = this.inputs.get(this.width - k - 1 + l * this.width);
                    } else {
                        ingredient = this.inputs.get(k + l * this.width);
                    }
                }

                if (!ingredient.test(inventory.getStackInSlot(i + j * 9))) {
                    return false;
                }
            }
        }

        return true;
    }

    public void setTransformers(TriFunction<Integer, Integer, ItemStack, ItemStack> transformers) {
        this.transformers = transformers;
    }

    public static class Serializer implements RecipeSerializer<ShapedExtremeCraftingRecipe> {
        @Override
        public @NotNull ShapedExtremeCraftingRecipe fromJson(@NotNull ResourceLocation recipeId, @NotNull JsonObject json) {
            var map = ShapedRecipe.keyFromJson(GsonHelper.getAsJsonObject(json, "key"));
            var pattern = ShapedRecipe.shrink(ShapedExtremeCraftingRecipe.patternFromJson(GsonHelper.getAsJsonArray(json, "pattern")));
            int width = pattern[0].length();
            int height = pattern.length;
            var inputs = ShapedRecipe.dissolvePattern(pattern, map, width, height);
            var output = ShapedRecipe.itemStackFromJson(GsonHelper.getAsJsonObject(json, "result"));

            return new ShapedExtremeCraftingRecipe(recipeId, width, height, inputs, output);
        }

        @Override
        public ShapedExtremeCraftingRecipe fromNetwork(@NotNull ResourceLocation recipeId, FriendlyByteBuf buffer) {
            int width = buffer.readVarInt();
            int height = buffer.readVarInt();
            var inputs = NonNullList.withSize(width * height, Ingredient.EMPTY);

            inputs.replaceAll(ignored -> Ingredient.fromNetwork(buffer));

            var output = buffer.readItem();

            return new ShapedExtremeCraftingRecipe(recipeId, width, height, inputs, output);
        }

        @Override
        public void toNetwork(FriendlyByteBuf buffer, ShapedExtremeCraftingRecipe recipe) {
            buffer.writeVarInt(recipe.width);
            buffer.writeVarInt(recipe.height);

            for (var ingredient : recipe.inputs) {
                ingredient.toNetwork(buffer);
            }

            buffer.writeItem(recipe.output);
        }
    }

}
