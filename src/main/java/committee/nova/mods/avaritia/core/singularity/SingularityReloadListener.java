package committee.nova.mods.avaritia.core.singularity;

import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import committee.nova.mods.avaritia.Const;
import committee.nova.mods.avaritia.common.crafting.recipe.EternalSingularityCraftRecipe;
import committee.nova.mods.avaritia.common.crafting.recipe.InfinityCatalystCraftRecipe;
import committee.nova.mods.avaritia.util.SingularityUtils;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.GsonHelper;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.crafting.conditions.ICondition;
import org.jetbrains.annotations.NotNull;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import static committee.nova.mods.avaritia.Const.GSON;

/**
 * Owns persistent Java registrations, the current datapack snapshot, per-reload
 * script registrations, and the committed effective view used by gameplay.
 */
public class SingularityReloadListener extends SimpleJsonResourceReloadListener {
    public static final SingularityReloadListener INSTANCE = new SingularityReloadListener();

    private volatile ICondition.IContext context;
    private volatile Map<ResourceLocation, Singularity> dataSingularities = Map.of();
    private final Map<ResourceLocation, Singularity> persistentSingularities = new ConcurrentHashMap<>();
    private final Map<ResourceLocation, Singularity> scriptSingularities = new ConcurrentHashMap<>();
    private final Set<ResourceLocation> removeRecipes = ConcurrentHashMap.newKeySet();
    private final Set<ResourceLocation> removeSingularities = ConcurrentHashMap.newKeySet();
    private volatile boolean removeAllRecipes;
    private volatile boolean removeAll;
    private volatile Map<ResourceLocation, Singularity> effectiveSingularities = Map.of();

    public SingularityReloadListener() {
        super(GSON, "singularities");
    }

    public void setContext(ICondition.IContext context) {
        this.context = context;
    }

    @Override
    protected void apply(@NotNull Map<ResourceLocation, JsonElement> objects, @NotNull ResourceManager resourceManager,
                         @NotNull ProfilerFiller profiler) {
        Map<ResourceLocation, Singularity> nextDataSnapshot = new LinkedHashMap<>();
        for (Map.Entry<ResourceLocation, JsonElement> entry : objects.entrySet()) {
            ResourceLocation resourceId = entry.getKey();
            if (resourceId.getPath().startsWith("_")) {
                continue;
            }

            try {
                if (entry.getValue().isJsonObject()
                        && !net.minecraftforge.common.crafting.CraftingHelper.processConditions(
                        entry.getValue().getAsJsonObject(), "conditions", this.context)) {
                    Const.LOGGER.debug("Singularity: Skipping {} because its conditions were not met", resourceId);
                    continue;
                }
                Singularity singularity = SingularityUtils.loadFromJson(
                        GsonHelper.convertToJsonObject(entry.getValue(), "top element"), resourceId).copy();
                if (!resourceId.equals(singularity.getRegistryName())) {
                    throw new JsonParseException("Singularity file id " + resourceId
                            + " does not match its name " + singularity.getRegistryName());
                }
                nextDataSnapshot.put(resourceId, singularity);
            } catch (IllegalArgumentException | JsonParseException exception) {
                Const.LOGGER.error("Singularity: Parsing error loading singularity {}", resourceId, exception);
            }
        }
        beginReload(nextDataSnapshot);
    }

    /** Clears all script-owned state and atomically replaces the datapack snapshot. */
    public synchronized void beginReload(Map<ResourceLocation, Singularity> nextDataSnapshot) {
        this.dataSingularities = copyMap(nextDataSnapshot, true);
        this.scriptSingularities.clear();
        this.removeRecipes.clear();
        this.removeSingularities.clear();
        this.removeAllRecipes = false;
        this.removeAll = false;
    }

    /** Publishes the staged reload state immediately before internal recipes are generated. */
    public synchronized void commitReload() {
        this.effectiveSingularities = Map.copyOf(buildEffectiveSnapshot());
        onSingularitiesReloaded(getAllSingularities());
    }

    /** Replaces the client view with the effective snapshot calculated by the server. */
    public synchronized void replaceEffectiveSnapshot(Iterable<Singularity> singularities) {
        Map<ResourceLocation, Singularity> replacement = new LinkedHashMap<>();
        for (Singularity singularity : singularities) {
            Singularity copy = singularity.copy();
            replacement.put(copy.getRegistryName(), copy);
        }
        this.effectiveSingularities = Map.copyOf(replacement);
        onSingularitiesReloaded(getAllSingularities());
    }

