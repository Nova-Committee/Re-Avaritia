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

public class ModMatterClusterRecipeBuilder extends CraftingRecipeBuilder implements RecipeBuilder {
    private final RecipeCategory category; // 保留RecipeCategory用于获取路径
    private final Item result;
    private final int count;
    private final List<Ingredient> ingredients = Lists.newArrayList();
    private final Advancement.Builder advancement = Advancement.Builder.advancement();
    @Nullable
    private String group;

    public ModMatterClusterRecipeBuilder(RecipeCategory category, ItemLike result, int count) {
        this.category = category;
        this.result = result.asItem();
        this.count = count;
    }

    @Contract("_, _, _ -> new")
    public static @NotNull ModMatterClusterRecipeBuilder shapeless(RecipeCategory category, ItemLike result, int count) {
        return new ModMatterClusterRecipeBuilder(category, result, count);
    }

    // 添加原料的系列方法（与ModCatalystRecipeBuilder一致）
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

    @Override
    public @NotNull ModMatterClusterRecipeBuilder unlockedBy(@NotNull String criterionName, @NotNull CriterionTriggerInstance criterion) {
        this.advancement.addCriterion(criterionName, criterion);
        return this;
    }

    @Override
    public @NotNull ModMatterClusterRecipeBuilder group(@Nullable String group) {
        this.group = group;
        return this;
    }

    @Override
    public @NotNull Item getResult() {
        return this.result; // 对应输出物品
    }

    @Override
    public void save(@NotNull Consumer<FinishedRecipe> consumer, @NotNull ResourceLocation id) {
        this.ensureValid(id);
        this.advancement.parent(ROOT_RECIPE_ADVANCEMENT)
                .addCriterion("has_the_recipe", RecipeUnlockedTrigger.unlocked(id))
                .rewards(AdvancementRewards.Builder.recipe(id))
                .requirements(RequirementsStrategy.OR);
        // 使用RecipeCategory的getFolderName()构建路径，避免依赖CraftingBookCategory
        consumer.accept(new Result(
                id,
                this.group == null ? "" : this.group,
                determineBookCategory(this.category), // 转换为CraftingBookCategory（父类逻辑）
                this.ingredients,
                this.result,
                this.count,
                this.advancement,
                id.withPrefix("recipes/" + this.category.getFolderName() + "/") // 关键：用RecipeCategory获取路径
        ));
    }

    private void ensureValid(ResourceLocation id) {
        if (this.advancement.getCriteria().isEmpty()) {
            throw new IllegalStateException("No way of obtaining recipe " + id);
        }
    }

    public static class Result extends CraftingRecipeBuilder.CraftingResult {
        private final ResourceLocation id;
        private final String group;
        private final List<Ingredient> ingredients;
        private final Item result;
        private final int count;
        private final Advancement.Builder advancement;
        private final ResourceLocation advancementId;

        public Result(ResourceLocation id, String group, CraftingBookCategory bookCategory,
                      List<Ingredient> ingredients, Item result, int count,
                      Advancement.Builder advancement, ResourceLocation advancementId) {
            super(bookCategory);
            this.id = id;
            this.group = group;
            this.ingredients = ingredients;
            this.result = result;
            this.count = count;
            this.advancement = advancement;
            this.advancementId = advancementId;
        }

        @Override
        public void serializeRecipeData(@NotNull JsonObject json) {
            super.serializeRecipeData(json);
            // 配方类型
            json.addProperty("type", "avaritia:full_matter_cluster");
            // 分组（参考ModCatalystRecipeBuilder）
            if (!this.group.isEmpty()) {
                json.addProperty("group", this.group);
            }
            // 原料列表（与ModCatalystRecipeBuilder一致）
            JsonArray ingredients = new JsonArray();
            for (Ingredient ingredient : this.ingredients) {
                ingredients.add(ingredient.toJson());
            }
            json.add("ingredients", ingredients);
            // 数量和结果（根据需求添加）
            json.addProperty("count", this.count);
            JsonObject resultObj = new JsonObject();
            resultObj.addProperty("item", new ResourceLocation(
                    "avaritia", "full_matter_cluster"
            ).toString());
            json.add("result", resultObj);
            // 不使用CraftingBookCategory.getFolderName()，如需category字段则用RecipeCategory的路径
            // 例如：json.addProperty("category", this.advancementId.getPath()); // 或其他方式
        }

        @Override
        public @NotNull RecipeSerializer<?> getType() {
            return ModRecipeSerializers.FULL_MATTER_CLUSTER_SERIALIZER.get();
        }

        @Override
        public @NotNull ResourceLocation getId() {
            return this.id;
        }

        @Nullable
        @Override
        public JsonObject serializeAdvancement() {
            return this.advancement.serializeToJson();
        }

        @Nullable
        @Override
        public ResourceLocation getAdvancementId() {
            return this.advancementId;
        }
    }
}