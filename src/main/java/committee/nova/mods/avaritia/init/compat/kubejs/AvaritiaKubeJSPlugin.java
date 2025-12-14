package committee.nova.mods.avaritia.init.compat.kubejs;

import com.google.gson.JsonElement;
import committee.nova.mods.avaritia.core.singularity.Singularity;
import committee.nova.mods.avaritia.init.compat.kubejs.component.ShapedRecipePatternComponent;
import committee.nova.mods.avaritia.init.compat.kubejs.event.AvaritiaEvents;
import committee.nova.mods.avaritia.init.compat.kubejs.event.SingularityRegisterEventJS;
import committee.nova.mods.avaritia.init.compat.kubejs.schema.*;
import committee.nova.mods.avaritia.init.registry.ModRecipeSerializers;
import dev.latvian.mods.kubejs.core.RecipeManagerKJS;
import dev.latvian.mods.kubejs.event.EventGroupRegistry;
import dev.latvian.mods.kubejs.plugin.ClassFilter;
import dev.latvian.mods.kubejs.plugin.KubeJSPlugin;
import dev.latvian.mods.kubejs.recipe.RecipesKubeEvent;
import dev.latvian.mods.kubejs.recipe.component.RecipeComponentTypeRegistry;
import dev.latvian.mods.kubejs.recipe.schema.RecipeSchemaRegistry;
import dev.latvian.mods.kubejs.script.BindingRegistry;
import dev.latvian.mods.kubejs.script.ScriptType;
import dev.latvian.mods.kubejs.script.TypeWrapperRegistry;
import net.minecraft.resources.ResourceLocation;

import java.util.Map;

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
        event.register(ModRecipeSerializers.SHAPED_CRAFT_SERIALIZER.getId(), ShapedTableRecipeSchema.SCHEMA);
        event.register(ModRecipeSerializers.SHAPELESS_CRAFT_SERIALIZER.getId(), ShapelessTableRecipeSchema.SCHEMA);
        event.register(ModRecipeSerializers.COMPRESSOR_SERIALIZER.getId(), CompressRecipeSchema.SCHEMA);
        event.register(ModRecipeSerializers.INFINITY_CATALYST_CRAFT_SERIALIZER.getId(), InfinityCatalystRecipeSchema.SCHEMA);
        event.register(ModRecipeSerializers.ETERNAL_SINGULARITY_CRAFT_SERIALIZER.getId(), EternalSingularityRecipeSchema.SCHEMA);
        event.register(ModRecipeSerializers.EXTREME_SMITHING_SERIALIZER.getId(), ExtremeSmithingRecipeSchema.SCHEMA);
        event.register(ModRecipeSerializers.NO_CONSUME_CATALYST_SHAPED_SERIALIZER.getId(), NoConsumeCatalystShapedSchema.SCHEMA);
    }


    @Override
    public void registerBindings(BindingRegistry event) {
        event.add("Singularity", Singularity.class);
    }

    @Override
    public void registerEvents(EventGroupRegistry registry) {
        registry.register(AvaritiaEvents.GROUP);
    }

    @Override
    public void beforeRecipeLoading(RecipesKubeEvent event, RecipeManagerKJS manager, Map<ResourceLocation, JsonElement> recipeJsons) {
        SingularityRegisterEventJS registerEventJS = new SingularityRegisterEventJS(event);
        AvaritiaEvents.REGISTRY.post(ScriptType.SERVER, registerEventJS);
    }

    @Override
    public void registerTypeWrappers(TypeWrapperRegistry wrapperRegistry) {
        wrapperRegistry.register(Singularity.class, Singularity::wrap);
    }
}
