package com.avaritia.client;

import com.avaritia.Const;
import com.avaritia.client.model.loader.base.AvaritiaItemModels;
import com.avaritia.client.particle.ChargeParticle;
import com.avaritia.client.particle.ShockwaveParticle;
import com.avaritia.client.render.entity.BladeSlashRender;
import com.avaritia.client.render.entity.BurningArrowRender;
import com.avaritia.client.render.entity.BurningBallRender;
import com.avaritia.client.render.entity.ExplosionsArrowRender;
import com.avaritia.client.render.entity.FireBallRender;
import com.avaritia.client.render.entity.GapingVoidRender;
import com.avaritia.client.render.entity.HeavenArrowRender;
import com.avaritia.client.render.entity.HeavenSubArrowRender;
import com.avaritia.client.render.entity.InfinityArmorRender;
import com.avaritia.client.render.entity.InfinityThrownTridentRender;
import com.avaritia.client.render.entity.NeutronArrowRender;
import com.avaritia.client.render.entity.RainProRender;
import com.avaritia.client.render.entity.StormProRender;
import com.avaritia.client.render.entity.SunProRender;
import com.avaritia.client.render.entity.TNTProEntityRender;
import com.avaritia.client.render.entity.TracerArrowRender;
import com.avaritia.client.render.tile.AcceleratorDisplayRender;
import com.avaritia.client.render.tile.CompressedChestRenderer;
import com.avaritia.client.render.tile.InfinitatoTileRender;
import com.avaritia.client.render.tile.InfinityChestBlockRender;
import com.avaritia.client.screen.AvaritiaConfigScreen;
import com.avaritia.client.screen.CompressedChestScreen;
import com.avaritia.client.screen.ExtremeAnvilScreen;
import com.avaritia.client.screen.ExtremeSmithingScreen;
import com.avaritia.client.screen.ItemFilterScreen;
import com.avaritia.client.screen.InfinityChestScreen;
import com.avaritia.client.screen.InfinityClockScreen;
import com.avaritia.client.screen.NeutronCollectorScreen;
import com.avaritia.client.screen.NeutronCompressorScreen;
import com.avaritia.client.screen.NeutronRingScreen;
import com.avaritia.client.screen.craft.EndCraftScreen;
import com.avaritia.client.screen.craft.ExtremeCraftScreen;
import com.avaritia.client.screen.craft.NetherCraftScreen;
import com.avaritia.client.screen.craft.SculkCraftScreen;
import com.avaritia.api.iface.IFilterItem;
import com.avaritia.common.net.C2SOpenRingPacket;
import com.avaritia.init.handler.NetworkHandler;
import com.avaritia.init.registry.ModEntityTypes;
import com.avaritia.init.registry.ModMenus;
import com.avaritia.init.registry.ModParticles;
import com.avaritia.init.registry.ModTileEntities;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.ClientAvatarEntity;
import net.minecraft.client.entity.ClientMannequin;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.entity.ItemEntityRenderer;
import net.minecraft.client.renderer.entity.ThrownItemRenderer;
import net.minecraft.client.renderer.entity.player.AvatarRenderer;
import net.minecraft.resources.Identifier;
import net.minecraft.util.context.ContextKey;
import net.minecraft.world.entity.Avatar;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.player.PlayerModelType;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.client.event.RegisterItemModelsEvent;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;
import net.neoforged.neoforge.client.event.RegisterParticleProvidersEvent;
import net.neoforged.neoforge.client.renderstate.AvatarRenderStateModifier;
import net.neoforged.neoforge.client.renderstate.RegisterRenderStateModifiersEvent;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import org.lwjgl.glfw.GLFW;

/**
 * Avaritia 客户端事件总线订阅类，集中注册实体渲染器、方块实体渲染器、界面、模型加载器和粒子提供器。
 */
@EventBusSubscriber(modid = Const.MOD_ID, value = Dist.CLIENT)
public class AvaritiaClient {
    public static final KeyMapping.Category KEY_CATEGORY = KeyMapping.Category.register(id("categories"));
    public static final KeyMapping CONFIG_KEY = new KeyMapping("key.avaritia.config", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_O, KEY_CATEGORY);
    public static final KeyMapping FILTER_KEY = new KeyMapping("key.avaritia.filter", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_H, KEY_CATEGORY);
    public static final KeyMapping RING_KEY = new KeyMapping("key.avaritia.neutron_ring", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_N, KEY_CATEGORY);
    public static final ContextKey<Boolean> INFINITY_ARMOR_FLYING = new ContextKey<>(id("infinity_armor_flying"));

    public static boolean inventoryRender = false;
    public static long lastTime = System.currentTimeMillis();

