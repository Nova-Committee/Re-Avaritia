package committee.nova.mods.avaritia.core.singularity;

import committee.nova.mods.avaritia.Const;
import committee.nova.mods.avaritia.common.crafting.recipe.EternalSingularityCraftRecipe;
import committee.nova.mods.avaritia.common.crafting.recipe.InfinityCatalystCraftRecipe;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;
import net.minecraft.core.HolderLookup;
import net.minecraft.resources.FileToIdConverter;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.util.profiling.ProfilerFiller;
import net.neoforged.neoforge.common.NeoForge;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 奇点数据的事务边界。数据包、Java API 和各脚本源先进入暂存区，配方重建成功后才发布。
 */
public class SingularityReloadListener extends SimpleJsonResourceReloadListener<JsonElement> {
    private static final Codec<JsonElement> JSON_CODEC = ExtraCodecs.JSON;
    public static final Identifier RELOAD_LISTENER_ID = Identifier.fromNamespaceAndPath(Const.MOD_ID, "singularities");
    public static final SingularityReloadListener INSTANCE = new SingularityReloadListener();

    private volatile Map<Identifier, Singularity> dataSingularities = Map.of();
    private final Map<Identifier, Singularity> persistentSingularities = new ConcurrentHashMap<>();
    private final EnumMap<ScriptSource, List<ScriptOperation>> scriptOperations = new EnumMap<>(ScriptSource.class);
    private final AtomicSnapshotTransaction<Snapshot> snapshotTransaction =
            new AtomicSnapshotTransaction<>(new Snapshot(Map.of(), Set.of()));

    public record Snapshot(Map<Identifier, Singularity> singularities, Set<Identifier> recipeRemovals) {
        public Snapshot {
            singularities = Collections.unmodifiableMap(copyMap(singularities, false));
            recipeRemovals = Collections.unmodifiableSet(new LinkedHashSet<>(recipeRemovals));
        }
    }

    public SingularityReloadListener() {
        super(JSON_CODEC, FileToIdConverter.json("singularities"));
        for (ScriptSource source : ScriptSource.values()) {
            this.scriptOperations.put(source, new ArrayList<>());
        }
    }

