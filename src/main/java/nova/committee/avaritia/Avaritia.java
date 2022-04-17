package nova.committee.avaritia;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import nova.committee.avaritia.init.handler.NetworkHandler;
import nova.committee.avaritia.init.proxy.ClientProxy;
import nova.committee.avaritia.init.proxy.IProxy;
import nova.committee.avaritia.init.proxy.ServerProxy;
import nova.committee.avaritia.init.registry.ModEntities;

@Mod("avaritia")
public class Avaritia {
    public static final IProxy proxy = DistExecutor.safeRunForDist(() -> ClientProxy::new, () -> ServerProxy::new);

    public Avaritia() {
        var bus = FMLJavaModLoadingContext.get().getModEventBus();
        bus.register(this);

        ModEntities.ENTITIES.register(bus);


        MinecraftForge.EVENT_BUS.register(this);
    }

    @SubscribeEvent
    public void setup(final FMLCommonSetupEvent event) {
        NetworkHandler.init();
    }

    @SubscribeEvent
    public void clientSetup(final FMLClientSetupEvent event) {
        proxy.init();
    }

}
