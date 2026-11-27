package committee.nova.mods.avaritia.init.handler;

import committee.nova.mods.avaritia.Const;
import committee.nova.mods.avaritia.init.registry.ModTileEntities;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;

/** Registers the tile itself as a stable proxy; each method resolves the currently selected channel. */
@EventBusSubscriber(modid = Const.MOD_ID)
public final class TesseractCapabilityHandler {
    private TesseractCapabilityHandler() { }

    @SubscribeEvent
    public static void registerCapabilities(RegisterCapabilitiesEvent event) {
        event.registerBlockEntity(Capabilities.ItemHandler.BLOCK, ModTileEntities.tesseract_tile.get(),
                (tile, side) -> tile);
        event.registerBlockEntity(Capabilities.FluidHandler.BLOCK, ModTileEntities.tesseract_tile.get(),
                (tile, side) -> tile);
        event.registerBlockEntity(Capabilities.EnergyStorage.BLOCK, ModTileEntities.tesseract_tile.get(),
                (tile, side) -> tile);
    }
}
