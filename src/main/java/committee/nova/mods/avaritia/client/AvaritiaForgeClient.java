package committee.nova.mods.avaritia.client;

import com.mojang.blaze3d.platform.InputConstants;
import committee.nova.mods.avaritia.Const;
import committee.nova.mods.avaritia.api.client.screen.ItemFilterScreen;
import committee.nova.mods.avaritia.api.iface.IFilterItem;
import committee.nova.mods.avaritia.client.screen.AvaritiaConfigScreen;
import committee.nova.mods.avaritia.common.entity.GapingVoidEntity;
import committee.nova.mods.avaritia.common.item.singularity.SingularityItem;
import committee.nova.mods.avaritia.common.net.C2SElytraSpeedUpPacket;
import committee.nova.mods.avaritia.init.config.ModConfig;
import committee.nova.mods.avaritia.init.handler.NetworkHandler;
import committee.nova.mods.avaritia.init.registry.ModItems;
import committee.nova.mods.avaritia.util.ToolUtils;
import net.minecraft.client.Camera;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NumericTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.FogType;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderBlockScreenEffectEvent;
import net.minecraftforge.client.event.ScreenEvent;
import net.minecraftforge.client.event.ViewportEvent;
import net.minecraftforge.client.gui.overlay.IGuiOverlay;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.loading.FMLLoader;

import java.util.Collections;
import java.util.List;
import java.util.TreeSet;

/**
 * Author cnlimiter
 * CreateTime 2023/6/17 23:24
 * Name AvaritiaForgeClient
 * Description
 */
