package committee.nova.mods.avaritia.core.singularity;

import lombok.Getter;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraftforge.eventbus.api.Event;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 奇点添加事件
 */
public class SingularityEvent extends Event {
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
        @Getter private final RecipeManager recipeManager;
        public Reload(Map<ResourceLocation, Singularity> allSingularities, RecipeManager recipeManager) {
            super(allSingularities);
            this.recipeManager = recipeManager;
        }
    }
}
