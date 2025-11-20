package committee.nova.mods.avaritia.init.compat.kubejs.event;

import dev.latvian.mods.kubejs.event.EventGroup;
import dev.latvian.mods.kubejs.event.EventHandler;

/**
 * @author cnlimiter
 */
public interface AvaritiaEvents {
    EventGroup GROUP = EventGroup.of("AvaritiaEvents");

    EventHandler REGISTRY = GROUP.server("singularity", () -> SingularityRegisterEventJS.class);
}
