package committee.nova.mods.avaritia.init.data.provider.recipe;

import com.google.common.collect.Lists;
import committee.nova.mods.avaritia.common.crafting.recipe.NoConsumeCatalystShapedRecipe;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementRequirements;
import net.minecraft.advancements.AdvancementRewards;
import net.minecraft.advancements.Criterion;
import net.minecraft.advancements.critereon.RecipeUnlockedTrigger;
import net.minecraft.data.recipes.RecipeBuilder;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.ShapedRecipePattern;
import net.minecraft.world.level.ItemLike;
import net.neoforged.neoforge.common.conditions.ICondition;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class NoConsumeCatalystShapedRecipeBuilder implements RecipeBuilder {
    private final RecipeCategory category;
    private final ItemStack result;
    private final List<String> rows = Lists.newArrayList();
    private final Map<Character, Ingredient> key = new HashMap<>();
    private final Map<String, Criterion<?>> criteria = new LinkedHashMap<>();
    @Nullable
    private String group;
    private int tier = 4;
    private ICondition[] conditions;


    public NoConsumeCatalystShapedRecipeBuilder(RecipeCategory category, ItemLike result, int count) {
        this.category = category;
        this.result = new ItemStack(result, count);
    }


    public static NoConsumeCatalystShapedRecipeBuilder shaped(RecipeCategory category, ItemLike result) {
        return new NoConsumeCatalystShapedRecipeBuilder(category, result, 1);
    }

    public static NoConsumeCatalystShapedRecipeBuilder shaped(RecipeCategory category, ItemLike result, int count) {
        return new NoConsumeCatalystShapedRecipeBuilder(category, result, count);
    }


    public NoConsumeCatalystShapedRecipeBuilder pattern(String row) {
        if (!this.rows.isEmpty() && row.length() != this.rows.get(0).length()) {
            throw new IllegalArgumentException("配方每行长度必须相同！");
        }
        this.rows.add(row);
        return this;
    }

    public NoConsumeCatalystShapedRecipeBuilder define(Character symbol, Ingredient ingredient) {
        if (this.key.containsKey(symbol)) {
            throw new IllegalArgumentException("Symbol '" + symbol + "' is already defined");
        }
        if (symbol == ' ') {
            throw new IllegalArgumentException("不能使用空格作为符号");
        }
        this.key.put(symbol, ingredient);
        return this;
    }

    public NoConsumeCatalystShapedRecipeBuilder define(Character symbol, ItemLike item) {
        return this.define(symbol, Ingredient.of(item));
    }

    public @NotNull NoConsumeCatalystShapedRecipeBuilder conditions(@Nullable ICondition... conditions) {
        this.conditions = conditions;
        return this;
    }

    public NoConsumeCatalystShapedRecipeBuilder tier(int tier) {
        this.tier = tier;
        return this;
    }

    @Override
    @NotNull
    public NoConsumeCatalystShapedRecipeBuilder unlockedBy(@NotNull String name, @NotNull Criterion<?> criterion) {
        this.criteria.put(name, criterion);
        return this;
    }

    @Override
    @NotNull
    public NoConsumeCatalystShapedRecipeBuilder group(@Nullable String group) {
        this.group = group;
        return this;
    }

    @Override
    @NotNull
    public Item getResult() {
        return result.getItem();
    }


    @Override
    public void save(RecipeOutput recipeOutput, @NotNull ResourceLocation id) {
        ShapedRecipePattern shapedrecipepattern = this.ensureValid(id);
        Advancement.Builder advancement$builder = recipeOutput.advancement()
                .addCriterion("has_the_recipe", RecipeUnlockedTrigger.unlocked(id))
                .rewards(AdvancementRewards.Builder.recipe(id))
                .requirements(AdvancementRequirements.Strategy.OR);
        this.criteria.forEach(advancement$builder::addCriterion);


        NoConsumeCatalystShapedRecipe shapedrecipe = new NoConsumeCatalystShapedRecipe(
                shapedrecipepattern,
                this.result,
                this.tier
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