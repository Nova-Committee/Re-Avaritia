package com.avaritia.common.crafting.recipe;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.avaritia.api.common.crafting.ITierCraftingRecipe;
import com.avaritia.api.common.crafting.RecipeCodecs;
import com.avaritia.api.common.crafting.TierInput;
import com.avaritia.init.registry.ModRecipeSerializers;
import com.avaritia.init.registry.ModRecipeTypes;
import lombok.Getter;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.common.util.RecipeMatcher;
import org.jetbrains.annotations.NotNull;

import java.util.function.BiFunction;

/**
 * Description:
 * Author: cnlimiter
 * Date: 2022/4/2 9:16
 * Version: 1.0
 */
public class ShapelessTableCraftingRecipe implements ITierCraftingRecipe {
    @Getter
    public final NonNullList<Ingredient> inputs;
    public final ItemStack result;
    public final int tier;
    private BiFunction<Integer, ItemStack, ItemStack> transformer;

    public ShapelessTableCraftingRecipe(NonNullList<Ingredient> inputs, ItemStack result) {
        this(inputs, result, 0);
    }

    public ShapelessTableCraftingRecipe(NonNullList<Ingredient> inputs, ItemStack result, int tier) {
        this.inputs = inputs;
        this.result = result;
        this.tier = tier;
    }

    @Override
    public @NotNull ItemStack getResultItem(HolderLookup.@NotNull Provider registries) {
        return this.result;
    }

    @Override
    public @NotNull NonNullList<Ingredient> getIngredients() {
        return this.inputs;
    }

    @Override
    public @NotNull RecipeSerializer<? extends Recipe<TierInput>> getSerializer() {
        return ModRecipeSerializers.SHAPELESS_CRAFT_SERIALIZER.get();
    }

    @Override
    public @NotNull RecipeType<? extends Recipe<TierInput>> getType() {
        return ModRecipeTypes.CRAFTING_TABLE_RECIPE.get();
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return width * height >= this.inputs.size();
    }

    @Override
    public @NotNull ItemStack assemble(@NotNull TierInput input) {
        return this.result.copy();
    }
    @Override
    public boolean matches(@NotNull TierInput input, @NotNull Level level) {
        if (this.tier != 0 && this.tier != input.tier())
            return false;

        if (this.inputs.size() != input.ingredientCount())
            return false;

        var inputs = NonNullList.<ItemStack>create();
        int matched = 0;

        for (var i = 0; i < input.size(); i++) {
            var item = input.getItem(i);
            if (!item.isEmpty()) {
                inputs.add(item);
                matched++;
            }
        }

        return matched == this.inputs.size() && RecipeMatcher.findMatches(inputs, this.inputs) != null;
    }

    @Override
    public @NotNull NonNullList<ItemStack> getRemainingItems(@NotNull TierInput inventory) {
        var remaining = ITierCraftingRecipe.super.getRemainingItems(inventory);

        if (this.transformer != null) {
            var used = new boolean[remaining.size()];

            for (int i = 0; i < remaining.size(); i++) {
                var stack = inventory.getItem(i);

                for (int j = 0; j < this.inputs.size(); j++) {
                    var input = this.inputs.get(j);

                    if (!used[j] && input.test(stack)) {
                        var ingredient = this.transformer.apply(j, stack);

                        used[j] = true;
                        remaining.set(i, ingredient);

                        break;
                    }
                }
            }
        }

        return remaining;
    }

    @Override
    public int getTier() {
        if (this.tier > 0) return this.tier;
        var size = this.inputs.size();
        return size < 10 ? 1
                : size < 26 ? 2
                : size < 50 ? 3
                : 4;
    }

    @Override
    public boolean hasRequiredTier() {
        return this.tier > 0;
    }

    public void setTransformers(BiFunction<Integer, ItemStack, ItemStack> transformer) {
        this.transformer = transformer;
    }

    public static class Serializer {
        public static final MapCodec<ShapelessTableCraftingRecipe> CODEC = RecordCodecBuilder.mapCodec(builder ->
                builder.group(
                        RecipeCodecs.ingredientList(81, false, "Combination recipe")
                                .fieldOf("ingredients")
                                .forGetter(recipe -> recipe.inputs),
                        RecipeCodecs.STRICT_ITEM_STACK.fieldOf("result").forGetter(recipe -> recipe.result),
                        Codec.INT.optionalFieldOf("tier", 0).forGetter(recipe -> recipe.tier)
                ).apply(builder, ShapelessTableCraftingRecipe::new)
        );
        public static final StreamCodec<RegistryFriendlyByteBuf, ShapelessTableCraftingRecipe> STREAM_CODEC = StreamCodec.of(
                ShapelessTableCraftingRecipe.Serializer::toNetwork, ShapelessTableCraftingRecipe.Serializer::fromNetwork
        );
        public static final RecipeSerializer<ShapelessTableCraftingRecipe> SERIALIZER = new RecipeSerializer<>(CODEC, STREAM_CODEC);

        private static ShapelessTableCraftingRecipe fromNetwork(RegistryFriendlyByteBuf buffer) {
            int size = buffer.readVarInt();
            var inputs = NonNullList.<Ingredient>createWithCapacity(size);

            for (int i = 0; i < size; ++i) {
                inputs.add(Ingredient.CONTENTS_STREAM_CODEC.decode(buffer));
            }

            var result = ItemStack.STREAM_CODEC.decode(buffer);
            int tier = buffer.readVarInt();

            return new ShapelessTableCraftingRecipe(inputs, result, tier);
        }

        private static void toNetwork(RegistryFriendlyByteBuf buffer, ShapelessTableCraftingRecipe recipe) {
            buffer.writeVarInt(recipe.inputs.size());

            for (var ingredient : recipe.inputs) {
                Ingredient.CONTENTS_STREAM_CODEC.encode(buffer, ingredient);
            }

            ItemStack.STREAM_CODEC.encode(buffer, recipe.result);
            buffer.writeVarInt(recipe.tier);
        }
    }

}
