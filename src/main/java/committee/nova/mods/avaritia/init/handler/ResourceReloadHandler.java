package committee.nova.mods.avaritia.init.handler;

import committee.nova.mods.avaritia.Const;
import committee.nova.mods.avaritia.api.init.event.RegisterRecipesEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.server.ReloadableServerResources;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.PathPackResources;
import net.minecraft.server.packs.repository.Pack;
import net.minecraft.server.packs.repository.PackSource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.ResourceManagerReloadListener;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.AddPackFindersEvent;
import net.minecraftforge.event.AddReloadListenerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModList;
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
        event.addListener(new SingularityResourceReloadListener(event.getServerResources()));
        event.addListener(new RegisterRecipesReloadListener(event.getServerResources()));
    }


    private record SingularityResourceReloadListener(
            ReloadableServerResources serverResources) implements ResourceManagerReloadListener {
        @Override
        public void onResourceManagerReload(@NotNull ResourceManager manager) {
            SingularityRegistryHandler.getInstance().onResourceManagerReload(serverResources.getConditionContext());
        }
    }

    private record RegisterRecipesReloadListener(
            ReloadableServerResources serverResources) implements ResourceManagerReloadListener {
        @Override
        public void onResourceManagerReload(@NotNull ResourceManager manager) {
            MinecraftForge.EVENT_BUS.post(new RegisterRecipesEvent(serverResources.getRecipeManager()));
        }
    }

    @SubscribeEvent
    public static void addPackFinders(AddPackFindersEvent event) {
        if (event.getPackType() == PackType.CLIENT_RESOURCES) {
            var resourcePath = ModList.get().getModFileById(Const.MOD_ID).getFile().findResource("resourcepacks/avaritia");
            var pack = new PathPackResources(ModList.get().getModFileById(Const.MOD_ID).getFile().getFileName() + ":" + resourcePath, resourcePath, false);
            Pack.ResourcesSupplier resourcesSupplier = (string) -> pack;
            Pack.Info info = Pack.readPackInfo("avaritia_vanilla", resourcesSupplier);

            event.addRepositorySource(packConsumer -> {
                packConsumer.accept(
                        Pack.create("avaritia_vanilla",
                                Component.literal("Re:Avaritia Vanilla"),
                                false, resourcesSupplier, info,
                                PackType.CLIENT_RESOURCES,
                                Pack.Position.TOP, false, PackSource.BUILT_IN)
                );
            });
        }
    }
}
