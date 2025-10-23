package committee.nova.mods.avaritia.init.compat.kubejs;

import committee.nova.mods.avaritia.Const;
import committee.nova.mods.avaritia.core.singularity.Singularity;
import committee.nova.mods.avaritia.init.handler.SingularityDataHandler;
import dev.latvian.mods.kubejs.item.InputItem;
import dev.latvian.mods.kubejs.recipe.RecipeJS;
import dev.latvian.mods.kubejs.recipe.RecipeKey;
import dev.latvian.mods.kubejs.recipe.ReplacementMatch;
import dev.latvian.mods.kubejs.recipe.component.BooleanComponent;
import dev.latvian.mods.kubejs.recipe.component.ItemComponents;
import dev.latvian.mods.kubejs.recipe.component.NumberComponent;
import dev.latvian.mods.kubejs.recipe.component.StringComponent;
import dev.latvian.mods.kubejs.recipe.schema.RecipeSchema;

import java.util.Arrays;

/**
 * KubeJS奇点模式 - 用于脚本化创建奇点
 *
 * 支持通过KubeJS脚本动态注册奇点，完美集成新的数据包系统
 *
 * @author cnlimiter
 * @version 1.0
 */
public interface SingularitySchema {

    // 奇点配置字段
    RecipeKey<String> ID = StringComponent.NON_BLANK.key("id").alt("奇点id").optional("NULL");
    RecipeKey<String> NAME = StringComponent.NON_BLANK.key("name").alt("奇点显示名称").optional("Custom Singularity");
    RecipeKey<String[]> COLORS = StringComponent.NON_BLANK.asArray().key("colors").alt("奇点颜色 [覆盖色, 底层色]").optional(new String[]{"FFFFFF", "000000"});
    RecipeKey<InputItem> INGREDIENT = ItemComponents.INPUT.key("ingredient").alt("奇点材料").allowEmpty().optional(InputItem.EMPTY);
    RecipeKey<String> TAG = StringComponent.ANY.key("tag").alt("材料标签").allowEmpty().optional("");
    RecipeKey<Integer> MATERIAL_COUNT = NumberComponent.INT.key("materialCount").alt("所需材料数量").optional(1000);
    RecipeKey<Integer> TIME_REQUIRED = NumberComponent.INT.key("timeRequired").alt("压缩时间(刻)").optional(200);
    RecipeKey<Boolean> ENABLED = BooleanComponent.BOOLEAN.key("enabled").alt("是否启用").optional(true);
    RecipeKey<Boolean> RECIPE_DISABLED = BooleanComponent.BOOLEAN.key("recipeDisabled").alt("是否禁用配方").optional(false);

    // 奇点模式定义
    RecipeSchema SCHEMA = new RecipeSchema(SingularityJS.class, SingularityJS::new,
            ID, NAME, COLORS, INGREDIENT, TAG, MATERIAL_COUNT, TIME_REQUIRED, ENABLED, RECIPE_DISABLED);

    /**
     * 奇点创建实现
     */
    class SingularityJS extends RecipeJS {

        private Singularity singularity;

        public SingularityJS() {
            // 构造函数
        }


        @Override
        public boolean hasInput(ReplacementMatch match) {
            return getValue(INGREDIENT) != null || getValue(TAG) != null;
        }

        @Override
        public void afterLoaded() {
            super.afterLoaded();

            // 创建奇点实例
            String id = getValue(ID);
            String name = getValue(NAME);
            String[] colors = getValue(COLORS);
            InputItem ingredient = getValue(INGREDIENT);
            String tag = getValue(TAG);
            int materialCount = getValue(MATERIAL_COUNT);
            int timeRequired = getValue(TIME_REQUIRED);
            boolean enabled = getValue(ENABLED);
            boolean recipeDisabled = getValue(RECIPE_DISABLED);

            // 解析颜色
            int[] colorInts = new int[2];
            try {
                if (colors.length >= 2) {
                    colorInts[0] = Integer.parseInt(colors[0], 16); // 覆盖色
                    colorInts[1] = Integer.parseInt(colors[1], 16); // 底层色
                } else if (colors.length == 1) {
                    colorInts[0] = Integer.parseInt(colors[0], 16);
                    colorInts[1] = 0x000000; // 默认黑色
                } else {
                    colorInts[0] = 0xFFFFFF; // 默认白色
                    colorInts[1] = 0x000000; // 默认黑色
                }
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("Invalid color format: " + Arrays.toString(colors) +
                    ". Colors should be 6-digit hex strings (e.g., 'FF0000')");
            }

            // 创建奇点
            if (tag != null && !tag.isEmpty()) {
                // 使用标签
                this.singularity = new Singularity(
                        Const.rl(id),
                    name,
                    colorInts,
                    tag,
                    materialCount,
                    timeRequired
                );
            } else if (ingredient != null && !ingredient.isEmpty()) {
                // 使用物品
                this.singularity = new Singularity(
                        Const.rl(id),
                    name,
                    colorInts,
                    ingredient.kjs$asIngredient(),
                    materialCount,
                    timeRequired
                );
            } else {
                throw new IllegalArgumentException("Singularity must have either 'ingredient' or 'tag' specified");
            }

            // 设置状态
            this.singularity.setEnabled(enabled);
            this.singularity.setRecipeDisabled(recipeDisabled);
            // 注册奇点到数据管理器
            SingularityDataHandler manager = SingularityDataHandler.getInstance();
            if (manager.isInitialized()) {
                // 使用数据管理器的运行时注册方法
                manager.registerRuntimeSingularity(this.singularity);
            }
        }

        /**
         * 获取创建的奇点
         */
        public Singularity getSingularity() {
            return this.singularity;
        }
    }
}