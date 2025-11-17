package committee.nova.mods.avaritia.client;

import com.mojang.blaze3d.platform.InputConstants;
import committee.nova.mods.avaritia.Const;
import committee.nova.mods.avaritia.api.iface.IFilterItem;
import committee.nova.mods.avaritia.client.screen.AvaritiaConfigScreen;
import committee.nova.mods.avaritia.client.screen.ItemFilterScreen;
import committee.nova.mods.avaritia.common.entity.GapingVoidEntity;
import committee.nova.mods.avaritia.common.item.singularity.SingularityItem;
import committee.nova.mods.avaritia.init.config.ModConfig;
import committee.nova.mods.avaritia.init.registry.ModItems;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.LayeredDraw;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.component.TypedDataComponent;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.loading.FMLLoader;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RenderFrameEvent;
import net.neoforged.neoforge.client.event.ScreenEvent;
import net.neoforged.neoforge.event.entity.player.ItemTooltipEvent;

import java.util.Collections;
import java.util.List;
import java.util.TreeSet;

/**
 * Author cnlimiter
 * CreateTime 2023/6/17 23:24
 * Name AvaritiaForgeClient
 * Description
 */

@EventBusSubscriber(modid = Const.MOD_ID, value = Dist.CLIENT)
public class AvaritiaForgeClient {
    private static final String CATEGORIES = "key.avaritia.categories";
    public static long lastTime = System.currentTimeMillis();
    public static int renderTime = 0;
    public static float renderFrame = 0;
    public static boolean inventoryRender = false;
    private static float darknessIntensity = 0.0f;
    private static boolean keepFlying = false;

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
    public static void onClientTickPost(ClientTickEvent.Post event) {
        Minecraft mc = Minecraft.getInstance();
        Player player = mc.player;
        Level level = mc.level;
        if (player == null || level == null) return;

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
        // 检查玩家是否装备了无限鞘翅
        if (!(player.getItemBySlot(EquipmentSlot.CHEST).getItem() == ModItems.infinity_elytra.get())) {
            keepFlying = false;
            return;
        }

        boolean isFlying = player.isFallFlying();

        // 如果按下跳跃键，则停止飞行
        if (mc.options.keyJump.isDown()) {
            keepFlying = false;
            return;
        }

        // 开始记录飞行状态
        if (isFlying && !keepFlying) {
            keepFlying = true;
        }

        // 处理着陆逻辑：当玩家着陆时造成范围伤害
        if (keepFlying && player.onGround()) {
            keepFlying = false;

            double radius = 2.5;
            List<LivingEntity> nearby = player.level().getEntitiesOfClass(
                    LivingEntity.class,
                    player.getBoundingBox().inflate(radius),
                    e -> e != player // 不伤害自己
            );

            for (LivingEntity target : nearby) {
                target.hurt(player.damageSources().fellOutOfWorld(), 6.0F);
            }
            return;
        }

        // 维持飞行状态并控制飞行速度
        if (keepFlying) {
            if (!player.isFallFlying()) {
                player.startFallFlying();
            }

            Vec3 look = player.getLookAngle().normalize();
            double FLY_SPEED = ModConfig.infinityElytraFlyingSpeed.get();
            player.setDeltaMovement(look.x * FLY_SPEED, look.y * FLY_SPEED, look.z * FLY_SPEED);
        }
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


    @SubscribeEvent(priority = EventPriority.LOW)
    public static void onItemTooltip(final ItemTooltipEvent e){
        if (!FMLLoader.isProduction() || ModConfig.useAdvanceTooltips.get()) {
            var stack = e.getItemStack();
            var tooltips = e.getToolTip();
            if (Screen.hasAltDown()) {
                for (TypedDataComponent<?> c : stack.getComponents()) {
                    if (c.value() instanceof CompoundTag tag) {
                        addTagCompound("  ", tooltips, tag);
                    }
                }
            }
        }
    }

    private static void addTagCompound(String prefix, List<Component> list, CompoundTag tag) {
        TreeSet<String> sortedKeys = new TreeSet<>(tag.getAllKeys());
        for (String key: sortedKeys) {
            Tag elem=tag.get(key);
            switch(elem.getId()) {
                case Tag.TAG_SHORT -> list.add(Component.literal(prefix+key+": §2"+tag.getShort(key)));
                case Tag.TAG_INT -> list.add(Component.literal(prefix+key+": §3"+tag.getInt(key)));
                case Tag.TAG_DOUBLE -> list.add(Component.literal(prefix+key+": §6"+tag.getDouble(key)));
                case Tag.TAG_STRING -> list.add(Component.literal(prefix+key+": §8"+tag.getString(key)));
                case Tag.TAG_BYTE -> list.add(Component.literal(prefix+key+": §9"+tag.getByte(key)));
                case Tag.TAG_LIST -> {
                    list.add(Component.literal(prefix+key+": §9List, "+((ListTag)elem).size()+" items"));
                    if (Screen.hasShiftDown()) {
                        for (Tag key1 : (ListTag)elem) {
                            addTagCompound(prefix+"    ", list, (CompoundTag)key1);
                        }
                    }
                }
                case Tag.TAG_COMPOUND -> {
                    list.add(Component.literal(prefix+key+": §aCompound"));
                    if (Screen.hasShiftDown()) {
                        addTagCompound(prefix + "    ", list, (CompoundTag)elem);
                    }
                }
                default -> list.add(Component.literal(prefix + key + ": Type "+ elem.getType()));
            }
        }
    }
    /**
     * 渲染黑暗遮罩
     *
     * @param guiGraphics GUI图形对象
     * @param intensity   黑暗强度
     */
    private static void renderDarknessOverlay(GuiGraphics guiGraphics, float intensity) {
        // 使用纯黑色渲染一个覆盖整个屏幕的矩形，透明度由intensity决定
        int alpha = (int) (intensity * 255);
        if (alpha > 255) alpha = 255;
        guiGraphics.fill(0, 0, guiGraphics.guiWidth(), guiGraphics.guiHeight(), (alpha << 24) | 0x000000);
    }

    public static final LayeredDraw.Layer DARKNESS_OVERLAY = (guiGraphics, deltaTracker) -> {
        if (darknessIntensity > 0.01f) {
            renderDarknessOverlay(guiGraphics, darknessIntensity);
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


        for (GapingVoidEntity pearl : level.getEntitiesOfClass(GapingVoidEntity.class, player.getBoundingBox().inflate(maxDistance))) {
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

    private static void singularityIconTimer(){
        if (renderTime % 20 != 0) return;
        if (SingularityItem.enabledSingularities != null && !SingularityItem.enabledSingularities.isEmpty()) {
            SingularityItem.currentSingularityIndex.set((SingularityItem.currentSingularityIndex.get() + 1) % SingularityItem.enabledSingularities.size());
        }
    }

    @SubscribeEvent
    public static void onRenderTickStart(RenderFrameEvent.Pre event) {
        if (!Minecraft.getInstance().isPaused()) {
            renderFrame = event.getPartialTick().getGameTimeDeltaTicks();
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
