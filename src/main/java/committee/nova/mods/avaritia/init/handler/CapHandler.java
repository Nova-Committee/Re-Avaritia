package committee.nova.mods.avaritia.init.handler;

import committee.nova.mods.avaritia.Static;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.animal.horse.AbstractHorse;
import net.minecraftforge.common.capabilities.RegisterCapabilitiesEvent;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * CapHandler
 *
 * @author cnlimiter
 * @version 1.0
 * @description
 * @date 2024/4/7 1:48
 */
@Mod.EventBusSubscriber
public class CapHandler {

    @SubscribeEvent
    public static void registerCaps(RegisterCapabilitiesEvent event){
    }

    @SubscribeEvent
    public static void attachCaps(AttachCapabilitiesEvent<Entity> event) {
    }
}
