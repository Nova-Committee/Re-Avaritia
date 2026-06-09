package committee.nova.mods.avaritia.compat.kubejs.schema;

import com.mojang.serialization.Codec;
import dev.latvian.mods.kubejs.recipe.RecipeKey;
import dev.latvian.mods.kubejs.recipe.RecipeScriptContext;
import dev.latvian.mods.kubejs.recipe.component.ComponentRole;
import dev.latvian.mods.kubejs.recipe.component.NumberComponent;
import dev.latvian.mods.kubejs.recipe.component.RecipeComponent;
import dev.latvian.mods.kubejs.recipe.component.RecipeComponentType;
import dev.latvian.mods.kubejs.recipe.component.StringComponent;
import dev.latvian.mods.kubejs.recipe.filter.RecipeMatchContext;
import dev.latvian.mods.kubejs.recipe.schema.RecipeSchema;
import dev.latvian.mods.rhino.type.TypeInfo;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.crafting.Ingredient;

import java.util.List;

import static committee.nova.mods.avaritia.compat.kubejs.KjsUtils.COMPAT_INGREDIENT;
import static committee.nova.mods.avaritia.compat.kubejs.KjsUtils.optionalList;

public interface EternalSingularityRecipeSchema {
    RecipeKey<String> GROUP = StringComponent.STRING
            .key("group", ComponentRole.INPUT)
            .optional("default");
    RecipeKey<List<Ingredient>> INGREDIENTS =
            optionalList(COMPAT_INGREDIENT, "ingredients", ComponentRole.INPUT);
    RecipeComponent<Integer> LEGACY_BOOL_COUNT = new RecipeComponent<>() {
        @Override
        public ResourceKey<RecipeComponentType<?>> type() {
            return NumberComponent.INT.type();
        }

        @Override
        public Codec<Integer> codec() {
            return NumberComponent.INT.codec();
        }

        @Override
        public TypeInfo typeInfo() {
            return NumberComponent.INT.typeInfo();
        }

        @Override
        public boolean hasPriority(RecipeMatchContext context, Object value) {
            return value instanceof Boolean || NumberComponent.INT.hasPriority(context, value);
        }

        @Override
        public Integer wrap(RecipeScriptContext context, Object value) {
            // 兼容旧示例脚本中的 eternal_singularity([...], true)，布尔值只表示自定义配方，数量仍使用默认 1。
            if (value instanceof Boolean) {
                return 1;
            }
            return NumberComponent.INT.wrap(context, value);
        }
    };
    RecipeKey<Integer> COUNT = LEGACY_BOOL_COUNT.inputKey("count").optional(1);

    RecipeSchema SCHEMA = new RecipeSchema(GROUP, INGREDIENTS, COUNT)
            .constructor(GROUP, INGREDIENTS, COUNT)
            .constructor(INGREDIENTS, COUNT);
}
