package committee.nova.mods.avaritia.client;

import committee.nova.mods.avaritia.Const;
import committee.nova.mods.avaritia.Res;
import committee.nova.mods.avaritia.api.client.render.CosmicRenderQueue;
import committee.nova.mods.avaritia.api.client.screen.component.UiInspector;
import committee.nova.mods.avaritia.api.iface.IColored;
import committee.nova.mods.avaritia.client.model.entity.InfinityShieldModel;
import committee.nova.mods.avaritia.client.model.loader.AvaritiaItemModelLoaders;
import committee.nova.mods.avaritia.client.particle.ChargeParticle;
import committee.nova.mods.avaritia.client.particle.ShockwaveParticle;
import committee.nova.mods.avaritia.client.render.NeutronRingPreviewRenderer;
import committee.nova.mods.avaritia.client.render.entity.*;
import committee.nova.mods.avaritia.client.render.item.InfinityChestItemRender;
import committee.nova.mods.avaritia.client.render.item.InfinityShieldRender;
import committee.nova.mods.avaritia.client.render.tile.*;
import committee.nova.mods.avaritia.client.screen.*;
import committee.nova.mods.avaritia.client.screen.craft.*;
import committee.nova.mods.avaritia.client.shader.AvaritiaShaders;
import committee.nova.mods.avaritia.client.tint.RainbowTintSource;
import committee.nova.mods.avaritia.common.net.ClientPacketProxy;
import committee.nova.mods.avaritia.common.net.client.NetworkClientPackets;
import committee.nova.mods.avaritia.init.registry.*;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.particle.v1.ParticleProviderRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.level.LevelRenderEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.client.color.item.ItemTintSources;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.renderer.entity.*;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderers;
import net.minecraft.client.renderer.item.ItemModels;
import net.minecraft.client.renderer.special.SpecialModelRenderers;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.world.inventory.InventoryMenu;

import java.util.Map;

/** Loader-only client bootstrap. Vanilla lifecycle hooks perform instance-bound setup. */
public final class AvaritiaModClient implements ClientModInitializer {
    public static final ModelLayerLocation COMPRESSED_CHEST = new ModelLayerLocation(Const.rl("compressed_chest"), "main");
    public static final ModelLayerLocation COMPRESSED_CHEST_LEFT = new ModelLayerLocation(Const.rl("compressed_chest_left"), "main");
    public static final ModelLayerLocation COMPRESSED_CHEST_RIGHT = new ModelLayerLocation(Const.rl("compressed_chest_right"), "main");
    public static final ModelLayerLocation INFINITY_CHEST = new ModelLayerLocation(Const.rl("infinity_chest"), "main");
    public static final ModelLayerLocation INFINITY_SHIELD = new ModelLayerLocation(Const.rl("infinity_shield"), "main");
    public static final ModelLayerLocation INFINITY_TRIDENT = new ModelLayerLocation(Const.rl("infinity_trident"), "main");

