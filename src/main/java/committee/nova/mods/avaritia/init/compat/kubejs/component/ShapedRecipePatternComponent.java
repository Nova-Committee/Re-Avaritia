package committee.nova.mods.avaritia.init.compat.kubejs.component;

import com.mojang.serialization.Codec;
import committee.nova.mods.avaritia.Static;
import dev.latvian.mods.kubejs.recipe.component.RecipeComponent;
import dev.latvian.mods.kubejs.recipe.component.RecipeComponentType;
import dev.latvian.mods.rhino.type.TypeInfo;
import net.minecraft.world.item.crafting.ShapedRecipePattern;

/**
 * @Project: Avaritia
 * @Author: cnlimiter
 * @CreateTime: 2025/5/18 22:33
 * @Description:
 */
public record ShapedRecipePatternComponent() implements RecipeComponent<ShapedRecipePattern> {
    public static final RecipeComponentType<ShapedRecipePattern> SHAPE_RECIPE = RecipeComponentType.unit(Static.rl("shaped_recipe_pattern"), new ShapedRecipePatternComponent());

    @Override
    public RecipeComponentType<?> type() {
        return SHAPE_RECIPE;
    }

    @Override
    public Codec<ShapedRecipePattern> codec() {
        return ShapedRecipePattern.MAP_CODEC.codec();
    }

    @Override
    public TypeInfo typeInfo() {
        return TypeInfo.of(ShapedRecipePattern.class);
    }

    @Override
    public String toString() {
        return "shaped_recipe_pattern";
    }
}
