package com.avaritia.client;

import com.avaritia.Avaritia;
import com.avaritia.Const;
import com.mojang.blaze3d.platform.InputConstants;
import com.avaritia.client.model.loader.CosmicArcModelLoader;
import com.avaritia.client.model.loader.CosmicModelLoader;
import com.avaritia.client.model.loader.EternalModelLoader;
import com.avaritia.client.model.loader.HaloCosmicModelLoader;
import com.avaritia.client.model.loader.HaloEternalModelLoader;
import com.avaritia.client.model.loader.HaloModelLoader;
import com.avaritia.client.model.loader.HellModelLoader;
import com.avaritia.client.model.loader.UnstableModelLoader;
import com.avaritia.client.particle.ChargeParticle;
import com.avaritia.client.particle.ShockwaveParticle;
import com.avaritia.client.render.entity.BurningBallRender;
import com.avaritia.client.render.entity.GapingVoidRender;
import com.avaritia.client.render.entity.HeavenArrowRender;
import com.avaritia.client.render.entity.HeavenSubArrowRender;
import com.avaritia.client.render.entity.InfinityArmorRender;
import com.avaritia.client.render.entity.InfinityThrownTridentRender;
import com.avaritia.client.render.entity.TNTProEntityRender;
import com.avaritia.client.render.entity.TracerArrowRender;
import com.avaritia.client.render.tile.AcceleratorDisplayRender;
import com.avaritia.client.render.tile.CompressedChestRenderer;
import com.avaritia.client.render.tile.InfinitatoTileRender;
import com.avaritia.client.render.tile.InfinityChestBlockRender;
import com.avaritia.client.screen.CompressedChestScreen;
import com.avaritia.client.screen.ExtremeAnvilScreen;
import com.avaritia.client.screen.ExtremeSmithingScreen;
import com.avaritia.client.screen.InfinityChestScreen;
import com.avaritia.client.screen.InfinityClockScreen;
import com.avaritia.client.screen.ItemFilterScreen;
import com.avaritia.client.screen.NeutronCollectorScreen;
import com.avaritia.client.screen.NeutronCompressorScreen;
import com.avaritia.client.screen.NeutronRingScreen;
import com.avaritia.client.screen.craft.EndCraftScreen;
import com.avaritia.client.screen.craft.ExtremeCraftScreen;
import com.avaritia.client.screen.craft.NetherCraftScreen;
import com.avaritia.client.screen.craft.SculkCraftScreen;
import com.avaritia.init.registry.ModEntityTypes;
import com.avaritia.init.registry.ModMenus;
import com.avaritia.init.registry.ModParticles;
import com.avaritia.init.registry.ModTileEntities;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.ItemEntityRenderer;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.ThrownItemRenderer;
import net.minecraft.client.renderer.entity.player.AvatarRenderer;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.PlayerModelType;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.ModelEvent;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;
import net.neoforged.neoforge.client.event.RegisterParticleProvidersEvent;

/**
 * Avaritia 客户端事件总线订阅类，集中注册实体渲染器、方块实体渲染器、界面、模型加载器和粒子提供器。
 */
@EventBusSubscriber(modid = Const.MOD_ID, value = Dist.CLIENT)
public class AvaritiaClient {
    public static final KeyMapping RING_KEY = new KeyMapping("key.avaritia.neutron_ring", InputConstants.Type.KEYSYM, org.lwjgl.glfw.GLFW.GLFW_KEY_R, "key.avaritia.categories");

    public static boolean inventoryRender = false;
    public static long lastTime = System.currentTimeMillis();

    @SubscribeEvent
    public static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(ModEntityTypes.IMMORTAL.get(), ItemEntityRenderer::new);
        event.registerEntityRenderer(ModEntityTypes.ENDER_PEARL.get(), ThrownItemRenderer::new);
        event.registerEntityRenderer(ModEntityTypes.GAPING_VOID.get(), GapingVoidRender::new);
        event.registerEntityRenderer(ModEntityTypes.HEAVEN_ARROW.get(), HeavenArrowRender::new);
        event.registerEntityRenderer(ModEntityTypes.HEAVEN_SUB_ARROW.get(), HeavenSubArrowRender::new);
        event.registerEntityRenderer(ModEntityTypes.BURNING_BALL.get(), BurningBallRender::new);
        event.registerEntityRenderer(ModEntityTypes.TRACE_ARROW.get(), TracerArrowRender::new);
        event.registerEntityRenderer(ModEntityTypes.ACCELERATOR_DISPLAY.get(), AcceleratorDisplayRender::new);
        event.registerEntityRenderer(ModEntityTypes.TNT_PRO.get(), TNTProEntityRender::new);
        event.registerEntityRenderer(ModEntityTypes.INFINITY_THROWN_TRIDENT.get(), InfinityThrownTridentRender::new);

