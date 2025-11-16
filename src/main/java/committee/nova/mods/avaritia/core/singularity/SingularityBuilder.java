package committee.nova.mods.avaritia.core.singularity;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.crafting.Ingredient;
import net.neoforged.fml.loading.FMLLoader;

import java.util.Objects;

/**
 * @author cnlimiter
 * @apiNote  奇点构建器 - 使用构建器模式简化奇点创建
 */
public class SingularityBuilder {
    private final String modId;
    private final String name;
    private int[] colors = new int[]{0xFFFFFF, 0xCCCCCC};
    private Ingredient ingredient = Ingredient.EMPTY;
    private String tag = null;
    private int ingredientCount = -1;
    private int timeRequired = FMLLoader.isProduction() ? 240 : 10;
    private boolean enabled = true;
    private boolean recipeDisabled = false;

    public SingularityBuilder(String modId, String name) {
        this.modId = Objects.requireNonNull(modId, "模组ID不能为空喵～");
        this.name = Objects.requireNonNull(name, "奇点名称不能为空喵～");
    }

    /**
     * 设置奇点颜色
     *
     * @param primaryColor   主颜色
     * @param secondaryColor 次颜色
     * @return 构建器实例
     */
    public SingularityBuilder colors(int primaryColor, int secondaryColor) {
        this.colors = new int[]{primaryColor, secondaryColor};
        return this;
    }

    /**
     * 设置奇点颜色（单一颜色）
     *
     * @param color 颜色
     * @return 构建器实例
     */
    public SingularityBuilder color(int color) {
        this.colors = new int[]{color, color};
        return this;
    }

    /**
     * 设置基于物品的材料
     *
     * @param ingredient 材料物品
     * @return 构建器实例
     */
    public SingularityBuilder ingredient(Ingredient ingredient) {
        this.ingredient = Objects.requireNonNull(ingredient, "材料不能为空喵～");
        this.tag = null;
        return this;
    }

    /**
     * 设置基于物品的材料
     *
     * @param item 材料物品
     * @return 构建器实例
     */
    public SingularityBuilder ingredient(Item item) {
        return ingredient(Ingredient.of(Objects.requireNonNull(item, "物品不能为空喵～")));
    }

    /**
     * 设置基于标签的材料
     *
     * @param tag 材料标签
     * @return 构建器实例
     */
    public SingularityBuilder tag(String tag) {
        this.tag = Objects.requireNonNull(tag, "标签不能为空喵～");
        this.ingredient = Ingredient.EMPTY;
        return this;
    }

    /**
     * 设置所需材料数量
     *
     * @param count 材料数量，-1表示使用默认值
     * @return 构建器实例
     */
    public SingularityBuilder ingredientCount(int count) {
        this.ingredientCount = count;
        return this;
    }

    /**
     * 设置所需处理时间
     *
     * @param time 时间（tick）
     * @return 构建器实例
     */
    public SingularityBuilder timeRequired(int time) {
        this.timeRequired = Math.max(0, time);
        return this;
    }

    /**
     * 设置是否启用
     *
     * @param enabled 是否启用
     * @return 构建器实例
     */
    public SingularityBuilder enabled(boolean enabled) {
        this.enabled = enabled;
        return this;
    }

    /**
     * 设置是否禁用配方
     *
     * @param recipeDisabled 是否禁用配方
     * @return 构建器实例
     */
    public SingularityBuilder recipeDisabled(boolean recipeDisabled) {
        this.recipeDisabled = recipeDisabled;
        return this;
    }

    /**
     * 构建奇点实例
     *
     * @return 构建的奇点
     */
    public Singularity build() {
        ResourceLocation id = ResourceLocation.fromNamespaceAndPath(modId, name);
        String translationKey = String.format("singularity.%s.%s", modId, name);

        Singularity singularity;
        if (tag != null) {
            singularity = new Singularity(id, translationKey, colors, tag, ingredientCount, timeRequired);
        } else {
            singularity = new Singularity(id, translationKey, colors, ingredient, ingredientCount, timeRequired);
        }

        singularity.setEnabled(enabled);
        singularity.setRecipeEnabled(!recipeDisabled);

        return singularity;
    }
}
