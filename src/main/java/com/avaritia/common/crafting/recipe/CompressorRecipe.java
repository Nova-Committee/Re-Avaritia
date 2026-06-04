package com.avaritia.common.crafting.recipe;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.avaritia.api.common.crafting.ICompressorRecipe;
import com.avaritia.init.registry.ModBlocks;
import com.avaritia.init.registry.ModRecipeSerializers;
import com.avaritia.init.registry.ModRecipeTypes;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.display.FurnaceRecipeDisplay;
import net.minecraft.world.item.crafting.display.RecipeDisplay;
import net.minecraft.world.item.crafting.display.SlotDisplay;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * Description:
 * Author: cnlimiter
 * Date: 2022/4/2 17:40
 * Version: 1.0
 */
public class CompressorRecipe implements ICompressorRecipe {
    private final Ingredient input;
    private final ItemStack result;
    private final int inputCount;
    private final int timeCost;

    public CompressorRecipe(Ingredient input, ItemStack result, int inputCount, int timeCost) {
        this.input = input;
        this.result = result;
        this.inputCount = inputCount;
        this.timeCost = timeCost;
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return true;
    }

    @Override
    public @NotNull ItemStack getResultItem(@NotNull HolderLookup.Provider pRegistryAccess) {
        return this.result;
    }

    public @NotNull ItemStack getResultItem() {
        return this.result;
    }

    @Override
    public @NotNull NonNullList<Ingredient> getIngredients() {
        return NonNullList.of(Ingredient.EMPTY, this.input);
    }

    @Override
    public @NotNull Ingredient getInput() {
        return this.input;
    }

    @Override
    public int getTimeCost() {
        return timeCost;
    }

    @Override
    public @NotNull RecipeSerializer<?> getSerializer() {
        return ModRecipeSerializers.COMPRESSOR_SERIALIZER.get();
    }

    @Override
    public @NotNull RecipeType<?> getType() {
        return ModRecipeTypes.COMPRESSOR_RECIPE.get();
    }

    @Override
    public @NotNull ItemStack assemble(@NotNull CraftingInput input, HolderLookup.@NotNull Provider registries) {
        return this.result.copy();
    }
    @Override
    public boolean matches(@NotNull CraftingInput inv, @NotNull Level level) {
        if (inv.size() != 1) return false;
        var craftInput = inv.getItem(0);
        if (craftInput.isEmpty()) return false;  // 明确检查空物品
        return this.input.test(craftInput);
    }

    @Override
    public int getInputCount() {
        return this.inputCount;
    }

    @Override
    public @NotNull List<RecipeDisplay> display() {
        if (this.result.isEmpty()) {
            return List.of();
        }
        return List.of(new FurnaceRecipeDisplay(
                this.input.display(),
                SlotDisplay.Empty.INSTANCE,
                new SlotDisplay.ItemStackSlotDisplay(ItemStackTemplate.fromNonEmptyStack(this.result)),
                new SlotDisplay.ItemSlotDisplay(ModBlocks.neutron_compressor.get().asItem()),
                this.timeCost,
                0.0F
        ));
    }

    public static class Serializer {
        public static final MapCodec<CompressorRecipe> CODEC = RecordCodecBuilder.mapCodec(builder ->
                builder.group(
                        Ingredient.CODEC
                                .fieldOf("ingredient").forGetter(recipe -> recipe.input),
                        ItemStack.STRICT_CODEC.fieldOf("result").forGetter(recipe -> recipe.result),
                        Codec.INT.optionalFieldOf("inputCount", 1000).forGetter(recipe -> recipe.inputCount),
                        Codec.INT.fieldOf("timeCost").forGetter(recipe -> recipe.timeCost)
                ).apply(builder, CompressorRecipe::new)
        );
        public static final StreamCodec<RegistryFriendlyByteBuf, CompressorRecipe> STREAM_CODEC = StreamCodec.of(
                CompressorRecipe.Serializer::toNetwork, CompressorRecipe.Serializer::fromNetwork
        );
        public static final RecipeSerializer<CompressorRecipe> SERIALIZER = new RecipeSerializer<>(CODEC, STREAM_CODEC);

        private static CompressorRecipe fromNetwork(RegistryFriendlyByteBuf buffer) {
            var ingredient = Ingredient.CONTENTS_STREAM_CODEC.decode(buffer);
            var output = ItemStack.STREAM_CODEC.decode(buffer);
            int inputCount = buffer.readVarInt();
            int timeCost = buffer.readVarInt();

            return new CompressorRecipe(ingredient, output, inputCount, timeCost);
        }

        private static void toNetwork(RegistryFriendlyByteBuf buffer, CompressorRecipe recipe) {
            Ingredient.CONTENTS_STREAM_CODEC.encode(buffer, recipe.input);
            ItemStack.STREAM_CODEC.encode(buffer, recipe.result);
            buffer.writeVarInt(recipe.inputCount);
            buffer.writeVarInt(recipe.timeCost);
        }
    }
}
