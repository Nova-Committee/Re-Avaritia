package committee.nova.mods.avaritia.core.singularity;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.mojang.serialization.JsonOps;
import committee.nova.mods.avaritia.Const;
import committee.nova.mods.avaritia.common.crafting.recipe.EternalSingularityCraftRecipe;
import committee.nova.mods.avaritia.common.crafting.recipe.InfinityCatalystCraftRecipe;
import net.minecraft.core.HolderLookup;
import net.minecraft.resources.RegistryOps;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import net.neoforged.neoforge.common.NeoForge;
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

    @Override
    protected void apply(@NotNull Map<ResourceLocation, JsonElement> objects, @NotNull ResourceManager resourceManager,
                         @NotNull ProfilerFiller profiler) {
        RegistryOps<JsonElement> registryOps = this.makeConditionalOps();
        Map<ResourceLocation, Singularity> nextDataSnapshot = new LinkedHashMap<>();
        for (Map.Entry<ResourceLocation, JsonElement> entry : objects.entrySet()) {
            ResourceLocation resourceId = entry.getKey();
            if (resourceId.getPath().startsWith("_")) {
                continue;
            }

            try {
                JsonElement normalized = normalizeLegacyJson(entry.getValue(), resourceId);
                var decoded = Singularity.CONDITIONAL_CODEC.parse(registryOps, normalized)
                        .getOrThrow(JsonParseException::new);
                decoded.ifPresentOrElse(withConditions -> {
                    Singularity singularity = withConditions.carrier().copy();
                    if (!resourceId.equals(singularity.getRegistryName())) {
                        throw new JsonParseException("Singularity file id " + resourceId
                                + " does not match its name " + singularity.getRegistryName());
                    }
                    nextDataSnapshot.put(resourceId, singularity);
                }, () -> Const.LOGGER.debug(
                        "Singularity: Skipping {} because its conditions were not met", resourceId));
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
        NeoForge.EVENT_BUS.post(new SingularityEvent.Add(buildEffectiveSnapshot(), copy.copy()));
    }

    public synchronized void registerScriptSingularity(Singularity singularity) {
        Singularity copy = singularity.copy();
        Singularity old = this.scriptSingularities.put(copy.getRegistryName(), copy);
        logRegistration("script", copy.getRegistryName(), old);
        NeoForge.EVENT_BUS.post(new SingularityEvent.Add(buildEffectiveSnapshot(), copy.copy()));
    }

    public synchronized void removeSingularityRecipe(ResourceLocation id) {
        this.removeRecipes.add(id);
    }

    public synchronized void removeSingularity(ResourceLocation id) {
        this.removeSingularities.add(id);
        NeoForge.EVENT_BUS.post(new SingularityEvent.Remove(buildEffectiveSnapshot(), id));
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

    public static Singularity fromJson(JsonObject json, HolderLookup.Provider registries) {
        JsonElement normalized = normalizeLegacyJson(json, null);
        return Singularity.CODEC.parse(registries.createSerializationContext(JsonOps.INSTANCE), normalized)
                .getOrThrow(JsonParseException::new).copy();
    }

    public static JsonElement toJson(Singularity singularity, HolderLookup.Provider registries) {
        return Singularity.CODEC.encodeStart(registries.createSerializationContext(JsonOps.INSTANCE), singularity.copy())
                .getOrThrow(JsonParseException::new);
    }

    public static JsonElement normalizeLegacyJson(JsonElement element, ResourceLocation sourceId) {
        if (!element.isJsonObject()) {
            return element;
        }
        JsonObject normalized = element.getAsJsonObject().deepCopy();
        copyLegacyAlias(normalized, "timeRequired", "timeCost", sourceId);
        copyLegacyAlias(normalized, "recipeDisabled", "recipeEnabled", sourceId);
        normalizeLegacyColor(normalized, "overlayColor");
        normalizeLegacyColor(normalized, "underlayColor");
        return normalized;
    }

    private static void copyLegacyAlias(JsonObject json, String legacyField, String canonicalField,
                                        ResourceLocation sourceId) {
        if (!json.has(canonicalField) && json.has(legacyField)) {
            // recipeDisabled historically contained recipeEnabled, despite its misleading name.
            json.add(canonicalField, json.get(legacyField).deepCopy());
            Const.LOGGER.warn("Singularity: {} uses deprecated field {}; use {}",
                    sourceId == null ? json.get("name") : sourceId, legacyField, canonicalField);
        }
        json.remove(legacyField);
    }

    private static void normalizeLegacyColor(JsonObject json, String field) {
        if (!json.has(field) || !json.get(field).isJsonPrimitive()
                || !json.getAsJsonPrimitive(field).isString()) {
            return;
        }
        String color = json.get(field).getAsString();
        if (color.startsWith("#")) {
            color = color.substring(1);
        }
        try {
            json.addProperty(field, Integer.parseUnsignedInt(color, 16));
        } catch (NumberFormatException exception) {
            throw new JsonParseException("Invalid hexadecimal " + field + ": " + color, exception);
        }
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
        InfinityCatalystCraftRecipe.invalidate();
        EternalSingularityCraftRecipe.invalidate();
        NeoForge.EVENT_BUS.post(new SingularityEvent.Reload(singularities));
    }

    @Override
    public @NotNull String getName() {
        return "Avaritia Singularity Listener";
    }
}
