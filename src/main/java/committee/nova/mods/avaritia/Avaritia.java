package committee.nova.mods.avaritia;

import committee.nova.mods.avaritia.client.model.GapingVoidModel;
import committee.nova.mods.avaritia.client.model.WingModel;
import committee.nova.mods.avaritia.client.render.layer.EyeInfinityLayer;
import committee.nova.mods.avaritia.client.render.layer.WingInfinityLayer;
import committee.nova.mods.avaritia.common.item.EndestPearlItem;
import committee.nova.mods.avaritia.init.config.ModConfig;
import committee.nova.mods.avaritia.init.handler.ColorHandler;
import committee.nova.mods.avaritia.init.handler.SingularityRegistryHandler;
import committee.nova.mods.avaritia.init.registry.*;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
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

    public Avaritia() {
        var bus = FMLJavaModLoadingContext.get().getModEventBus();
        bus.register(this);
        ModBlocks.BLOCKS.register(bus);
        ModItems.ITEMS.register(bus);
        ModTileEntities.BLOCK_ENTITIES.register(bus);
        ModMenus.MENUS.register(bus);
        ModEntities.ENTITIES.register(bus);
        ModRecipeTypes.RECIPES.register(bus);
        ModRecipeTypes.SERIALIZERS.register(bus);
        ModTab.TABS.register(bus);

        DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> {
            bus.register(new ColorHandler());
        });
        ModConfig.register();

    }

    @SubscribeEvent
    public void setup(final FMLCommonSetupEvent event) {
        MinecraftForge.EVENT_BUS.register(this);
        SingularityRegistryHandler.getInstance().writeDefaultSingularityFiles();
        EndestPearlItem.registerDispenser();
    }

    @SubscribeEvent
    public void onClientSetup(FMLClientSetupEvent event) {
        ModEntities.onClientSetup();
        ModMenus.onClientSetup();
        ModTileEntities.onClientSetup();
    }


    @SubscribeEvent
    public void registerLayer(EntityRenderersEvent.RegisterLayerDefinitions event) {
        event.registerLayerDefinition(GapingVoidModel.LAYER_LOCATION, GapingVoidModel::createBodyLayer);
        event.registerLayerDefinition(WingModel.LAYER_LOCATION, WingModel::createBodyLayer);

    }

    @SubscribeEvent
    public void addLayers(EntityRenderersEvent.AddLayers evt) {
        addPlayerLayer(evt, "default");
        addPlayerLayer(evt, "slim");
    }

    @OnlyIn(Dist.CLIENT)
    private void addPlayerLayer(EntityRenderersEvent.AddLayers evt, String skin) {
        EntityRenderer<?> renderer = evt.getSkin(skin);

        if (renderer instanceof LivingEntityRenderer livingRenderer) {
            livingRenderer.addLayer(new WingInfinityLayer(livingRenderer));
            livingRenderer.addLayer(new EyeInfinityLayer(livingRenderer));

        }
    }

}
