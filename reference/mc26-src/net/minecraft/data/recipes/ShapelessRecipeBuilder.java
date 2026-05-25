package net.minecraft.data.recipes;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import javax.annotation.Nullable;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementRequirements;
import net.minecraft.advancements.AdvancementRewards;
import net.minecraft.advancements.Criterion;
import net.minecraft.advancements.critereon.RecipeUnlockedTrigger;
import net.minecraft.core.NonNullList;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.ShapelessRecipe;
import net.minecraft.world.level.ItemLike;

public class ShapelessRecipeBuilder implements RecipeBuilder {
    private final RecipeCategory category;
    private final Item result;
    private final int count;
    private final ItemStack resultStack; // Neo: add stack result support
    private final NonNullList<Ingredient> ingredients = NonNullList.create();
    private final Map<String, Criterion<?>> criteria = new LinkedHashMap<>();
    @Nullable
    private String group;

    public ShapelessRecipeBuilder(RecipeCategory p_250837_, ItemLike p_251897_, int p_252227_) {
        this(p_250837_, new ItemStack(p_251897_, p_252227_));
    }

    public ShapelessRecipeBuilder(RecipeCategory p_250837_, ItemStack result) {
        this.category = p_250837_;
        this.result = result.getItem();
        this.count = result.getCount();
        this.resultStack = result;
    }

    public static ShapelessRecipeBuilder shapeless(RecipeCategory p_250714_, ItemLike p_249659_) {
        return new ShapelessRecipeBuilder(p_250714_, p_249659_, 1);
    }

    public static ShapelessRecipeBuilder shapeless(RecipeCategory p_252339_, ItemLike p_250836_, int p_249928_) {
        return new ShapelessRecipeBuilder(p_252339_, p_250836_, p_249928_);
    }

    public static ShapelessRecipeBuilder shapeless(RecipeCategory p_252339_, ItemStack result) {
        return new ShapelessRecipeBuilder(p_252339_, result);
    }

    public ShapelessRecipeBuilder requires(TagKey<Item> p_206420_) {
        return this.requires(Ingredient.of(p_206420_));
    }

    public ShapelessRecipeBuilder requires(ItemLike p_126210_) {
        return this.requires(p_126210_, 1);
    }

    public ShapelessRecipeBuilder requires(ItemLike p_126212_, int p_126213_) {
        for (int i = 0; i < p_126213_; i++) {
            this.requires(Ingredient.of(p_126212_));
        }

        return this;
    }

    public ShapelessRecipeBuilder requires(Ingredient p_126185_) {
        return this.requires(p_126185_, 1);
    }

    public ShapelessRecipeBuilder requires(Ingredient p_126187_, int p_126188_) {
        for (int i = 0; i < p_126188_; i++) {
            this.ingredients.add(p_126187_);
        }

        return this;
    }

    public ShapelessRecipeBuilder unlockedBy(String p_176781_, Criterion<?> p_300897_) {
        this.criteria.put(p_176781_, p_300897_);
        return this;
    }

    public ShapelessRecipeBuilder group(@Nullable String p_126195_) {
        this.group = p_126195_;
        return this;
    }

    @Override
    public Item getResult() {
        return this.result;
    }

    @Override
    public void save(RecipeOutput p_301215_, ResourceLocation p_126206_) {
        this.ensureValid(p_126206_);
        Advancement.Builder advancement$builder = p_301215_.advancement()
            .addCriterion("has_the_recipe", RecipeUnlockedTrigger.unlocked(p_126206_))
            .rewards(AdvancementRewards.Builder.recipe(p_126206_))
            .requirements(AdvancementRequirements.Strategy.OR);
        this.criteria.forEach(advancement$builder::addCriterion);
        ShapelessRecipe shapelessrecipe = new ShapelessRecipe(
            Objects.requireNonNullElse(this.group, ""),
            RecipeBuilder.determineBookCategory(this.category),
            this.resultStack,
            this.ingredients
        );
        p_301215_.accept(p_126206_, shapelessrecipe, advancement$builder.build(p_126206_.withPrefix("recipes/" + this.category.getFolderName() + "/")));
    }

    private void ensureValid(ResourceLocation p_126208_) {
        if (this.criteria.isEmpty()) {
            throw new IllegalStateException("No way of obtaining recipe " + p_126208_);
        }
    }
}
