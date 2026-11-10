package committee.nova.mods.avaritia.gametest;

import committee.nova.mods.avaritia.Const;
import committee.nova.mods.avaritia.init.registry.ModArmorMaterial;
import net.minecraft.client.resources.model.EquipmentAssetManager;
import net.minecraft.client.resources.model.EquipmentClientInfo;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.network.chat.Component;
import net.minecraft.server.packs.PackLocationInfo;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.PathPackResources;
import net.minecraft.server.packs.repository.PackSource;
import net.minecraft.server.packs.resources.MultiPackResourceManager;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.profiling.Profiler;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.data.event.GatherDataEvent;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

/** Runs the actual client equipment consumer without claiming a rendered-image check. */
@EventBusSubscriber(modid = "avaritia_gametest", value = Dist.CLIENT)
public final class ArmorAssetProbe {
    @SubscribeEvent
    public static void gather(GatherDataEvent.Client event) {
        event.getGenerator().addProvider(true, new DataProvider() {
            @Override
            public CompletableFuture<?> run(CachedOutput ignored) {
                Path resources = Path.of(System.getProperty("avaritia.armor.resources"));
                var location = new PackLocationInfo("armor-port", Component.literal("armor-port"), PackSource.BUILT_IN, Optional.empty());
                try (var manager = new MultiPackResourceManager(PackType.CLIENT_RESOURCES,
                        List.of(new PathPackResources(location, resources)))) {
                    var consumer = new ProbeManager();
                    consumer.consume(manager);
                    check(manager, consumer, ModArmorMaterial.NEUTRON_WOLF_ARMOR_ASSET,
                            EquipmentClientInfo.LayerType.WOLF_BODY, "neutron_wolf_armor");
                    check(manager, consumer, ModArmorMaterial.NEUTRON_NAUTILUS_ARMOR_ASSET,
                            EquipmentClientInfo.LayerType.NAUTILUS_BODY, "neutron_nautilus_armor");
                    if (!consumer.get(ModArmorMaterial.INFINITY_ARMOR_ASSET).getLayers(EquipmentClientInfo.LayerType.NAUTILUS_BODY).isEmpty())
                        throw new IllegalStateException("obsolete horse-armor nautilus layer remains");
                    Path receipt = Path.of(System.getProperty("avaritia.armor.assetReceipt"));
                    Files.writeString(receipt, "EquipmentAssetManager consumed wolf/nautilus BODY layers; both texture resources opened; obsolete shared layer absent.\n");
                    return CompletableFuture.completedFuture(null);
                } catch (IOException failure) {
                    throw new IllegalStateException(failure);
                }
            }

            @Override
            public String getName() { return "Armor equipment consumer verification"; }
        });
    }

    private static void check(ResourceManager resources, EquipmentAssetManager consumer,
                              net.minecraft.resources.ResourceKey<net.minecraft.world.item.equipment.EquipmentAsset> asset,
                              EquipmentClientInfo.LayerType type, String name) throws IOException {
        var layers = consumer.get(asset).getLayers(type);
        if (layers.size() != 1 || !layers.getFirst().textureId().equals(Const.rl(name)))
            throw new IllegalStateException("wrong equipment layer: " + asset);
        try (var stream = resources.getResourceOrThrow(layers.getFirst().getTextureLocation(type)).open()) {
            if (stream.read() != 137 || stream.read() != 80) throw new IllegalStateException("texture is not PNG: " + asset);
        }
    }

    private static final class ProbeManager extends EquipmentAssetManager {
        private void consume(ResourceManager resources) {
            apply(prepare(resources, Profiler.get()), resources, Profiler.get());
        }
    }
}
