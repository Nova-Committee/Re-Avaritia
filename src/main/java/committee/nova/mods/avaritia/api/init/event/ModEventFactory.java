package committee.nova.mods.avaritia.api.init.event;

import io.github.fabricators_of_create.porting_lib.core.event.BaseEvent;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;

/**
 * ModEventFactory
 *
 * @author cnlimiter
 * @version 1.0
 * @description
 * @date 2024/5/24 上午2:46
 */
public class ModEventFactory {
    public static boolean doPlayerHarvestCheck(Player player, BlockState state, boolean success)
    {
        PlayerHarvestCheckEvent event = new PlayerHarvestCheckEvent(player, state, success);
        event.sendEvent();
        return event.canHarvest();
    }

    public static int onItemPickup(ItemEntity entityItem, Player player)
    {
        EntityItemPickupEvent event = new EntityItemPickupEvent(player, entityItem);
        if (event.sendEvent2()) return -1;
        return event.getResult() == BaseEvent.Result.ALLOW ? 1 : 0;
    }

    public static void firePlayerItemPickupEvent(Player player, ItemEntity item, ItemStack clone)
    {
        ItemPickupEvent event = new ItemPickupEvent(player, item, clone);
        event.sendEvent();
    }

    public static int onItemExpire(ItemEntity entity, @NotNull ItemStack item)
    {
        if (item.isEmpty()) return -1;
        ItemEvent.ItemExpireEvent event = new ItemEvent.ItemExpireEvent(entity, 6000);
        if (!event.sendEvent2()) return -1;
        return event.getExtraLife();
    }


    public static InteractionResultHolder<ItemStack> onArrowNock(ItemStack item, Level level, Player player, InteractionHand hand, boolean hasAmmo)
    {
        ArrowNockEvent event = new ArrowNockEvent(player, item, hand, level, hasAmmo);
        if (event.sendEvent2())
            return new InteractionResultHolder<ItemStack>(InteractionResult.FAIL, item);
        return event.getAction();
    }

    public static int onArrowLoose(ItemStack stack, Level level, Player player, int charge, boolean hasAmmo)
    {
        ArrowLooseEvent event = new ArrowLooseEvent(player, stack, level, charge, hasAmmo);
        if (event.sendEvent2())
            return -1;
        return event.getCharge();
    }
}
