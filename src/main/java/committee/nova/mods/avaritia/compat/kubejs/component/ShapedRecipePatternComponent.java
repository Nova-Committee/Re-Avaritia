package committee.nova.mods.avaritia.compat.kubejs.component;

import committee.nova.mods.avaritia.Const;
import committee.nova.mods.avaritia.api.common.crafting.ShapedRecipePatternCodecs;
import com.mojang.serialization.Codec;
import dev.latvian.mods.kubejs.recipe.component.RecipeComponent;
import dev.latvian.mods.kubejs.recipe.component.RecipeComponentType;
import dev.latvian.mods.rhino.type.TypeInfo;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.crafting.ShapedRecipePattern;
import org.jetbrains.annotations.NotNull;

public record ShapedRecipePatternComponent() implements RecipeComponent<ShapedRecipePattern> {
    public static final ResourceKey<RecipeComponentType<?>> TYPE =
            RecipeComponentType.key(Const.rl("shaped_recipe_pattern"));

    @Override
    public ResourceKey<RecipeComponentType<?>> type() {
        return TYPE;
    }

    @Override
    public Codec<ShapedRecipePattern> codec() {
        return ShapedRecipePatternCodecs.MAP_CODEC.codec();
    }

    @Override
    public TypeInfo typeInfo() {
        return TypeInfo.of(ShapedRecipePattern.class);
    }

    @Override
    public @NotNull String toString() {
        return "shaped_recipe_pattern";
    }
}
