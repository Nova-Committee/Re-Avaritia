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

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
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
    private final List<ScriptOperation> craftTweakerOperations = new ArrayList<>();
    private final List<ScriptOperation> kubeJsOperations = new ArrayList<>();
    private boolean scriptTransactionFinalized;
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

    /** Replaces the datapack snapshot and opens a fresh per-reload script transaction. */
    public synchronized void beginReload(Map<ResourceLocation, Singularity> nextDataSnapshot) {
        this.dataSingularities = copyMap(nextDataSnapshot, true);
        beginScriptTransaction();
    }

    /** Clears all script operations collected by the previous resource reload. */
    public synchronized void beginScriptTransaction() {
        this.craftTweakerOperations.clear();
        this.kubeJsOperations.clear();
        this.scriptTransactionFinalized = false;
    }

    /**
     * Publishes the staged state once, after both script loaders have completed
     * and immediately before internal recipes are regenerated.
     *
     * @return {@code true} when this call published the transaction
     */
    public boolean finalizeScriptTransaction() {
        return finalizeScriptTransaction(() -> {
        });
    }

    /**
     * Publishes the staged state while {@code finalizationAction} rebuilds all
     * derived state. If rebuilding fails, the previously committed snapshot is
     * restored. Reload notification happens only after the snapshot and its
     * derived state are both committed.
     */
    public synchronized boolean finalizeScriptTransaction(Runnable finalizationAction) {
        if (this.scriptTransactionFinalized) {
            return false;
        }
        Map<ResourceLocation, Singularity> previousSnapshot = this.effectiveSingularities;
        this.effectiveSingularities = Map.copyOf(buildEffectiveSnapshot());
        this.scriptTransactionFinalized = true;
        try {
            finalizationAction.run();
        } catch (RuntimeException | Error exception) {
            this.effectiveSingularities = previousSnapshot;
            this.scriptTransactionFinalized = false;
            throw exception;
        }
        onSingularitiesReloaded(getAllSingularities());
        return true;
    }

    /** Kept for source compatibility; reload wiring should use the explicit finalizer. */
    public void commitReload() {
        finalizeScriptTransaction();
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

    public synchronized Map<ResourceLocation, Singularity> getRunSingularities() {
        Map<ResourceLocation, Singularity> allRuntime = copyMap(this.persistentSingularities, false);
        applyOperations(allRuntime, this.craftTweakerOperations);
        applyOperations(allRuntime, this.kubeJsOperations);
        return allRuntime;
    }

    /** Kept as the persistent Java API entry point for source compatibility. */
    public void registerSingularity(Singularity singularity) {
        registerPersistentSingularity(singularity);
    }

    public synchronized void registerPersistentSingularity(Singularity singularity) {
        Singularity copy = singularity.copy();
        Singularity old = this.persistentSingularities.put(copy.getRegistryName(), copy);
        logRegistration("persistent", copy.getRegistryName(), old != null);
        NeoForge.EVENT_BUS.post(new SingularityEvent.Add(buildEffectiveSnapshot(), copy.copy()));
    }

    public void registerScriptSingularity(Singularity singularity) {
        registerScriptSingularity(ScriptSource.KUBE_JS, singularity);
    }

    public synchronized void registerScriptSingularity(ScriptSource source, Singularity singularity) {
        Singularity copy = singularity.copy();
        boolean replaced = buildEffectiveSnapshot().containsKey(copy.getRegistryName());
        operations(source).add(ScriptOperation.add(copy));
        logRegistration(source.logName(), copy.getRegistryName(), replaced);
        NeoForge.EVENT_BUS.post(new SingularityEvent.Add(buildEffectiveSnapshot(), copy.copy()));
    }

    public void removeSingularityRecipe(ResourceLocation id) {
        removeSingularityRecipe(ScriptSource.KUBE_JS, id);
    }

    public synchronized void removeSingularityRecipe(ScriptSource source, ResourceLocation id) {
        operations(source).add(ScriptOperation.removeRecipe(id));
    }

    public void removeSingularity(ResourceLocation id) {
        removeSingularity(ScriptSource.KUBE_JS, id);
    }

    public synchronized void removeSingularity(ScriptSource source, ResourceLocation id) {
        operations(source).add(ScriptOperation.remove(id));
        NeoForge.EVENT_BUS.post(new SingularityEvent.Remove(buildEffectiveSnapshot(), id));
    }

    public void setRemoveAllRecipes(boolean removeAllRecipes) {
        setRemoveAllRecipes(ScriptSource.KUBE_JS, removeAllRecipes);
    }

    public synchronized void setRemoveAllRecipes(ScriptSource source, boolean removeAllRecipes) {
        if (removeAllRecipes) {
            operations(source).add(ScriptOperation.removeAllRecipes());
        }
    }

    public void setRemoveAll(boolean removeAll) {
        setRemoveAll(ScriptSource.KUBE_JS, removeAll);
    }

    public synchronized void setRemoveAll(ScriptSource source, boolean removeAll) {
        if (removeAll) {
            operations(source).add(ScriptOperation.removeAll());
        }
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
        applyOperations(all, this.craftTweakerOperations);
        applyOperations(all, this.kubeJsOperations);
        return all;
    }

    private List<ScriptOperation> operations(ScriptSource source) {
        return source == ScriptSource.CRAFT_TWEAKER ? this.craftTweakerOperations : this.kubeJsOperations;
    }

    private static void applyOperations(Map<ResourceLocation, Singularity> singularities,
                                        List<ScriptOperation> operations) {
        for (ScriptOperation operation : operations) {
            switch (operation.type()) {
                case ADD -> singularities.put(operation.id(), operation.singularity().copy());
                case REMOVE -> singularities.remove(operation.id());
                case REMOVE_RECIPE -> {
                    Singularity singularity = singularities.get(operation.id());
                    if (singularity != null) {
                        singularity.setRecipeEnabled(false);
                    }
                }
                case REMOVE_ALL_RECIPES ->
                        singularities.values().forEach(singularity -> singularity.setRecipeEnabled(false));
                case REMOVE_ALL -> singularities.clear();
            }
        }
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

    private static void logRegistration(String source, ResourceLocation id, boolean replaced) {
        Const.LOGGER.info("Singularity: {} {} {} singularity", replaced ? "Updated" : "Registered", source, id);
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

    public enum ScriptSource {
        CRAFT_TWEAKER("CraftTweaker"),
        KUBE_JS("KubeJS");

        private final String logName;

        ScriptSource(String logName) {
            this.logName = logName;
        }

        private String logName() {
            return this.logName;
        }
    }

    private enum ScriptOperationType {
        ADD,
        REMOVE,
        REMOVE_RECIPE,
        REMOVE_ALL_RECIPES,
        REMOVE_ALL
    }

    private record ScriptOperation(ScriptOperationType type, ResourceLocation id, Singularity singularity) {
        private static ScriptOperation add(Singularity singularity) {
            return new ScriptOperation(ScriptOperationType.ADD, singularity.getRegistryName(), singularity.copy());
        }

        private static ScriptOperation remove(ResourceLocation id) {
            return new ScriptOperation(ScriptOperationType.REMOVE, id, null);
        }

        private static ScriptOperation removeRecipe(ResourceLocation id) {
            return new ScriptOperation(ScriptOperationType.REMOVE_RECIPE, id, null);
        }

        private static ScriptOperation removeAllRecipes() {
            return new ScriptOperation(ScriptOperationType.REMOVE_ALL_RECIPES, null, null);
        }

        private static ScriptOperation removeAll() {
            return new ScriptOperation(ScriptOperationType.REMOVE_ALL, null, null);
        }
    }
}
