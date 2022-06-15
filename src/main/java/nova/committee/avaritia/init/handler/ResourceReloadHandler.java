package nova.committee.avaritia.init.handler;

import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.ResourceManagerReloadListener;
import net.minecraftforge.client.event.RecipesUpdatedEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.crafting.conditions.ICondition;
import net.minecraftforge.event.AddReloadListenerEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import nova.committee.avaritia.init.event.RegisterRecipesEvent;
import nova.committee.avaritia.util.RecipeUtil;
import org.jetbrains.annotations.NotNull;

/**
 * Description:
 * Author: cnlimiter
 * Date: 2022/5/15 11:40
 * Version: 1.0
 */
@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.FORGE)
public class ResourceReloadHandler {


    @SubscribeEvent(priority = EventPriority.HIGH)
    public static void onAddReloadListeners(AddReloadListenerEvent event) {

        event.addListener(new SingularityResourceReload(event.getServerResources().getConditionContext()));
        event.addListener(new RegisterRecipesReloadListener());
        RecipeUtil.recipeManager = event.getServerResources().getRecipeManager();
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onRecipesUpdated(RecipesUpdatedEvent event) {
        RecipeUtil.recipeManager = event.getRecipeManager();
    }

    private record SingularityResourceReload(ICondition.IContext context) implements ResourceManagerReloadListener {
        @Override
        public void onResourceManagerReload(@NotNull ResourceManager manager) {
            SingularityRegistryHandler.getInstance().onResourceManagerReload(context);
        }
    }

    private static class RegisterRecipesReloadListener implements ResourceManagerReloadListener {
        @Override
        public void onResourceManagerReload(@NotNull ResourceManager manager) {
            MinecraftForge.EVENT_BUS.post(new RegisterRecipesEvent(RecipeUtil.getRecipeManager()));
        }
    }
}
