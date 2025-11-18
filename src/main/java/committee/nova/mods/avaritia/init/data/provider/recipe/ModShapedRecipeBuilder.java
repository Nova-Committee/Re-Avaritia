package committee.nova.mods.avaritia.init.data.provider.recipe;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import committee.nova.mods.avaritia.common.crafting.recipe.ShapedTableCraftingRecipe;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementRequirements;
import net.minecraft.advancements.AdvancementRewards;
import net.minecraft.advancements.Criterion;
import net.minecraft.advancements.critereon.RecipeUnlockedTrigger;
import net.minecraft.data.recipes.RecipeBuilder;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.ShapedRecipePattern;
import net.minecraft.world.level.ItemLike;
import net.neoforged.neoforge.common.conditions.ICondition;
import net.neoforged.neoforge.common.crafting.DataComponentIngredient;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Name: Avaritia-forge / ModRecipeBuilder
 * Author: cnlimiter
 * CreateTime: 2023/8/24 13:59
 * Description:
 */

public class ModShapedRecipeBuilder implements RecipeBuilder {
    private final RecipeCategory category;
    private final ItemLike result;
    private final ItemStack resultStack;
    private final int tier;
    private final boolean compatible;
    private final List<String> rows = Lists.newArrayList();
    private final Map<Character, Ingredient> key = Maps.newLinkedHashMap();
    private final Map<String, Criterion<?>> criteria = new LinkedHashMap<>();
    @Nullable
    private String group;
    private ICondition[] conditions;

    public ModShapedRecipeBuilder(RecipeCategory category, ItemLike result, int count) {
        this(category, new ItemStack(result, count), 4, false);
    }

    public ModShapedRecipeBuilder(RecipeCategory category, ItemLike result, int count, boolean compatible) {
        this(category, new ItemStack(result, count), 4, compatible);
    }

    public ModShapedRecipeBuilder(RecipeCategory category, ItemLike result, int count, int tier, boolean compatible) {
        this(category, new ItemStack(result, count), tier, compatible);
    }

    public ModShapedRecipeBuilder(RecipeCategory category, ItemStack result, boolean compatible) {
        this(category, result, 4, compatible);
    }

    public ModShapedRecipeBuilder(RecipeCategory category, ItemStack result, int tier, boolean compatible) {
        this.category = category;
        this.result = result.getItem();
        this.resultStack = result;
        this.tier = tier;
        this.compatible = compatible;
    }

    public static ModShapedRecipeBuilder shaped(RecipeCategory category, ItemLike result) {
        return shaped(category, result, 4);
    }

    /**
     * Creates a new builder for a shaped recipe.
     */

    public static ModShapedRecipeBuilder shaped(RecipeCategory category, ItemLike result, int tier) {
        return new ModShapedRecipeBuilder(category, new ItemStack(result, 1), tier, false);
    }

    public static ModShapedRecipeBuilder shaped(RecipeCategory category, ItemLike result, int tier, boolean compatible) {
        return new ModShapedRecipeBuilder(category, new ItemStack(result, 1), tier, compatible);
    }

    public static ModShapedRecipeBuilder shaped(RecipeCategory category, ItemStack result, int tier, boolean compatible) {
        return new ModShapedRecipeBuilder(category, result, tier, compatible);
    }


    public ModShapedRecipeBuilder define(Character symbol, TagKey<Item> tag) {
        return this.define(symbol, Ingredient.of(tag));
    }

    public ModShapedRecipeBuilder define(Character symbol, ItemStack item) {
        return this.define(symbol, DataComponentIngredient.of(true, item));
    }

    /**
     * Adds a key to the recipe pattern.
     */
    public ModShapedRecipeBuilder define(Character symbol, ItemLike item) {
        return this.define(symbol, Ingredient.of(item));
    }

    public ModShapedRecipeBuilder define(Character character, Ingredient ingredient) {
        if (this.key.containsKey(character)) {
            throw new IllegalArgumentException("Symbol '" + character + "' is already defined!");
        } else if (character == ' ') {
            throw new IllegalArgumentException("Symbol ' ' (whitespace) is reserved and cannot be defined");
        } else {
            this.key.put(character, ingredient);
            return this;
        }
    }

    public ModShapedRecipeBuilder pattern(String s) {
        if (!this.rows.isEmpty() && s.length() != this.rows.get(0).length()) {
            throw new IllegalArgumentException("Pattern must be the same width on every line!");
        } else {
            this.rows.add(s);
            return this;
        }
    }

    @Override
    public @NotNull RecipeBuilder unlockedBy(@NotNull String name, @NotNull Criterion<?> criterion) {
        this.criteria.put(name, criterion);
        return this;
    }

    @Override
    public @NotNull ModShapedRecipeBuilder group(@Nullable String pGroupName) {
        this.group = pGroupName;
        return this;
    }

    public @NotNull ModShapedRecipeBuilder conditions(@Nullable ICondition... conditions) {
        this.conditions = conditions;
        return this;
    }

    @Override
    public @NotNull Item getResult() {
        if (this.result != null) {
            return this.result.asItem();
        }
        return Items.AIR;
    }

    @Override
    public void save(RecipeOutput recipeOutput, @NotNull ResourceLocation id) {
        ShapedRecipePattern shapedrecipepattern = this.ensureValid(id);
        Advancement.Builder advancement$builder = recipeOutput.advancement()
                .addCriterion("has_the_recipe", RecipeUnlockedTrigger.unlocked(id))
                .rewards(AdvancementRewards.Builder.recipe(id))
                .requirements(AdvancementRequirements.Strategy.OR);
        this.criteria.forEach(advancement$builder::addCriterion);

        ShapedTableCraftingRecipe shapedrecipe = new ShapedTableCraftingRecipe(
                shapedrecipepattern,
                this.resultStack,
                this.tier,
                this.compatible
        );
        var advancement = advancement$builder.build(id.withPrefix("recipes/" + this.category.getFolderName() + "/"));
        if (this.conditions != null) {
            recipeOutput.accept(id, shapedrecipe, advancement, this.conditions);
        } else recipeOutput.accept(id, shapedrecipe, advancement);
    }


    private ShapedRecipePattern ensureValid(ResourceLocation resourceLocation) {
        if (this.criteria.isEmpty()) {
            throw new IllegalStateException("No way of obtaining recipe " + resourceLocation);
        } else {
            return ShapedRecipePattern.of(this.key, this.rows);
        }
    }

}
