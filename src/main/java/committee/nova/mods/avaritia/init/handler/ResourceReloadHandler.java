package committee.nova.mods.avaritia.init.handler;

import committee.nova.mods.avaritia.init.event.RegisterRecipesEvent;
import committee.nova.mods.avaritia.util.RecipeUtil;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.ResourceManagerReloadListener;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.common.conditions.ICondition;
import net.neoforged.neoforge.event.AddReloadListenerEvent;
import org.jetbrains.annotations.NotNull;

/**
 * Description:
 * Author: cnlimiter
 * Date: 2022/5/15 11:40
 * Version: 1.0
 */
@Mod.EventBusSubscriber
public class ResourceReloadHandler {


    @SubscribeEvent
    public static void onAddReloadListeners(AddReloadListenerEvent event) {

        event.addListener(new SingularityResourceReload(event.getServerResources().getConditionContext()));
        event.addListener(new RegisterRecipesReloadListener());
    }


    private record SingularityResourceReload(ICondition.IContext context) implements ResourceManagerReloadListener {
        @Override
        public void onResourceManagerReload(@NotNull ResourceManager manager) {
            SingularityRegistryHandler.getInstance().onResourceManagerReload();
        }
    }

    private static class RegisterRecipesReloadListener implements ResourceManagerReloadListener {
        @Override
        public void onResourceManagerReload(@NotNull ResourceManager manager) {
            NeoForge.EVENT_BUS.post(new RegisterRecipesEvent(RecipeUtil.getRecipeManager()));
        }
    }
}
