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
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.function.Consumer;

/**
 * Name: Avaritia-forge / ModShapelessRecipeBuilder
 * Author: cnlimiter
 * CreateTime: 2023/8/24 14:07
 * Description:
 */

public class ModCatalystRecipeBuilder extends CraftingRecipeBuilder implements RecipeBuilder {
    private final RecipeCategory category;
    private final List<Ingredient> ingredients = Lists.newArrayList();
    private final Advancement.Builder advancement = Advancement.Builder.advancement();
    @Nullable
    private String group;

    public ModCatalystRecipeBuilder(RecipeCategory p_250837_) {
        this.category = p_250837_;
    }

    @Contract("_ -> new")
    public static @NotNull ModCatalystRecipeBuilder shapeless(RecipeCategory p_250714_) {
        return new ModCatalystRecipeBuilder(p_250714_);
    }


    public ModCatalystRecipeBuilder requires(TagKey<Item> p_206420_) {
        return this.requires(Ingredient.of(p_206420_));
    }

    public ModCatalystRecipeBuilder requires(ItemLike p_126210_) {
        return this.requires(p_126210_, 1);
    }

    public ModCatalystRecipeBuilder requires(ItemLike p_126212_, int p_126213_) {
        for(int i = 0; i < p_126213_; ++i) {
            this.requires(Ingredient.of(p_126212_));
        }

        return this;
    }

    public ModCatalystRecipeBuilder requires(Ingredient p_126185_) {
        return this.requires(p_126185_, 1);
    }

    public ModCatalystRecipeBuilder requires(Ingredient p_126187_, int p_126188_) {
        for(int i = 0; i < p_126188_; ++i) {
            this.ingredients.add(p_126187_);
        }

        return this;
    }

    @Override
    public @NotNull ModCatalystRecipeBuilder unlockedBy(@NotNull String p_126197_, @NotNull CriterionTriggerInstance p_126198_) {
        this.advancement.addCriterion(p_126197_, p_126198_);
        return this;
    }

    @Override
    public @NotNull ModCatalystRecipeBuilder group(@Nullable String p_126195_) {
        this.group = p_126195_;
        return this;
    }

    @Override
    public @NotNull Item getResult() {
        return ModItems.infinity_catalyst.get();
    }

    @Override
    public void save(@NotNull Consumer<FinishedRecipe> p_126205_, @NotNull ResourceLocation p_126206_) {
        this.ensureValid(p_126206_);
        this.advancement.parent(ROOT_RECIPE_ADVANCEMENT).addCriterion("has_the_recipe", RecipeUnlockedTrigger.unlocked(p_126206_)).rewards(AdvancementRewards.Builder.recipe(p_126206_)).requirements(RequirementsStrategy.OR);
        p_126205_.accept(new Result(p_126206_, this.group == null ? "" : this.group, determineBookCategory(this.category), this.ingredients, this.advancement, p_126206_.withPrefix("recipes/" + this.category.getFolderName() + "/")));
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
        private final Advancement.Builder advancement;
        private final ResourceLocation advancementId;

        public Result(ResourceLocation p_249007_, String p_248592_, CraftingBookCategory p_249485_, List<Ingredient> p_252312_, Advancement.Builder p_249909_, ResourceLocation p_249109_) {
            super(p_249485_);
            this.id = p_249007_;
            this.group = p_248592_;
            this.ingredients = p_252312_;
            this.advancement = p_249909_;
            this.advancementId = p_249109_;
        }

        public void serializeRecipeData(@NotNull JsonObject p_126230_) {
            super.serializeRecipeData(p_126230_);
            if (!this.group.isEmpty()) {
                p_126230_.addProperty("group", this.group);
            }

            JsonArray jsonarray = new JsonArray();

            for(Ingredient ingredient : this.ingredients) {
                jsonarray.add(ingredient.toJson());
            }

            p_126230_.add("ingredients", jsonarray);
        }

        @Override
        public @NotNull RecipeSerializer<?> getType() {
            return ModRecipeSerializers.INFINITY_SERIALIZER.get();
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
