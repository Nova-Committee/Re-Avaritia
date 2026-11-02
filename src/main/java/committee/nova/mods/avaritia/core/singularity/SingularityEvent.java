package committee.nova.mods.avaritia.core.singularity;

import lombok.Getter;
import net.minecraft.resources.Identifier;

import java.util.LinkedHashMap;
import java.util.Map;

import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;

/**
 * 奇点添加事件
 */
public class SingularityEvent {
    private static final CopyOnWriteArrayList<Consumer<SingularityEvent>> LISTENERS = new CopyOnWriteArrayList<>();

    public static void register(Consumer<SingularityEvent> listener) {
        LISTENERS.add(java.util.Objects.requireNonNull(listener));
    }

    public static void post(SingularityEvent event) {
        LISTENERS.forEach(listener -> listener.accept(event));
    }
    private final Map<Identifier, Singularity> allSingularities;

    public SingularityEvent(Map<Identifier, Singularity> allSingularities) {
        this.allSingularities = new LinkedHashMap<>(allSingularities);
    }

    public Map<Identifier, Singularity> getAllSingularities() {
        return new LinkedHashMap<>(this.allSingularities);
    }

    public static class Add extends SingularityEvent {
        @Getter private final Singularity singularity;
        public Add(Map<Identifier, Singularity> allSingularities, Singularity singularity) {
            super(allSingularities);
            this.singularity = singularity;
        }
    }
    public static class Remove extends SingularityEvent {
        @Getter private final Identifier singularityId;
        public Remove(Map<Identifier, Singularity> allSingularities, Identifier singularityId) {
            super(allSingularities);
            this.singularityId = singularityId;
        }
    }
    public static class Reload extends SingularityEvent {
        public Reload(Map<Identifier, Singularity> allSingularities) {
            super(allSingularities);
        }
    }
}
