package committee.nova.mods.avaritia.init.compat.kubejs;

import com.google.common.base.Stopwatch;
import committee.nova.mods.avaritia.api.Lib;
import committee.nova.mods.avaritia.api.init.event.RegisterRecipesEvent;
import committee.nova.mods.avaritia.core.singularity.Singularity;
import committee.nova.mods.avaritia.init.compat.kubejs.event.AvaritiaEvents;
import committee.nova.mods.avaritia.init.compat.kubejs.event.SingularityRegisterEventJS;
import committee.nova.mods.avaritia.init.registry.ModRecipeSerializers;
import dev.latvian.mods.kubejs.KubeJSPlugin;
import dev.latvian.mods.kubejs.recipe.RecipesEventJS;
import dev.latvian.mods.kubejs.recipe.schema.RegisterRecipeSchemasEvent;
import dev.latvian.mods.kubejs.script.BindingsEvent;
import dev.latvian.mods.kubejs.script.ScriptType;
import dev.latvian.mods.rhino.util.wrap.TypeWrappers;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraftforge.common.MinecraftForge;

import java.util.ArrayList;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.TimeUnit;

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
    public void injectRuntimeRecipes(RecipesEventJS event, RecipeManager manager, Map<ResourceLocation, Recipe<?>> recipesByName) {
        var stopwatch = Stopwatch.createStarted();
        CopyOnWriteArrayList<Recipe<?>> addRecipes = new CopyOnWriteArrayList<>();

        try {
            MinecraftForge.EVENT_BUS.post(new RegisterRecipesEvent(manager, addRecipes));
        } catch (Exception e) {
            Lib.LOGGER.error("Avaritia: An error occurred while firing RecipeManagerLoadingEvent", e);
        }

        for (var recipe : addRecipes) {
            recipesByName.put(recipe.getId(), recipe);
        }

        Lib.LOGGER.info("Avaritia: Registered {} recipes in {} ms (KubeJS mode)", addRecipes.size(), stopwatch.stop().elapsed(TimeUnit.MILLISECONDS));

        SingularityRegisterEventJS registerEventJS = new SingularityRegisterEventJS();
        AvaritiaEvents.REGISTRY.post(ScriptType.SERVER, registerEventJS);
    }

    @Override
    public void registerTypeWrappers(ScriptType type, TypeWrappers typeWrappers) {
        typeWrappers.register(Singularity.class, Singularity::wrap);
    }
}