    public Map<ResourceLocation, Singularity> getAllSingularities() {
        return copyMap(this.effectiveSingularities, false);
    }

    public Map<ResourceLocation, Singularity> getDataSingularities() {
        return copyMap(this.dataSingularities, false);
    }

    public Map<ResourceLocation, Singularity> getRunSingularities() {
        Map<ResourceLocation, Singularity> allRuntime = copyMap(this.persistentSingularities, false);
        allRuntime.putAll(copyMap(this.scriptSingularities, false));
        return allRuntime;
    }

    /** Kept as the persistent Java API entry point for source compatibility. */
    public void registerSingularity(Singularity singularity) {
        registerPersistentSingularity(singularity);
    }

    public synchronized void registerPersistentSingularity(Singularity singularity) {
        Singularity copy = singularity.copy();
        Singularity old = this.persistentSingularities.put(copy.getRegistryName(), copy);
        logRegistration("persistent", copy.getRegistryName(), old);
        MinecraftForge.EVENT_BUS.post(new SingularityEvent.Add(buildEffectiveSnapshot(), copy.copy()));
    }

    public synchronized void registerScriptSingularity(Singularity singularity) {
        Singularity copy = singularity.copy();
        Singularity old = this.scriptSingularities.put(copy.getRegistryName(), copy);
        logRegistration("script", copy.getRegistryName(), old);
        MinecraftForge.EVENT_BUS.post(new SingularityEvent.Add(buildEffectiveSnapshot(), copy.copy()));
    }

    public synchronized void removeSingularityRecipe(ResourceLocation id) {
        this.removeRecipes.add(id);
    }

    public synchronized void removeSingularity(ResourceLocation id) {
        this.removeSingularities.add(id);
        MinecraftForge.EVENT_BUS.post(new SingularityEvent.Remove(buildEffectiveSnapshot(), id));
    }

    public synchronized void setRemoveAllRecipes(boolean removeAllRecipes) {
        this.removeAllRecipes = removeAllRecipes;
    }

    public synchronized void setRemoveAll(boolean removeAll) {
        this.removeAll = removeAll;
    }

    public Singularity getSingularity(ResourceLocation id) {
        Singularity singularity = this.effectiveSingularities.get(id);
        return singularity == null ? null : singularity.copy();
    }

    private Map<ResourceLocation, Singularity> buildEffectiveSnapshot() {
        Map<ResourceLocation, Singularity> all = copyMap(this.dataSingularities, false);
        all.putAll(copyMap(this.persistentSingularities, false));
        all.putAll(copyMap(this.scriptSingularities, false));
        this.removeRecipes.forEach(id -> {
            Singularity singularity = all.get(id);
            if (singularity != null) {
                singularity.setRecipeEnabled(false);
            }
        });
        this.removeSingularities.forEach(all::remove);
        if (this.removeAllRecipes) {
            all.values().forEach(singularity -> singularity.setRecipeEnabled(false));
        }
        if (this.removeAll) {
            all.clear();
        }
        return all;
    }

    private static Map<ResourceLocation, Singularity> copyMap(Map<ResourceLocation, Singularity> source,
                                                               boolean requireMatchingIds) {
        Map<ResourceLocation, Singularity> copy = new LinkedHashMap<>();
        source.forEach((id, singularity) -> {
            Singularity singularityCopy = singularity.copy();
            if (requireMatchingIds && !id.equals(singularityCopy.getRegistryName())) {
                throw new IllegalArgumentException("Singularity map id " + id
                        + " does not match " + singularityCopy.getRegistryName());
            }
            copy.put(id, singularityCopy);
        });
        return copy;
    }

    private static void logRegistration(String source, ResourceLocation id, Singularity old) {
        Const.LOGGER.info("Singularity: {} {} {} singularity", old == null ? "Registered" : "Updated", source, id);
    }

    private void onSingularitiesReloaded(Map<ResourceLocation, Singularity> singularities) {
        InfinityCatalystCraftRecipe.INGREDIENTS_LOADED.clear();
        EternalSingularityCraftRecipe.INGREDIENTS_LOADED.clear();
        MinecraftForge.EVENT_BUS.post(new SingularityEvent.Reload(singularities));
    }

    @Override
    public @NotNull String getName() {
        return "Avaritia Singularity Listener";
    }
}
