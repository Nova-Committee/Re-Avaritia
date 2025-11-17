package committee.nova.mods.avaritia.client;

import committee.nova.mods.avaritia.Const;
import committee.nova.mods.avaritia.Res;
import committee.nova.mods.avaritia.api.client.util.ColorUtils;
import committee.nova.mods.avaritia.api.iface.IColored;
import committee.nova.mods.avaritia.client.model.CosmicModelLoader;
import committee.nova.mods.avaritia.client.model.HaloModelLoader;
import committee.nova.mods.avaritia.client.model.entity.InfinityArmorModel;
import committee.nova.mods.avaritia.client.model.entity.InfinityShieldModel;
import committee.nova.mods.avaritia.client.particle.ChargeParticle;
import committee.nova.mods.avaritia.client.particle.ShockwaveParticle;
import committee.nova.mods.avaritia.client.render.tile.CompressedChestRenderer;
import committee.nova.mods.avaritia.client.screen.AvaritiaConfigScreen;
import committee.nova.mods.avaritia.client.shader.AvaritiaShaders;
import committee.nova.mods.avaritia.init.registry.*;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.resources.PlayerSkin;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModList;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.*;
import net.neoforged.neoforge.client.extensions.common.IClientItemExtensions;
import net.neoforged.neoforge.client.extensions.common.RegisterClientExtensionsEvent;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;
import net.neoforged.neoforge.client.gui.VanillaGuiLayers;
import org.jetbrains.annotations.NotNull;


import static committee.nova.mods.avaritia.Const.LOGGER;
import static committee.nova.mods.avaritia.client.AvaritiaForgeClient.*;
import static committee.nova.mods.avaritia.client.shader.AvaritiaShaders.COSMIC_SPRITES;
import static committee.nova.mods.avaritia.client.shader.AvaritiaShaders.ETERNAL_SPRITES;

/**
 * Author cnlimiter
 * CreateTime 2023/6/17 23:24
 * Name AvaritiaClient
 * Description
 */

@EventBusSubscriber(modid = Const.MOD_ID, value = Dist.CLIENT)
public class AvaritiaModClient {
    public static final ModelLayerLocation COMPRESSED_CHEST = new ModelLayerLocation(Const.rl("compressed_chest"), "main");
    public static final ModelLayerLocation COMPRESSED_CHEST_LEFT = new ModelLayerLocation(Const.rl("compressed_chest_left"), "main");
    public static final ModelLayerLocation COMPRESSED_CHEST_RIGHT = new ModelLayerLocation(Const.rl("compressed_chest_right"), "main");
    public static final ModelLayerLocation INFINITY_CHEST = new ModelLayerLocation(Const.rl("infinity_chest"), "main");
    public static final ModelLayerLocation INFINITY_SHIELD = new ModelLayerLocation(Const.rl("infinity_shield"), "main");

    /**
     * 注册键绑定
     */
    @SubscribeEvent
    public static void registerKeyMappings(RegisterKeyMappingsEvent event) {
        LOGGER.debug("Registering key bindings");
        event.register(FILTER_KEY);
        event.register(RING_KEY);
        event.register(CONFIG_KEY);
    }

    @SubscribeEvent
    public static void clientSetUp(FMLClientSetupEvent event) {
        ModList.get().getModContainerById(Const.MOD_ID).orElseThrow().registerExtensionPoint(IConfigScreenFactory.class,
                (container, last) -> new AvaritiaConfigScreen(last));
        ModEntities.onClientSetup();
        ModTileEntities.onClientSetup();
        ModSearches.onClientSetup();
    }

    @SubscribeEvent
    public static void onRegisterMenuScreens(RegisterMenuScreensEvent event) {
        ModMenus.onClientSetup(event);
    }

