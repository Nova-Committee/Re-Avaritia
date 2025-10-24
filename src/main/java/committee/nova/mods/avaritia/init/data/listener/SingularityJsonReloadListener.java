package committee.nova.mods.avaritia.init.data.listener;

import com.google.gson.JsonElement;
import committee.nova.mods.avaritia.Const;
import committee.nova.mods.avaritia.core.singularity.Singularity;
import committee.nova.mods.avaritia.core.singularity.SingularityDataManager;
import committee.nova.mods.avaritia.util.SingularityUtils;
import lombok.Getter;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.crafting.conditions.ICondition;
import org.jetbrains.annotations.NotNull;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 奇点JSON重载监听器 - 处理数据包重载
 *
 * @author cnlimiter
 * @version 1.0
 */
public class SingularityJsonReloadListener extends SimpleJsonResourceReloadListener {
    /**
     * -- GETTER --
     *  获取条件上下文
     */
    @Getter
    private ICondition.IContext conditionContext;

    public SingularityJsonReloadListener(ICondition.IContext conditionContext) {
        super(Const.GSON, "singularities");
        this.conditionContext = conditionContext;
    }

    @Override
    protected void apply(Map<ResourceLocation, JsonElement> object, @NotNull ResourceManager resourceManager, @NotNull ProfilerFiller profiler) {
        Map<ResourceLocation, Singularity> singularities = new LinkedHashMap<>();
        for(Map.Entry<ResourceLocation, JsonElement> entry : object.entrySet()) {
            var singularity = SingularityUtils.loadFromJson(entry.getKey(), entry.getValue().getAsJsonObject(), getConditionContext());
            if (singularity != null) {
                singularities.put(entry.getKey(), singularity);
            }
        }
        // 更新缓存
        SingularityDataManager.INSTANCE.getSingularities().clear();
        SingularityDataManager.INSTANCE.setSingularities(singularities.values().stream().toList());
        Const.LOGGER.info("Loaded {} singularities", singularities.size());
        // 通知其他组件奇点数据已更新
        onSingularitiesReloaded(singularities.values().stream().toList());
    }

    /**
     * 奇点数据重载完成后的回调
     */
    private void onSingularitiesReloaded(List<Singularity> singularities) {
        MinecraftForge.EVENT_BUS.post(new SingularityDataManager.SingularityReloadEvent(singularities));
        // 这里可以添加通知其他组件的逻辑
        // 例如：刷新配方缓存、重新计算某些数据等
    }

    @Override
    public @NotNull String getName() {
        return "Avaritia Singularity Listener";
    }
}