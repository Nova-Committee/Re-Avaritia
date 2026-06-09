package com.avaritia.compat.kubejs;

import com.avaritia.compat.kubejs.component.ShapedRecipePatternComponent;
import com.avaritia.compat.kubejs.event.AvaritiaEvents;
import com.avaritia.compat.kubejs.event.SingularityRegisterEventJS;
import com.avaritia.compat.kubejs.schema.CompressRecipeSchema;
import com.avaritia.compat.kubejs.schema.EternalSingularityRecipeSchema;
import com.avaritia.compat.kubejs.schema.ExtremeSmithingRecipeSchema;
import com.avaritia.compat.kubejs.schema.InfinityCatalystRecipeSchema;
import com.avaritia.compat.kubejs.schema.NoConsumeCatalystShapedSchema;
import com.avaritia.compat.kubejs.schema.ShapedTableRecipeSchema;
import com.avaritia.compat.kubejs.schema.ShapelessTableRecipeSchema;
import com.avaritia.core.singularity.Singularity;
import com.avaritia.init.registry.ModRecipeSerializers;
import com.google.gson.JsonElement;
import dev.latvian.mods.kubejs.event.EventGroupRegistry;
import dev.latvian.mods.kubejs.plugin.ClassFilter;
import dev.latvian.mods.kubejs.plugin.KubeJSPlugin;
import dev.latvian.mods.kubejs.recipe.RecipesKubeEvent;
import dev.latvian.mods.kubejs.recipe.component.RecipeComponentTypeRegistry;
import dev.latvian.mods.kubejs.recipe.schema.RecipeSchemaRegistry;
import dev.latvian.mods.kubejs.script.BindingRegistry;
import dev.latvian.mods.kubejs.script.ScriptType;
import dev.latvian.mods.kubejs.script.TypeWrapperRegistry;
import net.minecraft.resources.Identifier;

import java.util.Map;

public class AvaritiaKubeJSPlugin implements KubeJSPlugin {
    @Override
    public void registerClasses(ClassFilter filter) {
        filter.allow("com.avaritia.common.crafting.recipe");
        filter.allow("com.avaritia.compat.kubejs");
        filter.allow("com.avaritia.core.singularity");
    }

    @Override
    public void registerRecipeComponents(RecipeComponentTypeRegistry registry) {
        registry.unit(new ShapedRecipePatternComponent());
    }

    @Override
    public void registerRecipeSchemas(RecipeSchemaRegistry registry) {
        registry.register(ModRecipeSerializers.SHAPED_CRAFT_SERIALIZER.getId(), ShapedTableRecipeSchema.SCHEMA);
        registry.register(ModRecipeSerializers.SHAPELESS_CRAFT_SERIALIZER.getId(), ShapelessTableRecipeSchema.SCHEMA);
        registry.register(ModRecipeSerializers.COMPRESSOR_SERIALIZER.getId(), CompressRecipeSchema.SCHEMA);
        registry.register(ModRecipeSerializers.INFINITY_CATALYST_CRAFT_SERIALIZER.getId(), InfinityCatalystRecipeSchema.SCHEMA);
        registry.register(ModRecipeSerializers.ETERNAL_SINGULARITY_CRAFT_SERIALIZER.getId(), EternalSingularityRecipeSchema.SCHEMA);
        registry.register(ModRecipeSerializers.EXTREME_SMITHING_SERIALIZER.getId(), ExtremeSmithingRecipeSchema.SCHEMA);
        registry.register(ModRecipeSerializers.NO_CONSUME_CATALYST_SHAPED_SERIALIZER.getId(), NoConsumeCatalystShapedSchema.SCHEMA);
    }

    @Override
    public void registerBindings(BindingRegistry registry) {
        registry.add("Singularity", Singularity.class);
    }

    @Override
    public void registerEvents(EventGroupRegistry registry) {
        registry.register(AvaritiaEvents.GROUP);
    }

    @Override
    public void beforeRecipeLoading(RecipesKubeEvent event, Map<Identifier, JsonElement> recipeJsons) {
        AvaritiaEvents.REGISTRY.post(ScriptType.SERVER, new SingularityRegisterEventJS(event));
    }

    @Override
    public void registerTypeWrappers(TypeWrapperRegistry registry) {
        registry.register(Singularity.class, Singularity::wrap);
    }
}
