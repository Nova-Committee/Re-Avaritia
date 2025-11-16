package committee.nova.mods.avaritia.init.compat.kubejs;

import committee.nova.mods.avaritia.core.singularity.Singularity;
import committee.nova.mods.avaritia.init.compat.kubejs.event.AvaritiaEvents;
import committee.nova.mods.avaritia.init.compat.kubejs.event.SingularityRegisterEventJS;
import committee.nova.mods.avaritia.init.registry.ModRecipeSerializers;
import dev.latvian.mods.kubejs.KubeJSPlugin;
import dev.latvian.mods.kubejs.recipe.schema.RegisterRecipeSchemasEvent;
import dev.latvian.mods.kubejs.script.BindingsEvent;
import dev.latvian.mods.kubejs.script.ScriptType;
import dev.latvian.mods.rhino.util.wrap.TypeWrappers;

/**
 * Name: Avaritia-forge / KubeJSAvaritiaPlugin
 * @author cnlimiter
 * CreateTime: 2023/9/17 0:49
 * Description:
 */

public class KubeJSAvaritiaPlugin extends KubeJSPlugin {

    @Override
    public void registerRecipeSchemas(RegisterRecipeSchemasEvent event) {
        event.register(ModRecipeSerializers.SHAPED_CRAFT_SERIALIZER.getId(), ShapedTableRecipeSchema.SCHEMA);
        event.register(ModRecipeSerializers.SHAPELESS_CRAFT_SERIALIZER.getId(), ShapelessTableRecipeSchema.SCHEMA);
        event.register(ModRecipeSerializers.COMPRESSOR_SERIALIZER.getId(), CompressRecipeSchema.SCHEMA);
        event.register(ModRecipeSerializers.INFINITY_CATALYST_CRAFT_SERIALIZER.getId(), InfinityCatalystRecipeSchema.SCHEMA);
        event.register(ModRecipeSerializers.ETERNAL_SINGULARITY_CRAFT_SERIALIZER.getId(), EternalSingularityRecipeSchema.SCHEMA);
        event.register(ModRecipeSerializers.EXTREME_SMITHING_SERIALIZER.getId(), ExtremeSmithingRecipeSchema.SCHEMA);
    }

    @Override
    public void registerBindings(BindingsEvent event) {
        event.add("Singularity", Singularity.class);
    }

    @Override
    public void registerEvents() {
        AvaritiaEvents.GROUP.register();
    }

    @Override
    public void onServerReload() {
        SingularityRegisterEventJS registerEventJS = new SingularityRegisterEventJS();
        AvaritiaEvents.REGISTRY.post(ScriptType.SERVER, registerEventJS);
    }

    @Override
    public void registerTypeWrappers(ScriptType type, TypeWrappers typeWrappers) {
        typeWrappers.register(Singularity.class, Singularity::wrap);
    }
}
