package committee.nova.mods.avaritia.client;

import committee.nova.mods.avaritia.Static;
import committee.nova.mods.avaritia.api.iface.IColored;
import committee.nova.mods.avaritia.client.model.*;
import committee.nova.mods.avaritia.client.render.tile.CompressedChestRenderer;
import committee.nova.mods.avaritia.client.shader.AvaritiaShaders;
import committee.nova.mods.avaritia.init.registry.ModEntities;
import committee.nova.mods.avaritia.init.registry.ModItems;
import committee.nova.mods.avaritia.init.registry.ModMenus;
import committee.nova.mods.avaritia.init.registry.ModTileEntities;
import committee.nova.mods.avaritia.util.client.ColorUtils;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.*;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

/**
 * Author cnlimiter
 * CreateTime 2023/6/17 23:24
 * Name AvaritiaClient
 * Description
 */
@Mod.EventBusSubscriber(modid = Static.MOD_ID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
public final class AvaritiaClient {
    public static final ModelLayerLocation COMPRESSED_CHEST = new ModelLayerLocation(Static.rl("compressed_chest"), "main");
    public static final ModelLayerLocation COMPRESSED_CHEST_LEFT = new ModelLayerLocation(Static.rl("compressed_chest_left"), "main");
    public static final ModelLayerLocation COMPRESSED_CHEST_RIGHT = new ModelLayerLocation(Static.rl("compressed_chest_right"), "main");

    @SubscribeEvent
    public static void clientSetUp(FMLClientSetupEvent event){
        ModEntities.onClientSetup();
        ModMenus.onClientSetup();
        ModTileEntities.onClientSetup();
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onRegisterShaders(RegisterShadersEvent event){
        AvaritiaShaders.onRegisterShaders(event);//注册着色器
    }

    @SubscribeEvent
    public static void registerEntityLayerDefinitions(EntityRenderersEvent.RegisterLayerDefinitions event) {
        event.registerLayerDefinition(COMPRESSED_CHEST, CompressedChestRenderer::createSingleBodyLayer);
        event.registerLayerDefinition(COMPRESSED_CHEST_LEFT, CompressedChestRenderer::createDoubleBodyLeftLayer);
        event.registerLayerDefinition(COMPRESSED_CHEST_RIGHT, CompressedChestRenderer::createDoubleBodyRightLayer);
    }

    @SubscribeEvent
    public static void onItemColors(RegisterColorHandlersEvent.Item event) {
        event.register(new IColored.ItemColors(), ModItems.singularity.get());
        event.register(
                (stack, index) -> getCurrentRainbowColor(),
                ModItems.eternal_singularity.get()
        );
    }

    @SubscribeEvent
    public static void registerOverlays(RegisterGuiOverlaysEvent event) {

    }

    @SubscribeEvent
    public static void registerLoaders(ModelEvent.RegisterGeometryLoaders event) {
        event.register("cosmic", CosmicModelLoader.INSTANCE);
        event.register("halo", HaloModelLoader.INSTANCE);
    }

    @SubscribeEvent
    public static void addPlayerLayer(EntityRenderersEvent.AddLayers event) {
        addLayer(event, "default");
        addLayer(event, "slim");
    }

    private static void addLayer(final EntityRenderersEvent.AddLayers e, final String s) {
        final LivingEntityRenderer entityRenderer = e.getSkin(s);
        entityRenderer.addLayer(new InfinityArmorModel.PlayerRender((RenderLayerParent<Player, PlayerModel<Player>>)entityRenderer));
    }

    public static int getCurrentRainbowColor() {
        var hue = (System.currentTimeMillis() % 18000) / 18000F;
        return ColorUtils.HSBToRGB(hue, 1, 1);
    }
}
