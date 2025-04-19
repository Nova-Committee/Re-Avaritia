package committee.nova.mods.avaritia.init.handler;

import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;

import java.util.LinkedHashSet;
import java.util.Set;

/**
 * ItemCaptureHandler
 *
 * @author cnlimiter
 * @version 1.0
 * @description
 * @date 2024/4/3 18:25
 */
@EventBusSubscriber
public class ItemCaptureHandler {
    private static final Set<ItemStack> capturedDrops = new LinkedHashSet<>();
    private static boolean doItemCapture = false;

    public static void enableItemCapture(boolean on) {
        doItemCapture = on;
    }

    public static boolean isItemCaptureEnabled() {
        return doItemCapture;
    }

    public static Set<ItemStack> getCapturedDrops() {
        Set<ItemStack> dropsCopy = new LinkedHashSet<>(capturedDrops);
        capturedDrops.clear();
        return dropsCopy;
    }

    @SubscribeEvent
    public static void onEntityJoinLevel(EntityJoinLevelEvent event) {
        if (isItemCaptureEnabled()) {
            if (event.getEntity() instanceof ItemEntity itemEntity) {
                ItemStack stack = itemEntity.getItem();
                capturedDrops.add(stack);
                event.setCanceled(true);
            }
        }
    }
}
