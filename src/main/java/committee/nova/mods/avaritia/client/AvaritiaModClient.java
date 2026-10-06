package committee.nova.mods.avaritia.client;

import committee.nova.mods.avaritia.Const;
import committee.nova.mods.avaritia.Res;
import committee.nova.mods.avaritia.api.client.util.ColorUtils;
import committee.nova.mods.avaritia.api.iface.IColored;
import committee.nova.mods.avaritia.client.model.entity.InfinityShieldModel;
import committee.nova.mods.avaritia.client.render.entity.*;
import committee.nova.mods.avaritia.client.render.tile.*;
import committee.nova.mods.avaritia.client.screen.*;
import committee.nova.mods.avaritia.client.screen.craft.*;
import committee.nova.mods.avaritia.common.net.ClientPacketProxy;
import committee.nova.mods.avaritia.common.net.client.NetworkClientPackets;
import committee.nova.mods.avaritia.client.shader.AvaritiaShaders;
import committee.nova.mods.avaritia.init.registry.*;
import net.fabricmc.api.ClientModInitializer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.renderer.entity.*;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderers;
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
        ClientPacketProxy.updateDimensions = InfinityRingClient::applyDimensions;
        ClientPacketProxy.neutronRingOpen = NeutronRingManageScreen::open;
        ClientPacketProxy.neutronRingPreview = NeutronRingManageScreen::acceptPreview;
        committee.nova.mods.avaritia.init.handler.ItemOverrideHandler.init();
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
        BlockEntityRenderers.register(ModTileEntities.compressed_chest_tile.get(), CompressedChestRender::new);
        BlockEntityRenderers.register(ModTileEntities.tesseract_tile.get(), TesseractRender::new);
        BlockEntityRenderers.register(ModTileEntities.infinity_chest_tile.get(), InfinityChestBlockRender::new);
    }

    public static void finishClientSetup(Minecraft minecraft) {
        minecraft.itemColors.register(new IColored.ItemColors(), ModItems.singularity.get());
        minecraft.itemColors.register((stack, tint) -> getCurrentRainbowColor(), ModItems.eternal_singularity.get());
        ModSearches.onClientSetup();
    }

    public static void addLayers(Map<ModelLayerLocation, LayerDefinition> roots) {
        roots.put(COMPRESSED_CHEST, CompressedChestRender.createSingleBodyLayer());
        roots.put(COMPRESSED_CHEST_LEFT, CompressedChestRender.createDoubleBodyLeftLayer());
        roots.put(COMPRESSED_CHEST_RIGHT, CompressedChestRender.createDoubleBodyRightLayer());
        roots.put(INFINITY_CHEST, InfinityChestBlockRender.createLayer());
        roots.put(INFINITY_SHIELD, InfinityShieldModel.createLayer());
    }

    public static void texturesUploaded(TextureAtlas atlas) {
        if (!atlas.location().equals(InventoryMenu.BLOCK_ATLAS)) return;
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
        committee.nova.mods.avaritia.client.render.NeutronSpacePreviewRenderer.invalidate();
        committee.nova.mods.avaritia.api.client.model.bakedmodels.WrappedItemModel.clearMaskCache();
    }

    public static int getCurrentRainbowColor() {
        return ColorUtils.HSBToRGB((System.currentTimeMillis() % 18000) / 18000F, 1, 1);
    }
}
