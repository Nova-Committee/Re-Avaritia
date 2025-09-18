package committee.nova.mods.avaritia.client;

import committee.nova.mods.avaritia.Const;
import committee.nova.mods.avaritia.api.client.util.ColorUtils;
import committee.nova.mods.avaritia.api.iface.IColored;
import committee.nova.mods.avaritia.client.model.*;
import committee.nova.mods.avaritia.client.particle.ChargeParticle;
import committee.nova.mods.avaritia.client.render.tile.CompressedChestRenderer;
import committee.nova.mods.avaritia.client.screen.AvaritiaConfigScreen;
import committee.nova.mods.avaritia.client.shader.AvaritiaShaders;
import committee.nova.mods.avaritia.init.registry.*;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.ConfigScreenHandler;
import net.minecraftforge.client.event.*;
import net.minecraftforge.client.gui.overlay.VanillaGuiOverlay;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

import static committee.nova.mods.avaritia.Const.LOGGER;
import static committee.nova.mods.avaritia.client.AvaritiaForgeClient.FILTER_KEY;
import static committee.nova.mods.avaritia.client.AvaritiaForgeClient.RING_KEY;
import static committee.nova.mods.avaritia.client.shader.AvaritiaShaders.COSMIC_SPRITES;

/**
 * Author cnlimiter
 * CreateTime 2023/6/17 23:24
 * Name AvaritiaClient
 * Description
 */

@Mod.EventBusSubscriber(modid = Const.MOD_ID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
public class AvaritiaModClient {
    public static final ModelLayerLocation COMPRESSED_CHEST = new ModelLayerLocation(Const.rl("compressed_chest"), "main");
    public static final ModelLayerLocation COMPRESSED_CHEST_LEFT = new ModelLayerLocation(Const.rl("compressed_chest_left"), "main");
    public static final ModelLayerLocation COMPRESSED_CHEST_RIGHT = new ModelLayerLocation(Const.rl("compressed_chest_right"), "main");


    /**
     * 注册键绑定
     */
    @SubscribeEvent
    public static void registerKeyMappings(RegisterKeyMappingsEvent event) {
        LOGGER.debug("Registering key bindings");
        event.register(FILTER_KEY);
        event.register(RING_KEY);
    }



    @SubscribeEvent
    public static void clientSetUp(FMLClientSetupEvent event) {
        registerConfigScreen();
        ModEntities.onClientSetup();
        ModMenus.onClientSetup();
        ModTileEntities.onClientSetup();
        ModSearches.onClientSetup();
    }

    @SubscribeEvent
    public static void registerParticleProviders(RegisterParticleProvidersEvent event){
        event.registerSpriteSet(ModParticles.CHARGE.get(), ChargeParticle.Factory::new);
    }

    @SubscribeEvent
    public static void onTexturesSwitchPost(TextureStitchEvent.Post event){
        if (event.getAtlas().location().equals(InventoryMenu.BLOCK_ATLAS)) {
            for (int i = 0; i < COSMIC_SPRITES.length; i++) {
                COSMIC_SPRITES[i] = event.getAtlas().getSprite(Const.rl("misc/cosmic_" + i));
                AvaritiaShaders.COSMIC_UVS[i * 4] = COSMIC_SPRITES[i].getU0();
                AvaritiaShaders.COSMIC_UVS[i * 4 + 1] = COSMIC_SPRITES[i].getV0();
                AvaritiaShaders.COSMIC_UVS[i * 4 + 2] = COSMIC_SPRITES[i].getU1();
                AvaritiaShaders.COSMIC_UVS[i * 4 + 3] = COSMIC_SPRITES[i].getV1();
            }
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onRegisterShaders(RegisterShadersEvent event) {
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
        event.registerAbove(VanillaGuiOverlay.EXPERIENCE_BAR.id(), "endest_pearl_darkness", AvaritiaForgeClient.DARKNESS_OVERLAY);
    }

    @SubscribeEvent
    public static void registerLoaders(ModelEvent.RegisterGeometryLoaders event) {
        event.register("cosmic", CosmicModelLoader.INSTANCE);
        event.register("halo", HaloModelLoader.INSTANCE);
        event.register("halo_cosmic", HaloCosmicModelLoader.INSTANCE);
    }

    @SubscribeEvent
    public static void addPlayerLayer(EntityRenderersEvent.AddLayers event) {
        for (var model : event.getSkins()) {
            LivingEntityRenderer eventSkin = event.getSkin(model);
            eventSkin.addLayer(new InfinityArmorModel.PlayerRender((RenderLayerParent<Player, PlayerModel<Player>>) eventSkin));
        }
    }

    public static int getCurrentRainbowColor() {
        var hue = (System.currentTimeMillis() % 18000) / 18000F;
        return ColorUtils.HSBToRGB(hue, 1, 1);
    }

    private static void registerConfigScreen() {
        ModLoadingContext.get().registerExtensionPoint(ConfigScreenHandler.ConfigScreenFactory.class,
                () -> new ConfigScreenHandler.ConfigScreenFactory(
                        (mc, parent) -> new AvaritiaConfigScreen(parent)
                ));
    }
}
