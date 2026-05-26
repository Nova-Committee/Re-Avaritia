package com.avaritia.common.crafting.recipe;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.avaritia.api.common.crafting.ITierCraftingRecipe;
import com.avaritia.api.common.crafting.ShapedRecipePatternCodecs;
import com.avaritia.api.common.crafting.TierInput;
import com.avaritia.api.utils.java.TriFunction;
import com.avaritia.init.registry.ModRecipeSerializers;
import com.avaritia.init.registry.ModRecipeTypes;
import lombok.Getter;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.ShapedRecipePattern;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

/**
 * Description:
 * Author: cnlimiter
 * Date: 2022/4/2 9:16
 * Version: 1.0
 * from <a href="https://github.com/BlakeBr0/ExtendedCrafting/blob/1.21/src/main/java/com/blakebr0/extendedcrafting/crafting/recipe/ShapedTableRecipe.java">...</a>
 */
public class ShapedTableCraftingRecipe implements ITierCraftingRecipe {
    public final ShapedRecipePattern pattern;
    public final ItemStack result;
    public final int tier;
    @Getter
    private final boolean compatible;
    private TriFunction<Integer, Integer, ItemStack, ItemStack> transformers;

    public ShapedTableCraftingRecipe(ShapedRecipePattern pattern, ItemStack result) {
        this(pattern, result, 0, false);
    }

    public ShapedTableCraftingRecipe(ShapedRecipePattern pattern, ItemStack result, int tier, boolean compatible) {
        this.pattern = pattern;
        this.result = result;
        this.tier = tier;
        this.compatible = compatible;
    }

    @Override
    public @NotNull ItemStack getResultItem(HolderLookup.@NotNull Provider registries) {
        return this.result;
    }

    @Override
    public @NotNull ItemStack assemble(@NotNull TierInput input, HolderLookup.@NotNull Provider registries) {
        return this.result.copy();
    }

    @Override
    public boolean matches(@NotNull TierInput input, @NotNull Level level) {
        if (this.tier != 0 && this.tier != input.tier())
            return false;

        return this.pattern.matches(input);
    }

    @Override
    public @NotNull NonNullList<Ingredient> getIngredients() {
        return this.pattern.ingredients();
    }

    @Override
    public @NotNull RecipeSerializer<?> getSerializer() {
        return ModRecipeSerializers.SHAPED_CRAFT_SERIALIZER.get();
    }

    @Override
    public @NotNull RecipeType<?> getType() {
        return ModRecipeTypes.CRAFTING_TABLE_RECIPE.get();
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return width >= this.pattern.width() && height >= this.pattern.height();
    }

    @Override
    public @NotNull NonNullList<ItemStack> getRemainingItems(@NotNull TierInput inventory) {
        var remaining = ITierCraftingRecipe.super.getRemainingItems(inventory);

        if (this.transformers != null) {
            var width = this.pattern.width();
            var height = this.pattern.height();

            if (inventory.width() != width && inventory.height() != height)
                return remaining;

            if (this.checkMatch(inventory, true)) {
                for (int i = 0; i < height; i++) {
                    for (int j = 0; j < width; j++) {
                        int index = width - j - 1 + i * width;
                        var stack = inventory.getItem(j, i);

                        remaining.set(index, this.transformers.apply(j, i, stack));
                    }
                }
            } else if (this.checkMatch(inventory, false)) {
                for (int i = 0; i < height; i++) {
                    for (int j = 0; j < width; j++) {
                        int index = j + i * width;
                        var stack = inventory.getItem(j, i);

                        remaining.set(index, this.transformers.apply(j, i, stack));
                    }
                }
            }
        }

        return remaining;
    }

    @Override
    public int getTier() {
        if (this.tier > 0) return this.tier;
        var width = this.pattern.width();
        var height = this.pattern.height();
        return width < 4 && height < 4 ? 1
                : width < 6 && height < 6 ? 2
                : width < 8 && height < 8 ? 3
                : 4;
    }

    @Override
    public boolean hasRequiredTier() {
        return this.tier > 0;
    }

    public int getWidth() {
        return this.pattern.width();
    }

    public int getHeight() {
        return this.pattern.height();
    }

    private boolean checkMatch(TierInput inventory, boolean symmetrical) {
        var width = this.pattern.width();
        var height = this.pattern.height();
        var ingredients = this.pattern.ingredients();

        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++) {
                Ingredient ingredient;
                if (symmetrical) {
                    ingredient = ingredients.get(width - j - 1 + i * width);
                } else {
                    ingredient = ingredients.get(j + i * width);
                }

                var stack = inventory.getItem(j, i);
                if (!ingredient.test(stack)) {
                    return false;
                }
            }
        }

        return true;
    }

    public void setTransformers(TriFunction<Integer, Integer, ItemStack, ItemStack> transformers) {
        this.transformers = transformers;
    }

    public static class Serializer {
        public static final MapCodec<ShapedTableCraftingRecipe> CODEC = RecordCodecBuilder.mapCodec(builder ->
                builder.group(
                        ShapedRecipePatternCodecs.MAP_CODEC.forGetter(recipe -> recipe.pattern),
                        ItemStack.STRICT_CODEC.fieldOf("result").forGetter(recipe -> recipe.result),
                        Codec.INT.optionalFieldOf("tier", 0).forGetter(recipe -> recipe.tier),
                        Codec.BOOL.optionalFieldOf("compatible", false).forGetter(recipe -> recipe.compatible)
                        ).apply(builder, ShapedTableCraftingRecipe::new)
        );
        public static final StreamCodec<RegistryFriendlyByteBuf, ShapedTableCraftingRecipe> STREAM_CODEC = StreamCodec.of(
                ShapedTableCraftingRecipe.Serializer::toNetwork, ShapedTableCraftingRecipe.Serializer::fromNetwork
        );
        public static final RecipeSerializer<ShapedTableCraftingRecipe> SERIALIZER = new RecipeSerializer<>(CODEC, STREAM_CODEC);

        private static ShapedTableCraftingRecipe fromNetwork(RegistryFriendlyByteBuf buffer) {
            var pattern = ShapedRecipePattern.STREAM_CODEC.decode(buffer);
            var result = ItemStack.STREAM_CODEC.decode(buffer);
            int tier = buffer.readVarInt();
            var compatible = buffer.readBoolean();

            return new ShapedTableCraftingRecipe(pattern, result, tier, compatible);
        }

        private static void toNetwork(RegistryFriendlyByteBuf buffer, ShapedTableCraftingRecipe recipe) {
            ShapedRecipePattern.STREAM_CODEC.encode(buffer, recipe.pattern);
            ItemStack.STREAM_CODEC.encode(buffer, recipe.result);
            buffer.writeVarInt(recipe.tier);
            buffer.writeBoolean(recipe.compatible);
        }
    }

}
