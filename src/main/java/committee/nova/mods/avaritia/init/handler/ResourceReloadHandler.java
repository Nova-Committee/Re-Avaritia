package committee.nova.mods.avaritia.init.handler;

import committee.nova.mods.avaritia.api.init.event.AddReloadListenerEvent;
import committee.nova.mods.avaritia.api.init.event.RegisterRecipesEvent;
import committee.nova.mods.avaritia.util.RecipeUtil;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.ResourceManagerReloadListener;
import org.jetbrains.annotations.NotNull;

/**
 * Description:
 * Author: cnlimiter
 * Date: 2022/5/15 11:40
 * Version: 1.0
 */
public class ResourceReloadHandler {


    public static void init() {
        AddReloadListenerEvent.RELOAD.register(event -> {
            event.addListener(new SingularityResourceReload());
            event.addListener(new RegisterRecipesReloadListener());
        });
        //ResourceManagerHelper.get(PackType.SERVER_DATA).registerReloadListener(new SingularityResourceReload());
        //ResourceManagerHelper.get(PackType.SERVER_DATA).registerReloadListener(new RegisterRecipesReload());
    }


    private static class SingularityResourceReload implements ResourceManagerReloadListener {
        @Override
        public void onResourceManagerReload(@NotNull ResourceManager manager) {
            SingularityRegistryHandler.getInstance().onResourceManagerReload();
        }
    }

    private static class RegisterRecipesReloadListener implements ResourceManagerReloadListener {
        @Override
        public void onResourceManagerReload(@NotNull ResourceManager manager) {
            RegisterRecipesEvent event = new RegisterRecipesEvent(RecipeUtil.getRecipeManager());
            event.sendEvent();
        }
    }

}
