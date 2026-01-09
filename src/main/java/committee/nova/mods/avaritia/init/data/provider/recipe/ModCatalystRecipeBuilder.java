package committee.nova.mods.avaritia.init.data.provider.recipe;

import com.google.common.collect.Lists;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import committee.nova.mods.avaritia.init.registry.ModItems;
import committee.nova.mods.avaritia.init.registry.ModRecipeSerializers;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementRewards;
import net.minecraft.advancements.CriterionTriggerInstance;
import net.minecraft.advancements.RequirementsStrategy;
import net.minecraft.advancements.critereon.RecipeUnlockedTrigger;
import net.minecraft.data.recipes.CraftingRecipeBuilder;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.data.recipes.RecipeBuilder;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.ItemLike;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.List;
import java.util.function.Consumer;

/**
 * Name: Avaritia-forge / ModShapelessRecipeBuilder
 * @author cnlimiter
 * CreateTime: 2023/8/24 14:07
 * Description:
 */

public class ModCatalystRecipeBuilder extends CraftingRecipeBuilder implements RecipeBuilder {
    private final RecipeCategory category;
    private final List<Ingredient> ingredients = Lists.newArrayList();
    private final Advancement.Builder advancement = Advancement.Builder.advancement();
    @Nullable
    private String group;
    private final int count;

    public ModCatalystRecipeBuilder(RecipeCategory p_250837_, int count) {
        this.category = p_250837_;
        this.count = count;
    }

    public static @NotNull ModCatalystRecipeBuilder shapeless(RecipeCategory recipeCategory) {
        return new ModCatalystRecipeBuilder(recipeCategory, 1);
    }

    public static @NotNull ModCatalystRecipeBuilder shapeless(RecipeCategory recipeCategory, int count) {
        return new ModCatalystRecipeBuilder(recipeCategory, count);
    }

    public ModCatalystRecipeBuilder requires(TagKey<Item> p_206420_) {
        return this.requires(Ingredient.of(p_206420_));
    }

    public ModCatalystRecipeBuilder requires(ItemLike p_126210_) {
        return this.requires(p_126210_, 1);
    }

    public ModCatalystRecipeBuilder requires(ItemLike p_126212_, int p_126213_) {
        for (int i = 0; i < p_126213_; ++i) {
            this.requires(Ingredient.of(p_126212_));
        }

        return this;
    }

    public ModCatalystRecipeBuilder requires(Ingredient ingredient) {
        return this.requires(ingredient, 1);
    }

    public ModCatalystRecipeBuilder requires(Ingredient ingredient, int i1) {
        for (int i = 0; i < i1; ++i) {
            this.ingredients.add(ingredient);
        }

        return this;
    }

    @Override
    public @NotNull ModCatalystRecipeBuilder unlockedBy(@NotNull String p_126197_, @NotNull CriterionTriggerInstance p_126198_) {
        this.advancement.addCriterion(p_126197_, p_126198_);
        return this;
    }

    @Override
    public @NotNull ModCatalystRecipeBuilder group(@Nullable String groupName) {
        this.group = groupName;
        return this;
    }

    @Override
    public @NotNull Item getResult() {
        return ModItems.infinity_catalyst.get();
    }

    @Override
    public void save(@NotNull Consumer<FinishedRecipe> recipeConsumer, @NotNull ResourceLocation recipeId) {
        this.ensureValid(recipeId);
        this.advancement.parent(ROOT_RECIPE_ADVANCEMENT).addCriterion("has_the_recipe", RecipeUnlockedTrigger.unlocked(recipeId)).rewards(AdvancementRewards.Builder.recipe(recipeId)).requirements(RequirementsStrategy.OR);
        recipeConsumer.accept(new ModCatalystRecipeBuilder.Result(recipeId, this.group == null ? "default" : this.group, determineBookCategory(this.category), this.ingredients, this.count, this.advancement, recipeId.withPrefix("recipes/" + this.category.getFolderName() + "/")));
    }

    private void ensureValid(ResourceLocation p_126208_) {
        if (this.advancement.getCriteria().isEmpty()) {
            throw new IllegalStateException("No way of obtaining recipe " + p_126208_);
        }
    }

    public static class Result extends CraftingResult {
        private final ResourceLocation id;
        private final String group;
        private final List<Ingredient> ingredients;
        private final int count;
        private final Advancement.Builder advancement;
        private final ResourceLocation advancementId;

        public Result(ResourceLocation resourceLocation, String string, CraftingBookCategory bookCategory, List<Ingredient> ingredients, int count, Advancement.Builder builder, ResourceLocation resourceLocation1) {
            super(bookCategory);
            this.id = resourceLocation;
            this.group = string;
            this.ingredients = ingredients;
            this.count = count;
            this.advancement = builder;
            this.advancementId = resourceLocation1;
        }

        public void serializeRecipeData(@NotNull JsonObject json) {
            super.serializeRecipeData(json);
            if (!this.group.isEmpty()) {
                json.addProperty("group", this.group);
            }

            JsonArray jsonarray = new JsonArray();

            for (Ingredient ingredient : this.ingredients) {
                jsonarray.add(ingredient.toJson());
            }

            json.add("ingredients", jsonarray);
            if (this.count > 1) {
                json.addProperty("count", this.count);
            }
        }

        @Override
        public @NotNull RecipeSerializer<?> getType() {
            return ModRecipeSerializers.INFINITY_CATALYST_CRAFT_SERIALIZER.get();
        }

        @Override
        public @NotNull ResourceLocation getId() {
            return this.id;
        }

        @Nullable
        public JsonObject serializeAdvancement() {
            return this.advancement.serializeToJson();
        }

        @Nullable
        public ResourceLocation getAdvancementId() {
            return this.advancementId;
        }
    }
}
