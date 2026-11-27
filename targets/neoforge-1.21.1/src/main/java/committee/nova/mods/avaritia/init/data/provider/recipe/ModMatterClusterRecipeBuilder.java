package committee.nova.mods.avaritia.init.data.provider.recipe;

import committee.nova.mods.avaritia.common.crafting.recipe.FullMatterClusterRecipe;
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
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.LinkedHashMap;
import java.util.Map;

public class ModMatterClusterRecipeBuilder implements RecipeBuilder {
    private final RecipeCategory category;
    private final Item result;
    private final int count;
    private final NonNullList<Ingredient> ingredients = NonNullList.create();
    private final Map<String, Criterion<?>> criteria = new LinkedHashMap<>();
    @Nullable
    private String group;
    private ICondition[] conditions;

    public ModMatterClusterRecipeBuilder(RecipeCategory category, ItemLike result, int count) {
        this.category = category;
        this.result = result.asItem();
        this.count = count;
    }

    @Contract("_, _, _ -> new")
    public static @NotNull ModMatterClusterRecipeBuilder shapeless(RecipeCategory category, ItemLike result, int count) {
        return new ModMatterClusterRecipeBuilder(category, result, count);
    }


    public ModMatterClusterRecipeBuilder requires(TagKey<Item> tag) {
        return this.requires(Ingredient.of(tag));
    }

    public ModMatterClusterRecipeBuilder requires(ItemLike item) {
        return this.requires(item, 1);
    }

    public ModMatterClusterRecipeBuilder requires(ItemLike item, int count) {
        for (int i = 0; i < count; i++) {
            this.requires(Ingredient.of(item));
        }
        return this;
    }

    public ModMatterClusterRecipeBuilder requires(Ingredient ingredient) {
        this.ingredients.add(ingredient);
        return this;
    }

    public @NotNull ModMatterClusterRecipeBuilder conditions(@Nullable ICondition... conditions) {
        this.conditions = conditions;
        return this;
    }

    @Override
    public @NotNull ModMatterClusterRecipeBuilder unlockedBy(@NotNull String criterionName, @NotNull Criterion<?> criterion) {
        this.criteria.put(criterionName, criterion);
        return this;
    }

    @Override
    public @NotNull ModMatterClusterRecipeBuilder group(@Nullable String group) {
        this.group = group;
        return this;
    }

    @Override
    public @NotNull Item getResult() {
        return this.result;
    }

    @Override
    public void save(RecipeOutput recipeOutput, @NotNull ResourceLocation id) {
        this.ensureValid(id);
        Advancement.Builder advancement$builder = recipeOutput.advancement()
                .addCriterion("has_the_recipe", RecipeUnlockedTrigger.unlocked(id))
                .rewards(AdvancementRewards.Builder.recipe(id))
                .requirements(AdvancementRequirements.Strategy.OR);
        this.criteria.forEach(advancement$builder::addCriterion);


        FullMatterClusterRecipe shapedrecipe = new FullMatterClusterRecipe(
                this.group,
                this.ingredients,
                this.count
        );
        var advancement = advancement$builder.build(id.withPrefix("recipes/" + this.category.getFolderName() + "/"));
        if (this.conditions != null) {
            recipeOutput.accept(id, shapedrecipe, advancement, this.conditions);
        } else recipeOutput.accept(id, shapedrecipe, advancement);
    }

    private void ensureValid(ResourceLocation id) {
        if (this.criteria.isEmpty()) {
            throw new IllegalStateException("No way of obtaining recipe " + id);
        }
    }
}