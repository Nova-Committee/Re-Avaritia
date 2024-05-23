package committee.nova.mods.avaritia.api.init.event;

import io.github.fabricators_of_create.porting_lib.entity.events.PlayerEvents;
import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
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
public class ArrowLooseEvent extends PlayerEvents {
    public static Event<Loose> ARROW_LOOSE = EventFactory.createArrayBacked(Loose.class, callbacks -> ((event) -> {
        for (Loose e : callbacks){
            return e.post(event);
        }
        return false;
    }));
    public interface Loose {
        boolean post(ArrowLooseEvent event);
    }

    private final ItemStack bow;
    private final Level level;
    private final boolean hasAmmo;
    private int charge;

    public ArrowLooseEvent(Player player, @NotNull ItemStack bow, Level level, int charge, boolean hasAmmo) {
        super(player);
        this.bow = bow;
        this.level = level;
        this.charge = charge;
        this.hasAmmo = hasAmmo;
    }

    @NotNull
    public ItemStack getBow() { return this.bow; }
    public Level getLevel() { return this.level; }
    public boolean hasAmmo() { return this.hasAmmo; }
    public int getCharge() { return this.charge; }
    public void setCharge(int charge) { this.charge = charge; }

    @Override
    public void sendEvent() {
        ARROW_LOOSE.invoker().post(this);
    }

    public boolean sendEvent2() {
       return ARROW_LOOSE.invoker().post(this);
    }
}
