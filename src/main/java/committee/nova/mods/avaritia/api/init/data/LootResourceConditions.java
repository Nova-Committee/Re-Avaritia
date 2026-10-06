package committee.nova.mods.avaritia.api.init.data;

import com.google.gson.JsonObject;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.storage.loot.LootDataType;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/** Defers tag-dependent loot gates until the tags reload has completed. */
public final class LootResourceConditions {
    private record Key(LootDataType<?> type, ResourceLocation id) {}
    private static final Map<Key, JsonObject> CONDITIONS = new ConcurrentHashMap<>();

    public static void beginReload() { CONDITIONS.clear(); }

    public static boolean prepare(LootDataType<?> type, ResourceLocation id, JsonObject resource) {
        if (!resource.has("avaritia:conditions")) return true;
        if (!ResourceConditions.modDependenciesSatisfied(resource)) return false;
        CONDITIONS.put(new Key(type, id), resource);
        return true;
    }

    public static void apply(Map<LootDataType<?>, Map<ResourceLocation, ?>> elements) {
        elements.forEach((type, resources) -> resources.keySet().removeIf(id -> {
            JsonObject condition = CONDITIONS.remove(new Key(type, id));
            return condition != null && !ResourceConditions.processConditions(condition);
        }));
        CONDITIONS.clear();
    }

    private LootResourceConditions() {}
}
