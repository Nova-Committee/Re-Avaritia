package committee.nova.mods.avaritia.common.crafting.recipe;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import committee.nova.mods.avaritia.api.common.crafting.ICompressorRecipe;
import committee.nova.mods.avaritia.init.registry.ModRecipeSerializers;
import committee.nova.mods.avaritia.init.registry.ModRecipeTypes;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;

/**
 * Description:
 * Author: cnlimiter
 * Date: 2022/4/2 17:40
 * Version: 1.0
 */
public class CompressorRecipe implements ICompressorRecipe {
    private final NonNullList<Ingredient> inputs;
    private final ItemStack result;
    private final int inputCount;
    private final int timeCost;

    public CompressorRecipe(Ingredient input, ItemStack result, int inputCount, int timeCost) {
        this.inputs = NonNullList.of(Ingredient.EMPTY, input);
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
        return this.inputs;
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
        if (inv.ingredientCount() != 1)
            return false;
        var input = inv.getItem(0);
        return Arrays.stream(this.inputs.getFirst().getItems()).anyMatch(s -> s.is(input.getItem()));
    }

    @Override
    public int getInputCount() {
        return this.inputCount;
    }

    public static class Serializer implements RecipeSerializer<CompressorRecipe> {
        public static final MapCodec<CompressorRecipe> CODEC = RecordCodecBuilder.mapCodec(builder ->
                builder.group(
                        Ingredient.CODEC
                                .fieldOf("ingredient").forGetter(recipe -> recipe.inputs.getFirst()),
                        ItemStack.STRICT_CODEC.fieldOf("result").forGetter(recipe -> recipe.result),
                        Codec.INT.optionalFieldOf("inputCount", 1000).forGetter(recipe -> recipe.inputCount),
                        Codec.INT.fieldOf("timeCost").forGetter(recipe -> recipe.timeCost)
                ).apply(builder, CompressorRecipe::new)
        );
        public static final StreamCodec<RegistryFriendlyByteBuf, CompressorRecipe> STREAM_CODEC = StreamCodec.of(
                CompressorRecipe.Serializer::toNetwork, CompressorRecipe.Serializer::fromNetwork
        );
        @Override
        public @NotNull MapCodec<CompressorRecipe> codec() {
            return CODEC;
        }

        @Override
        public @NotNull StreamCodec<RegistryFriendlyByteBuf, CompressorRecipe> streamCodec() {
            return STREAM_CODEC;
        }

        private static CompressorRecipe fromNetwork(RegistryFriendlyByteBuf buffer) {
            var ingredient = Ingredient.CONTENTS_STREAM_CODEC.decode(buffer);
            var output = ItemStack.STREAM_CODEC.decode(buffer);
            int inputCount = buffer.readVarInt();
            int timeCost = buffer.readVarInt();

            return new CompressorRecipe(ingredient, output, inputCount, timeCost);
        }

        private static void toNetwork(RegistryFriendlyByteBuf buffer, CompressorRecipe recipe) {
            Ingredient.CONTENTS_STREAM_CODEC.encode(buffer, recipe.inputs.getFirst());
            ItemStack.STREAM_CODEC.encode(buffer, recipe.result);
            buffer.writeVarInt(recipe.inputCount);
            buffer.writeVarInt(recipe.timeCost);
        }
    }
}