package committee.nova.mods.avaritia.client;

import committee.nova.mods.avaritia.Const;
import committee.nova.mods.avaritia.api.client.util.ColorUtils;
import committee.nova.mods.avaritia.api.iface.IColored;
import committee.nova.mods.avaritia.client.model.CosmicModelLoader;
import committee.nova.mods.avaritia.client.model.HaloModelLoader;
import committee.nova.mods.avaritia.client.model.InfinityArmorModel;
import committee.nova.mods.avaritia.client.render.tile.CompressedChestRenderer;
import committee.nova.mods.avaritia.client.screen.AvaritiaConfigScreen;
import committee.nova.mods.avaritia.client.shader.AvaritiaShaders;
import committee.nova.mods.avaritia.init.registry.*;
import dev.emi.emi.screen.ConfigScreen;
import net.minecraft.client.gui.screens.Screen;
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
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.ModList;
import net.neoforged.fml.ModLoadingContext;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.*;
import net.neoforged.neoforge.client.extensions.common.IClientItemExtensions;
import net.neoforged.neoforge.client.extensions.common.RegisterClientExtensionsEvent;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;
import org.jetbrains.annotations.NotNull;


import static committee.nova.mods.avaritia.Const.LOGGER;
import static committee.nova.mods.avaritia.client.AvaritiaForgeClient.FILTER_KEY;
import static committee.nova.mods.avaritia.client.AvaritiaForgeClient.RING_KEY;
import static net.neoforged.fml.common.EventBusSubscriber.Bus.MOD;

/**
 * Author cnlimiter
 * CreateTime 2023/6/17 23:24
 * Name AvaritiaClient
 * Description
 */

@EventBusSubscriber(modid = Const.MOD_ID, value = Dist.CLIENT, bus = MOD)
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
        ModEntities.onClientSetup();
        ModTileEntities.onClientSetup();
        ModSearches.onClientSetup();
        ModList.get().getModContainerById(Const.MOD_ID).orElseThrow().registerExtensionPoint(IConfigScreenFactory.class,
                (container, last) -> new AvaritiaConfigScreen(last));
    }

    @SubscribeEvent
    public static void onRegisterMenuScreens(RegisterMenuScreensEvent event) {
        ModMenus.onClientSetup(event);
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
