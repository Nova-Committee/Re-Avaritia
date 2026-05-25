package net.minecraft.data.recipes;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import javax.annotation.Nullable;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementRequirements;
import net.minecraft.advancements.AdvancementRewards;
import net.minecraft.advancements.Criterion;
import net.minecraft.advancements.critereon.RecipeUnlockedTrigger;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.ShapedRecipe;
import net.minecraft.world.item.crafting.ShapedRecipePattern;
import net.minecraft.world.level.ItemLike;

public class ShapedRecipeBuilder implements RecipeBuilder {
    private final RecipeCategory category;
    private final Item result;
    private final int count;
    private final ItemStack resultStack; // Neo: add stack result support
    private final List<String> rows = Lists.newArrayList();
    private final Map<Character, Ingredient> key = Maps.newLinkedHashMap();
    private final Map<String, Criterion<?>> criteria = new LinkedHashMap<>();
    @Nullable
    private String group;
    private boolean showNotification = true;

    public ShapedRecipeBuilder(RecipeCategory p_249996_, ItemLike p_251475_, int p_248948_) {
        this(p_249996_, new ItemStack(p_251475_, p_248948_));
    }

    public ShapedRecipeBuilder(RecipeCategory p_249996_, ItemStack result) {
        this.category = p_249996_;
        this.result = result.getItem();
        this.count = result.getCount();
        this.resultStack = result;
    }

    public static ShapedRecipeBuilder shaped(RecipeCategory p_250853_, ItemLike p_249747_) {
        return shaped(p_250853_, p_249747_, 1);
    }

    public static ShapedRecipeBuilder shaped(RecipeCategory p_251325_, ItemLike p_250636_, int p_249081_) {
        return new ShapedRecipeBuilder(p_251325_, p_250636_, p_249081_);
    }

    public static ShapedRecipeBuilder shaped(RecipeCategory p_251325_, ItemStack result) {
        return new ShapedRecipeBuilder(p_251325_, result);
    }

    public ShapedRecipeBuilder define(Character p_206417_, TagKey<Item> p_206418_) {
        return this.define(p_206417_, Ingredient.of(p_206418_));
    }

    public ShapedRecipeBuilder define(Character p_126128_, ItemLike p_126129_) {
        return this.define(p_126128_, Ingredient.of(p_126129_));
    }

    public ShapedRecipeBuilder define(Character p_126125_, Ingredient p_126126_) {
        if (this.key.containsKey(p_126125_)) {
            throw new IllegalArgumentException("Symbol '" + p_126125_ + "' is already defined!");
        } else if (p_126125_ == ' ') {
            throw new IllegalArgumentException("Symbol ' ' (whitespace) is reserved and cannot be defined");
        } else {
            this.key.put(p_126125_, p_126126_);
            return this;
        }
    }

    public ShapedRecipeBuilder pattern(String p_126131_) {
        if (!this.rows.isEmpty() && p_126131_.length() != this.rows.get(0).length()) {
            throw new IllegalArgumentException("Pattern must be the same width on every line!");
        } else {
            this.rows.add(p_126131_);
            return this;
        }
    }

    public ShapedRecipeBuilder unlockedBy(String p_126133_, Criterion<?> p_301126_) {
        this.criteria.put(p_126133_, p_301126_);
        return this;
    }

    public ShapedRecipeBuilder group(@Nullable String p_126146_) {
        this.group = p_126146_;
        return this;
    }

    public ShapedRecipeBuilder showNotification(boolean p_273326_) {
        this.showNotification = p_273326_;
        return this;
    }

    @Override
    public Item getResult() {
        return this.result;
    }

    @Override
    public void save(RecipeOutput p_301098_, ResourceLocation p_126142_) {
        ShapedRecipePattern shapedrecipepattern = this.ensureValid(p_126142_);
        Advancement.Builder advancement$builder = p_301098_.advancement()
            .addCriterion("has_the_recipe", RecipeUnlockedTrigger.unlocked(p_126142_))
            .rewards(AdvancementRewards.Builder.recipe(p_126142_))
            .requirements(AdvancementRequirements.Strategy.OR);
        this.criteria.forEach(advancement$builder::addCriterion);
        ShapedRecipe shapedrecipe = new ShapedRecipe(
            Objects.requireNonNullElse(this.group, ""),
            RecipeBuilder.determineBookCategory(this.category),
            shapedrecipepattern,
            this.resultStack,
            this.showNotification
        );
        p_301098_.accept(p_126142_, shapedrecipe, advancement$builder.build(p_126142_.withPrefix("recipes/" + this.category.getFolderName() + "/")));
    }

    private ShapedRecipePattern ensureValid(ResourceLocation p_126144_) {
        if (this.criteria.isEmpty()) {
            throw new IllegalStateException("No way of obtaining recipe " + p_126144_);
        } else {
            return ShapedRecipePattern.of(this.key, this.rows);
        }
    }
}
