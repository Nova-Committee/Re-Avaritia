package committee.nova.mods.avaritia.core.singularity;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import committee.nova.mods.avaritia.Const;
import committee.nova.mods.avaritia.common.crafting.recipe.EternalSingularityCraftRecipe;
import committee.nova.mods.avaritia.common.crafting.recipe.InfinityCatalystCraftRecipe;
import committee.nova.mods.avaritia.common.item.singularity.SingularityItem;
import committee.nova.mods.avaritia.util.SingularityUtils;
import lombok.Getter;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.GsonHelper;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.crafting.conditions.ICondition;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static committee.nova.mods.avaritia.Const.GSON;

/**
 * @author cnlimiter
 */
public class SingularityReloadListener extends SimpleJsonResourceReloadListener {
    public static SingularityReloadListener INSTANCE = new SingularityReloadListener();
    public ICondition.IContext context;
    @Getter private Map<ResourceLocation, Singularity> dataSingularities = Maps.newConcurrentMap();
    @Getter private Map<ResourceLocation, Singularity> runSingularities = Maps.newConcurrentMap();
    @Getter private List<ResourceLocation> removeRecipes = Lists.newCopyOnWriteArrayList();
    @Getter private List<ResourceLocation> removeSingularities = Lists.newCopyOnWriteArrayList();
    @Getter private boolean removeAllRecipes = false;
    @Getter private boolean removeAll = false;
    private volatile Map<ResourceLocation, Singularity> allSingularities = Collections.emptyMap();
    private volatile List<Singularity> singularitiesWithIngredient = List.of();

    public SingularityReloadListener() {
        super(GSON, "singularities");
    }

    public SingularityReloadListener(ICondition.IContext context) {
        super(GSON, "singularities");
        this.context = context;
    }

    @Override
    protected void apply(@NotNull Map<ResourceLocation, JsonElement> object, @NotNull ResourceManager resourceManager, @NotNull ProfilerFiller profiler) {
        Map<ResourceLocation, Singularity> loadedSingularities = Maps.newConcurrentMap();

        for (Map.Entry<ResourceLocation, JsonElement> entry : object.entrySet()) {
            ResourceLocation resourcelocation = entry.getKey();
            if (resourcelocation.getPath().startsWith("_")) continue;

            try {
                if (entry.getValue().isJsonObject() && !net.minecraftforge.common.crafting.CraftingHelper.processConditions(entry.getValue().getAsJsonObject(), "conditions", this.context)) {
                    Const.LOGGER.debug("Singularity: Skipping loading singularity {} as it's conditions were not met", resourcelocation);
                    continue;
                }
                Singularity singularity = SingularityUtils.loadFromJson(GsonHelper.convertToJsonObject(entry.getValue(), "top element"));
                if (singularity == null) {
                    Const.LOGGER.info("Singularity: Skipping loading singularity {} as it's serializer returned null", resourcelocation);
                    continue;
                }
                loadedSingularities.put(resourcelocation, singularity);
            } catch (IllegalArgumentException | JsonParseException jsonparseexception) {
                Const.LOGGER.error("Singularity: Parsing error loading singularity {}", resourcelocation, jsonparseexception);
            }
        }
        this.dataSingularities = loadedSingularities;
        refreshSingularityView();
        onSingularitiesReloaded(getAllSingularities());
    }

    public Map<ResourceLocation, Singularity> getAllSingularities() {
        return this.allSingularities;
    }

    public List<Singularity> getSingularitiesWithIngredient() {
        return this.singularitiesWithIngredient;
    }

    public void registerSingularity(Singularity singularity) {
        if (singularity != null && singularity.getRegistryName() != null) {
            var oldSingularity = this.runSingularities.put(singularity.getRegistryName(), singularity);
            if (oldSingularity == null) {
                Const.LOGGER.info("Singularity: Registered runtime singularity: {}", singularity.getRegistryName());
            } else {
                Const.LOGGER.info("Singularity: Updated runtime singularity: {}", singularity.getRegistryName());
            }
            refreshSingularityView();
            invalidateDerivedCaches();
            MinecraftForge.EVENT_BUS.post(new SingularityEvent.Add(getAllSingularities(), singularity));
        }
    }

    public void removeSingularityRecipe(ResourceLocation id) {
        this.removeRecipes.add(id);
        refreshSingularityView();
        invalidateDerivedCaches();
    }

    public void removeSingularity(ResourceLocation id) {
        this.removeSingularities.add(id);
        refreshSingularityView();
        invalidateDerivedCaches();
        MinecraftForge.EVENT_BUS.post(new SingularityEvent.Remove(getAllSingularities(), id));

    }

