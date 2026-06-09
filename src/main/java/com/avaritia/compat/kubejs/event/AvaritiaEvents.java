package com.avaritia.compat.kubejs.event;

import dev.latvian.mods.kubejs.event.EventGroup;
import dev.latvian.mods.kubejs.event.EventHandler;

public interface AvaritiaEvents {
    EventGroup GROUP = EventGroup.of("AvaritiaEvents");

    EventHandler REGISTRY = GROUP.server("singularity", () -> SingularityRegisterEventJS.class);
}
