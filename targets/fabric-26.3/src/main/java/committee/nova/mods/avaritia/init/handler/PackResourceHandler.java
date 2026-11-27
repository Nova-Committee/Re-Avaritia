package committee.nova.mods.avaritia.init.handler;

import committee.nova.mods.avaritia.Const;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.network.chat.Component;
import net.minecraft.server.packs.PackLocationInfo;
import net.minecraft.server.packs.PackSelectionConfig;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.PathPackResources;
import net.minecraft.server.packs.repository.Pack;
import net.minecraft.server.packs.repository.PackSource;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;
import java.util.Map;

/** Supplies this mod's required resources and the optional bundled art pack using vanilla packs. */
public final class PackResourceHandler {
    private static final Set<String> RESOURCE_MODS = new LinkedHashSet<>(Set.of(Const.MOD_ID));

    private PackResourceHandler() {}

    public static void registerResources(String modId) {
        RESOURCE_MODS.add(modId);
    }

    public static void addResources(Consumer<Pack> consumer, PackType type) {
        for (String modId : RESOURCE_MODS) {
            var mod = FabricLoader.getInstance().getModContainer(modId).orElseThrow();
            int index = 0;
            for (Path root : mod.getRootPaths()) {
                if (!Files.isDirectory(root.resolve(type.getDirectory()))) continue;
                String packId = "mod/" + modId + "/" + index++;
                // 26.3 起 Pack 构造改为 PackLocationInfo + ResourcesSupplier + PackSelectionConfig。
                Pack pack = Pack.readMetaAndCreate(
                        new PackLocationInfo(packId, Component.literal(mod.getMetadata().getName()),
                                PackSource.BUILT_IN, Optional.empty()),
                        new PathPackResources.PathResourcesSupplier(root),
                        type,
                        new PackSelectionConfig(true, Pack.Position.TOP, false));
                consumer.accept(Objects.requireNonNull(pack, "Missing resource pack metadata: " + packId));
            }
        }
    }

    public static void addPack(Map<String, Pack> packs) {
        FabricLoader.getInstance().getModContainer(Const.MOD_ID)
                .flatMap(mod -> mod.findPath("resourcepacks/avaritia"))
                .ifPresent(path -> {
                    Pack pack = Pack.readMetaAndCreate(
                            new PackLocationInfo("avaritia:default",
                                    Component.translatable("title.avaritia.resourcepack"),
                                    PackSource.BUILT_IN, Optional.empty()),
                            new PathPackResources.PathResourcesSupplier(path),
                            PackType.CLIENT_RESOURCES,
                            new PackSelectionConfig(false, Pack.Position.TOP, false));
                    if (pack != null) packs.put(pack.getId(), pack);
                });
    }
}
