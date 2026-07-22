package committee.nova.mods.avaritia.init.handler;

import committee.nova.mods.avaritia.Const;
import committee.nova.mods.avaritia.init.registry.ModTileEntities;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;

/** Registers the stable channel-delegating transfer capabilities. */
@EventBusSubscriber(modid = Const.MOD_ID)
public final class TesseractCapabilityHandler {
    private TesseractCapabilityHandler() {
    }

    @SubscribeEvent
    private static void registerCapabilities(RegisterCapabilitiesEvent event) {
        event.registerBlockEntity(Capabilities.Item.BLOCK, ModTileEntities.TESSERACT_TILE.get(),
                (tile, side) -> tile.itemHandler());
        event.registerBlockEntity(Capabilities.Fluid.BLOCK, ModTileEntities.TESSERACT_TILE.get(),
                (tile, side) -> tile.fluidHandler());
        event.registerBlockEntity(Capabilities.Energy.BLOCK, ModTileEntities.TESSERACT_TILE.get(),
                (tile, side) -> tile.energyHandler());
    }
}
