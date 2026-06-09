package com.avaritia.common.crafting.recipe;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.avaritia.api.common.crafting.RecipeCodecs;
import com.avaritia.common.crafting.input.ExtremeSmithingRecipeInput;
import com.avaritia.init.registry.ModBlocks;
import com.avaritia.init.registry.ModRecipeSerializers;
import com.avaritia.init.registry.ModRecipeTypes;
import net.minecraft.core.HolderSet;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.PlacementInfo;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeBookCategories;
import net.minecraft.world.item.crafting.RecipeBookCategory;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.display.RecipeDisplay;
import net.minecraft.world.item.crafting.display.SlotDisplay;
import net.minecraft.world.item.crafting.display.SmithingRecipeDisplay;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

import java.util.List;
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
    public final ItemStackTemplate result;

    public ExtremeSmithingRecipe(Ingredient pTemplate, Ingredient pBase, Ingredient additions, ItemStack pResult) {
        this(pTemplate, pBase, additions, ItemStackTemplate.fromNonEmptyStack(pResult));
    }

    public ExtremeSmithingRecipe(Ingredient pTemplate, Ingredient pBase, Ingredient additions, ItemStackTemplate pResult) {
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
    public @NotNull ItemStack assemble(@NotNull ExtremeSmithingRecipeInput input) {
        ItemStack itemstack = input.base().transmuteCopy(this.result.item().value(), this.result.count());
        itemstack.applyComponents(this.result.components());
        return itemstack;
    }

    public boolean canCraftInDimensions(int width, int height) {
        return true;
    }

    public @NotNull ItemStack getResultItem(HolderLookup.@NotNull Provider registries) {
        return this.result.create();
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

    public @NotNull NonNullList<Ingredient> getIngredients() {
        NonNullList<Ingredient> ingredients = NonNullList.create();

        ingredients.add(this.template);
        ingredients.add(this.base);

        ingredients.addAll(this.getAdditionIngredients());

        return ingredients;
    }

    public List<Ingredient> getAdditionIngredients() {
        return this.additions.items()
                .limit(3)
                .map(holder -> Ingredient.of(HolderSet.direct(holder)))
                .toList();
    }

    @Override
    public @NotNull RecipeSerializer<ExtremeSmithingRecipe> getSerializer() {
        return ModRecipeSerializers.EXTREME_SMITHING_SERIALIZER.get();
    }

    @Override
    public @NotNull RecipeType<ExtremeSmithingRecipe> getType() {
        return ModRecipeTypes.EXTREME_SMITHING_RECIPE.get();
    }

    @Override
    public boolean showNotification() {
        return true;
    }

    @Override
    public @NotNull String group() {
        return "";
    }

    @Override
    public @NotNull PlacementInfo placementInfo() {
        return PlacementInfo.create(this.getIngredients());
    }

    @Override
    public @NotNull RecipeBookCategory recipeBookCategory() {
        return RecipeBookCategories.SMITHING;
    }

    @Override
    public @NotNull List<RecipeDisplay> display() {
        ItemStack result = this.result.create();
        if (result.isEmpty()) {
            return List.of();
        }
        return List.of(new SmithingRecipeDisplay(
                this.template.display(),
                this.base.display(),
                this.additions.display(),
                new SlotDisplay.ItemStackSlotDisplay(this.result),
                new SlotDisplay.ItemSlotDisplay(ModBlocks.extreme_smithing_table.get().asItem())
        ));
    }

    public boolean isIncomplete() {
        return Stream.of(this.template, this.base, this.additions).anyMatch(Ingredient::isEmpty)
                || this.getAdditionIngredients().size() < 3;
    }

    private static final MapCodec<ExtremeSmithingRecipe> CODEC = RecordCodecBuilder.mapCodec(
            p_340782_ -> p_340782_.group(
                            RecipeCodecs.LEGACY_INGREDIENT.fieldOf("template").forGetter(recipe -> recipe.template),
                            RecipeCodecs.LEGACY_INGREDIENT.fieldOf("base").forGetter(recipe -> recipe.base),
                            RecipeCodecs.LEGACY_INGREDIENT.fieldOf("addition").forGetter(recipe -> recipe.additions),
                            RecipeCodecs.LEGACY_ITEM_STACK_TEMPLATE.fieldOf("result").forGetter(recipe -> recipe.result)
                    )
                    .apply(p_340782_, ExtremeSmithingRecipe::new)
    );
    public static final StreamCodec<RegistryFriendlyByteBuf, ExtremeSmithingRecipe> STREAM_CODEC = StreamCodec.of(
            ExtremeSmithingRecipe::toNetwork, ExtremeSmithingRecipe::fromNetwork
    );

    public static final RecipeSerializer<ExtremeSmithingRecipe> SERIALIZER = new RecipeSerializer<>(CODEC, STREAM_CODEC);

    private static ExtremeSmithingRecipe fromNetwork(RegistryFriendlyByteBuf buffer) {
        Ingredient ingredient = Ingredient.CONTENTS_STREAM_CODEC.decode(buffer);
        Ingredient ingredient1 = Ingredient.CONTENTS_STREAM_CODEC.decode(buffer);
        Ingredient ingredient2 = Ingredient.CONTENTS_STREAM_CODEC.decode(buffer);
        ItemStackTemplate itemstack = ItemStackTemplate.STREAM_CODEC.decode(buffer);
        return new ExtremeSmithingRecipe(ingredient, ingredient1, ingredient2, itemstack);
    }

    private static void toNetwork(RegistryFriendlyByteBuf buffer, ExtremeSmithingRecipe recipe) {
        Ingredient.CONTENTS_STREAM_CODEC.encode(buffer, recipe.template);
        Ingredient.CONTENTS_STREAM_CODEC.encode(buffer, recipe.base);
        Ingredient.CONTENTS_STREAM_CODEC.encode(buffer, recipe.additions);
        ItemStackTemplate.STREAM_CODEC.encode(buffer, recipe.result);
    }
}
