package committee.nova.mods.avaritia.init.data.listener;

import com.google.gson.JsonElement;
import committee.nova.mods.avaritia.Const;
import committee.nova.mods.avaritia.common.crafting.recipe.EternalSingularityCraftRecipe;
import committee.nova.mods.avaritia.common.crafting.recipe.InfinityCatalystCraftRecipe;
import committee.nova.mods.avaritia.core.singularity.Singularity;
import committee.nova.mods.avaritia.core.singularity.SingularityDataManager;
import committee.nova.mods.avaritia.core.singularity.SingularityReloadEvent;
import committee.nova.mods.avaritia.util.SingularityUtils;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import net.neoforged.neoforge.common.NeoForge;
import org.jetbrains.annotations.NotNull;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 奇点JSON重载监听器 - 处理数据包重载
 *
 * @author cnlimiter
 * @version 1.0
 */
public class SingularityJsonReloadListener extends SimpleJsonResourceReloadListener {
    public SingularityJsonReloadListener() {
        super(Const.GSON, "singularities");
    }

    @Override
    protected void apply(Map<ResourceLocation, JsonElement> object, @NotNull ResourceManager resourceManager, @NotNull ProfilerFiller profiler) {
        Map<ResourceLocation, Singularity> singularities = new LinkedHashMap<>();
        for(Map.Entry<ResourceLocation, JsonElement> entry : object.entrySet()) {
            var singularity = SingularityUtils.loadFromJson(entry.getKey(), entry.getValue().getAsJsonObject());
            if (singularity != null) {
                singularities.put(entry.getKey(), singularity);
            }
        }
        // 更新缓存
        SingularityDataManager.getInstance().getCachedSingularities().clear();
        SingularityDataManager.getInstance().setCachedSingularities(singularities);
        Const.LOGGER.info("Singularity: Loaded {} singularities", singularities.size());
        // 通知其他组件奇点数据已更新
        onSingularitiesReloaded(singularities);
    }

    /**
     * 奇点数据重载完成后的回调
     */
    private void onSingularitiesReloaded(Map<ResourceLocation, Singularity> singularities) {
        // 使EternalSingularityCraftRecipe缓存失效
        InfinityCatalystCraftRecipe.invalidate();
        EternalSingularityCraftRecipe.invalidate();
        // 通知奇点更新
        NeoForge.EVENT_BUS.post(new SingularityReloadEvent(singularities));
    }

    @Override
    public @NotNull String getName() {
        return "Avaritia Singularity Listener";
    }
}