package nova.committee.avaritia.init.handler;

import net.minecraftforge.client.event.ColorHandlerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import nova.committee.avaritia.api.util.IColored;
import nova.committee.avaritia.init.registry.ModItems;
import nova.committee.avaritia.util.ColorHelper;

/**
 * Description:
 * Author: cnlimiter
 * Date: 2022/4/2 12:52
 * Version: 1.0
 */
@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD)
public class ColorHandler {
    @SubscribeEvent
    public static void onItemColors(ColorHandlerEvent.Item event) {
        var colors = event.getItemColors();

        colors.register(new IColored.ItemColors(), ModItems.singularity);

    }

    private static int getCurrentRainbowColor() {
        var hue = (System.currentTimeMillis() % 18000) / 18000F;
        return ColorHelper.hsbToRGB(hue, 1, 1);
    }
}
