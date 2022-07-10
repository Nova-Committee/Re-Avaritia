package nova.committee.avaritia.init.proxy;

import com.mojang.authlib.GameProfile;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.event.lifecycle.FMLDedicatedServerSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

import java.util.UUID;

/**
 * Description:
 * Author: cnlimiter
 * Date: 2022/3/31 20:12
 * Version: 1.0
 */
public class ServerProxy implements IProxy {
    public ServerProxy() {
        IEventBus modBus = FMLJavaModLoadingContext.get().getModEventBus();
        modBus.addListener(this::serverSetup);

        IEventBus forgeBus = MinecraftForge.EVENT_BUS;
    }

    private void serverSetup(FMLDedicatedServerSetupEvent event) {

    }


    public static final GameProfile avaritiaFakePlayer = new GameProfile(UUID.fromString("32283731-bbef-487c-bb69-c7e32f84ed27"), "[Avaritia]");

}