@Mod.EventBusSubscriber(modid = Const.MOD_ID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class AvaritiaForgeClient {
    private static final String CATEGORIES = "key.avaritia.categories";
    public static long lastTime = System.currentTimeMillis();
    public static int renderTime = 0;
    public static float renderFrame = 0;
    public static boolean inventoryRender = false;
    private static float darknessIntensity = 0.0f;
    private static final int INFINITY_ELYTRA_BOOST_PACKET_INTERVAL = 1;
    private static boolean infinityElytraLastFlyingIntent = false;
    private static boolean infinityElytraLastBoosting = false;
    private static boolean infinityElytraLastJumpDown = false;
    private static boolean infinityElytraLastOnGround = true;
    private static boolean infinityElytraLastFallFlying = false;
    private static boolean infinityElytraCancelSent = false;
    private static int infinityElytraPacketCooldown = 0;

    // region 定义按键绑定
    public static final KeyMapping FILTER_KEY = new KeyMapping("key.avaritia.filter", InputConstants.KEY_H, CATEGORIES);
    public static final KeyMapping RING_KEY = new KeyMapping("key.avaritia.neutron_ring", InputConstants.KEY_N, CATEGORIES);
    public static final KeyMapping CONFIG_KEY = new KeyMapping("key.avaritia.config", InputConstants.KEY_O, CATEGORIES);
    // endregion

    /**
     * 在客户端Tick事件触发时执行
     *
     * @param event 客户端Tick事件
     */
    @SubscribeEvent
    public static void onClientTickEnd(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;

        Minecraft mc = Minecraft.getInstance();
        Player player = mc.player;
        Level level = mc.level;
        if (player == null || level == null) {
            resetInfinityElytraControls();
            return;
        }

        while (CONFIG_KEY.consumeClick()) {
            mc.setScreen(new AvaritiaConfigScreen(mc.screen));
        }

        // region filter 过滤界面
        while (FILTER_KEY.consumeClick()) {
            if (!player.getMainHandItem().isEmpty() && player.getMainHandItem().getItem() instanceof IFilterItem) {
                Minecraft.getInstance().setScreen(new ItemFilterScreen());
            }
        }
        // endregion

        if (!Minecraft.getInstance().isPaused()) {
            ++renderTime;
        }

        handleInfinityElytraFallFlying(mc, player);

        //计算黑暗强度
        calculateDarknessIntensity(player, level);

        singularityIconTimer();
    }

    /**
     * 处理无限鞘翅飞行逻辑
     *
     * @param mc     Minecraft客户端实例
     * @param player 当前玩家对象
     */
    public static void handleInfinityElytraFallFlying(Minecraft mc, Player player) {
        if (player == null || mc.level == null || mc.screen != null) {
            resetInfinityElytraControls();
            return;
        }

        if (!player.getItemBySlot(EquipmentSlot.CHEST).is(ModItems.infinity_elytra.get())) {
            resetInfinityElytraControls();
            return;
        }

        boolean onGround = player.onGround();
        boolean wasOnGround = infinityElytraLastOnGround;
        boolean wasFallFlying = infinityElytraLastFallFlying;
        boolean jumpDown = mc.options.keyJump.isDown();
        boolean jumpPressed = jumpDown && !infinityElytraLastJumpDown;
        infinityElytraLastJumpDown = jumpDown;
        infinityElytraLastOnGround = onGround;
        infinityElytraLastFallFlying = player.isFallFlying();

        if (player.isFallFlying() && ((jumpPressed && wasFallFlying) || onGround || player.horizontalCollision)) {
            sendInfinityElytraCancel();
            return;
        }

        infinityElytraCancelSent = false;

        boolean canRequestGlide = !player.isPassenger()
                && !player.isInWater()
                && !player.onClimbable()
                && !player.getAbilities().flying;
        boolean wantsLaunch = canRequestGlide
                && !player.isFallFlying()
                && !onGround
                && !wasOnGround
                && jumpPressed;
        boolean wantsBoost = player.isFallFlying() || wantsLaunch;

        syncInfinityElytraControls(wantsLaunch || wantsBoost, wantsBoost);
    }

    private static void syncInfinityElytraControls(boolean flyingIntent, boolean boosting) {
        if (!flyingIntent) {
            resetInfinityElytraPacketState();
            return;
        }

        boolean changed = flyingIntent != infinityElytraLastFlyingIntent || boosting != infinityElytraLastBoosting;
        if (changed || infinityElytraPacketCooldown <= 0) {
            NetworkHandler.CHANNEL.sendToServer(new C2SElytraSpeedUpPacket(flyingIntent, boosting));
            infinityElytraLastFlyingIntent = flyingIntent;
            infinityElytraLastBoosting = boosting;
            infinityElytraPacketCooldown = INFINITY_ELYTRA_BOOST_PACKET_INTERVAL;
        } else {
            infinityElytraPacketCooldown--;
        }
    }

    private static void sendInfinityElytraCancel() {
        if (!infinityElytraCancelSent) {
            NetworkHandler.CHANNEL.sendToServer(new C2SElytraSpeedUpPacket(false, false));
            infinityElytraCancelSent = true;
        }
        resetInfinityElytraPacketState();
    }

    private static void resetInfinityElytraControls() {
        resetInfinityElytraPacketState();
        infinityElytraLastJumpDown = false;
        infinityElytraLastOnGround = true;
        infinityElytraLastFallFlying = false;
        infinityElytraCancelSent = false;
    }

    private static void resetInfinityElytraPacketState() {
        infinityElytraLastFlyingIntent = false;
        infinityElytraLastBoosting = false;
        infinityElytraPacketCooldown = 0;
    }

    // region tooltipExt
    private static Component[] tooltipExt = new Component[0];

    public static void setTooltip(Component... string) {
        tooltipExt = string;
    }

    @SubscribeEvent
    public static void getTooltip(ItemTooltipEvent evt) {
        Collections.addAll(evt.getToolTip(), tooltipExt);
    }
    // endregion

    //无尽套免疫视觉效果
    @SubscribeEvent
    public static void onRenderOverlay(RenderBlockScreenEffectEvent event) {
        var player = Minecraft.getInstance().player;
        if (player != null && ToolUtils.isInfinite(player) && event.getOverlayType() == RenderBlockScreenEffectEvent.OverlayType.FIRE) {
            event.setCanceled(true);
        } else if (player != null && ToolUtils.isInfinite(player) && event.getOverlayType() == RenderBlockScreenEffectEvent.OverlayType.BLOCK) {
            event.setCanceled(true);
        } else if (player != null && ToolUtils.isInfinite(player) && event.getOverlayType() == RenderBlockScreenEffectEvent.OverlayType.WATER) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public static void onFog(ViewportEvent.RenderFog event) {
        Camera camera = event.getCamera();
        Entity entity = camera.getEntity();

        if (!(entity instanceof Player player)) return;
        if (!ToolUtils.isWearingInfinityHelmet(player)) return;

        FogType fogType = camera.getFluidInCamera();
        if (fogType == FogType.LAVA || fogType == FogType.POWDER_SNOW) {
            float farPlane = event.getRenderer().getRenderDistance();

            event.setNearPlaneDistance(-8.0f);
            event.setFarPlaneDistance(Math.min(96.0f, farPlane));
            event.setCanceled(true);
        }
    }

    @SubscribeEvent(priority = EventPriority.LOW)
    public static void onItemTooltip(final ItemTooltipEvent e) {
        if (!FMLLoader.isProduction() || ModConfig.useAdvanceTooltips.get()) {
            var stack = e.getItemStack();
            var tooltips = e.getToolTip();
            if (Screen.hasAltDown()) {
                CompoundTag tag = stack.getTag();
                if (tag != null) {
                    addTagCompound("  ", tooltips, tag);
                }
            }
        }
    }

    private static void addTagCompound(String prefix, List<Component> list, CompoundTag tag) {
        TreeSet<String> sortedKeys = new TreeSet<>(tag.getAllKeys());
        for (String key : sortedKeys) {
            addTag(prefix, key, list, tag.get(key));
        }
    }

    private static void addTag(String prefix, String key, List<Component> list, Tag tag) {
        switch (tag.getId()) {
            case Tag.TAG_SHORT -> list.add(Component.literal(prefix + key + ": §2" + ((NumericTag) tag).getAsShort()));
            case Tag.TAG_INT -> list.add(Component.literal(prefix + key + ": §3" + ((NumericTag) tag).getAsInt()));
            case Tag.TAG_DOUBLE -> list.add(Component.literal(prefix + key + ": §6" + ((NumericTag) tag).getAsDouble()));
            case Tag.TAG_BYTE -> list.add(Component.literal(prefix + key + ": §9" + ((NumericTag) tag).getAsByte()));
            case Tag.TAG_STRING -> list.add(Component.literal(prefix + key + ": §8" + tag.getAsString()));
            case Tag.TAG_LIST -> {
                list.add(Component.literal(prefix + key + ": §9List, " + ((ListTag) tag).size() + " items"));
                if (Screen.hasShiftDown()) {
                    for (Tag key1 : (ListTag) tag) {
                        addTag(prefix, "    ", list, key1);
                    }
                }
            }
            case Tag.TAG_COMPOUND -> {
                list.add(Component.literal(prefix + key + ": §aCompound"));
                if (Screen.hasShiftDown()) {
                    addTagCompound(prefix + "    ", list, (CompoundTag) tag);
                }
            }
            default -> list.add(Component.literal(prefix + key + ": Type " + tag.getType()));
        }
    }

    /**
     * 渲染黑暗遮罩
     *
     * @param guiGraphics GUI图形对象
     * @param width       屏幕宽度
     * @param height      屏幕高度
     * @param intensity   黑暗强度
     */
    private static void renderDarknessOverlay(GuiGraphics guiGraphics, int width, int height, float intensity) {
        // 使用纯黑色渲染一个覆盖整个屏幕的矩形，透明度由intensity决定
        int alpha = (int) (intensity * 255);
        if (alpha > 255) alpha = 255;
        guiGraphics.fill(0, 0, width, height, (alpha << 24) | 0x000000);
    }

    public static final IGuiOverlay DARKNESS_OVERLAY = (gui, guiGraphics, partialTick, screenWidth, screenHeight) -> {
        if (darknessIntensity > 0.01f) {
            renderDarknessOverlay(guiGraphics, screenWidth, screenHeight, darknessIntensity);
        }
    };

    /**
     * 计算玩家附近终望珍珠的黑暗强度
     *
     * @param player 玩家
     * @param level  世界
     */
    private static void calculateDarknessIntensity(Player player, Level level) {
        Vec3 playerPos = player.position();
        double maxDistance = 10.0;  //渲染最大距离
        float maxIntensity = 0.0f;

        for (GapingVoidEntity pearl : level.getEntitiesOfClass(GapingVoidEntity.class, player.getBoundingBox().inflate(maxDistance), Entity::isAlive)) {//疑似对非GapingVoidEntity进行操作
            double distance = playerPos.distanceTo(pearl.position());
            if (distance < maxDistance) {

                //(distance-x)/y,x是完全黑屏的距离,y则是maxDistance-x
                float intensity = (float) Math.max(0.0, 1.0 - Math.max(0.0, (distance - 4.0) / 6.0));
                if (intensity > maxIntensity) {
                    maxIntensity = intensity;
                }
            }
        }

        // 平滑过渡效果
        if (maxIntensity > darknessIntensity) {
            darknessIntensity = Math.min(maxIntensity, darknessIntensity + 0.05f); // 渐强
        } else if (maxIntensity < darknessIntensity) {
            darknessIntensity = Math.max(maxIntensity, darknessIntensity - 0.05f); // 渐弱
        }
    }

    private static void singularityIconTimer() {
        if (renderTime % 20 != 0) return;
        if (SingularityItem.enabledSingularities != null && !SingularityItem.enabledSingularities.isEmpty()) {
            SingularityItem.currentSingularityIndex.set((SingularityItem.currentSingularityIndex.get() + 1) % SingularityItem.enabledSingularities.size());
        }
    }

    @SubscribeEvent
    public static void onRenderTickStart(TickEvent.RenderTickEvent event) {
        if (!Minecraft.getInstance().isPaused() && event.phase == TickEvent.Phase.START) {
            renderFrame = event.renderTickTime;
        }
    }

    @SubscribeEvent
    public static void drawScreenPre(final ScreenEvent.Render.Pre e) {
        inventoryRender = true;
    }

    @SubscribeEvent
    public static void drawScreenPost(final ScreenEvent.Render.Post e) {
        inventoryRender = false;
    }
}
