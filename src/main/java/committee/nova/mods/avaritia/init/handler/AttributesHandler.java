package committee.nova.mods.avaritia.init.handler;

import committee.nova.mods.avaritia.common.entity.InfinityGolem;
import committee.nova.mods.avaritia.init.registry.ModEntities;
import net.minecraftforge.event.entity.EntityAttributeCreationEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * AttributesHandler
 *
 * @author cnlimiter
 * @version 1.0
 * @description
 * @date 2024/4/5 11:37
 */
@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD)
public class AttributesHandler {

    @SubscribeEvent
    public static void addAttributes(EntityAttributeCreationEvent event){
        event.put(ModEntities.INFINITY_GOLEM.get(), InfinityGolem.createAttributes().build());
    }
}
