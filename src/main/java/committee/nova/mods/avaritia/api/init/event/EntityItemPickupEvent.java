package committee.nova.mods.avaritia.api.init.event;

import io.github.fabricators_of_create.porting_lib.entity.events.PlayerEvents;
import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;

/**
 * EntityItemPickupEvent
 *
 * @author cnlimiter
 * @version 1.0
 * @description
 * @date 2024/5/24 上午2:53
 */
public class EntityItemPickupEvent extends PlayerEvents {
    public static Event<EntityItemPickup> ENTITY_ITEM_PICKUP = EventFactory.createArrayBacked(EntityItemPickup.class, callbacks -> ((event) -> {
        for (EntityItemPickup e : callbacks){
            return e.post(event);
        }
        return false;
    }));

    public interface EntityItemPickup {
        boolean post(EntityItemPickupEvent event);
    }

    private final ItemEntity item;

    public EntityItemPickupEvent(Player player, ItemEntity item) {
        super(player);
        this.item = item;
    }

    public ItemEntity getItem() {
        return item;
    }

    @Override
    public void sendEvent() {
        ENTITY_ITEM_PICKUP.invoker().post(this);
    }

    public boolean sendEvent2() {
        return ENTITY_ITEM_PICKUP.invoker().post(this);
    }
}
