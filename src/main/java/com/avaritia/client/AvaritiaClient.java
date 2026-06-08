package com.avaritia.client;

import com.avaritia.Const;
import com.avaritia.Res;
import com.avaritia.api.client.render.CosmicRenderQueue;
import com.avaritia.api.iface.IColored;
import com.avaritia.api.iface.IFilterItem;
import com.avaritia.client.model.entity.InfinityShieldModel;
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
import com.avaritia.client.render.item.InfinityChestItemRender;
import com.avaritia.client.render.item.InfinityShieldRender;
import com.avaritia.client.render.tile.AcceleratorDisplayRender;
import com.avaritia.client.render.tile.CompressedChestRenderer;
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
import com.avaritia.client.shader.AvaritiaShaders;
import com.avaritia.client.tint.RainbowTintSource;
import com.avaritia.common.entity.GapingVoidEntity;
import com.avaritia.common.net.C2SOpenRingPacket;
import com.avaritia.init.handler.NetworkHandler;
import com.avaritia.init.registry.ModEntityTypes;
import com.avaritia.init.registry.ModItems;
import com.avaritia.init.registry.ModMenus;
import com.avaritia.init.registry.ModParticles;
import com.avaritia.init.registry.ModTileEntities;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.ClientAvatarEntity;
import net.minecraft.client.entity.ClientMannequin;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.resources.model.EquipmentClientInfo;
import net.minecraft.client.renderer.entity.ItemEntityRenderer;
import net.minecraft.client.renderer.entity.ThrownItemRenderer;
import net.minecraft.client.renderer.entity.player.AvatarRenderer;
import net.minecraft.resources.Identifier;
import net.minecraft.util.context.ContextKey;
import net.minecraft.world.entity.Avatar;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.player.PlayerModelType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterColorHandlersEvent;
import net.neoforged.neoforge.client.event.RegisterGuiLayersEvent;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.client.event.RegisterItemModelsEvent;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;
import net.neoforged.neoforge.client.event.RegisterParticleProvidersEvent;
import net.neoforged.neoforge.client.event.RegisterRenderPipelinesEvent;
import net.neoforged.neoforge.client.event.RegisterSpecialModelRendererEvent;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import net.neoforged.neoforge.client.event.TextureAtlasStitchedEvent;
import net.neoforged.neoforge.client.extensions.common.IClientItemExtensions;
import net.neoforged.neoforge.client.extensions.common.RegisterClientExtensionsEvent;
import net.neoforged.neoforge.client.gui.VanillaGuiLayers;
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
    public static final ModelLayerLocation COMPRESSED_CHEST = new ModelLayerLocation(id("compressed_chest"), "main");
    public static final ModelLayerLocation COMPRESSED_CHEST_LEFT = new ModelLayerLocation(id("compressed_chest_left"), "main");
    public static final ModelLayerLocation COMPRESSED_CHEST_RIGHT = new ModelLayerLocation(id("compressed_chest_right"), "main");

    public static boolean inventoryRender = false;
    public static long lastTime = System.currentTimeMillis();
    public static int renderTime = 0;
    private static float darknessIntensity = 0.0f;
    private static final IClientItemExtensions INFINITY_ARMOR_EXTENSIONS = new IClientItemExtensions() {
        @Override
        public Identifier getArmorTexture(ItemStack stack, EquipmentClientInfo.LayerType type, EquipmentClientInfo.Layer layer, Identifier fallback) {
            return type == EquipmentClientInfo.LayerType.HUMANOID_LEGGINGS
                    ? Const.rl("textures/models/armor/infinity_armor_layer_2.png")
                    : Const.rl("textures/models/armor/infinity_armor_layer_1.png");
        }
    };

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
        event.registerBlockEntityRenderer(ModTileEntities.compressed_chest_tile.get(), CompressedChestRenderer::new);
    }

    @SubscribeEvent
    public static void registerEntityLayerDefinitions(EntityRenderersEvent.RegisterLayerDefinitions event) {
        event.registerLayerDefinition(COMPRESSED_CHEST, CompressedChestRenderer::createSingleBodyLayer);
        event.registerLayerDefinition(COMPRESSED_CHEST_LEFT, CompressedChestRenderer::createDoubleBodyLeftLayer);
        event.registerLayerDefinition(COMPRESSED_CHEST_RIGHT, CompressedChestRenderer::createDoubleBodyRightLayer);
        event.registerLayerDefinition(InfinityChestBlockRender.INFINITY_CHEST, InfinityChestBlockRender::createLayer);
        event.registerLayerDefinition(InfinityShieldRender.INFINITY_SHIELD, InfinityShieldModel::createLayer);
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
    public static void registerRenderPipelines(RegisterRenderPipelinesEvent event) {
        AvaritiaShaders.onRegisterShaders(event);
    }

    @SubscribeEvent
    public static void onTexturesSwitchPost(TextureAtlasStitchedEvent event) {
        if (!TextureAtlas.LOCATION_BLOCKS.equals(event.getAtlas().location())) {
            return;
        }

        for (int i = 0; i < AvaritiaShaders.COSMIC_SPRITES.length; i++) {
            AvaritiaShaders.COSMIC_SPRITES[i] = event.getAtlas().getSprite(Const.rl("misc/cosmic/cosmic_" + i));
            AvaritiaShaders.COSMIC_UVS[i * 4] = AvaritiaShaders.COSMIC_SPRITES[i].getU0();
            AvaritiaShaders.COSMIC_UVS[i * 4 + 1] = AvaritiaShaders.COSMIC_SPRITES[i].getV0();
            AvaritiaShaders.COSMIC_UVS[i * 4 + 2] = AvaritiaShaders.COSMIC_SPRITES[i].getU1();
            AvaritiaShaders.COSMIC_UVS[i * 4 + 3] = AvaritiaShaders.COSMIC_SPRITES[i].getV1();
        }
        for (int i = 0; i < AvaritiaShaders.ETERNAL_SPRITES.length; i++) {
            AvaritiaShaders.ETERNAL_SPRITES[i] = event.getAtlas().getSprite(Const.rl("misc/eternal/eternal_" + i));
            AvaritiaShaders.ETERNAL_UVS[i * 4] = AvaritiaShaders.ETERNAL_SPRITES[i].getU0();
            AvaritiaShaders.ETERNAL_UVS[i * 4 + 1] = AvaritiaShaders.ETERNAL_SPRITES[i].getV0();
            AvaritiaShaders.ETERNAL_UVS[i * 4 + 2] = AvaritiaShaders.ETERNAL_SPRITES[i].getU1();
            AvaritiaShaders.ETERNAL_UVS[i * 4 + 3] = AvaritiaShaders.ETERNAL_SPRITES[i].getV1();
        }
        Res.ARMOR_MASK = event.getAtlas().getSprite(Const.rl("mask/armor/infinity_armor_mask"));
        Res.ARMOR_MASK_INV = event.getAtlas().getSprite(Const.rl("mask/armor/infinity_armor_mask_inv"));
        Res.ARMOR_WING_MASK = event.getAtlas().getSprite(Const.rl("mask/armor/infinity_armor_mask_wings"));
    }

    @SubscribeEvent
    public static void registerGuiLayers(RegisterGuiLayersEvent event) {
        event.registerAbove(VanillaGuiLayers.EXPERIENCE_LEVEL, id("endest_pearl_darkness"), AvaritiaClient::renderDarknessOverlay);
    }

    @SubscribeEvent
    public static void registerClientExtensions(RegisterClientExtensionsEvent event) {
        event.registerItem(INFINITY_ARMOR_EXTENSIONS,
                ModItems.infinity_helmet.get(),
                ModItems.infinity_chestplate.get(),
                ModItems.infinity_pants.get(),
                ModItems.infinity_boots.get());
    }

    @SubscribeEvent
    public static void registerItemTintSources(RegisterColorHandlersEvent.ItemTintSources event) {
        event.register(id("item_color"), IColored.ItemColors.CODEC);
        event.register(id("item_block_color"), IColored.ItemBlockColors.CODEC);
        event.register(id("rainbow"), RainbowTintSource.CODEC);
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

        if (!minecraft.isPaused()) {
            ++renderTime;
        }
        if (minecraft.player != null && minecraft.level != null) {
            calculateDarknessIntensity(minecraft.player, minecraft.level);
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
    public static void registerSpecialModelRenderers(RegisterSpecialModelRendererEvent event) {
        event.register(id("infinity_shield"), InfinityShieldRender.Unbaked.MAP_CODEC);
        event.register(id("infinity_chest"), InfinityChestItemRender.Unbaked.MAP_CODEC);
    }

    @SubscribeEvent
    public static void registerParticleProviders(RegisterParticleProvidersEvent event) {
        event.registerSpriteSet(ModParticles.CHARGE.get(), ChargeParticle.Factory::new);
        event.registerSpriteSet(ModParticles.SHOCKWAVE_PARTICLE.get(), ShockwaveParticle.Provider::new);
    }

    @SubscribeEvent
    public static void onRenderLevel(RenderLevelStageEvent.AfterLevel event) {
        CosmicRenderQueue.renderAll();
    }

    private static void renderDarknessOverlay(GuiGraphicsExtractor guiGraphics, net.minecraft.client.DeltaTracker deltaTracker) {
        if (darknessIntensity > 0.01f) {
            int alpha = Math.min(255, (int) (darknessIntensity * 255));
            guiGraphics.fill(0, 0, guiGraphics.guiWidth(), guiGraphics.guiHeight(), alpha << 24);
        }
    }

    private static void calculateDarknessIntensity(Player player, Level level) {
        Vec3 playerPos = player.position();
        double maxDistance = 10.0;
        float maxIntensity = 0.0f;

        for (GapingVoidEntity pearl : level.getEntitiesOfClass(GapingVoidEntity.class, player.getBoundingBox().inflate(maxDistance))) {
            double distance = playerPos.distanceTo(pearl.position());
            if (distance < maxDistance) {
                float intensity = (float) Math.max(0.0, 1.0 - Math.max(0.0, (distance - 4.0) / 6.0));
                if (intensity > maxIntensity) {
                    maxIntensity = intensity;
                }
            }
        }

        if (maxIntensity > darknessIntensity) {
            darknessIntensity = Math.min(maxIntensity, darknessIntensity + 0.05f);
        } else if (maxIntensity < darknessIntensity) {
            darknessIntensity = Math.max(maxIntensity, darknessIntensity - 0.05f);
        }
    }

    private static Identifier id(String path) {
        return Identifier.fromNamespaceAndPath(Const.MOD_ID, path);
    }
}
