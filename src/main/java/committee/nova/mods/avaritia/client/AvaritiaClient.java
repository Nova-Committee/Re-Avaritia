package committee.nova.mods.avaritia.client;

import committee.nova.mods.avaritia.api.iface.IColored;
import committee.nova.mods.avaritia.client.model.CosmicModelLoader;
import committee.nova.mods.avaritia.client.model.GapingVoidModel;
import committee.nova.mods.avaritia.client.model.HaloModelLoader;
import committee.nova.mods.avaritia.client.model.WingModel;
import committee.nova.mods.avaritia.client.render.layer.EyeInfinityLayer;
import committee.nova.mods.avaritia.client.render.layer.WingInfinityLayer;
import committee.nova.mods.avaritia.init.registry.ModItems;
import committee.nova.mods.avaritia.util.client.color.ColorUtil;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.client.event.ModelEvent;
import net.minecraftforge.client.event.RegisterColorHandlersEvent;
import net.minecraftforge.client.event.RegisterGuiOverlaysEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

/**
 * Author cnlimiter
 * CreateTime 2023/6/17 23:24
 * Name AvaritiaClient
 * Description
 */

public class AvaritiaClient {
    public static void init(){
        var eventbus = FMLJavaModLoadingContext.get().getModEventBus();
        var forgeBus = MinecraftForge.EVENT_BUS;
        eventbus.addListener(AvaritiaClient::registerEntityLayerDefinitions);
        eventbus.addListener(AvaritiaClient::onItemColors);
        eventbus.addListener(AvaritiaClient::registerOverlays);
        eventbus.addListener(AvaritiaClient::addPlayerLayer);
        //eventbus.addListener(AvaritiaClient::registerLoaders);
    }
    private static void registerEntityLayerDefinitions(EntityRenderersEvent.RegisterLayerDefinitions event) {
        event.registerLayerDefinition(GapingVoidModel.LAYER_LOCATION, GapingVoidModel::createBodyLayer);
        event.registerLayerDefinition(WingModel.LAYER_LOCATION, WingModel::createBodyLayer);

    }

    private static void onItemColors(RegisterColorHandlersEvent.Item event) {
        event.register(new IColored.ItemColors(), ModItems.singularity.get());
    }

    private static void registerOverlays(RegisterGuiOverlaysEvent event) {

    }

    private static void registerLoaders(ModelEvent.RegisterGeometryLoaders event) {
        event.register("cosmic", CosmicModelLoader.INSTANCE);
        event.register("halo", HaloModelLoader.INSTANCE);
    }

    private static int getCurrentRainbowColor() {
        var hue = (System.currentTimeMillis() % 18000) / 18000F;
        return ColorUtil.hsbToRGB(hue, 1, 1);
    }

    private static void addPlayerLayer(EntityRenderersEvent.AddLayers event) {
        event.getSkins().forEach(skin -> {
            EntityRenderer<?> renderer = event.getSkin(skin);
            if (renderer != null && renderer instanceof LivingEntityRenderer livingRender){
                livingRender.addLayer(new WingInfinityLayer(livingRender));
                livingRender.addLayer(new EyeInfinityLayer(livingRender));
            }
        });


    }
}
