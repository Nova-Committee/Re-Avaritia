package committee.nova.mods.avaritia.core.singularity;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.mojang.serialization.JsonOps;
import committee.nova.mods.avaritia.Const;
import committee.nova.mods.avaritia.common.crafting.recipe.EternalSingularityCraftRecipe;
import committee.nova.mods.avaritia.common.crafting.recipe.InfinityCatalystCraftRecipe;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.core.HolderLookup;
import net.minecraft.resources.RegistryOps;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import net.neoforged.neoforge.common.NeoForge;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static committee.nova.mods.avaritia.Const.GSON;

/**
 * @author cnlimiter
 */
public class SingularityReloadListener extends SimpleJsonResourceReloadListener {
    public static SingularityReloadListener INSTANCE = new SingularityReloadListener();

    @Getter @Setter private Map<ResourceLocation, Singularity> dataSingularities = Maps.newConcurrentMap();
    @Getter @Setter private Map<ResourceLocation, Singularity> runSingularities = Maps.newConcurrentMap();
    @Getter @Setter private List<ResourceLocation> removeRecipes = Lists.newCopyOnWriteArrayList();
    @Getter @Setter private List<ResourceLocation> removeSingularities = Lists.newCopyOnWriteArrayList();
    @Getter @Setter private boolean removeAllRecipes = false;
    @Getter @Setter private boolean removeAll = false;

    public SingularityReloadListener() {
        super(GSON, "singularities");
    }

    @Override
    protected void apply(@NotNull Map<ResourceLocation, JsonElement> object, @NotNull ResourceManager resourceManager, @NotNull ProfilerFiller profiler) {
        RegistryOps<JsonElement> registryops = this.makeConditionalOps();
        for (Map.Entry<ResourceLocation, JsonElement> entry : object.entrySet()) {
            ResourceLocation resourcelocation = entry.getKey();
            if (resourcelocation.getPath().startsWith("_")) continue;

            try {
                var decoded = Singularity.CONDITIONAL_CODEC.parse(registryops, entry.getValue()).getOrThrow(JsonParseException::new);
                decoded.ifPresentOrElse(r -> {
                    var singularity = r.carrier();
                    dataSingularities.put(resourcelocation, singularity);
                }, () -> {
                    Const.LOGGER.debug("Singularity: Skipping loading singularity {} as its conditions were not met", resourcelocation);
                });
            } catch (IllegalArgumentException | JsonParseException jsonparseexception) {
                Const.LOGGER.error("Singularity: Parsing error loading singularity {}", resourcelocation, jsonparseexception);
            }
        }
        onSingularitiesReloaded(dataSingularities);
    }

    public Map<ResourceLocation, Singularity> getAllSingularities() {
        Map<ResourceLocation, Singularity> all = new ConcurrentHashMap<>(this.dataSingularities);
        all.putAll(this.runSingularities);
        all.forEach((id, singularity) -> {
            if (this.removeRecipes.contains(id)) all.get(id).setRecipeEnabled(false);
            if (this.removeSingularities.contains(id)) all.remove(id);
        });
        if (this.removeAllRecipes) all.forEach((id, singularity) -> singularity.setRecipeEnabled(false));
        if (this.removeAll) all.clear();
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

    public void removeSingularityRecipe(ResourceLocation id) {
        this.removeRecipes.add(id);
    }

    public void removeSingularity(ResourceLocation id) {
        this.removeSingularities.add(id);
        NeoForge.EVENT_BUS.post(new SingularityEvent.Remove(getAllSingularities(), id));
    }

    public Singularity getSingularity(ResourceLocation id) {
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
