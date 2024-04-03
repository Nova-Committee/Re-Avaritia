package committee.nova.mods.avaritia.init.handler;

import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

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
@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.FORGE)
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
