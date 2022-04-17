package nova.committee.avaritia.common.recipe;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import net.minecraft.core.NonNullList;
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
import net.minecraftforge.registries.ForgeRegistryEntry;
import nova.committee.avaritia.init.registry.ModRecipe;

import java.util.Map;
import java.util.function.Function;

/**
 * Description:
 * Author: cnlimiter
 * Date: 2022/4/2 9:16
 * Version: 1.0
 */
public class ShapedExtremeCraftingRecipe implements ISpecialRecipe, ICraftRecipe {
    private final ResourceLocation recipeId;
    private final NonNullList<Ingredient> inputs;
    private final ItemStack output;
    private final int width;
    private final int height;
    private final int tier;
    private Map<Integer, Function<ItemStack, ItemStack>> transformers;

    public ShapedExtremeCraftingRecipe(ResourceLocation recipeId, int width, int height, NonNullList<Ingredient> inputs, ItemStack output) {
        this(recipeId, width, height, inputs, output, 0);
    }

    public ShapedExtremeCraftingRecipe(ResourceLocation recipeId, int width, int height, NonNullList<Ingredient> inputs, ItemStack output, int tier) {
        this.recipeId = recipeId;
        this.inputs = inputs;
        this.output = output;
        this.width = width;
        this.height = height;
        this.tier = tier;
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

    @Override
    public ItemStack getResultItem() {
        return this.output;
    }

    @Override
    public ItemStack assemble(IItemHandler inventory) {
        return this.output.copy();
    }

    @Override
    public ItemStack assemble(Container inv) {
        return this.output.copy();
    }

    @Override
    public boolean matches(IItemHandler inventory) {
        if (this.tier != 0 && this.tier != this.getTierFromGridSize(inventory))
            return false;

        int size = (int) Math.sqrt(inventory.getSlots());
        for (int i = 0; i <= size - this.width; i++) {
            for (int j = 0; j <= size - this.height; j++) {
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
    public boolean matches(Container inv, Level level) {
        return this.matches(new InvWrapper(inv));
    }

    @Override
    public NonNullList<Ingredient> getIngredients() {
        return this.inputs;
    }

    @Override
    public ResourceLocation getId() {
        return this.recipeId;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return ModRecipe.EXTREME_CRAFT_RECIPE;
    }

    @Override
    public RecipeType<?> getType() {
        return ModRecipe.RecipeTypes.CRAFTING;
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return width >= this.width && height >= this.height;
    }

    @Override
    public NonNullList<ItemStack> getRemainingItems(Container inv) {
        if (this.transformers != null) {
            var remaining = NonNullList.withSize(inv.getContainerSize(), ItemStack.EMPTY);

            this.transformers.forEach((i, stack) -> {
                remaining.set(i, stack.apply(inv.getItem(i)));
            });

            return remaining;
        }

        return ISpecialRecipe.super.getRemainingItems(inv);
    }

    private int getTierFromGridSize(IItemHandler inv) {
        int size = inv.getSlots();
        return size < 10 ? 1
                : size < 26 ? 2
                : size < 50 ? 3
                : 4;
    }

    private boolean checkMatch(IItemHandler inventory, int x, int y, boolean mirror) {
        int size = (int) Math.sqrt(inventory.getSlots());
        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
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

                if (!ingredient.test(inventory.getStackInSlot(i + j * size))) {
                    return false;
                }
            }
        }

        return true;
    }

    public void setTransformers(Map<Integer, Function<ItemStack, ItemStack>> transformers) {
        this.transformers = transformers;
    }

    public static class Serializer extends ForgeRegistryEntry<RecipeSerializer<?>> implements RecipeSerializer<ShapedExtremeCraftingRecipe> {
        @Override
        public ShapedExtremeCraftingRecipe fromJson(ResourceLocation recipeId, JsonObject json) {
            var map = ShapedRecipe.keyFromJson(GsonHelper.getAsJsonObject(json, "key"));
            var pattern = ShapedRecipe.shrink(ShapedExtremeCraftingRecipe.patternFromJson(GsonHelper.getAsJsonArray(json, "pattern")));
            int width = pattern[0].length();
            int height = pattern.length;
            var inputs = ShapedRecipe.dissolvePattern(pattern, map, width, height);
            var output = ShapedRecipe.itemStackFromJson(GsonHelper.getAsJsonObject(json, "result"));
            int tier = GsonHelper.getAsInt(json, "tier", 0);
            int size = tier * 2 + 1;

            if (tier != 0 && (width > size || height > size))
                throw new JsonSyntaxException("The pattern size is larger than the specified tier can support");

            return new ShapedExtremeCraftingRecipe(recipeId, width, height, inputs, output, tier);
        }

        @Override
        public ShapedExtremeCraftingRecipe fromNetwork(ResourceLocation recipeId, FriendlyByteBuf buffer) {
            int width = buffer.readVarInt();
            int height = buffer.readVarInt();
            var inputs = NonNullList.withSize(width * height, Ingredient.EMPTY);

            for (int i = 0; i < inputs.size(); i++) {
                inputs.set(i, Ingredient.fromNetwork(buffer));
            }

            var output = buffer.readItem();
            int tier = buffer.readVarInt();

            return new ShapedExtremeCraftingRecipe(recipeId, width, height, inputs, output, tier);
        }

        @Override
        public void toNetwork(FriendlyByteBuf buffer, ShapedExtremeCraftingRecipe recipe) {
            buffer.writeVarInt(recipe.width);
            buffer.writeVarInt(recipe.height);

            for (var ingredient : recipe.inputs) {
                ingredient.toNetwork(buffer);
            }

            buffer.writeItem(recipe.output);
            buffer.writeVarInt(recipe.tier);
        }
    }

}
