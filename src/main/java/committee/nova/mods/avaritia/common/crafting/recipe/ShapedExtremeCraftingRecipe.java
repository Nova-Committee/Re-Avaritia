package committee.nova.mods.avaritia.common.crafting.recipe;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import committee.nova.mods.avaritia.api.common.crafting.ISpecialRecipe;
import committee.nova.mods.avaritia.init.registry.ModRecipeSerializers;
import committee.nova.mods.avaritia.init.registry.ModRecipeTypes;
import net.minecraft.core.NonNullList;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.function.Function;

/**
 * Description:
 * Author: cnlimiter
 * Date: 2022/4/2 9:16
 * Version: 1.0
 */
public class ShapedExtremeCraftingRecipe implements ISpecialRecipe {
    final ShapedExtremePattern pattern;
    final ItemStack result;
    final String group;
    private Map<Integer, Function<ItemStack, ItemStack>> transformers;

    public ShapedExtremeCraftingRecipe(String group, ShapedExtremePattern pattern, ItemStack result) {
        this.pattern = pattern;
        this.result = result;
        this.group = group;
    }


    public void setTransformers(Map<Integer, Function<ItemStack, ItemStack>> transformers) {
        this.transformers = transformers;
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
    public @NotNull String getGroup() {
        return this.group;
    }
    @Override
    public @NotNull ItemStack getResultItem(@NotNull RegistryAccess pRegistryAccess) {
        return this.result;
    }

    @Override
    public @NotNull NonNullList<Ingredient> getIngredients() {
        return this.pattern.ingredients();
    }

    @Override
    public boolean showNotification() {
        return false;
    }

    @Override
    public boolean canCraftInDimensions(int pWidth, int pHeight) {
        return pWidth >= this.pattern.width() && pHeight >= this.pattern.height();
    }


    @Override
    public boolean matches(@NotNull CraftingContainer pInv, @NotNull Level pLevel) {
        return this.pattern.matches(pInv);
    }


    @Override
    public @NotNull ItemStack assemble(@NotNull CraftingContainer pContainer, @NotNull RegistryAccess pRegistryAccess) {
        return this.getResultItem(pRegistryAccess).copy();
    }

    public int getWidth() {
        return this.pattern.width();
    }

    public int getHeight() {
        return this.pattern.height();
    }


    @Override
    public @NotNull NonNullList<ItemStack> getRemainingItems(@NotNull CraftingContainer pContainer) {
        if (this.transformers != null) {
            var remaining = NonNullList.withSize(pContainer.getContainerSize(), ItemStack.EMPTY);

            this.transformers.forEach((i, stack) -> {
                remaining.set(i, stack.apply(pContainer.getItem(i)));
            });

            return remaining;
        }

        return ISpecialRecipe.super.getRemainingItems(pContainer);
    }

    @Override
    public boolean isIncomplete() {
        NonNullList<Ingredient> nonnulllist = this.getIngredients();
        return nonnulllist.isEmpty() || nonnulllist.stream().filter(p_151277_ -> !p_151277_.isEmpty()).anyMatch(net.neoforged.neoforge.common.CommonHooks::hasNoElements);
    }

    public static class Serializer implements RecipeSerializer<ShapedExtremeCraftingRecipe> {
        public static final Codec<ShapedExtremeCraftingRecipe> CODEC = RecordCodecBuilder.create(
                recipeInstance -> recipeInstance.group(
                                ExtraCodecs.strictOptionalField(Codec.STRING, "group", "").forGetter(recipe -> recipe.group),
                                ShapedExtremePattern.MAP_CODEC.forGetter(recipe -> recipe.pattern),
                                ItemStack.ITEM_WITH_COUNT_CODEC.fieldOf("result").forGetter(recipe -> recipe.result)
                        )
                        .apply(recipeInstance, ShapedExtremeCraftingRecipe::new)
        );

        @Override
        public @NotNull Codec<ShapedExtremeCraftingRecipe> codec() {
            return CODEC;
        }

        @Override
        public @NotNull ShapedExtremeCraftingRecipe fromNetwork(FriendlyByteBuf pBuffer) {
            String s = pBuffer.readUtf();
            ShapedExtremePattern shaped = ShapedExtremePattern.fromNetwork(pBuffer);
            ItemStack itemstack = pBuffer.readItem();
            return new ShapedExtremeCraftingRecipe(s, shaped, itemstack);
        }

        @Override
        public void toNetwork(FriendlyByteBuf pBuffer, ShapedExtremeCraftingRecipe pRecipe) {
            pBuffer.writeUtf(pRecipe.group);
            pRecipe.pattern.toNetwork(pBuffer);
            pBuffer.writeItem(pRecipe.result);
        }
    }

}
