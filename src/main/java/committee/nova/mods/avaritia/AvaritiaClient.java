package committee.nova.mods.avaritia;

import committee.nova.mods.avaritia.api.iface.IColored;
import committee.nova.mods.avaritia.client.model.WingModel;
import committee.nova.mods.avaritia.client.render.layer.EyeInfinityLayer;
import committee.nova.mods.avaritia.client.render.layer.WingInfinityLayer;
import committee.nova.mods.avaritia.init.handler.InfinityHandler;
import committee.nova.mods.avaritia.init.registry.*;
import io.github.fabricators_of_create.porting_lib.event.client.ColorHandlersCallback;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.EntityModelLayerRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.LivingEntityFeatureRendererRegistrationCallback;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;

public class AvaritiaClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        ModMenus.onClientSetup();
        ModEntities.onClientSetup();
        ModTileEntities.onClientSetup();
        InfinityHandler.clientInit();

        ColorHandlersCallback.ITEM.register((itemColors, blockColors) -> {
            itemColors.register(new IColored.ItemColors(), ModItems.singularity.get());
        });

        //ModModelsPredicateProviders.registerProviders();
        EntityModelLayerRegistry.registerModelLayer(WingModel.LAYER_LOCATION, WingModel::createBodyLayer);
        EntityModelLayerRegistry.registerModelLayer(WingModel.LAYER_LOCATION, WingModel::createBodyLayer);

        LivingEntityFeatureRendererRegistrationCallback.EVENT.register((entityType, entityRenderer, registrationHelper, context) -> {
            if(entityRenderer instanceof PlayerRenderer playerEntityRenderer) {
                registrationHelper.register(new WingInfinityLayer(playerEntityRenderer));
                registrationHelper.register(new EyeInfinityLayer(playerEntityRenderer));
            }
        });
    }
}
