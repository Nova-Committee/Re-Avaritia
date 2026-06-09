package committee.nova.mods.avaritia.init.handler;

import committee.nova.mods.avaritia.Const;
import committee.nova.mods.avaritia.api.iface.item.IItemCapability;
import committee.nova.mods.avaritia.init.registry.ModItems;
import committee.nova.mods.avaritia.init.registry.ModTileEntities;
import net.minecraft.core.Holder;
import net.minecraft.world.WorldlyContainer;
import net.minecraft.world.item.Item;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.transfer.item.VanillaContainerWrapper;
import net.neoforged.neoforge.transfer.item.WorldlyContainerWrapper;

import java.util.List;

/**
 * CapHandler
 *
 * @author cnlimiter
 * @version 1.0
 * @description
 * @date 2024/4/7 1:48
 */
@EventBusSubscriber(modid = Const.MOD_ID)
public class CapHandler {
    @SubscribeEvent
    private static void registerCapabilities(RegisterCapabilitiesEvent event) {
        for (Holder<Item> entry : ModItems.ITEMS.getEntries()) {
            Item item = entry.value();
            if (item instanceof IItemCapability iItemCapability) {
                iItemCapability.attachCapabilities(event);
            }
        }

        event.registerBlockEntity(
                Capabilities.Item.BLOCK,
                ModTileEntities.compressed_chest_tile.get(),
                (be, side) -> VanillaContainerWrapper.of(be)
        );

        event.registerBlockEntity(
                Capabilities.Item.BLOCK,
                ModTileEntities.INFINITY_CHEST_TILE.get(),
                (be, side) -> VanillaContainerWrapper.of(be.chest)
        );

        var sidedVanillaContainers = List.of(
                ModTileEntities.neutron_collector_tile.get(),
                ModTileEntities.neutron_compressor_tile.get()
        );

        for (var type : sidedVanillaContainers) {
            event.registerBlockEntity(Capabilities.Item.BLOCK, type, (sidedContainer, side) ->
                    sidedContainer instanceof WorldlyContainer container ? new WorldlyContainerWrapper(container, side) : null);
        }

    }
}
