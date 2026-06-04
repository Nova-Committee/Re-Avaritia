package com.avaritia.init.handler;

import com.avaritia.Const;
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

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;

/**
 * @author cnlimiter
 */
@EventBusSubscriber(modid = Const.MOD_ID)
public class PackResourceHandler {
    @SubscribeEvent
    public static void addPackFinders(final AddPackFindersEvent event) {
        if (event.getPackType() == PackType.CLIENT_RESOURCES) {
            var modFile = net.neoforged.fml.ModList.get().getModFileById(Const.MOD_ID).getFile();
            Path resourcePath = modFile.getContents().getContentRoots().stream()
                    .map(root -> root.resolve("resourcepacks/avaritia"))
                    .filter(Files::exists)
                    .findFirst()
                    .orElseGet(() -> modFile.getFilePath().resolve("resourcepacks/avaritia"));
            var supplier = new PathPackResources.PathResourcesSupplier(resourcePath);

            event.addRepositorySource(packConsumer -> {
                final PackLocationInfo packInfo = new PackLocationInfo(
                        "builtin/avaritia_vanilla",
                        Component.translatable("title.avaritia.resourcepack"),
                        PackSource.BUILT_IN,
                        Optional.of(new KnownPack(Const.MOD_ID, "builtin/avaritia_vanilla", "1.0")));
                final PackSelectionConfig selectionConfig = new PackSelectionConfig(false, Pack.Position.TOP, false);
                packConsumer.accept(Pack.readMetaAndCreate(packInfo, supplier, PackType.CLIENT_RESOURCES, selectionConfig));
            });
        }
    }
}
