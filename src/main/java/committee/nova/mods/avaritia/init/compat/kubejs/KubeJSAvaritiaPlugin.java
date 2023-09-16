package committee.nova.mods.avaritia.init.compat.kubejs;

import committee.nova.mods.avaritia.init.registry.ModRecipeSerializers;
import dev.latvian.mods.kubejs.KubeJSPlugin;
import dev.latvian.mods.kubejs.recipe.schema.RegisterRecipeSchemasEvent;
import dev.latvian.mods.kubejs.recipe.schema.minecraft.ShapedRecipeSchema;
import dev.latvian.mods.kubejs.recipe.schema.minecraft.ShapelessRecipeSchema;

/**
 * Name: Avaritia-forge / KubeJSAvaritiaPlugin
 * Author: cnlimiter
 * CreateTime: 2023/9/17 0:49
 * Description:
 */

public class KubeJSAvaritiaPlugin extends KubeJSPlugin {

    @Override
    public void registerRecipeSchemas(RegisterRecipeSchemasEvent event) {
        event.register(ModRecipeSerializers.SHAPED_EXTREME_CRAFT_SERIALIZER.getId(), ShapedRecipeSchema.SCHEMA);
        event.register(ModRecipeSerializers.SHAPELESS_EXTREME_CRAFT_SERIALIZER.getId(), ShapelessRecipeSchema.SCHEMA);
        event.register(ModRecipeSerializers.COMPRESSOR_SERIALIZER.getId(), CompressRecipeSchema.SCHEMA);
    }
}
