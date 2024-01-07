package committee.nova.mods.avaritia.init.handler;

import committee.nova.mods.avaritia.init.event.RegisterRecipesEvent;
import committee.nova.mods.avaritia.util.RecipeUtil;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.ResourceManagerReloadListener;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.crafting.conditions.ICondition;
import net.minecraftforge.event.AddReloadListenerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
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