    public Singularity getSingularity(ResourceLocation id) {
        return this.allSingularities.get(id);
    }

    public void replaceSingularities(Collection<Singularity> dataSingularities, Collection<Singularity> runSingularities) {
        this.dataSingularities = toSingularityMap(dataSingularities);
        this.runSingularities = toSingularityMap(runSingularities);
        refreshSingularityView();
        invalidateDerivedCaches();
    }

    public void setDataSingularities(Map<ResourceLocation, Singularity> dataSingularities) {
        this.dataSingularities = copyToConcurrentMap(dataSingularities);
        refreshSingularityView();
        invalidateDerivedCaches();
    }

    public void setRunSingularities(Map<ResourceLocation, Singularity> runSingularities) {
        this.runSingularities = copyToConcurrentMap(runSingularities);
        refreshSingularityView();
        invalidateDerivedCaches();
    }

    public void setRemoveRecipes(List<ResourceLocation> removeRecipes) {
        this.removeRecipes = Lists.newCopyOnWriteArrayList(removeRecipes);
        refreshSingularityView();
        invalidateDerivedCaches();
    }

    public void setRemoveSingularities(List<ResourceLocation> removeSingularities) {
        this.removeSingularities = Lists.newCopyOnWriteArrayList(removeSingularities);
        refreshSingularityView();
        invalidateDerivedCaches();
    }

    public void setRemoveAllRecipes(boolean removeAllRecipes) {
        this.removeAllRecipes = removeAllRecipes;
        refreshSingularityView();
        invalidateDerivedCaches();
    }

    public void setRemoveAll(boolean removeAll) {
        this.removeAll = removeAll;
        refreshSingularityView();
        invalidateDerivedCaches();
    }

    private static Map<ResourceLocation, Singularity> toSingularityMap(Collection<Singularity> singularities) {
        Map<ResourceLocation, Singularity> map = Maps.newConcurrentMap();
        if (singularities == null) {
            return map;
        }
        for (Singularity singularity : singularities) {
            if (singularity != null && singularity.getRegistryName() != null) {
                map.put(singularity.getRegistryName(), singularity);
            }
        }
        return map;
    }

    private static Map<ResourceLocation, Singularity> copyToConcurrentMap(Map<ResourceLocation, Singularity> singularities) {
        Map<ResourceLocation, Singularity> map = Maps.newConcurrentMap();
        if (singularities != null) {
            map.putAll(singularities);
        }
        return map;
    }

    private void refreshSingularityView() {
        if (this.removeAll) {
            this.allSingularities = Collections.emptyMap();
            this.singularitiesWithIngredient = List.of();
            return;
        }

        Map<ResourceLocation, Singularity> singularities = new LinkedHashMap<>();
        appendNormalizedSingularities(singularities, this.dataSingularities);
        appendNormalizedSingularities(singularities, this.runSingularities);

        this.removeSingularities.forEach(singularities::remove);

        if (this.removeAllRecipes) {
            singularities.replaceAll((id, singularity) -> singularity.copy().setRecipeEnabled(false));
        } else {
            for (ResourceLocation id : this.removeRecipes) {
                Singularity singularity = singularities.get(id);
                if (singularity != null) {
                    singularities.put(id, singularity.copy().setRecipeEnabled(false));
                }
            }
        }

        this.allSingularities = Collections.unmodifiableMap(singularities);
        this.singularitiesWithIngredient = singularities.values()
                .stream()
                .filter(singularity -> singularity.getIngredient() != Ingredient.EMPTY)
                .toList();
    }

    private static void appendNormalizedSingularities(Map<ResourceLocation, Singularity> target, Map<ResourceLocation, Singularity> source) {
        source.forEach((id, singularity) -> {
            if (singularity != null) {
                target.put(id, singularity.copy());
            }
        });
    }

    private static void invalidateDerivedCaches() {
        InfinityCatalystCraftRecipe.INGREDIENTS_LOADED.clear();
        EternalSingularityCraftRecipe.INGREDIENTS_LOADED.clear();
        SingularityItem.clearCachedSingularities();
    }

    private void onSingularitiesReloaded(Map<ResourceLocation, Singularity> singularities) {
        invalidateDerivedCaches();
        MinecraftForge.EVENT_BUS.post(new SingularityEvent.Reload(singularities));
    }

    @Override
    public @NotNull String getName() {
        return "Avaritia Singularity Listener";
    }
}
