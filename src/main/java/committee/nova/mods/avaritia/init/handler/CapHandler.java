package committee.nova.mods.avaritia.init.handler;

import committee.nova.mods.avaritia.api.common.item.iface.IItemCapability;
import committee.nova.mods.avaritia.init.registry.ModItems;
import net.minecraft.core.Holder;
import net.minecraft.world.item.Item;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;

/**
 * CapHandler
 *
 * @author cnlimiter
 * @version 1.0
 * @description
 * @date 2024/4/7 1:48
 */
@EventBusSubscriber(bus = EventBusSubscriber.Bus.MOD)
public class CapHandler {
    @SubscribeEvent
    private static void registerCapabilities(RegisterCapabilitiesEvent event) {
        for (Holder<Item> entry : ModItems.ITEMS.getEntries()) {
            Item item = entry.value();
            if (item instanceof IItemCapability iItemCapability) {
                iItemCapability.attachCapabilities(event);
            }
        }
    }

}