    @SubscribeEvent
    public static void registerParticleProviders(RegisterParticleProvidersEvent event) {
        event.registerSpriteSet(ModParticles.CHARGE.get(), ChargeParticle.Factory::new);
        event.registerSpriteSet(ModParticles.SHOCKWAVE_PARTICLE.get(), ShockwaveParticle.Provider::new);
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onTexturesSwitchPost(TextureAtlasStitchedEvent event) {
        if (event.getAtlas().location().equals(InventoryMenu.BLOCK_ATLAS)) {
            for (int i = 0; i < COSMIC_SPRITES.length; i++) {
                COSMIC_SPRITES[i] = event.getAtlas().getSprite(Const.rl("misc/cosmic/cosmic_" + i));
                AvaritiaShaders.COSMIC_UVS[i * 4] = COSMIC_SPRITES[i].getU0();
                AvaritiaShaders.COSMIC_UVS[i * 4 + 1] = COSMIC_SPRITES[i].getV0();
                AvaritiaShaders.COSMIC_UVS[i * 4 + 2] = COSMIC_SPRITES[i].getU1();
                AvaritiaShaders.COSMIC_UVS[i * 4 + 3] = COSMIC_SPRITES[i].getV1();
            }
            for (int i = 0; i < ETERNAL_SPRITES.length; i++) {
                ETERNAL_SPRITES[i] = event.getAtlas().getSprite(Const.rl("misc/eternal/eternal_" + i));
                AvaritiaShaders.ETERNAL_UVS[i * 4] = ETERNAL_SPRITES[i].getU0();
                AvaritiaShaders.ETERNAL_UVS[i * 4 + 1] = ETERNAL_SPRITES[i].getV0();
                AvaritiaShaders.ETERNAL_UVS[i * 4 + 2] = ETERNAL_SPRITES[i].getU1();
                AvaritiaShaders.ETERNAL_UVS[i * 4 + 3] = ETERNAL_SPRITES[i].getV1();
            }
            Res.ARMOR_MASK = event.getAtlas().getSprite(Const.rl("mask/armor/infinity_armor_mask"));
            Res.ARMOR_MASK_INV = event.getAtlas().getSprite(Const.rl("mask/armor/infinity_armor_mask_inv"));
            Res.ARMOR_WING_MASK = event.getAtlas().getSprite(Const.rl("mask/armor/infinity_armor_mask_wings"));
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onRegisterShaders(RegisterShadersEvent event) {
        AvaritiaShaders.onRegisterShaders(event);//注册着色器
    }

    @SubscribeEvent
    public static void registerClientExtensions(RegisterClientExtensionsEvent event) {
        event.registerItem(new IClientItemExtensions() {
            @Override
            public @NotNull HumanoidModel<Player> getHumanoidArmorModel(@NotNull LivingEntity entityLiving, @NotNull ItemStack itemstack, @NotNull EquipmentSlot armorSlot, @NotNull HumanoidModel _deafult) {
                InfinityArmorModel model =
                        armorSlot == EquipmentSlot.LEGS
                                ? new InfinityArmorModel(InfinityArmorModel.createMesh(new CubeDeformation(1.0F), 0.0F, true).getRoot().bake(64, 64))
                                : new InfinityArmorModel(InfinityArmorModel.createMesh(new CubeDeformation(1.0F), 0.0F, false).getRoot().bake(64, 64));
                model.update(entityLiving, itemstack, armorSlot);
                return model;
            }
        }, ModItems.infinity_helmet, ModItems.infinity_chestplate, ModItems.infinity_pants, ModItems.infinity_boots);
        event.registerItem(new IClientItemExtensions() {}, ModBlocks.infinity_chest.asItem());
    }

    @SubscribeEvent
    public static void registerEntityLayerDefinitions(EntityRenderersEvent.RegisterLayerDefinitions event) {
        event.registerLayerDefinition(COMPRESSED_CHEST, CompressedChestRenderer::createSingleBodyLayer);
        event.registerLayerDefinition(COMPRESSED_CHEST_LEFT, CompressedChestRenderer::createDoubleBodyLeftLayer);
        event.registerLayerDefinition(COMPRESSED_CHEST_RIGHT, CompressedChestRenderer::createDoubleBodyRightLayer);
        event.registerLayerDefinition(INFINITY_CHEST, InfinityChestBlockRender::createLayer);
        event.registerLayerDefinition(INFINITY_SHIELD, InfinityShieldModel::createLayer);
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
    public static void registerOverlays(RegisterGuiLayersEvent event) {
        event.registerAbove(VanillaGuiLayers.EXPERIENCE_BAR, Const.rl("endest_pearl_darkness"), AvaritiaForgeClient.DARKNESS_OVERLAY);
    }

    @SubscribeEvent
    public static void registerLoaders(ModelEvent.RegisterGeometryLoaders event) {
        event.register(Const.rl("cosmic"), CosmicModelLoader.INSTANCE);
        event.register(Const.rl("halo"), HaloModelLoader.INSTANCE);
    }

    @SubscribeEvent
    public static void addPlayerLayer(EntityRenderersEvent.AddLayers event) {
        addLayer(event, "default");
        addLayer(event, "slim");
    }

    private static void addLayer(final EntityRenderersEvent.AddLayers e, final String s) {
        final LivingEntityRenderer entityRenderer = e.getSkin(PlayerSkin.Model.byName(s));
        entityRenderer.addLayer(new InfinityArmorModel.PlayerRender((RenderLayerParent<Player, PlayerModel<Player>>) entityRenderer));
    }

    public static int getCurrentRainbowColor() {
        var hue = (System.currentTimeMillis() % 18000) / 18000F;
        return ColorUtils.HSBToRGB(hue, 1, 1);
    }
}
