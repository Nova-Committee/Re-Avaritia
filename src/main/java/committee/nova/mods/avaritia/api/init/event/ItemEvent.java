package committee.nova.mods.avaritia.api.init.event;

import io.github.fabricators_of_create.porting_lib.entity.events.EntityEvents;
import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.world.entity.item.ItemEntity;

/**
 * ItemEvent
 *
 * @author cnlimiter
 * @version 1.0
 * @description
 * @date 2024/5/24 上午3:09
 */
public class ItemEvent extends EntityEvents {

    public static Event<ItemEvent.Item> ITEM_EVENT = EventFactory.createArrayBacked(ItemEvent.Item.class, callbacks -> ((event) -> {
        for (ItemEvent.Item e : callbacks){
            e.post(event);
        }
    }));

    public static Event<ItemEvent.Expire> ITEM_EXPIRE = EventFactory.createArrayBacked(ItemEvent.Expire.class, callbacks -> ((event) -> {
        for (ItemEvent.Expire e : callbacks){
            return e.post(event);
        }
        return false;
    }));

    public interface Item {
        void post(ItemEvent event);
    }

    public interface Expire {
        boolean post(ItemExpireEvent event);
    }

    private final ItemEntity itemEntity;
    public ItemEvent(ItemEntity itemEntity) {
        super(itemEntity);
        this.itemEntity = itemEntity;
    }

    @Override
    public ItemEntity getEntity()
    {
        return itemEntity;
    }

    @Override
    public void sendEvent() {
        ITEM_EVENT.invoker().post(this);
    }

    public static class ItemExpireEvent extends ItemEvent
    {

        private int extraLife;

        public ItemExpireEvent(ItemEntity entityItem, int extraLife)
        {
            super(entityItem);
            this.setExtraLife(extraLife);
        }

        public int getExtraLife()
        {
            return extraLife;
        }

        public void setExtraLife(int extraLife)
        {
            this.extraLife = extraLife;
        }

        @Override
        public void sendEvent() {
            ITEM_EXPIRE.invoker().post(this);
        }

        public boolean sendEvent2() {
           return ITEM_EXPIRE.invoker().post(this);
        }
    }
}
