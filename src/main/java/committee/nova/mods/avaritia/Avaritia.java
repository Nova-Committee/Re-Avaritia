package committee.nova.mods.avaritia;

import committee.nova.mods.avaritia.init.config.ModConfig;
import committee.nova.mods.avaritia.init.handler.*;
import committee.nova.mods.avaritia.init.registry.*;
import net.fabricmc.api.ModInitializer;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;

public class Avaritia implements ModInitializer {
	@Override
	public void onInitialize() {
		ModConfig.register();
		NetworkHandler.registerPackets();
		SingularityRegistryHandler.getInstance().writeDefaultSingularityFiles();

		ModCreativeModeTabs.init();
		ModItems.init();
		ModBlocks.init();
		ModEntities.init();
		ModTileEntities.init();
		ModMenus.init();
		ModRecipeSerializers.init();
		ModRecipeTypes.init();

		ServerLifecycleEvents.SERVER_STARTING.register(server -> {
			Static.SERVER = server;
		});

		AbilityHandler.init();
		InfinityHandler.init();
		DynamicRecipeHandler.init();
		ItemCaptureHandler.init();
		ItemOverrideHandler.init();
		ResourceReloadHandler.init();
		SingularityRegistryHandler.init();
	}
}