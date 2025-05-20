package committee.nova.mods.avaritia.common.crafting.recipe;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import committee.nova.mods.avaritia.common.crafting.input.ExtremeSmithingRecipeInput;
import committee.nova.mods.avaritia.init.registry.ModRecipeSerializers;
import committee.nova.mods.avaritia.init.registry.ModRecipeTypes;
import net.minecraft.core.HolderLookup;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

import java.util.stream.Stream;

/**
 * @Project: Avaritia
 * @Author: cnlimiter
 * @CreateTime: 2024/12/20 12:52
 * @Description:
 */
public class ExtremeSmithingRecipe implements Recipe<ExtremeSmithingRecipeInput> {
    public final Ingredient template;
    public final Ingredient base;
    public final Ingredient additions;
    public final ItemStack result;

    public ExtremeSmithingRecipe(Ingredient pTemplate, Ingredient pBase, Ingredient additions, ItemStack pResult) {
        this.template = pTemplate;
        this.base = pBase;
        this.additions = additions;
        this.result = pResult;
    }
    @Override
    public boolean matches(@NotNull ExtremeSmithingRecipeInput input, @NotNull Level level) {
        return this.template.test(input.getItem(0)) && this.base.test(input.getItem(1))
                && this.additions.test(input.getItem(2))
                && this.additions.test(input.getItem(3))
                && this.additions.test(input.getItem(4));
    }

    @Override
    public @NotNull ItemStack assemble(@NotNull ExtremeSmithingRecipeInput input, HolderLookup.@NotNull Provider registries) {
        ItemStack itemstack = input.base().transmuteCopy(this.result.getItem(), this.result.getCount());
        itemstack.applyComponents(this.result.getComponentsPatch());
        return itemstack;
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return true;
    }

    @Override
    public @NotNull ItemStack getResultItem(HolderLookup.@NotNull Provider registries) {
        return this.result;
    }
    public boolean isTemplateIngredient(@NotNull ItemStack pStack) {
        return this.template.test(pStack);
    }
    public boolean isBaseIngredient(@NotNull ItemStack pStack) {
        return this.base.test(pStack);
    }
    public boolean isAdditionIngredient(@NotNull ItemStack pStack) {
        return this.additions.test(pStack);
    }

    @Override
    public @NotNull RecipeSerializer<?> getSerializer() {
        return ModRecipeSerializers.EXTREME_SMITHING_SERIALIZER.get();
    }

    @Override
    public @NotNull RecipeType<?> getType() {
        return ModRecipeTypes.EXTREME_SMITHING_RECIPE.get();
    }

    @Override
    public boolean isIncomplete() {
        return Stream.of(this.template, this.base, this.additions).anyMatch(Ingredient::hasNoItems);
    }

    public static class Serializer implements RecipeSerializer<ExtremeSmithingRecipe> {
        private static final MapCodec<ExtremeSmithingRecipe> CODEC = RecordCodecBuilder.mapCodec(
                p_340782_ -> p_340782_.group(
                                Ingredient.CODEC.fieldOf("template").forGetter(recipe -> recipe.template),
                                Ingredient.CODEC.fieldOf("base").forGetter(recipe -> recipe.base),
                                Ingredient.CODEC.fieldOf("addition").forGetter(recipe -> recipe.additions),
                                ItemStack.STRICT_CODEC.fieldOf("result").forGetter(recipe -> recipe.result)
                        )
                        .apply(p_340782_, ExtremeSmithingRecipe::new)
        );
        public static final StreamCodec<RegistryFriendlyByteBuf, ExtremeSmithingRecipe> STREAM_CODEC = StreamCodec.of(
                ExtremeSmithingRecipe.Serializer::toNetwork, ExtremeSmithingRecipe.Serializer::fromNetwork
        );

        @Override
        public @NotNull MapCodec<ExtremeSmithingRecipe> codec() {
            return CODEC;
        }

        @Override
        public @NotNull StreamCodec<RegistryFriendlyByteBuf, ExtremeSmithingRecipe> streamCodec() {
            return STREAM_CODEC;
        }

        private static ExtremeSmithingRecipe fromNetwork(RegistryFriendlyByteBuf buffer) {
            Ingredient ingredient = Ingredient.CONTENTS_STREAM_CODEC.decode(buffer);
            Ingredient ingredient1 = Ingredient.CONTENTS_STREAM_CODEC.decode(buffer);
            Ingredient ingredient2 = Ingredient.CONTENTS_STREAM_CODEC.decode(buffer);
            ItemStack itemstack = ItemStack.STREAM_CODEC.decode(buffer);
            return new ExtremeSmithingRecipe(ingredient, ingredient1, ingredient2, itemstack);
        }

        private static void toNetwork(RegistryFriendlyByteBuf buffer, ExtremeSmithingRecipe recipe) {
            Ingredient.CONTENTS_STREAM_CODEC.encode(buffer, recipe.template);
            Ingredient.CONTENTS_STREAM_CODEC.encode(buffer, recipe.base);
            Ingredient.CONTENTS_STREAM_CODEC.encode(buffer, recipe.additions);
            ItemStack.STREAM_CODEC.encode(buffer, recipe.result);
        }
    }
}