    @Override public void onInitializeClient() {
        NetworkClientPackets.initialize();
        ClientPacketProxy.infinityRingOpen = InfinityRingControlScreen::open;
        ClientPacketProxy.updateDimensions = InfinityRingControlScreen::applyKnownLevels;
        ClientPacketProxy.neutronRingOpen = NeutronRingManageScreen::open;
        ClientPacketProxy.neutronRingPreview = NeutronRingManageScreen::acceptPreview;
        committee.nova.mods.avaritia.init.handler.ItemOverrideHandler.init();
        AvaritiaShaders.register();
        // 粒子供应商：26.3 ParticleResources 的注册接口为私有，改经 Fabric ParticleProviderRegistry。
        ParticleProviderRegistry.getInstance().register(ModParticles.CHARGE.get(), ChargeParticle.Factory::new);
        ParticleProviderRegistry.getInstance().register(ModParticles.SHOCKWAVE_PARTICLE.get(), ShockwaveParticle.Provider::new);
        // 按键（SDL3 InputConstants）与自注册调试/预览组件。
        AvaritiaClient.register();
        UiInspector.register();
        NeutronRingPreviewRenderer.register();
        // 自定义 item model type（对应 datagen 输出的 items/*.json type 字段）。
        ItemModels.ID_MAPPER.put(Const.rl("cosmic"), AvaritiaItemModelLoaders.Cosmic.MAP_CODEC);
        ItemModels.ID_MAPPER.put(Const.rl("cosmic_arc"), AvaritiaItemModelLoaders.CosmicArc.MAP_CODEC);
        ItemModels.ID_MAPPER.put(Const.rl("hell"), AvaritiaItemModelLoaders.Hell.MAP_CODEC);
        ItemModels.ID_MAPPER.put(Const.rl("eternal"), AvaritiaItemModelLoaders.Eternal.MAP_CODEC);
        ItemModels.ID_MAPPER.put(Const.rl("unstable"), AvaritiaItemModelLoaders.Unstable.MAP_CODEC);
        ItemModels.ID_MAPPER.put(Const.rl("halo"), AvaritiaItemModelLoaders.Halo.MAP_CODEC);
        ItemModels.ID_MAPPER.put(Const.rl("halo_cosmic"), AvaritiaItemModelLoaders.HaloCosmic.MAP_CODEC);
        ItemModels.ID_MAPPER.put(Const.rl("halo_eternal"), AvaritiaItemModelLoaders.HaloEternal.MAP_CODEC);
        // 自定义 item tint source。
        ItemTintSources.ID_MAPPER.put(Const.rl("item_color"), IColored.ItemColors.CODEC);
        ItemTintSources.ID_MAPPER.put(Const.rl("item_block_color"), IColored.ItemBlockColors.CODEC);
        ItemTintSources.ID_MAPPER.put(Const.rl("rainbow"), RainbowTintSource.CODEC);
        // 自定义 special model renderer。
        SpecialModelRenderers.ID_MAPPER.put(Const.rl("infinity_shield"), InfinityShieldRender.Unbaked.MAP_CODEC);
        SpecialModelRenderers.ID_MAPPER.put(Const.rl("infinity_chest"), InfinityChestItemRender.Unbaked.MAP_CODEC);
        // 世界提交阶段的宇宙渲染队列（Neo 版对应 SubmitCustomGeometryEvent）。
        LevelRenderEvents.COLLECT_SUBMITS.register(context -> CosmicRenderQueue.renderAll(context.submitNodeCollector()));
        MenuScreens.register(ModMenus.sculk_crafting_tile_table.get(), SculkCraftScreen::new);
        MenuScreens.register(ModMenus.nether_crafting_tile_table.get(), NetherCraftScreen::new);
        MenuScreens.register(ModMenus.end_crafting_tile_table.get(), EndCraftScreen::new);
        MenuScreens.register(ModMenus.extreme_crafting_table.get(), ExtremeCraftScreen::new);
        MenuScreens.register(ModMenus.neutron_collector.get(), NeutronCollectorScreen::new);
        MenuScreens.register(ModMenus.compressor.get(), NeutronCompressorScreen::new);
        MenuScreens.register(ModMenus.GENERIC_9x27.get(), CompressedChestScreen::new);
        MenuScreens.register(ModMenus.tesseract.get(), TesseractScreen::new);
        MenuScreens.register(ModMenus.tesseract_channel.get(), TesseractChannelScreen::new);
        MenuScreens.register(ModMenus.extreme_smithing_table.get(), ExtremeSmithingScreen::new);
        MenuScreens.register(ModMenus.extreme_anvil.get(), ExtremeAnvilScreen::new);
        MenuScreens.register(ModMenus.infinity_clock_menu.get(), InfinityClockScreen::new);
        MenuScreens.register(ModMenus.infinity_chest.get(), InfinityChestScreen::new);
        MenuScreens.register(ModMenus.infinity_bucket.get(), InfinityBucketScreen::new);
        EntityRenderers.register(ModEntities.IMMORTAL.get(), ItemEntityRenderer::new);
        EntityRenderers.register(ModEntities.ENDER_PEARL.get(), ThrownItemRenderer::new);
        EntityRenderers.register(ModEntities.GAPING_VOID.get(), GapingVoidRender::new);
        EntityRenderers.register(ModEntities.HEAVEN_ARROW.get(), HeavenArrowRender::new);
        EntityRenderers.register(ModEntities.NEUTRON_ARROW.get(), NeutronArrowRender::new);
        EntityRenderers.register(ModEntities.HEAVEN_SUB_ARROW.get(), HeavenSubArrowRender::new);
        EntityRenderers.register(ModEntities.EXPLOSIONS_ARROW.get(), ExplosionsArrowRender::new);
        EntityRenderers.register(ModEntities.BURNING_ARROW.get(), BurningArrowRender::new);
        EntityRenderers.register(ModEntities.BURNING_BALL.get(), BurningBallRender::new);
        EntityRenderers.register(ModEntities.TRACE_ARROW.get(), TracerArrowRender::new);
        EntityRenderers.register(ModEntities.FIRE_BALL.get(), FireBallRender::new);
        EntityRenderers.register(ModEntities.BLADE_SLASH.get(), BladeSlashRender::new);
        EntityRenderers.register(ModEntities.SUN_PRO.get(), SunProRender::new);
        EntityRenderers.register(ModEntities.RAIN_PRO.get(), RainProRender::new);
        EntityRenderers.register(ModEntities.STORM_PRO.get(), StormProRender::new);
        EntityRenderers.register(ModEntities.ACCELERATOR_DISPLAY_ENTITY.get(), AcceleratorDisplayRender::new);
        EntityRenderers.register(ModEntities.TNT_PRO_ENTITY.get(), TNTProEntityRender::new);
        EntityRenderers.register(ModEntities.INFINITY_THROWN_TRIDENT.get(), InfinityThrownTridentRender::new);
        BlockEntityRenderers.register(ModTileEntities.compressed_chest_tile.get(), CompressedChestRenderer::new);
        BlockEntityRenderers.register(ModTileEntities.tesseract_tile.get(), TesseractRender::new);
        BlockEntityRenderers.register(ModTileEntities.infinity_chest_tile.get(), InfinityChestBlockRender::new);
    }

