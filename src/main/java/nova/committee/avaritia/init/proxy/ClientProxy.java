package nova.committee.avaritia.init.proxy;

import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import nova.committee.avaritia.client.model.GapingVoidModel;
import nova.committee.avaritia.client.model.WingModel;
import nova.committee.avaritia.client.render.layer.EyeInfinityLayer;
import nova.committee.avaritia.client.render.layer.WingInfinityLayer;
import nova.committee.avaritia.init.registry.ModEntities;
import nova.committee.avaritia.init.registry.ModMenus;

/**
 * Description:
 * Author: cnlimiter
 * Date: 2022/3/31 12:41
 * Version: 1.0
 */
public class ClientProxy implements IProxy {

    public void registerLayer(EntityRenderersEvent.RegisterLayerDefinitions event) {
        //Register entity rendering handlers
        event.registerLayerDefinition(GapingVoidModel.LAYER_LOCATION, GapingVoidModel::createBodyLayer);
        event.registerLayerDefinition(WingModel.LAYER_LOCATION, WingModel::createBodyLayer);

    }

    public void addLayers(EntityRenderersEvent.AddLayers evt) {
        addPlayerLayer(evt, "default");
        addPlayerLayer(evt, "slim");
    }

    private void addPlayerLayer(EntityRenderersEvent.AddLayers evt, String skin) {
        EntityRenderer<? extends Player> renderer = evt.getSkin(skin);

        if (renderer instanceof LivingEntityRenderer livingRenderer) {
            livingRenderer.addLayer(new WingInfinityLayer(livingRenderer));
            livingRenderer.addLayer(new EyeInfinityLayer(livingRenderer));

        }
    }

    private void addTESR(FMLClientSetupEvent event) {
        event.enqueueWork(() -> {
            //BlockEntityRenderers.register(ModTileEntities.infinitato_tile, InfinitatoTileRender::new);//todo, add infinitato_tile render
        });
    }


    public void onClientSetUpEvent(FMLClientSetupEvent event) {
        //ClientRegistry.registerKeyBinding(openKey);
        ModMenus.onClientSetup();
        ModEntities.onClientSetup();
        addTESR(event);
    }

    @Override
    public void init() {


        IEventBus modBus = FMLJavaModLoadingContext.get().getModEventBus();
        modBus.addListener(this::registerLayer);
        modBus.addListener(this::onClientSetUpEvent);
        modBus.addListener(this::addLayers);

        IEventBus forgeBus = MinecraftForge.EVENT_BUS;

    }
}
