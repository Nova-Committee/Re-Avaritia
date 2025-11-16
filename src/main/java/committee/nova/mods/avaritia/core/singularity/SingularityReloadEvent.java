package committee.nova.mods.avaritia.core.singularity;

import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.Event;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 奇点重载事件
 */
public class SingularityReloadEvent extends Event {
    private final Map<ResourceLocation, Singularity> singularities;

    public SingularityReloadEvent(Map<ResourceLocation, Singularity> singularities) {
        this.singularities = new LinkedHashMap<>(singularities);
    }

    public Map<ResourceLocation, Singularity> getSingularities() {
        return new LinkedHashMap<>(this.singularities);
    }
}