    public static void finishClientSetup(Minecraft minecraft) {
        ModSearches.onClientSetup();
    }

    public static void addLayers(Map<ModelLayerLocation, LayerDefinition> roots) {
        roots.put(COMPRESSED_CHEST, CompressedChestRenderer.createSingleBodyLayer());
        roots.put(COMPRESSED_CHEST_LEFT, CompressedChestRenderer.createDoubleBodyLeftLayer());
        roots.put(COMPRESSED_CHEST_RIGHT, CompressedChestRenderer.createDoubleBodyRightLayer());
        roots.put(INFINITY_CHEST, InfinityChestBlockRender.createLayer());
        roots.put(INFINITY_SHIELD, InfinityShieldModel.createLayer());
    }

    public static void texturesUploaded(TextureAtlas atlas) {
        if (!atlas.location().equals(TextureAtlas.LOCATION_BLOCKS)) return;
        for (int i = 0; i < 10; i++) {
            var cosmic = atlas.getSprite(Const.rl("misc/cosmic/cosmic_" + i));
            AvaritiaShaders.COSMIC_SPRITES[i] = cosmic;
            int j = i * 4;
            AvaritiaShaders.COSMIC_UVS[j] = cosmic.getU0();
            AvaritiaShaders.COSMIC_UVS[j + 1] = cosmic.getV0();
            AvaritiaShaders.COSMIC_UVS[j + 2] = cosmic.getU1();
            AvaritiaShaders.COSMIC_UVS[j + 3] = cosmic.getV1();
            var eternal = atlas.getSprite(Const.rl("misc/eternal/eternal_" + i));
            AvaritiaShaders.ETERNAL_SPRITES[i] = eternal;
            AvaritiaShaders.ETERNAL_UVS[j] = eternal.getU0();
            AvaritiaShaders.ETERNAL_UVS[j + 1] = eternal.getV0();
            AvaritiaShaders.ETERNAL_UVS[j + 2] = eternal.getU1();
            AvaritiaShaders.ETERNAL_UVS[j + 3] = eternal.getV1();
        }
        Res.ARMOR_MASK = atlas.getSprite(Const.rl("mask/armor/infinity_armor_mask"));
        Res.ARMOR_MASK_INV = atlas.getSprite(Const.rl("mask/armor/infinity_armor_mask_inv"));
        Res.ARMOR_WING_MASK = atlas.getSprite(Const.rl("mask/armor/infinity_armor_mask_wings"));
    }
}
