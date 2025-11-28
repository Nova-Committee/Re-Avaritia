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
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Name: Avaritia-forge / ModShapelessRecipeBuilder
 * @author cnlimiter
 * CreateTime: 2023/8/24 14:07
 * Description:
 */

public class ModEternalRecipeBuilder implements RecipeBuilder {
    private final RecipeCategory category;
    private final NonNullList<Ingredient> ingredients = NonNullList.create();
    private final Map<String, Criterion<?>> criteria = new LinkedHashMap<>();
    @Nullable
    private String group;
    private final boolean custom;

    public ModEternalRecipeBuilder(RecipeCategory recipeCategory, boolean custom) {
        this.category = recipeCategory;
        this.custom = custom;
    }

    @Contract("_ -> new")
    public static @NotNull ModEternalRecipeBuilder shapeless(RecipeCategory recipeCategory) {
        return new ModEternalRecipeBuilder(recipeCategory, false);
    }

    @Contract("_, _ -> new")
    public static @NotNull ModEternalRecipeBuilder shapeless(RecipeCategory recipeCategory, boolean custom) {
        return new ModEternalRecipeBuilder(recipeCategory, custom);
    }


    public ModEternalRecipeBuilder requires(TagKey<Item> itemTagKey) {
        return this.requires(Ingredient.of(itemTagKey));
    }

    public ModEternalRecipeBuilder requires(ItemLike itemLike) {
        return this.requires(itemLike, 1);
    }

    public ModEternalRecipeBuilder requires(ItemLike itemLike, int p_126213_) {
        for (int i = 0; i < p_126213_; ++i) {
            this.requires(Ingredient.of(itemLike));
        }

        return this;
    }

    public ModEternalRecipeBuilder requires(Ingredient ingredient) {
        return this.requires(ingredient, 1);
    }

    public ModEternalRecipeBuilder requires(Ingredient ingredient, int i1) {
        for (int i = 0; i < i1; ++i) {
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
    public @NotNull ModEternalRecipeBuilder group(@Nullable String groupName) {
        this.group = groupName;
        return this;
    }

    @Override
    public @NotNull Item getResult() {
        return ModItems.eternal_singularity.get();
    }

    @Override
    public void save(@NotNull RecipeOutput recipeOutput, @NotNull ResourceLocation id) {
        this.ensureValid(id);
        Advancement.Builder advancement$builder = recipeOutput.advancement()
                .addCriterion("has_the_recipe", RecipeUnlockedTrigger.unlocked(id))
                .rewards(AdvancementRewards.Builder.recipe(id))
                .requirements(AdvancementRequirements.Strategy.OR);
        this.criteria.forEach(advancement$builder::addCriterion);
        EternalSingularityCraftRecipe shapelessrecipe = new EternalSingularityCraftRecipe(
                this.ingredients,
                this.custom
        );

        recipeOutput.accept(id, shapelessrecipe, advancement$builder.build(id.withPrefix("recipes/" + this.category.getFolderName() + "/")));
    }

    private void ensureValid(ResourceLocation id) {
        if (this.criteria.isEmpty()) {
            throw new IllegalStateException("No way of obtaining recipe " + id);
        }
    }
}
