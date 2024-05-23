package committee.nova.mods.avaritia.api.init.event;

import io.github.fabricators_of_create.porting_lib.entity.events.PlayerEvents;
import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.state.BlockState;

/**
 * PlayerHarvestCheckCallback
 *
 * @author cnlimiter
 * @version 1.0
 * @description
 * @date 2024/5/24 上午2:38
 */
public class PlayerHarvestCheckEvent extends PlayerEvents {
    public static Event<HarvestCheck> HARVEST_CHECK = EventFactory.createArrayBacked(HarvestCheck.class, callbacks -> ((event) -> {
        for(HarvestCheck e : callbacks)
            e.check(event);
    }));

    public interface HarvestCheck {
        void check(PlayerHarvestCheckEvent event);
    }
    private final BlockState state;
    private boolean success;
    public PlayerHarvestCheckEvent(Player player, BlockState state, boolean success) {
        super(player);
        this.state = state;
        this.success = success;
    }

    public BlockState getTargetBlock() { return this.state; }
    public boolean canHarvest() { return this.success; }
    public void setCanHarvest(boolean success){ this.success = success; }

    @Override
    public void sendEvent() {
        HARVEST_CHECK.invoker().check(this);
    }
}
