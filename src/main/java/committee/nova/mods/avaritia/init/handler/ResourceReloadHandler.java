package committee.nova.mods.avaritia.init.handler;

import net.minecraft.server.ReloadableServerResources;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.ResourceManagerReloadListener;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.AddReloadListenerEvent;
import org.jetbrains.annotations.NotNull;

/**
 * Description:
 * Author: cnlimiter
 * Date: 2022/5/15 11:40
 * Version: 1.0
 */
@EventBusSubscriber
public class ResourceReloadHandler {


    @SubscribeEvent
    public static void onAddReloadListeners(AddReloadListenerEvent event) {

        event.addListener(new SingularityResourceReloadListener(event.getServerResources()));
        //event.addListener(new RegisterRecipesReloadListener(event.getServerResources()));
    }


    private record SingularityResourceReloadListener(
            ReloadableServerResources serverResources) implements ResourceManagerReloadListener {
        @Override
        public void onResourceManagerReload(@NotNull ResourceManager manager) {
            SingularityRegistryHandler.getInstance().onResourceManagerReload();
        }
    }

//    private record RegisterRecipesReloadListener(
//            ReloadableServerResources serverResources) implements ResourceManagerReloadListener {
//        @Override
//        public void onResourceManagerReload(@NotNull ResourceManager manager) {
//            NeoForge.EVENT_BUS.post(new RegisterRecipesEvent(serverResources.getRecipeManager()));
//        }
//    }
}
