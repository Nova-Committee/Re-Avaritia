package committee.nova.mods.avaritia;

import committee.nova.mods.avaritia.common.item.EndestPearlItem;
import committee.nova.mods.avaritia.init.config.ModConfig;
import committee.nova.mods.avaritia.init.handler.SingularityRegistryHandler;
import committee.nova.mods.avaritia.init.proxy.ClientProxy;
import committee.nova.mods.avaritia.init.proxy.IProxy;
import committee.nova.mods.avaritia.init.proxy.ServerProxy;
import committee.nova.mods.avaritia.init.registry.ModEntities;
import committee.nova.mods.avaritia.util.registry.RegistryUtil;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

/**
 * Description:
 * Author: cnlimiter
 * Date: 2022/5/15 10:10
 * Version: 1.0
 */
@Mod(Static.MOD_ID)
public class Avaritia {
    public static IProxy proxy;

    public Avaritia() {
        ModConfig.register();
        var bus = FMLJavaModLoadingContext.get().getModEventBus();
        bus.register(this);
        RegistryUtil.init(bus);
        ModEntities.ENTITIES.register(bus);
        proxy = DistExecutor.safeRunForDist(() -> ClientProxy::new, () -> ServerProxy::new);
    }

    @SubscribeEvent
    public void setup(final FMLCommonSetupEvent event) {
        SingularityRegistryHandler.getInstance().writeDefaultSingularityFiles();
        event.enqueueWork(EndestPearlItem::registerDispenser);
    }


}
