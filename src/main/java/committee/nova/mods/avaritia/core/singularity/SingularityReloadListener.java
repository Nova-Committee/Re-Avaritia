package committee.nova.mods.avaritia.core.singularity;

import committee.nova.mods.avaritia.Const;
import committee.nova.mods.avaritia.common.crafting.recipe.EternalSingularityCraftRecipe;
import committee.nova.mods.avaritia.common.crafting.recipe.InfinityCatalystCraftRecipe;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
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

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 加载数据包和运行时脚本声明的奇点，并统一计算脚本覆盖后的有效视图。
 */
public class SingularityReloadListener extends SimpleJsonResourceReloadListener<JsonElement> {
    private static final Codec<JsonElement> JSON_CODEC = ExtraCodecs.JSON;
    public static final Identifier RELOAD_LISTENER_ID = Identifier.fromNamespaceAndPath(Const.MOD_ID, "singularities");

    public static SingularityReloadListener INSTANCE = new SingularityReloadListener();

    private Map<Identifier, Singularity> dataSingularities = new ConcurrentHashMap<>();
    private Map<Identifier, Singularity> runSingularities = new ConcurrentHashMap<>();
    private Set<Identifier> removeRecipes = ConcurrentHashMap.newKeySet();
    private Set<Identifier> removeSingularities = ConcurrentHashMap.newKeySet();
    private boolean removeAllRecipes = false;
    private boolean removeAll = false;

    /**
     * 只读快照是奇点系统对外的稳定视图：singularities 用于展示和配方生成，recipeRemovals 用于清理已加载的默认 JSON 配方。
     */
    public record Snapshot(Map<Identifier, Singularity> singularities, Set<Identifier> recipeRemovals) {
        public Snapshot {
            singularities = Collections.unmodifiableMap(new LinkedHashMap<>(singularities));
            recipeRemovals = Collections.unmodifiableSet(new LinkedHashSet<>(recipeRemovals));
        }
    }

    public SingularityReloadListener() {
        super(JSON_CODEC, FileToIdConverter.json("singularities"));
    }


    @Override
    protected void apply(@NotNull Map<Identifier, JsonElement> object, @NotNull ResourceManager resourceManager, @NotNull ProfilerFiller profiler) {
        var registryops = this.makeConditionalOps();
        this.dataSingularities.clear();

        for (Map.Entry<Identifier, JsonElement> entry : object.entrySet()) {
            Identifier identifier = entry.getKey();
            if (identifier.getPath().startsWith("_")) {
                continue;
            }

            try {
                var decoded = Singularity.CONDITIONAL_CODEC.parse(registryops, entry.getValue()).getOrThrow(JsonParseException::new);
                decoded.ifPresentOrElse(r -> {
                    var singularity = r.carrier();
                    dataSingularities.put(identifier, singularity);
                }, () -> Const.LOGGER.debug("Singularity: Skipping loading singularity {} as its conditions were not met", identifier));
            } catch (IllegalArgumentException | JsonParseException jsonparseexception) {
                Const.LOGGER.error("Singularity: Parsing error loading singularity {}", identifier, jsonparseexception);
            }
        }

        onSingularitiesReloaded();
    }

    public Map<Identifier, Singularity> getDataSingularities() {
        return this.dataSingularities;
    }

    public void setDataSingularities(Map<Identifier, Singularity> dataSingularities) {
        this.dataSingularities = new ConcurrentHashMap<>(dataSingularities);
    }

    public Map<Identifier, Singularity> getRunSingularities() {
        return this.runSingularities;
    }

    public void setRunSingularities(Map<Identifier, Singularity> runSingularities) {
        this.runSingularities = new ConcurrentHashMap<>(runSingularities);
    }

    public List<Identifier> getRemoveRecipes() {
        return List.copyOf(this.removeRecipes);
    }

    public void setRemoveRecipes(List<Identifier> removeRecipes) {
        this.removeRecipes = concurrentSet(removeRecipes);
    }

    public List<Identifier> getRemoveSingularities() {
        return List.copyOf(this.removeSingularities);
    }

    public void setRemoveSingularities(List<Identifier> removeSingularities) {
        this.removeSingularities = concurrentSet(removeSingularities);
    }

    public boolean isRemoveAllRecipes() {
        return this.removeAllRecipes;
    }

    public void setRemoveAllRecipes(boolean removeAllRecipes) {
        this.removeAllRecipes = removeAllRecipes;
    }

    public boolean isRemoveAll() {
        return this.removeAll;
    }

    public void setRemoveAll(boolean removeAll) {
        this.removeAll = removeAll;
    }

    public Map<Identifier, Singularity> getAllSingularities() {
        return new LinkedHashMap<>(this.getSnapshot().singularities());
    }