        event.registerBlockEntityRenderer(ModTileEntities.INFINITY_CHEST_TILE.get(), InfinityChestBlockRender::new);
        event.registerBlockEntityRenderer(ModTileEntities.infinitato_tile.get(), InfinitatoTileRender::new);
        event.registerBlockEntityRenderer(ModTileEntities.accelerator_display_tile.get(), AcceleratorDisplayRender::new);
        event.registerBlockEntityRenderer(ModTileEntities.compressed_chest_tile.get(), CompressedChestRenderer::new);
    }

    @SubscribeEvent
    public static void addEntityLayers(EntityRenderersEvent.AddLayers event) {
        EntityRenderer<?> renderer = event.getRenderer(EntityType.PLAYER);
        if (renderer instanceof AvatarRenderer<?> playerRenderer && playerRenderer.getModel() instanceof HumanoidModel<?>) {
            playerRenderer.addLayer(new InfinityArmorRender<>(playerRenderer, event.getEntityModels(), false));
        }

        for (PlayerModelType skin : event.getSkins()) {
            LivingEntityRenderer<?, ?> skinRenderer = event.getPlayerRenderer(skin);
            if (skinRenderer != null) {
                skinRenderer.addLayer(new InfinityArmorRender<>(skinRenderer, event.getEntityModels(), skin == PlayerModelType.SLIM));
            }
        }
    }

    @SubscribeEvent
    public static void registerScreens(RegisterMenuScreensEvent event) {
        event.register(ModMenus.extreme_crafting_table.get(), ExtremeCraftScreen::new);
        event.register(ModMenus.nether_crafting_tile_table.get(), NetherCraftScreen::new);
        event.register(ModMenus.end_crafting_tile_table.get(), EndCraftScreen::new);
        event.register(ModMenus.sculk_crafting_tile_table.get(), SculkCraftScreen::new);
        event.register(ModMenus.neutron_collector.get(), NeutronCollectorScreen::new);
        event.register(ModMenus.neutron_compressor.get(), NeutronCompressorScreen::new);
        event.register(ModMenus.neutron_ring.get(), NeutronRingScreen::new);
        event.register(ModMenus.extreme_smithing_table.get(), ExtremeSmithingScreen::new);
        event.register(ModMenus.extreme_anvil.get(), ExtremeAnvilScreen::new);
        event.register(ModMenus.infinity_chest.get(), InfinityChestScreen::new);
        event.register(ModMenus.infinity_clock_menu.get(), InfinityClockScreen::new);
        event.register(ModMenus.item_filter.get(), ItemFilterScreen::new);
        event.register(ModMenus.GENERIC_9x27.get(), CompressedChestScreen::new);
    }

    @SubscribeEvent
    public static void registerModelLoaders(ModelEvent.RegisterLoaders event) {
        event.register(id("cosmic"), CosmicModelLoader.INSTANCE);
        event.register(id("cosmic_arc"), CosmicArcModelLoader.INSTANCE);
        event.register(id("hell"), HellModelLoader.INSTANCE);
        event.register(id("eternal"), EternalModelLoader.INSTANCE);
        event.register(id("unstable"), UnstableModelLoader.INSTANCE);
        event.register(id("halo"), HaloModelLoader.INSTANCE);
        event.register(id("halo_cosmic"), HaloCosmicModelLoader.INSTANCE);
        event.register(id("halo_eternal"), HaloEternalModelLoader.INSTANCE);
    }

    @SubscribeEvent
    public static void registerParticleProviders(RegisterParticleProvidersEvent event) {
        event.registerSpriteSet(ModParticles.CHARGE.get(), ChargeParticle.Factory::new);
        event.registerSpriteSet(ModParticles.SHOCKWAVE_PARTICLE.get(), ShockwaveParticle.Provider::new);
    }

    private static Identifier id(String path) {
        return Identifier.fromNamespaceAndPath(Const.MOD_ID, path);
    }
}
