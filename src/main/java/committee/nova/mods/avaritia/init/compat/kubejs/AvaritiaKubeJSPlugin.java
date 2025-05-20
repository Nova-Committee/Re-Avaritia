package committee.nova.mods.avaritia.init.compat.kubejs;

import committee.nova.mods.avaritia.init.compat.kubejs.component.ShapedRecipePatternComponent;
import committee.nova.mods.avaritia.init.compat.kubejs.schema.CompressRecipeSchema;
import committee.nova.mods.avaritia.init.compat.kubejs.schema.ExtremeSmithingRecipeSchema;
import committee.nova.mods.avaritia.init.compat.kubejs.schema.InfinityCatalystRecipeSchema;
import committee.nova.mods.avaritia.init.compat.kubejs.schema.ShapelessTableRecipeSchema;
import committee.nova.mods.avaritia.init.registry.ModRecipeSerializers;
import dev.latvian.mods.kubejs.plugin.ClassFilter;
import dev.latvian.mods.kubejs.plugin.KubeJSPlugin;
import dev.latvian.mods.kubejs.recipe.component.RecipeComponentTypeRegistry;
import dev.latvian.mods.kubejs.recipe.schema.RecipeSchemaRegistry;

/**
 * Name: Avaritia-forge / KubeJSAvaritiaPlugin
 * Author: cnlimiter
 * CreateTime: 2023/9/17 0:49
 * Description:
 */

public class AvaritiaKubeJSPlugin implements KubeJSPlugin {
    @Override
    public void registerClasses(ClassFilter filter) {
        filter.allow("committee.nova.mods.avaritia.common.crafting.recipe");
        filter.allow("committee.nova.mods.avaritia.init.compat.kubejs");
    }
    @Override
    public void registerRecipeComponents(RecipeComponentTypeRegistry registry) {
        registry.register(ShapedRecipePatternComponent.SHAPE_RECIPE);

    }

    @Override
    public void registerRecipeSchemas(RecipeSchemaRegistry event) {
        //event.register(ModRecipeSerializers.SHAPED_CRAFT_SERIALIZER.getId(), ShapedTableRecipeSchema.SCHEMA);
        event.register(ModRecipeSerializers.SHAPELESS_CRAFT_SERIALIZER.getId(), ShapelessTableRecipeSchema.SCHEMA);
        event.register(ModRecipeSerializers.COMPRESSOR_SERIALIZER.getId(), CompressRecipeSchema.SCHEMA);
        event.register(ModRecipeSerializers.INFINITY_CATALYST_CRAFT_SERIALIZER.getId(), InfinityCatalystRecipeSchema.SCHEMA);
        event.register(ModRecipeSerializers.EXTREME_SMITHING_SERIALIZER.getId(), ExtremeSmithingRecipeSchema.SCHEMA);
    }
}
