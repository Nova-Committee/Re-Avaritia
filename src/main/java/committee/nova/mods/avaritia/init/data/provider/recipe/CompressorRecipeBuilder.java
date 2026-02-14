package committee.nova.mods.avaritia.init.data.provider.recipe;

import committee.nova.mods.avaritia.init.registry.ModRecipeSerializers;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementRewards;
import net.minecraft.advancements.CriterionTriggerInstance;
import net.minecraft.advancements.RequirementsStrategy;
import net.minecraft.advancements.critereon.RecipeUnlockedTrigger;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.data.recipes.RecipeBuilder;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.ItemLike;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;
import java.util.function.Consumer;

public class CompressorRecipeBuilder implements RecipeBuilder {
    private final RecipeCategory category;
    private final ItemLike result;
    private final ResourceLocation result2;
    private final int count;
    private final int inputCount;
    private final int timeCost;
    private final boolean isSingularity;
    private Ingredient input;
    private final Advancement.Builder advancement = Advancement.Builder.advancement();
    @Nullable
    private String group;

    public CompressorRecipeBuilder(RecipeCategory category, ItemLike itemLike, ResourceLocation itemLocation, int count, int inputCount, int timeCost, boolean isSingularity) {
        this.category = category;
        this.result = itemLike;
        this.result2 = itemLocation;
        this.count = count;
        this.inputCount = inputCount;
        this.timeCost = timeCost;
        this.isSingularity = isSingularity;
    }

    /**
     * 创建压缩机配方构建器
     */
    public static @NotNull CompressorRecipeBuilder compressing(RecipeCategory category, ItemLike result, int count, int inputCount, int timeCost) {
        return new CompressorRecipeBuilder(category, result, null, count, inputCount, timeCost, false);
    }

    /**
     * 创建压缩机配方构建器（指定为奇点）
     */
    public static @NotNull CompressorRecipeBuilder singularity(RecipeCategory category, ItemLike result, int count, int inputCount, int timeCost) {
        return new CompressorRecipeBuilder(category, result, null, count, inputCount, timeCost, true);
    }

    /**
     * 设置输入材料
     */
    public CompressorRecipeBuilder requires(Ingredient input) {
        this.input = input;
        return this;
    }

    /**
     * 设置输入材料（单个物品）
     */
    public CompressorRecipeBuilder requires(ItemLike item) {
        this.input = Ingredient.of(item);
        return this;
    }

    /**
     * 设置输入材料（带数量）
     */
    public CompressorRecipeBuilder requires(ItemLike item, int count) {
        this.input = Ingredient.of(item);
        return this;
    }

    @Override
    public @NotNull CompressorRecipeBuilder unlockedBy(@NotNull String string, @NotNull CriterionTriggerInstance pCriterionTrigger) {
        this.advancement.addCriterion(string, pCriterionTrigger);
        return this;
    }

    @Override
    public @NotNull CompressorRecipeBuilder group(@Nullable String pGroupName) {
        this.group = pGroupName;
        return this;
    }

    @Override
    public @NotNull Item getResult() {
        if (this.result != null) {
            return this.result.asItem();
        }
        if (this.result2 != null) {
            return Objects.requireNonNull(net.minecraftforge.registries.ForgeRegistries.ITEMS.getValue(this.result2));
        }
        return net.minecraft.world.item.Items.AIR;
    }

    @Override
    public void save(@NotNull Consumer<FinishedRecipe> recipeConsumer, @NotNull ResourceLocation location) {
        this.ensureValid(location);
        this.advancement.parent(ROOT_RECIPE_ADVANCEMENT)
                .addCriterion("has_the_recipe", RecipeUnlockedTrigger.unlocked(location))
                .rewards(AdvancementRewards.Builder.recipe(location))
                .requirements(RequirementsStrategy.OR);

        recipeConsumer.accept(new CompressorRecipeBuilder.Result(
                location,
                this.result,
                this.result2,
                this.count,
                this.input,
                this.inputCount,
                this.timeCost,
                this.isSingularity,
                this.group == null ? "" : this.group,
                this.advancement,
                location.withPrefix("recipes/" + this.category.getFolderName() + "/")
        ));
    }

    private void ensureValid(ResourceLocation resourceLocation) {
        if (this.input == null || this.input.isEmpty()) {
            throw new IllegalStateException("No input is defined for compressor recipe " + resourceLocation + "!");
        } else if (this.advancement.getCriteria().isEmpty()) {
            throw new IllegalStateException("No way of obtaining recipe " + resourceLocation);
        }
    }

    public static class Result implements FinishedRecipe {
        private final ResourceLocation id;
        private final ItemLike result;
        private final ResourceLocation result2;
        private final int count;
        private final Ingredient input;
        private final int inputCount;
        private final int timeCost;
        private final boolean isSingularity;
        private final String group;
        private final Advancement.Builder advancement;
        private final ResourceLocation advancementId;

        public Result(ResourceLocation id, ItemLike result, ResourceLocation result2, int count,
                      Ingredient input, int inputCount, int timeCost, boolean isSingularity,
                      String group, Advancement.Builder advancement, ResourceLocation advancementId) {
            this.id = id;
            this.result = result;
            this.result2 = result2;
            this.count = count;
            this.input = input;
            this.inputCount = inputCount;
            this.timeCost = timeCost;
            this.isSingularity = isSingularity;
            this.group = group;
            this.advancement = advancement;
            this.advancementId = advancementId;
        }

        @Override
        public void serializeRecipeData(@NotNull com.google.gson.JsonObject pJson) {
            // 添加输入材料
            pJson.add("ingredient", this.input.toJson());

            // 添加输出结果
            com.google.gson.JsonObject resultObj = new com.google.gson.JsonObject();
            if (this.result != null) {
                resultObj.addProperty("item", Objects.requireNonNull(
                        net.minecraftforge.registries.ForgeRegistries.ITEMS.getKey(this.result.asItem())).toString());
            }
            if (this.result2 != null) {
                resultObj.addProperty("item", this.result2.toString());
            }
            if (this.count > 1) {
                resultObj.addProperty("count", this.count);
            }
            pJson.add("result", resultObj);

            // 添加输入数量和时间成本
            pJson.addProperty("inputCount", this.inputCount);
            pJson.addProperty("timeCost", this.timeCost);

            // 如果是奇点，添加奇点标识
            if (this.isSingularity) {
                pJson.addProperty("isSingularity", true);
            }

            // 添加配方组
            if (!this.group.isEmpty()) {
                pJson.addProperty("group", this.group);
            }
        }

        @Override
        public @NotNull RecipeSerializer<?> getType() {
            return ModRecipeSerializers.COMPRESSOR_SERIALIZER.get();
        }

        @Override
        public @NotNull ResourceLocation getId() {
            return this.id;
        }

        @Nullable
        public com.google.gson.JsonObject serializeAdvancement() {
            return this.advancement.serializeToJson();
        }

        @Nullable
        public ResourceLocation getAdvancementId() {
            return this.advancementId;
        }
    }
}
