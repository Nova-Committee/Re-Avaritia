package committee.nova.mods.avaritia.init.data.provider.recipe;

import committee.nova.mods.avaritia.common.crafting.recipe.EternalSingularityCraftRecipe;
import committee.nova.mods.avaritia.init.registry.ModItems;
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

public class ModEternalRecipeBuilder implements RecipeBuilder {
    private final RecipeCategory category;
    private final NonNullList<Ingredient> ingredients = NonNullList.create();
    private final Map<String, Criterion<?>> criteria = new LinkedHashMap<>();
    @Nullable
    private String group;
    private ICondition[] conditions;


    public ModEternalRecipeBuilder(RecipeCategory category) {
        this.category = category;
    }

    public static ModEternalRecipeBuilder shapeless(RecipeCategory category) {
        return new ModEternalRecipeBuilder(category);
    }

    public ModEternalRecipeBuilder requires(TagKey<Item> tag) {
        return this.requires(Ingredient.of(tag));
    }

    public ModEternalRecipeBuilder requires(ItemLike item) {
        return this.requires(item, 1);
    }

    public ModEternalRecipeBuilder requires(ItemLike item, int quantity) {
        for (int i = 0; i < quantity; i++) {
            this.requires(Ingredient.of(item));
        }

        return this;
    }

    public ModEternalRecipeBuilder requires(Ingredient ingredient) {
        return this.requires(ingredient, 1);
    }

    public ModEternalRecipeBuilder requires(Ingredient ingredient, int quantity) {
        for (int i = 0; i < quantity; i++) {
            this.ingredients.add(ingredient);
        }

        return this;
    }

    @Override
    public @NotNull ModEternalRecipeBuilder unlockedBy(@NotNull String name, @NotNull Criterion<?> criterion) {
        this.criteria.put(name, criterion);
        return this;
    }

    @Override
    public @NotNull ModEternalRecipeBuilder group(@Nullable String group) {
        this.group = group;
        return this;
    }

    @Override
    public @NotNull Item getResult() {
        return ModItems.infinity_catalyst.get();
    }


    public @NotNull ModEternalRecipeBuilder conditions(@Nullable ICondition... conditions) {
        this.conditions = conditions;
        return this;
    }

    @Override
    public void save(RecipeOutput recipeOutput, @NotNull ResourceLocation id) {
        this.ensureValid(id);
        Advancement.Builder advancement$builder = recipeOutput.advancement()
                .addCriterion("has_the_recipe", RecipeUnlockedTrigger.unlocked(id))
                .rewards(AdvancementRewards.Builder.recipe(id))
                .requirements(AdvancementRequirements.Strategy.OR);
        this.criteria.forEach(advancement$builder::addCriterion);
        EternalSingularityCraftRecipe shapelessrecipe = new EternalSingularityCraftRecipe(
                ModItems.eternal_singularity.get().getDefaultInstance()
        );
        if (this.conditions != null) {
            recipeOutput.accept(id, shapelessrecipe, advancement$builder.build(id.withPrefix("recipes/" + this.category.getFolderName() + "/")), this.conditions);
        } else {
            recipeOutput.accept(id, shapelessrecipe, advancement$builder.build(id.withPrefix("recipes/" + this.category.getFolderName() + "/")));
        }
    }

    private void ensureValid(ResourceLocation id) {
        if (this.criteria.isEmpty()) {
            throw new IllegalStateException("No way of obtaining recipe " + id);
        }
    }

}
