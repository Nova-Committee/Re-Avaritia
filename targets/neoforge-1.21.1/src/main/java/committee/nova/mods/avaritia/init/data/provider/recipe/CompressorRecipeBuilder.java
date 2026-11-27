package committee.nova.mods.avaritia.init.data.provider.recipe;

import committee.nova.mods.avaritia.common.crafting.recipe.CompressorRecipe;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementRequirements;
import net.minecraft.advancements.critereon.RecipeUnlockedTrigger;
import net.minecraft.data.recipes.RecipeBuilder;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.ItemLike;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.LinkedHashMap;
import java.util.Map;


public class CompressorRecipeBuilder implements RecipeBuilder {
    private final ItemLike result;
    private final Ingredient input;
    private final int inputCount;
    private final int timeCost;
    private final Map<String, String> singularityData; // 存储奇点数据
    private final boolean isSingularity;
    private String group;

    public CompressorRecipeBuilder(ItemLike result, Ingredient input, int inputCount, int timeCost, boolean isSingularity) {
        this.result = result;
        this.input = input;
        this.inputCount = inputCount;
        this.timeCost = timeCost;
        this.isSingularity = isSingularity;
        this.singularityData = new LinkedHashMap<>();
    }

    /**
     * 创建一个压缩机配方构建器
     *
     * @param result 输出物品
     * @param input 输入材料
     * @param inputCount 需要的输入数量
     * @param timeCost 所需时间
     * @return 压缩机配方构建器实例
     */
    public static @NotNull CompressorRecipeBuilder compressing(ItemLike result, Ingredient input, int inputCount, int timeCost) {
        return new CompressorRecipeBuilder(result, input, inputCount, timeCost, false);
    }

    /**
     * 创建一个压缩机配方构建器，用于制作奇点
     *
     * @param result 输出物品
     * @param input 输入材料
     * @param inputCount 需要的输入数量
     * @param timeCost 所需时间
     * @return 压缩机配方构建器实例
     */
    public static @NotNull CompressorRecipeBuilder singularityCompressing(ItemLike result, Ingredient input, int inputCount, int timeCost) {
        return new CompressorRecipeBuilder(result, input, inputCount, timeCost, true);
    }

    /**
     * 设置奇点数据
     *
     * @param name 奇点名称
     * @param displayName 奇点显示名称
     * @param overlayColor 外层颜色
     * @param underlayColor 内层颜色
     * @return 当前构建器实例
     */
    public CompressorRecipeBuilder setSingularityData(String name, String displayName, int overlayColor, int underlayColor) {
        this.singularityData.put("name", name);
        this.singularityData.put("displayName", displayName);
        this.singularityData.put("overlayColor", String.valueOf(overlayColor));
        this.singularityData.put("underlayColor", String.valueOf(underlayColor));
        return this;
    }

    @Override
    public @NotNull RecipeBuilder unlockedBy(@NotNull String name, @NotNull net.minecraft.advancements.Criterion<?> criterion) {
        // 在实际实现中，这会添加解锁条件
        return this;
    }

    @Override
    public @NotNull RecipeBuilder group(@Nullable String groupName) {
        this.group = groupName;
        return this;
    }

    @Override
    public @NotNull Item getResult() {
        return this.result.asItem();
    }

    @Override
    public void save(RecipeOutput recipeOutput, @NotNull ResourceLocation id) {
        // 创建压缩机配方
        ItemStack resultStack = new ItemStack(this.result, 1); // 默认数量为1
        CompressorRecipe compressorRecipe = new CompressorRecipe(
                this.input,
                resultStack,
                this.inputCount,
                this.timeCost
        );

        // 构建进度
        Advancement.Builder advancementBuilder = recipeOutput.advancement()
                .addCriterion("has_the_recipe", RecipeUnlockedTrigger.unlocked(id))
                .requirements(AdvancementRequirements.Strategy.OR);

        var advancement = advancementBuilder.build(id.withPrefix("recipes/compression/"));

        // 添加配方到输出
        recipeOutput.accept(id, compressorRecipe, advancement);
    }

    /**
     * 检查是否为奇点配方
     *
     * @return 如果是奇点配方返回true，否则返回false
     */
    public boolean isSingularity() {
        return isSingularity;
    }

    /**
     * 获取奇点数据
     *
     * @return 奇点数据映射
     */
    public Map<String, String> getSingularityData() {
        return this.singularityData;
    }
}
