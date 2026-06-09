package committee.nova.mods.avaritia.core.singularity;

import committee.nova.mods.avaritia.Avaritia;
import committee.nova.mods.avaritia.Const;
import committee.nova.mods.avaritia.common.crafting.recipe.EternalSingularityCraftRecipe;
import committee.nova.mods.avaritia.common.crafting.recipe.InfinityCatalystCraftRecipe;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
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
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.AddServerReloadListenersEvent;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 加载 data/*&#47;singularities 下的奇点定义。
 */
public class SingularityReloadListener extends SimpleJsonResourceReloadListener<JsonElement> {
    private static final Codec<JsonElement> JSON_CODEC = ExtraCodecs.JSON;
    public static final Identifier RELOAD_LISTENER_ID = Identifier.fromNamespaceAndPath(Const.MOD_ID, "singularities");

    public static SingularityReloadListener INSTANCE = new SingularityReloadListener();

    private Map<Identifier, Singularity> dataSingularities = Maps.newConcurrentMap();
    private Map<Identifier, Singularity> runSingularities = Maps.newConcurrentMap();
    private List<Identifier> removeRecipes = Lists.newCopyOnWriteArrayList();
    private List<Identifier> removeSingularities = Lists.newCopyOnWriteArrayList();
    private boolean removeAllRecipes = false;
    private boolean removeAll = false;

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

        onSingularitiesReloaded(dataSingularities);
    }

    public Map<Identifier, Singularity> getDataSingularities() {
        return this.dataSingularities;
    }

    public void setDataSingularities(Map<Identifier, Singularity> dataSingularities) {
        this.dataSingularities = dataSingularities;
    }

    public Map<Identifier, Singularity> getRunSingularities() {
        return this.runSingularities;
    }

    public void setRunSingularities(Map<Identifier, Singularity> runSingularities) {
        this.runSingularities = runSingularities;
    }

    public List<Identifier> getRemoveRecipes() {
        return this.removeRecipes;
    }

    public void setRemoveRecipes(List<Identifier> removeRecipes) {
        this.removeRecipes = removeRecipes;
    }

    public List<Identifier> getRemoveSingularities() {
        return this.removeSingularities;
    }

    public void setRemoveSingularities(List<Identifier> removeSingularities) {
        this.removeSingularities = removeSingularities;
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
        Map<Identifier, Singularity> all = new ConcurrentHashMap<>(this.dataSingularities);
        all.putAll(this.runSingularities);
        all.forEach((id, singularity) -> {
            if (this.removeRecipes.contains(id)) {
                all.get(id).setRecipeEnabled(false);
            }
            if (this.removeSingularities.contains(id)) {
                all.remove(id);
            }
        });
        if (this.removeAllRecipes) {
            all.forEach((id, singularity) -> singularity.setRecipeEnabled(false));
        }
        if (this.removeAll) {
            all.clear();
        }
        return all;
    }

    public void registerSingularity(Singularity singularity) {
        if (singularity != null && singularity.getRegistryName() != null) {
            var oldSingularity = this.runSingularities.put(singularity.getRegistryName(), singularity);
            if (oldSingularity == null) {
                Const.LOGGER.info("Singularity: Registered runtime singularity: {}", singularity.getRegistryName());
            } else {
                Const.LOGGER.info("Singularity: Updated runtime singularity: {}", singularity.getRegistryName());
            }
            NeoForge.EVENT_BUS.post(new SingularityEvent.Add(runSingularities, singularity));
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
        Singularity runtimeSingularity = this.runSingularities.get(id);
        if (runtimeSingularity != null) {
            return runtimeSingularity;
        }

        return this.dataSingularities.get(id);
    }

    public static Singularity fromJson(JsonObject json, HolderLookup.Provider registries) {
        return Singularity.CODEC.parse(registries.createSerializationContext(JsonOps.INSTANCE), json).getOrThrow(JsonParseException::new);
    }

    public static JsonElement toJson(Singularity singularity, HolderLookup.Provider registries) {
        return Singularity.CODEC.encodeStart(registries.createSerializationContext(JsonOps.INSTANCE), singularity).getOrThrow(JsonParseException::new);
    }

    private void onSingularitiesReloaded(Map<Identifier, Singularity> singularities) {
        InfinityCatalystCraftRecipe.invalidate();
        EternalSingularityCraftRecipe.invalidate();
        NeoForge.EVENT_BUS.post(new SingularityEvent.Reload(singularities));
    }

    @Override
    public @NotNull String getName() {
        return "Avaritia Singularity Listener";
    }
}