    @Override
    protected void apply(@NotNull Map<Identifier, JsonElement> objects, @NotNull ResourceManager resourceManager,
                         @NotNull ProfilerFiller profiler) {
        var registryOps = this.makeConditionalOps();
        Map<Identifier, Singularity> nextDataSnapshot = new LinkedHashMap<>();
        for (Map.Entry<Identifier, JsonElement> entry : objects.entrySet()) {
            Identifier resourceId = entry.getKey();
            if (resourceId.getPath().startsWith("_")) {
                continue;
            }
            try {
                var decoded = Singularity.CONDITIONAL_CODEC.parse(registryOps, entry.getValue())
                        .getOrThrow(JsonParseException::new);
                decoded.ifPresentOrElse(withConditions -> {
                    Singularity singularity = validatedCopy(withConditions.carrier());
                    if (!resourceId.equals(singularity.getRegistryName())) {
                        throw new SingularityValidationException("Singularity file id " + resourceId
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

    public synchronized void beginReload(Map<Identifier, Singularity> nextDataSnapshot) {
        this.dataSingularities = copyMap(nextDataSnapshot, true);
        this.snapshotTransaction.begin();
    }

    public synchronized void beginScriptTransaction() {
        this.scriptOperations.values().forEach(List::clear);
        this.snapshotTransaction.begin();
    }

    public boolean finalizeScriptTransaction() {
        return finalizeScriptTransaction(() -> {
        });
    }

    /**
     * 将暂存状态与派生配方作为一个提交发布。派生状态失败时恢复旧快照并允许本轮重试。
     */
    public synchronized boolean finalizeScriptTransaction(Runnable finalizationAction) {
        Snapshot candidate = buildSnapshot();
        return this.snapshotTransaction.commit(candidate, () -> {
            finalizationAction.run();
            onSingularitiesReloaded(candidate.singularities());
        });
    }

    public Map<Identifier, Singularity> getDataSingularities() {
        return copyMap(this.dataSingularities, false);
    }

    public synchronized void setDataSingularities(Map<Identifier, Singularity> singularities) {
        this.dataSingularities = copyMap(singularities, true);
    }

    public synchronized Map<Identifier, Singularity> getRunSingularities() {
        Map<Identifier, Singularity> runtime = copyMap(this.persistentSingularities, false);
        applyOperations(runtime, new LinkedHashSet<>(), this.scriptOperations.get(ScriptSource.CRAFT_TWEAKER));
        applyOperations(runtime, new LinkedHashSet<>(), this.scriptOperations.get(ScriptSource.KUBE_JS));
        return runtime;
    }

    public synchronized void setRunSingularities(Map<Identifier, Singularity> singularities) {
        this.persistentSingularities.clear();
        this.persistentSingularities.putAll(copyMap(singularities, true));
    }

    public synchronized List<Identifier> getRemoveRecipes() {
        return List.copyOf(buildSnapshot().recipeRemovals());
    }

    public synchronized void setRemoveRecipes(List<Identifier> ids) {
        ids.forEach(id -> operations(ScriptSource.KUBE_JS).add(ScriptOperation.removeRecipe(id)));
    }

    public synchronized List<Identifier> getRemoveSingularities() {
        return this.scriptOperations.values().stream()
                .flatMap(Collection::stream)
                .filter(operation -> operation.type == ScriptOperationType.REMOVE)
                .map(ScriptOperation::id)
                .distinct()
                .toList();
    }

    public synchronized void setRemoveSingularities(List<Identifier> ids) {
        ids.forEach(id -> operations(ScriptSource.KUBE_JS).add(ScriptOperation.remove(id)));
    }

    public synchronized boolean isRemoveAllRecipes() {
        return hasOperation(ScriptOperationType.REMOVE_ALL_RECIPES);
    }

    public void setRemoveAllRecipes(boolean removeAllRecipes) {
        setRemoveAllRecipes(ScriptSource.KUBE_JS, removeAllRecipes);
    }

    public synchronized void setRemoveAllRecipes(ScriptSource source, boolean removeAllRecipes) {
        if (removeAllRecipes) {
            operations(source).add(ScriptOperation.removeAllRecipes());
        }
    }

    public synchronized boolean isRemoveAll() {
        return hasOperation(ScriptOperationType.REMOVE_ALL);
    }

    public void setRemoveAll(boolean removeAll) {
        setRemoveAll(ScriptSource.KUBE_JS, removeAll);
    }

    public synchronized void setRemoveAll(ScriptSource source, boolean removeAll) {
        if (removeAll) {
            operations(source).add(ScriptOperation.removeAll());
        }
    }

    public Map<Identifier, Singularity> getAllSingularities() {
        return copyMap(this.snapshotTransaction.current().singularities(), false);
    }

    public Snapshot getSnapshot() {
        Snapshot snapshot = this.snapshotTransaction.current();
        return new Snapshot(snapshot.singularities(), snapshot.recipeRemovals());
    }

    public void replaceDataSingularities(Collection<Singularity> singularities) {
        this.dataSingularities = toMap(singularities);
    }

    public synchronized void replaceRunSingularities(Collection<Singularity> singularities) {
        this.persistentSingularities.clear();
        this.persistentSingularities.putAll(toMap(singularities));
    }

    /** 客户端只在单次调用末尾替换有效快照，避免观察到半同步状态。 */
    public synchronized void applySyncedState(Collection<Singularity> dataSingularities,
                                              Collection<Singularity> runSingularities,
                                              Collection<Identifier> removeRecipes,
                                              Collection<Identifier> removeSingularities,
                                              boolean removeAllRecipes,
                                              boolean removeAll) {
        this.dataSingularities = toMap(dataSingularities);
        this.persistentSingularities.clear();
        this.persistentSingularities.putAll(toMap(runSingularities));
        beginScriptTransaction();
        setRemoveRecipes(List.copyOf(removeRecipes));
        setRemoveSingularities(List.copyOf(removeSingularities));
        setRemoveAllRecipes(removeAllRecipes);
        setRemoveAll(removeAll);
        Snapshot syncedSnapshot = buildSnapshot();
        this.snapshotTransaction.replaceCommitted(syncedSnapshot);
        onSingularitiesReloaded(syncedSnapshot.singularities());
    }

    /** Java API 注册在所有资源重载之间持久保留。 */
    public void registerSingularity(Singularity singularity) {
        registerPersistentSingularity(singularity);
    }

    public synchronized void registerPersistentSingularity(Singularity singularity) {
        Singularity copy = validatedCopy(singularity);
        Singularity old = this.persistentSingularities.put(copy.getRegistryName(), copy);
        logRegistration("persistent", copy.getRegistryName(), old != null);
        NeoForge.EVENT_BUS.post(new SingularityEvent.Add(buildSnapshot().singularities(), copy.copy()));
    }

    public void registerScriptSingularity(Singularity singularity) {
        registerScriptSingularity(ScriptSource.KUBE_JS, singularity);
    }

    public synchronized void registerScriptSingularity(ScriptSource source, Singularity singularity) {
        Singularity copy = validatedCopy(singularity);
        boolean replaced = buildSnapshot().singularities().containsKey(copy.getRegistryName());
        operations(source).add(ScriptOperation.add(copy));
        logRegistration(source.logName, copy.getRegistryName(), replaced);
        NeoForge.EVENT_BUS.post(new SingularityEvent.Add(buildSnapshot().singularities(), copy.copy()));
    }

    public void removeSingularityRecipe(Identifier id) {
        removeSingularityRecipe(ScriptSource.KUBE_JS, id);
    }

    public synchronized void removeSingularityRecipe(ScriptSource source, Identifier id) {
        operations(source).add(ScriptOperation.removeRecipe(requireId(id)));
    }

    public void removeSingularity(Identifier id) {
        removeSingularity(ScriptSource.KUBE_JS, id);
    }

    public synchronized void removeSingularity(ScriptSource source, Identifier id) {
        Identifier validatedId = requireId(id);
        operations(source).add(ScriptOperation.remove(validatedId));
        NeoForge.EVENT_BUS.post(new SingularityEvent.Remove(buildSnapshot().singularities(), validatedId));
    }

    public Singularity getSingularity(Identifier id) {
        Singularity singularity = this.snapshotTransaction.current().singularities().get(id);
        return singularity == null ? null : singularity.copy();
    }

    public static Singularity fromJson(JsonObject json, HolderLookup.Provider registries) {
        return validatedCopy(Singularity.CODEC.parse(registries.createSerializationContext(JsonOps.INSTANCE), json)
                .getOrThrow(JsonParseException::new));
    }

    public static JsonElement toJson(Singularity singularity, HolderLookup.Provider registries) {
        return Singularity.CODEC.encodeStart(registries.createSerializationContext(JsonOps.INSTANCE),
                validatedCopy(singularity)).getOrThrow(JsonParseException::new);
    }

    @Override
    public @NotNull String getName() {
        return "Avaritia Singularity Listener";
    }

    private Snapshot buildSnapshot() {
        Map<Identifier, Singularity> singularities = copyMap(this.dataSingularities, false);
        singularities.putAll(copyMap(this.persistentSingularities, false));
        Set<Identifier> recipeRemovals = new LinkedHashSet<>();
        applyOperations(singularities, recipeRemovals, this.scriptOperations.get(ScriptSource.CRAFT_TWEAKER));
        applyOperations(singularities, recipeRemovals, this.scriptOperations.get(ScriptSource.KUBE_JS));
        singularities.forEach((id, singularity) -> {
            if (!singularity.isEnabled() || !singularity.isRecipeEnabled()) {
                recipeRemovals.add(id);
            }
        });
        return new Snapshot(singularities, recipeRemovals);
    }

    private List<ScriptOperation> operations(ScriptSource source) {
        return this.scriptOperations.get(source);
    }

    private boolean hasOperation(ScriptOperationType type) {
        return this.scriptOperations.values().stream().flatMap(Collection::stream)
                .anyMatch(operation -> operation.type == type);
    }

    private static void applyOperations(Map<Identifier, Singularity> singularities,
                                        Set<Identifier> recipeRemovals,
                                        List<ScriptOperation> operations) {
        for (ScriptOperation operation : operations) {
            switch (operation.type) {
                case ADD -> singularities.put(operation.id, operation.singularity.copy());
                case REMOVE -> {
                    singularities.remove(operation.id);
                    recipeRemovals.add(operation.id);
                }
                case REMOVE_RECIPE -> {
                    recipeRemovals.add(operation.id);
                    singularities.computeIfPresent(operation.id,
                            (ignored, singularity) -> singularity.copyWithRecipeEnabled(false));
                }
                case REMOVE_ALL_RECIPES -> {
                    recipeRemovals.addAll(singularities.keySet());
                    singularities.replaceAll((ignored, singularity) -> singularity.copyWithRecipeEnabled(false));
                }
                case REMOVE_ALL -> {
                    recipeRemovals.addAll(singularities.keySet());
                    singularities.clear();
                }
            }
        }
    }

    private static Map<Identifier, Singularity> copyMap(Map<Identifier, Singularity> source,
                                                        boolean requireMatchingIds) {
        Map<Identifier, Singularity> copy = new LinkedHashMap<>();
        source.forEach((id, singularity) -> {
            Singularity singularityCopy = validatedCopy(singularity);
            if (requireMatchingIds && !id.equals(singularityCopy.getRegistryName())) {
                throw new SingularityValidationException("Singularity map id " + id
                        + " does not match " + singularityCopy.getRegistryName());
            }
            copy.put(id, singularityCopy);
        });
        return copy;
    }

    private static Map<Identifier, Singularity> toMap(Collection<Singularity> singularities) {
        Map<Identifier, Singularity> map = new LinkedHashMap<>();
        for (Singularity singularity : singularities) {
            Singularity copy = validatedCopy(singularity);
            map.put(copy.getRegistryName(), copy);
        }
        return map;
    }

    private static Singularity validatedCopy(Singularity singularity) {
        if (singularity == null || singularity.getRegistryName() == null) {
            throw new SingularityValidationException("Singularity and its id must not be null");
        }
        if (singularity.getRealCount() == 0 || singularity.getRealCount() < -1) {
            throw new SingularityValidationException("Singularity count must be positive or -1");
        }
        if (singularity.getTimeCost() <= 0) {
            throw new SingularityValidationException("Singularity timeCost must be positive");
        }
        return singularity.copy();
    }

    private static Identifier requireId(Identifier id) {
        if (id == null) {
            throw new SingularityValidationException("Singularity id must not be null");
        }
        return id;
    }

    private static void logRegistration(String source, Identifier id, boolean replaced) {
        Const.LOGGER.info("Singularity: {} {} {} singularity", replaced ? "Updated" : "Registered", source, id);
    }

    private void onSingularitiesReloaded(Map<Identifier, Singularity> singularities) {
        InfinityCatalystCraftRecipe.invalidate();
        EternalSingularityCraftRecipe.invalidate();
        NeoForge.EVENT_BUS.post(new SingularityEvent.Reload(singularities));
    }

    public enum ScriptSource {
        CRAFT_TWEAKER("CraftTweaker"),
        KUBE_JS("KubeJS");

        private final String logName;

        ScriptSource(String logName) {
            this.logName = logName;
        }
    }

    private enum ScriptOperationType {
        ADD,
        REMOVE,
        REMOVE_RECIPE,
        REMOVE_ALL_RECIPES,
        REMOVE_ALL
    }

    private record ScriptOperation(ScriptOperationType type, Identifier id, Singularity singularity) {
        private static ScriptOperation add(Singularity singularity) {
            return new ScriptOperation(ScriptOperationType.ADD, singularity.getRegistryName(), singularity.copy());
        }

        private static ScriptOperation remove(Identifier id) {
            return new ScriptOperation(ScriptOperationType.REMOVE, id, null);
        }

        private static ScriptOperation removeRecipe(Identifier id) {
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
