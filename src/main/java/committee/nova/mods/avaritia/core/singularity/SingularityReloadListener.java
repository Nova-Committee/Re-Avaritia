package committee.nova.mods.avaritia.core.singularity;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import committee.nova.mods.avaritia.Const;
import committee.nova.mods.avaritia.common.crafting.recipe.EternalSingularityCraftRecipe;
import committee.nova.mods.avaritia.common.crafting.recipe.InfinityCatalystCraftRecipe;
import committee.nova.mods.avaritia.util.SingularityUtils;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.GsonHelper;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.crafting.conditions.ICondition;
import org.jetbrains.annotations.NotNull;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static committee.nova.mods.avaritia.Const.GSON;

/**
 * @author cnlimiter
 */
public class SingularityReloadListener extends SimpleJsonResourceReloadListener {
    public static SingularityReloadListener INSTANCE = new SingularityReloadListener();
    public ICondition.IContext context;
    @Getter @Setter private Map<ResourceLocation, Singularity> dataSingularities = Maps.newConcurrentMap();
    @Getter @Setter private Map<ResourceLocation, Singularity> runSingularities = Maps.newConcurrentMap();
    @Getter @Setter private List<ResourceLocation> removeRecipes = Lists.newCopyOnWriteArrayList();
    @Getter @Setter private List<ResourceLocation> removeSingularities = Lists.newCopyOnWriteArrayList();
    @Getter @Setter private boolean removeAllRecipes = false;
    @Getter @Setter private boolean removeAll = false;

    public SingularityReloadListener() {
        super(GSON, "singularities");
    }

    public SingularityReloadListener(ICondition.IContext context) {
        super(GSON, "singularities");
        this.context = context;
    }

    @Override
    protected void apply(@NotNull Map<ResourceLocation, JsonElement> object, @NotNull ResourceManager resourceManager, @NotNull ProfilerFiller profiler) {
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
                dataSingularities.put(resourcelocation, singularity);
            } catch (IllegalArgumentException | JsonParseException jsonparseexception) {
                Const.LOGGER.error("Singularity: Parsing error loading singularity {}", resourcelocation, jsonparseexception);
            }
        }
        onSingularitiesReloaded(getAllSingularities());
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
            MinecraftForge.EVENT_BUS.post(new SingularityEvent.Add(runSingularities, singularity));
        }
    }

    public void removeSingularityRecipe(ResourceLocation id) {
        this.removeRecipes.add(id);
    }

    public void removeSingularity(ResourceLocation id) {
        this.removeSingularities.add(id);
        MinecraftForge.EVENT_BUS.post(new SingularityEvent.Remove(getAllSingularities(), id));

    }

    public Singularity getSingularity(ResourceLocation id) {
        Singularity runtimeSingularity = this.runSingularities.get(id);
        if (runtimeSingularity != null) {
            return runtimeSingularity;
        }

        return this.dataSingularities.get(id);
    }


    private void onSingularitiesReloaded(Map<ResourceLocation, Singularity> singularities) {
        InfinityCatalystCraftRecipe.invalidate();
        EternalSingularityCraftRecipe.invalidate();
        MinecraftForge.EVENT_BUS.post(new SingularityEvent.Reload(singularities));
    }

    @Override
    public @NotNull String getName() {
        return "Avaritia Singularity Listener";
    }
}
