package committee.nova.mods.avaritia.init.handler;

import committee.nova.mods.avaritia.Const;
import net.minecraft.network.chat.Component;
import net.minecraft.server.packs.PackLocationInfo;
import net.minecraft.server.packs.PackSelectionConfig;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.PathPackResources;
import net.minecraft.server.packs.repository.KnownPack;
import net.minecraft.server.packs.repository.Pack;
import net.minecraft.server.packs.repository.PackSource;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.AddPackFindersEvent;

import java.util.Optional;

/**
 * @author cnlimiter
 */
@EventBusSubscriber()
public class PackResourceHandler {
    @SubscribeEvent
    public static void addPackFinders(AddPackFindersEvent event) {
        if (event.getPackType() == PackType.CLIENT_RESOURCES) {
            var resourcePath = net.neoforged.fml.ModList.get().getModFileById(Const.MOD_ID).getFile().findResource("resourcepacks/avaritia");
            var supplier = new PathPackResources.PathResourcesSupplier(resourcePath);

            event.addRepositorySource(packConsumer -> {
                final PackLocationInfo packInfo = new PackLocationInfo(
                        "builtin/avaritia_vanilla",
                        Component.literal("Re:Avaritia Vanilla"),
                        PackSource.BUILT_IN,
                        Optional.of(new KnownPack(Const.MOD_ID, "builtin/avaritia_vanilla", "1.0")));
                final PackSelectionConfig selectionConfig = new PackSelectionConfig(false, Pack.Position.TOP, false);
                packConsumer.accept(Pack.readMetaAndCreate(packInfo, supplier, PackType.CLIENT_RESOURCES, selectionConfig));
            });
        }
    }
}
