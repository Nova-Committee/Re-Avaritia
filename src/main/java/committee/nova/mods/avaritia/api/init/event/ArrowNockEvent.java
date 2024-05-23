package committee.nova.mods.avaritia.api.init.event;

import io.github.fabricators_of_create.porting_lib.entity.events.PlayerEvents;
import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

/**
 * ArrowNockEvent
 *
 * @author cnlimiter
 * @version 1.0
 * @description
 * @date 2024/5/24 上午3:47
 */
public class ArrowNockEvent extends PlayerEvents {
    public static Event<ArrowNockEvent.Nock> ARROW_NOCK = EventFactory.createArrayBacked(ArrowNockEvent.Nock.class, callbacks -> ((event) -> {
        for (ArrowNockEvent.Nock e : callbacks){
            return e.post(event);
        }
        return false;
    }));
    public interface Nock {
        boolean post(ArrowNockEvent event);
    }

    private final ItemStack bow;
    private final InteractionHand hand;
    private final Level level;
    private final boolean hasAmmo;
    private InteractionResultHolder<ItemStack> action;

    public ArrowNockEvent(Player player, @NotNull ItemStack item, InteractionHand hand, Level level, boolean hasAmmo) {
        super(player);
        this.bow = item;
        this.hand = hand;
        this.level = level;
        this.hasAmmo = hasAmmo;
    }

    @NotNull
    public ItemStack getBow() { return this.bow; }
    public Level getLevel() { return this.level; }
    public InteractionHand getHand() { return this.hand; }
    public boolean hasAmmo() { return this.hasAmmo; }
    public InteractionResultHolder<ItemStack> getAction()
    {
        return this.action;
    }

    public void setAction(InteractionResultHolder<ItemStack> action)
    {
        this.action = action;
    }

    @Override
    public void sendEvent() {
        ARROW_NOCK.invoker().post(this);
    }

    public boolean sendEvent2() {
       return ARROW_NOCK.invoker().post(this);
    }
}
