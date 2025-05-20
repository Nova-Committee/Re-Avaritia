package committee.nova.mods.avaritia.init.data.provider.recipe;

import committee.nova.mods.avaritia.common.crafting.recipe.ShapelessTableCraftingRecipe;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementRequirements;
import net.minecraft.advancements.AdvancementRewards;
import net.minecraft.advancements.Criterion;
import net.minecraft.advancements.critereon.RecipeUnlockedTrigger;
import net.minecraft.core.NonNullList;
import net.minecraft.data.recipes.RecipeBuilder;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.ItemLike;
import net.neoforged.neoforge.common.conditions.ICondition;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Name: Avaritia-forge / ModShapelessRecipeBuilder
 * Author: cnlimiter
 * CreateTime: 2023/8/24 14:07
 * Description:
 */

public class ModShapelessRecipeBuilder implements RecipeBuilder {
    private final RecipeCategory category;
    private final Item result;
    private final int count;
    private final int tier;
    private final ItemStack resultStack;
    private final NonNullList<Ingredient> ingredients = NonNullList.create();
    private final Map<String, Criterion<?>> criteria = new LinkedHashMap<>();
    @Nullable
    private String group;
    private ICondition[] conditions;

    public ModShapelessRecipeBuilder(RecipeCategory category, ItemLike result, int count) {
        this(category, new ItemStack(result, count), 4);
    }

    public ModShapelessRecipeBuilder(RecipeCategory category, ItemLike result, int count, int tier) {
        this(category, new ItemStack(result, count), tier);
    }

    public ModShapelessRecipeBuilder(RecipeCategory category, ItemStack result, int tier) {
        this.category = category;
        this.result = result.getItem();
        this.count = result.getCount();
        this.resultStack = result;
        this.tier = tier;
    }

    public static ModShapelessRecipeBuilder shapeless(RecipeCategory category, ItemLike result) {
        return new ModShapelessRecipeBuilder(category, result, 1);
    }

    public static ModShapelessRecipeBuilder shapeless(RecipeCategory category, ItemLike result, int count) {
        return new ModShapelessRecipeBuilder(category, result, count);
    }

    public static ModShapelessRecipeBuilder shapeless(RecipeCategory category, ItemLike result, int count, int tier) {
        return new ModShapelessRecipeBuilder(category, result, count, tier);
    }

    public static ModShapelessRecipeBuilder shapeless(RecipeCategory category, ItemStack result, int tier) {
        return new ModShapelessRecipeBuilder(category, result, tier);
    }

    public ModShapelessRecipeBuilder requires(TagKey<Item> tag) {
        return this.requires(Ingredient.of(tag));
    }

    public ModShapelessRecipeBuilder requires(ItemLike item) {
        return this.requires(item, 1);
    }

    public ModShapelessRecipeBuilder requires(ItemLike item, int quantity) {
        for (int i = 0; i < quantity; i++) {
            this.requires(Ingredient.of(item));
        }

        return this;
    }

    public ModShapelessRecipeBuilder requires(Ingredient ingredient) {
        return this.requires(ingredient, 1);
    }

    public ModShapelessRecipeBuilder requires(Ingredient ingredient, int quantity) {
        for (int i = 0; i < quantity; i++) {
            this.ingredients.add(ingredient);
        }

        return this;
    }

    @Override
    public @NotNull ModShapelessRecipeBuilder unlockedBy(@NotNull String name, @NotNull Criterion<?> criterion) {
        this.criteria.put(name, criterion);
        return this;
    }

    @Override
    public @NotNull ModShapelessRecipeBuilder group(@Nullable String group) {
        this.group = group;
        return this;
    }

    @Override
    public @NotNull Item getResult() {
        return this.result;
    }

    public @NotNull ModShapelessRecipeBuilder conditions(@Nullable ICondition... conditions) {
        this.conditions = conditions;
        return this;
    }

    @Override
    public void save(RecipeOutput recipeOutput, ResourceLocation id) {
        this.ensureValid(id);
        Advancement.Builder advancement$builder = recipeOutput.advancement()
                .addCriterion("has_the_recipe", RecipeUnlockedTrigger.unlocked(id))
                .rewards(AdvancementRewards.Builder.recipe(id))
                .requirements(AdvancementRequirements.Strategy.OR);
        this.criteria.forEach(advancement$builder::addCriterion);
        ShapelessTableCraftingRecipe shapelessrecipe = new ShapelessTableCraftingRecipe(
                this.ingredients,
                this.resultStack,
                this.tier
        );
        recipeOutput.accept(id, shapelessrecipe, advancement$builder.build(id.withPrefix("recipes/" + this.category.getFolderName() + "/")), this.conditions);
    }

    private void ensureValid(ResourceLocation id) {
        if (this.criteria.isEmpty()) {
            throw new IllegalStateException("No way of obtaining recipe " + id);
        }
    }
}
