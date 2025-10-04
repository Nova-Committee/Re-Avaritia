package committee.nova.mods.avaritia.client;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.platform.Window;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import committee.nova.mods.avaritia.Const;
import committee.nova.mods.avaritia.api.iface.IFilterItem;
import committee.nova.mods.avaritia.client.screen.ItemFilterScreen;
import committee.nova.mods.avaritia.common.entity.GapingVoidEntity;
import committee.nova.mods.avaritia.common.net.C2SElytraSpeedUpPacket;
import committee.nova.mods.avaritia.common.net.C2SOpenRingPack;
import committee.nova.mods.avaritia.init.config.ModConfig;
import committee.nova.mods.avaritia.init.registry.ModItems;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.component.TypedDataComponent;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.loading.FMLLoader;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RenderGuiEvent;
import net.neoforged.neoforge.event.entity.player.ItemTooltipEvent;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.Collections;
import java.util.List;
import java.util.TreeSet;

/**
 * Author cnlimiter
 * CreateTime 2023/6/17 23:24
 * Name AvaritiaForgeClient
 * Description
 */

@EventBusSubscriber(modid = Const.MOD_ID, value = Dist.CLIENT, bus = EventBusSubscriber.Bus.GAME)
public class AvaritiaForgeClient {
    private static final String CATEGORIES = "key.avaritia.categories";
    private static boolean keepFlying = false;
    // 定义按键绑定
    public static final KeyMapping FILTER_KEY = new KeyMapping("key.avaritia.filter",
            InputConstants.KEY_H, CATEGORIES);
    public static final KeyMapping RING_KEY = new KeyMapping("key.avaritia.neutron_ring", InputConstants.KEY_N, CATEGORIES);

    public static final KeyMapping SORT_0 = new KeyMapping("key.avaritia.infinity_chest.sort0", InputConstants.KEY_0, CATEGORIES);
    public static final KeyMapping SORT_1 = new KeyMapping("key.avaritia.infinity_chest.sort1", InputConstants.KEY_1, CATEGORIES);
    public static final KeyMapping SORT_2 = new KeyMapping("key.avaritia.infinity_chest.sort2", InputConstants.KEY_2, CATEGORIES);
    public static final KeyMapping SORT_3 = new KeyMapping("key.avaritia.infinity_chest.sort3", InputConstants.KEY_3, CATEGORIES);
    public static final KeyMapping SORT_4 = new KeyMapping("key.avaritia.infinity_chest.sort4", InputConstants.KEY_4, CATEGORIES);
    public static final KeyMapping SORT_5 = new KeyMapping("key.avaritia.infinity_chest.sort5", InputConstants.KEY_5, CATEGORIES);
    public static final KeyMapping SORT_6 = new KeyMapping("key.avaritia.infinity_chest.sort6", InputConstants.KEY_6, CATEGORIES);
    public static final KeyMapping SORT_7 = new KeyMapping("key.avaritia.infinity_chest.sort7", InputConstants.KEY_7, CATEGORIES);
    public static final KeyMapping SORT_8 = new KeyMapping("key.avaritia.infinity_chest.sort8", InputConstants.KEY_8, CATEGORIES);
    public static final KeyMapping SORT_9 = new KeyMapping("key.avaritia.infinity_chest.sort9", InputConstants.KEY_9, CATEGORIES);

    private static int infinityElytraCooldown = 0;

    /**
     * 在客户端Tick事件触发时执行
     *
     * @param event 客户端Tick事件
     */
    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event) {


        Minecraft mc = Minecraft.getInstance();
        LocalPlayer player = Minecraft.getInstance().player;
        // 检测并消费点击事件
        while (FILTER_KEY.consumeClick() && player != null) {
            // 打开界面
            if (!player.getMainHandItem().isEmpty() && player.getMainHandItem().getItem() instanceof IFilterItem) {
                Minecraft.getInstance().setScreen(new ItemFilterScreen());
            }
        }
        while (RING_KEY.consumeClick() && player != null) {
            PacketDistributor.sendToServer(new C2SOpenRingPack());
        }

        handleInfinityElytraFallFlying(mc, player);
    }

    public static void handleInfinityElytraFallFlying(Minecraft mc, Player player) {
        if (player == null) return;

        if (!player.getItemBySlot(EquipmentSlot.CHEST).is(ModItems.infinity_elytra.get())) {
            keepFlying = false;
            return;
        }

        boolean isFlying = player.isFallFlying();

        if (mc.options.keyJump.isDown()) {
            keepFlying = false;
            return;
        }

        if (isFlying && !keepFlying) {
            keepFlying = true;
        }

        if (keepFlying && player.onGround()) {
            keepFlying = false;

            double radius = 2.5;
            List<LivingEntity> nearby = player.level().getEntitiesOfClass(
                    LivingEntity.class,
                    player.getBoundingBox().inflate(radius),
                    e -> e != player && !e.isInvulnerable()
            );

            for (LivingEntity target : nearby) {
                target.hurt(player.damageSources().fellOutOfWorld(), 6.0F);
            }
            return;
        }

        if (keepFlying) {
            if (player.isFallFlying()) {
                Vec3 look = player.getLookAngle().normalize();
                double FLY_SPEED = ModConfig.infinityElytraFlyingSpeed.get();
                player.setDeltaMovement(look.x * FLY_SPEED, look.y * FLY_SPEED, look.z * FLY_SPEED);
            }
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
//终望珍珠渲染
    @SubscribeEvent
    public static void onRenderOverlay(RenderGuiEvent.Pre event) {
        Minecraft mc = Minecraft.getInstance();
        Player player = mc.player;
        if (player == null || mc.level == null) return;

        final double START_DISTANCE = 6.0;  // 开始变黑的距离
        final double FULL_DISTANCE = 4.0;   // 完全黑暗的距离

        List<GapingVoidEntity> voids = mc.level.getEntitiesOfClass(GapingVoidEntity.class,
                player.getBoundingBox().inflate(START_DISTANCE + 10));

        float maxDarkness = 0.0f;
        for (GapingVoidEntity gap : voids) {
            double distance = player.distanceTo(gap);

            if (distance <= START_DISTANCE) {

                float darkness = (float) ((START_DISTANCE - distance) / (START_DISTANCE - FULL_DISTANCE));
                darkness = Math.max(0.0f, Math.min(1.0f, darkness));
                maxDarkness = Math.max(maxDarkness, darkness);
            }
        }

        if (maxDarkness > 0) {
            GuiGraphics guiGraphics = event.getGuiGraphics();
            Window window = mc.getWindow();

            int screenWidth = window.getGuiScaledWidth();
            int screenHeight = window.getGuiScaledHeight();

            int alpha = (int) (maxDarkness * 255);
            if (alpha > 255) alpha = 255;
            guiGraphics.fill(0, 0, screenWidth, screenHeight, (alpha << 24));
        }
    }
}
