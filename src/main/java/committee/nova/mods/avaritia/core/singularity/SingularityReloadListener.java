package committee.nova.mods.avaritia.core.singularity;

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
import java.util.Map;

import static committee.nova.mods.avaritia.Const.GSON;

/**
 * @author cnlimiter
 */
public class SingularityReloadListener extends SimpleJsonResourceReloadListener {
    public static SingularityReloadListener INSTANCE;
    public final ICondition.IContext context;
    @Getter @Setter private Map<ResourceLocation, Singularity> dataSingularities = Maps.newConcurrentMap();
    @Getter @Setter private Map<ResourceLocation, Singularity> runSingularities = Maps.newConcurrentMap();

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
                Singularity singularity = SingularityUtils.loadFromJson(resourcelocation, GsonHelper.convertToJsonObject(entry.getValue(), "top element"));
                if (singularity == null) {
                    Const.LOGGER.info("Singularity: Skipping loading singularity {} as it's serializer returned null", resourcelocation);
                    continue;
                }
                dataSingularities.put(resourcelocation, singularity);
            } catch (IllegalArgumentException | JsonParseException jsonparseexception) {
                Const.LOGGER.error("Singularity: Parsing error loading singularity {}", resourcelocation, jsonparseexception);
            }
        }
        onSingularitiesReloaded(dataSingularities);
    }

    public Map<ResourceLocation, Singularity> getAllSingularities() {
        Map<ResourceLocation, Singularity> all = new LinkedHashMap<>(this.dataSingularities);
        all.putAll(this.runSingularities);
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

    public void removeSingularity(ResourceLocation id) {
        if (this.runSingularities.remove(id) != null) {
            Const.LOGGER.info("Singularity: Removed runtime singularity: {}", id);
        } else if (this.dataSingularities.remove(id) != null) {
            Const.LOGGER.info("Singularity: Removed data singularity: {}", id);
        }
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
