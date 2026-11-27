package committee.nova.mods.avaritia.init.data.provider.recipe;

import com.google.common.collect.Lists;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
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
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.ItemLike;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;

public class NoConsumeCatalystShapedRecipeBuilder implements RecipeBuilder {
    private final RecipeCategory category;
    private final ItemStack result;
    private final List<String> pattern = Lists.newArrayList();
    private final Map<Character, Ingredient> key = new HashMap<>();
    private final Advancement.Builder advancement = Advancement.Builder.advancement();
    @Nullable
    private String group;
    private int tier = 4;


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
        if (!this.pattern.isEmpty() && row.length() != this.pattern.get(0).length()) {
            throw new IllegalArgumentException("配方每行长度必须相同！");
        }
        this.pattern.add(row);
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

    public NoConsumeCatalystShapedRecipeBuilder tier(int tier) {
        this.tier = tier;
        return this;
    }

    @Override
    @NotNull
    public NoConsumeCatalystShapedRecipeBuilder unlockedBy(@NotNull String name, @NotNull CriterionTriggerInstance trigger) {
        this.advancement.addCriterion(name, trigger);
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
    public void save(@NotNull Consumer<FinishedRecipe> consumer, @NotNull ResourceLocation id) {
        this.ensureValid(id);
        this.advancement.parent(ROOT_RECIPE_ADVANCEMENT)
                .addCriterion("has_the_recipe", RecipeUnlockedTrigger.unlocked(id))
                .rewards(AdvancementRewards.Builder.recipe(id))
                .requirements(RequirementsStrategy.OR);

        // 调用手动实现的分类转换方法
        CraftingBookCategory bookCategory = getCraftingBookCategory(this.category);
        consumer.accept(new Result(
                id, this.group, bookCategory,
                this.pattern.toArray(new String[0]), this.key,
                this.result, this.tier, this.advancement,
                id.withPrefix("recipes/" + this.category.getFolderName() + "/")
        ));
    }

    private void ensureValid(ResourceLocation id) {
        if (this.pattern.isEmpty()) {
            throw new IllegalStateException("配方 " + id + " 未设置形状！");
        }
        if (this.advancement.getCriteria().isEmpty()) {
            throw new IllegalStateException("No unlock criteria for recipe " + id);
        }

        // 验证配方符号（避免未定义的符号）
        for (String row : pattern) {
            for (char c : row.toCharArray()) {
                if (c != ' ' && !key.containsKey(c)) {
                    throw new IllegalStateException("配方 " + id + " 包含未定义的符号: " + c);
                }
            }
        }
    }

    /**
     * 手动复现 CraftingRecipeBuilder.determineBookCategory 的转换逻辑（解决 protected 访问问题）
     * Minecraft 1.20.1 内置规则：根据 RecipeCategory 映射到 CraftingBookCategory
     */
    private static CraftingBookCategory getCraftingBookCategory(RecipeCategory category) {
        return switch (category) {
            case BUILDING_BLOCKS -> CraftingBookCategory.BUILDING;
            case TOOLS, COMBAT, BREWING -> CraftingBookCategory.EQUIPMENT;
            case REDSTONE -> CraftingBookCategory.REDSTONE;
            default -> CraftingBookCategory.MISC; // 其他分类默认归为 MISC
        };
    }

    public static class Result implements FinishedRecipe {
        private final ResourceLocation id;
        @Nullable
        private final String group;
        private final CraftingBookCategory bookCategory;
        private final String[] pattern;
        private final Map<Character, Ingredient> key;
        private final ItemStack result;
        private final int tier;
        private final Advancement.Builder advancement;
        private final ResourceLocation advancementId;

        // 构造函数接收转换后的 CraftingBookCategory（避免在序列化时转换）
        public Result(ResourceLocation id, @Nullable String group, CraftingBookCategory bookCategory,
                      String[] pattern, Map<Character, Ingredient> key, ItemStack result, int tier,
                      Advancement.Builder advancement, ResourceLocation advancementId) {
            this.id = id;
            this.group = group;
            this.bookCategory = bookCategory;
            this.pattern = pattern;
            this.key = key;
            this.result = result;
            this.tier = tier;
            this.advancement = advancement;
            this.advancementId = advancementId;
        }

        @Override
        public void serializeRecipeData(@NotNull JsonObject json) {
            // 1. 输出分类（与 Minecraft 内置格式一致）
            json.addProperty("category", bookCategory.getSerializedName());

            // 2. 输出分组（可选，不设置则不显示）
            if (this.group != null && !this.group.isEmpty()) {
                json.addProperty("group", this.group);
            }

            // 3. 输出 tier（自定义字段，与你的配方需求匹配）
            json.addProperty("tier", this.tier);

            // 4. 输出配方形状（与你示例中的 pattern 格式完全一致）
            JsonArray patternArray = new JsonArray();
            for (String row : this.pattern) {
                patternArray.add(row);
            }
            json.add("pattern", patternArray);

            // 5. 输出材料映射（与你示例中的 key 格式完全一致）
            JsonObject keyObject = new JsonObject();
            for (Map.Entry<Character, Ingredient> entry : this.key.entrySet()) {
                keyObject.add(String.valueOf(entry.getKey()), entry.getValue().toJson());
            }
            json.add("key", keyObject);

            // 6. 输出结果（支持 count 字段，与你示例中的 result 格式一致）
            JsonObject resultObject = new JsonObject();
            ResourceLocation itemId = ForgeRegistries.ITEMS.getKey(this.result.getItem());
            resultObject.addProperty("item", Objects.requireNonNull(itemId).toString());
            if (this.result.getCount() > 1) {
                resultObject.addProperty("count", this.result.getCount());
            }
            // 支持 NBT（可选，不设置则不显示）
            if (this.result.hasTag()) {
                resultObject.addProperty("nbt", this.result.getTag().toString());
            }
            json.add("result", resultObject);

            // 7. 输出配方类型（与你示例中的 "type" 完全一致）
            json.addProperty("type", "avaritia:no_consume_catalyst_shaped");
        }

        @Override
        @NotNull
        public ResourceLocation getId() {
            return this.id;
        }

        @Override
        @NotNull
        public RecipeSerializer<?> getType() {
            return ModRecipeSerializers.NO_CONSUME_CATALYST_SHAPED_SERIALIZER.get();
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