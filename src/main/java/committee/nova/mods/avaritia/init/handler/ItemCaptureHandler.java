package committee.nova.mods.avaritia.init.handler;

import io.github.fabricators_of_create.porting_lib.entity.events.EntityEvents;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;

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
public class ItemCaptureHandler {
    private static boolean doItemCapture = false;
    private static final Set<ItemStack> capturedDrops = new LinkedHashSet<>();
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

    public static void init() {
        EntityEvents.ON_JOIN_WORLD.register(((entity, world, loadedFromDisk) -> {
            if (isItemCaptureEnabled()) {
                if (entity instanceof ItemEntity itemEntity) {
                    ItemStack stack = itemEntity.getItem();
                    capturedDrops.add(stack);
                    return false;
                }
            }
            return true;
        }));

    }
}