    @SubscribeEvent
    public static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(ModEntityTypes.IMMORTAL.get(), ItemEntityRenderer::new);
        event.registerEntityRenderer(ModEntityTypes.ENDER_PEARL.get(), ThrownItemRenderer::new);
        event.registerEntityRenderer(ModEntityTypes.GAPING_VOID.get(), GapingVoidRender::new);
        event.registerEntityRenderer(ModEntityTypes.HEAVEN_ARROW.get(), HeavenArrowRender::new);
        event.registerEntityRenderer(ModEntityTypes.NEUTRON_ARROW.get(), NeutronArrowRender::new);
        event.registerEntityRenderer(ModEntityTypes.HEAVEN_SUB_ARROW.get(), HeavenSubArrowRender::new);
        event.registerEntityRenderer(ModEntityTypes.EXPLOSIONS_ARROW.get(), ExplosionsArrowRender::new);
        event.registerEntityRenderer(ModEntityTypes.BURNING_ARROW.get(), BurningArrowRender::new);
        event.registerEntityRenderer(ModEntityTypes.BURNING_BALL.get(), BurningBallRender::new);
        event.registerEntityRenderer(ModEntityTypes.TRACE_ARROW.get(), TracerArrowRender::new);
        event.registerEntityRenderer(ModEntityTypes.FIRE_BALL.get(), FireBallRender::new);
        event.registerEntityRenderer(ModEntityTypes.BLADE_SLASH.get(), BladeSlashRender::new);
        event.registerEntityRenderer(ModEntityTypes.SUN_PRO.get(), SunProRender::new);
        event.registerEntityRenderer(ModEntityTypes.RAIN_PRO.get(), RainProRender::new);
        event.registerEntityRenderer(ModEntityTypes.STORM_PRO.get(), StormProRender::new);
        event.registerEntityRenderer(ModEntityTypes.ACCELERATOR_DISPLAY.get(), AcceleratorDisplayRender::new);
        event.registerEntityRenderer(ModEntityTypes.TNT_PRO.get(), TNTProEntityRender::new);
        event.registerEntityRenderer(ModEntityTypes.INFINITY_THROWN_TRIDENT.get(), InfinityThrownTridentRender::new);

        event.registerBlockEntityRenderer(ModTileEntities.INFINITY_CHEST_TILE.get(), InfinityChestBlockRender::new);
        event.registerBlockEntityRenderer(ModTileEntities.infinitato_tile.get(), InfinitatoTileRender::new);
        event.registerBlockEntityRenderer(ModTileEntities.compressed_chest_tile.get(), CompressedChestRenderer::new);
    }

    @SubscribeEvent
    public static void addEntityLayers(EntityRenderersEvent.AddLayers event) {
        for (PlayerModelType skin : event.getSkins()) {
            AvatarRenderer<AbstractClientPlayer> playerRenderer = event.getPlayerRenderer(skin);
            if (playerRenderer != null) {
                playerRenderer.addLayer(new InfinityArmorRender<>(playerRenderer, event.getEntityModels(), skin == PlayerModelType.SLIM));
            }
            AvatarRenderer<ClientMannequin> mannequinRenderer = event.getMannequinRenderer(skin);
            if (mannequinRenderer != null) {
                mannequinRenderer.addLayer(new InfinityArmorRender<>(mannequinRenderer, event.getEntityModels(), skin == PlayerModelType.SLIM));
            }
        }
    }

    @SubscribeEvent
    public static void registerRenderStateModifiers(RegisterRenderStateModifiersEvent event) {
        event.registerAvatarEntityModifier(new AvatarRenderStateModifier() {
            @Override
            public <T extends Avatar & ClientAvatarEntity> void accept(T avatar, AvatarRenderState renderState) {
                renderState.setRenderData(INFINITY_ARMOR_FLYING, avatar instanceof Player player && player.getAbilities().flying);
            }
        });
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
        event.register(ModMenus.GENERIC_9x27.get(), CompressedChestScreen::new);
    }

    @SubscribeEvent
    public static void registerKeyMappings(RegisterKeyMappingsEvent event) {
        event.register(CONFIG_KEY);
        event.register(FILTER_KEY);
        event.register(RING_KEY);
    }

    @SubscribeEvent
    public static void handleClientTick(ClientTickEvent.Post event) {
        Minecraft minecraft = Minecraft.getInstance();

        while (CONFIG_KEY.consumeClick()) {
            minecraft.setScreen(new AvaritiaConfigScreen(minecraft.screen));
        }

        while (FILTER_KEY.consumeClick()) {
            if (minecraft.player == null) continue;
            if (minecraft.screen instanceof ItemFilterScreen) {
                minecraft.setScreen(null);
            } else if (minecraft.player.getMainHandItem().getItem() instanceof IFilterItem) {
                minecraft.setScreen(new ItemFilterScreen());
            }
        }

        while (RING_KEY.consumeClick()) {
            if (minecraft.player != null) {
                NetworkHandler.sendToServer(new C2SOpenRingPacket());
            }
        }
    }

    @SubscribeEvent
    public static void registerItemModels(RegisterItemModelsEvent event) {
        event.register(id("cosmic"), AvaritiaItemModels.Cosmic.MAP_CODEC);
        event.register(id("cosmic_arc"), AvaritiaItemModels.CosmicArc.MAP_CODEC);
        event.register(id("hell"), AvaritiaItemModels.Hell.MAP_CODEC);
        event.register(id("eternal"), AvaritiaItemModels.Eternal.MAP_CODEC);
        event.register(id("unstable"), AvaritiaItemModels.Unstable.MAP_CODEC);
        event.register(id("halo"), AvaritiaItemModels.Halo.MAP_CODEC);
        event.register(id("halo_cosmic"), AvaritiaItemModels.HaloCosmic.MAP_CODEC);
        event.register(id("halo_eternal"), AvaritiaItemModels.HaloEternal.MAP_CODEC);
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
