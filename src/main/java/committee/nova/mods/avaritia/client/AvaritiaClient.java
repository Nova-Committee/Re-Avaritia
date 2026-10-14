package committee.nova.mods.avaritia.client;

import com.mojang.blaze3d.platform.InputConstants;
import committee.nova.mods.avaritia.Const;
import committee.nova.mods.avaritia.api.iface.IFilterItem;
import committee.nova.mods.avaritia.client.screen.AvaritiaConfigScreen;
import committee.nova.mods.avaritia.client.screen.ItemFilterScreen;
import committee.nova.mods.avaritia.common.net.C2SOpenRingPack;
import committee.nova.mods.avaritia.init.handler.NetworkHandler;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;

/** Client-wide key bindings, registered through the Fabric key-binding API (26.3 SDL3 input). */
public final class AvaritiaClient {
    public static final KeyMapping.Category KEY_CATEGORY = KeyMapping.Category.register(Const.rl("categories"));
    public static final KeyMapping CONFIG_KEY = new KeyMapping("key.avaritia.config", InputConstants.Type.KEYBOARD, InputConstants.KEY_O, KEY_CATEGORY);
    public static final KeyMapping FILTER_KEY = new KeyMapping("key.avaritia.filter", InputConstants.Type.KEYBOARD, InputConstants.KEY_H, KEY_CATEGORY);
    public static final KeyMapping RING_KEY = new KeyMapping("key.avaritia.neutron_ring", InputConstants.Type.KEYBOARD, InputConstants.KEY_N, KEY_CATEGORY);

    /**
     * 标记当前是否处于物品栏/GUI 内的实体渲染（如背包玩家模型预览）。
     * InfinityArmorModel 据此把 cosmic 盔甲的 scale 提到 100.0F 并锁定 yaw/pitch，
     * 避免 GUI 内渲染时把星空材质糊满全身。
     */
    public static boolean inventoryRender = false;

    private AvaritiaClient() {}

    public static void register() {
        KeyMappingHelper.registerKeyMapping(CONFIG_KEY);
        KeyMappingHelper.registerKeyMapping(FILTER_KEY);
        KeyMappingHelper.registerKeyMapping(RING_KEY);
        ClientTickEvents.END_CLIENT_TICK.register(minecraft -> {
            while (CONFIG_KEY.consumeClick()) {
                minecraft.gui.setScreen(new AvaritiaConfigScreen(minecraft.gui.screen()));
            }

            while (FILTER_KEY.consumeClick()) {
                if (minecraft.player == null) continue;
                if (minecraft.gui.screen() instanceof ItemFilterScreen) {
                    minecraft.gui.setScreen(null);
                } else if (minecraft.player.getMainHandItem().getItem() instanceof IFilterItem) {
                    minecraft.gui.setScreen(new ItemFilterScreen());
                }
            }

            while (RING_KEY.consumeClick()) {
                if (minecraft.player != null) {
                    NetworkHandler.CHANNEL.sendToServer(new C2SOpenRingPack());
                }
            }
        });
    }
}
