package committee.nova.mods.avaritia.init.data.provider.recipe;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import committee.nova.mods.avaritia.common.crafting.recipe.ShapedTableCraftingRecipe;
import committee.nova.mods.avaritia.common.ingredient.SimpleDatagenIngredient;
import committee.nova.mods.avaritia.init.registry.enums.Mods;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementRequirements;
import net.minecraft.advancements.AdvancementRewards;
import net.minecraft.advancements.Criterion;
import net.minecraft.advancements.critereon.RecipeUnlockedTrigger;
import net.minecraft.core.registries.BuiltInRegistries;
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
import org.jetbrains.annotations.Contract;
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
    private final ResourceLocation result2;
    private final int count;
    private final int tier;
    private final List<String> rows = Lists.newArrayList();
    private final Map<Character, Ingredient> key = Maps.newLinkedHashMap();
    private final Map<String, Criterion<?>> criteria = new LinkedHashMap<>();
    @Nullable
    private String group;
    private ICondition[] conditions;

    public ModShapedRecipeBuilder(RecipeCategory category, ItemLike itemLike, ResourceLocation itemLocation, int count, int tier) {
        this.category = category;
        this.result = itemLike;
        this.result2 = itemLocation;
        this.count = count;
        this.tier = tier;
    }

    @Contract("_, _, _ -> new")
    public static @NotNull ModShapedRecipeBuilder shaped(RecipeCategory category, ResourceLocation itemLocation, int tier) {
        return shaped(category, null, itemLocation, 1, tier);
    }

    @Contract("_, _ -> new")
    public static @NotNull ModShapedRecipeBuilder shaped(RecipeCategory category, ResourceLocation itemLocation) {
        return shaped(category, null, itemLocation, 1, 4);
    }


    @Contract("_, _, _ -> new")
    public static @NotNull ModShapedRecipeBuilder shaped(RecipeCategory category, ItemLike itemLike, int tier) {
        return shaped(category, itemLike, null, 1, tier);
    }

    @Contract("_, _ -> new")
    public static @NotNull ModShapedRecipeBuilder shaped(RecipeCategory category, ItemLike itemLike) {
        return shaped(category, itemLike, null, 1, 4);
    }

    @Contract("_, _, _, _, _ -> new")
    public static @NotNull ModShapedRecipeBuilder shaped(RecipeCategory category, ItemLike itemLike, ResourceLocation itemLocation, int count, int tier) {
        return new ModShapedRecipeBuilder(category, itemLike, itemLocation, count, tier);
    }

    @Contract("_,_,_,_ -> new")
    public static @NotNull ModShapedRecipeBuilder shaped(RecipeCategory category, ItemLike itemLike, int count, int tier) {
        return shaped(category, itemLike, null, count, tier);
    }


    public ModShapedRecipeBuilder define(Character character, TagKey<Item> tagKey) {
        return this.define(character, Ingredient.of(tagKey));
    }

    public ModShapedRecipeBuilder define(Character character, ItemLike itemLike) {
        return this.define(character, Ingredient.of(itemLike));
    }

    public ModShapedRecipeBuilder define(Character character, Mods mod, String stack) {
        return this.define(character, new SimpleDatagenIngredient(mod,  stack).toVanilla());
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

        ItemStack resultStack = ItemStack.EMPTY;
        if (this.result != null)
            resultStack = new ItemStack(this.result, this.count);
        if (this.result2 != null)
            resultStack = new ItemStack(BuiltInRegistries.ITEM.get(this.result2), this.count);

        ShapedTableCraftingRecipe shapedrecipe = new ShapedTableCraftingRecipe(
                shapedrecipepattern,
                resultStack,
                this.tier,
                false
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
