package committee.nova.mods.avaritia.api.init.event;

import io.github.fabricators_of_create.porting_lib.entity.events.PlayerEvents;
import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

/**
 * ItemPickupEvent
 *
 * @author cnlimiter
 * @version 1.0
 * @description
 * @date 2024/5/24 上午3:05
 */
public class ItemPickupEvent extends PlayerEvents {
    public static Event<ItemPickupEvent.ItemPickup> HARVEST_CHECK = EventFactory.createArrayBacked(ItemPickupEvent.ItemPickup.class, callbacks -> ((event) -> {
        for (ItemPickupEvent.ItemPickup e : callbacks){
            e.post(event);
        }
    }));

    public interface ItemPickup {
        void post(ItemPickupEvent event);
    }

    private final ItemEntity originalEntity;
    private final ItemStack stack;

    public ItemPickupEvent(Player player, ItemEntity entPickedUp, ItemStack stack) {
        super(player);
        this.originalEntity = entPickedUp;
        this.stack = stack;
    }

    public ItemStack getStack() {
        return stack;
    }

    public ItemEntity getOriginalEntity() {
        return originalEntity;
    }

    @Override
    public void sendEvent() {
        HARVEST_CHECK.invoker().post(this);
    }

}
