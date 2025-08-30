package committee.nova.mods.avaritia.client;

import com.mojang.blaze3d.platform.InputConstants;
import committee.nova.mods.avaritia.Const;
import committee.nova.mods.avaritia.init.config.ModConfig;
import committee.nova.mods.avaritia.init.registry.ModItems;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
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

    private static boolean keepFlying = false;
    private static final double FLY_SPEED = 1.5;
    /**
     * 在客户端Tick事件触发时执行
     *
     * @param event 客户端Tick事件
     */
    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;

        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.level == null) return;

        Player player = mc.player;


        ItemStack chest = player.getItemBySlot(EquipmentSlot.CHEST);
        boolean wearingInfiniteElytra = (chest != null && chest.getItem() == ModItems.infinity_elytra.get());

        if (!wearingInfiniteElytra) {
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
                    e -> e != player // 不伤害自己
            );

            for (LivingEntity target : nearby) {
                target.hurt(player.damageSources().fellOutOfWorld(), 6.0F);
            }

            return;
        }


        if (keepFlying) {
            if (!player.isFallFlying()) {
                player.startFallFlying();
            }


            Vec3 look = player.getLookAngle().normalize();
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
                CompoundTag tag=stack.getTag();
                if (tag != null) {
                    addTagCompound("  ", tooltips, tag);
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
}
