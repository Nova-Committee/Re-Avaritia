package committee.nova.mods.avaritia.init.handler;

import committee.nova.mods.avaritia.api.init.iface.IColored;
import committee.nova.mods.avaritia.init.registry.ModItems;
import committee.nova.mods.avaritia.util.ColorUtil;
import net.minecraftforge.client.event.RegisterColorHandlersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

/**
 * Description:
 * Author: cnlimiter
 * Date: 2022/4/2 12:52
 * Version: 1.0
 */
public class ColorHandler {
    @SubscribeEvent
    public void onItemColors(RegisterColorHandlersEvent.Item event) {
        event.register(new IColored.ItemColors(), ModItems.singularity.get());
    }

    private static int getCurrentRainbowColor() {
        var hue = (System.currentTimeMillis() % 18000) / 18000F;
        return ColorUtil.hsbToRGB(hue, 1, 1);
    }
}
