package committee.nova.mods.avaritia.core.singularity;

import lombok.Getter;
import net.minecraft.resources.ResourceLocation;

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
    private final Map<ResourceLocation, Singularity> allSingularities;

    public SingularityEvent(Map<ResourceLocation, Singularity> allSingularities) {
        this.allSingularities = new LinkedHashMap<>(allSingularities);
    }

    public Map<ResourceLocation, Singularity> getAllSingularities() {
        return new LinkedHashMap<>(this.allSingularities);
    }

    public static class Add extends SingularityEvent {
        @Getter private final Singularity singularity;
        public Add(Map<ResourceLocation, Singularity> allSingularities, Singularity singularity) {
            super(allSingularities);
            this.singularity = singularity;
        }
    }
    public static class Remove extends SingularityEvent {
        @Getter private final ResourceLocation singularityId;
        public Remove(Map<ResourceLocation, Singularity> allSingularities, ResourceLocation singularityId) {
            super(allSingularities);
            this.singularityId = singularityId;
        }
    }
    public static class Reload extends SingularityEvent {
        public Reload(Map<ResourceLocation, Singularity> allSingularities) {
            super(allSingularities);
        }
    }
}
