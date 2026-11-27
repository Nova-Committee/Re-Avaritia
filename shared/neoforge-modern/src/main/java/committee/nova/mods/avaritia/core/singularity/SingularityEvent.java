package committee.nova.mods.avaritia.core.singularity;

import net.minecraft.resources.Identifier;
import net.neoforged.bus.api.Event;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 奇点变更事件。
 */
public class SingularityEvent extends Event {
    private final Map<Identifier, Singularity> allSingularities;

    public SingularityEvent(Map<Identifier, Singularity> allSingularities) {
        this.allSingularities = new LinkedHashMap<>(allSingularities);
    }

    public Map<Identifier, Singularity> getAllSingularities() {
        return new LinkedHashMap<>(this.allSingularities);
    }

    public static class Add extends SingularityEvent {
        private final Singularity singularity;

        public Add(Map<Identifier, Singularity> allSingularities, Singularity singularity) {
            super(allSingularities);
            this.singularity = singularity;
        }

        public Singularity getSingularity() {
            return this.singularity;
        }
    }

    public static class Remove extends SingularityEvent {
        private final Identifier singularityId;

        public Remove(Map<Identifier, Singularity> allSingularities, Identifier singularityId) {
            super(allSingularities);
            this.singularityId = singularityId;
        }

        public Identifier getSingularityId() {
            return this.singularityId;
        }
    }

    public static class Reload extends SingularityEvent {
        public Reload(Map<Identifier, Singularity> allSingularities) {
            super(allSingularities);
        }
    }
}