    /**
     * 所有运行时覆盖规则都在这里解释，避免 KubeJS、网络同步和配方注册各自推导出不同状态。
     */
    public Snapshot getSnapshot() {
        Map<Identifier, Singularity> singularities = new LinkedHashMap<>();
        this.dataSingularities.forEach((id, singularity) -> singularities.put(id, singularity.copy()));
        this.runSingularities.forEach((id, singularity) -> singularities.put(id, singularity.copy()));

        Set<Identifier> recipeRemovals = new LinkedHashSet<>();
        recipeRemovals.addAll(this.removeRecipes);
        recipeRemovals.addAll(this.removeSingularities);

        if (this.removeAll || this.removeAllRecipes) {
            recipeRemovals.addAll(singularities.keySet());
        }

        if (this.removeAll) {
            singularities.clear();
            return new Snapshot(singularities, recipeRemovals);
        }

        this.removeSingularities.forEach(singularities::remove);
        if (this.removeAllRecipes) {
            singularities.replaceAll((id, singularity) -> singularity.copyWithRecipeEnabled(false));
        } else {
            this.removeRecipes.forEach(id ->
                    singularities.computeIfPresent(id, (ignored, singularity) -> singularity.copyWithRecipeEnabled(false))
            );
        }

        singularities.forEach((id, singularity) -> {
            if (!singularity.isEnabled() || !singularity.isRecipeEnabled()) {
                recipeRemovals.add(id);
            }
        });

        return new Snapshot(singularities, recipeRemovals);
    }

    /**
     * 客户端同步直接替换数据包奇点集合，不暴露内部 Map 的可变实现。
     */
    public void replaceDataSingularities(Collection<Singularity> singularities) {
        this.dataSingularities = toMap(singularities);
    }

    /**
     * 客户端同步直接替换运行时奇点集合，不暴露内部 Map 的可变实现。
     */
    public void replaceRunSingularities(Collection<Singularity> singularities) {
        this.runSingularities = toMap(singularities);
    }

    public void applySyncedState(Collection<Singularity> dataSingularities,
                                 Collection<Singularity> runSingularities,
                                 Collection<Identifier> removeRecipes,
                                 Collection<Identifier> removeSingularities,
                                 boolean removeAllRecipes,
                                 boolean removeAll) {
        this.dataSingularities = toMap(dataSingularities);
        this.runSingularities = toMap(runSingularities);
        this.removeRecipes = concurrentSet(removeRecipes);
        this.removeSingularities = concurrentSet(removeSingularities);
        this.removeAllRecipes = removeAllRecipes;
        this.removeAll = removeAll;
        onSingularitiesReloaded();
    }

    public void registerSingularity(Singularity singularity) {
        if (singularity != null && singularity.getRegistryName() != null) {
            var oldSingularity = this.runSingularities.put(singularity.getRegistryName(), singularity);
            if (oldSingularity == null) {
                Const.LOGGER.info("Singularity: Registered runtime singularity: {}", singularity.getRegistryName());
            } else {
                Const.LOGGER.info("Singularity: Updated runtime singularity: {}", singularity.getRegistryName());
            }
            NeoForge.EVENT_BUS.post(new SingularityEvent.Add(getAllSingularities(), singularity));
        }
    }

    public void removeSingularityRecipe(Identifier id) {
        this.removeRecipes.add(id);
    }

    public void removeSingularity(Identifier id) {
        this.removeSingularities.add(id);
        NeoForge.EVENT_BUS.post(new SingularityEvent.Remove(getAllSingularities(), id));
    }

    public Singularity getSingularity(Identifier id) {
        return this.getSnapshot().singularities().get(id);
    }

    public static Singularity fromJson(JsonObject json, HolderLookup.Provider registries) {
        return Singularity.CODEC.parse(registries.createSerializationContext(JsonOps.INSTANCE), json).getOrThrow(JsonParseException::new);
    }

    public static JsonElement toJson(Singularity singularity, HolderLookup.Provider registries) {
        return Singularity.CODEC.encodeStart(registries.createSerializationContext(JsonOps.INSTANCE), singularity).getOrThrow(JsonParseException::new);
    }

    private void onSingularitiesReloaded() {
        InfinityCatalystCraftRecipe.invalidate();
        EternalSingularityCraftRecipe.invalidate();
        NeoForge.EVENT_BUS.post(new SingularityEvent.Reload(getAllSingularities()));
    }

    @Override
    public @NotNull String getName() {
        return "Avaritia Singularity Listener";
    }

    private static Set<Identifier> concurrentSet(Collection<Identifier> ids) {
        Set<Identifier> set = ConcurrentHashMap.newKeySet();
        set.addAll(ids);
        return set;
    }

    private static Map<Identifier, Singularity> toMap(Collection<Singularity> singularities) {
        Map<Identifier, Singularity> map = new ConcurrentHashMap<>();
        for (Singularity singularity : singularities) {
            if (singularity != null && singularity.getRegistryName() != null) {
                map.put(singularity.getRegistryName(), singularity);
            }
        }
        return map;
    }
}
