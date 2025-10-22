package committee.nova.mods.avaritia.init.data.listener;

import committee.nova.mods.avaritia.Const;
import committee.nova.mods.avaritia.common.item.singularity.Singularity;
import committee.nova.mods.avaritia.init.handler.SingularityDataHandler;
import committee.nova.mods.avaritia.util.SingularityUtils;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import lombok.Getter;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimplePreparableReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.crafting.conditions.ICondition;
import net.minecraftforge.common.crafting.conditions.ICondition.IContext;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.Reader;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 奇点JSON重载监听器 - 处理数据包重载
 *
 * @author cnlimiter
 * @version 1.0
 */
public class SingularityJsonReloadListener extends SimplePreparableReloadListener<Map<ResourceLocation, Singularity>> {

    private static final Gson GSON = new Gson();
    private static final Logger LOGGER = Const.LOGGER;

    private Map<ResourceLocation, Singularity> singularities = new LinkedHashMap<>();
    /**
     * -- GETTER --
     *  获取条件上下文
     */
    @Getter
    private ICondition.IContext conditionContext;

    public SingularityJsonReloadListener(ICondition.IContext conditionContext) {
        this.conditionContext = conditionContext;
    }

    @Override
    public @NotNull Map<ResourceLocation, Singularity> prepare(ResourceManager resourceManager, @NotNull ProfilerFiller profiler) {
        Map<ResourceLocation, Singularity> loadedSingularities = new LinkedHashMap<>();
        // 从数据包中加载奇点定义
        var resources = resourceManager.listResourceStacks("singularities",
            id -> id.getPath().endsWith(".json"));

        for (var entry : resources.entrySet()) {
            var id = entry.getKey();
            var resourceStack = entry.getValue();

            if (id.getNamespace().equals(Const.MOD_ID)) {
                try {
                    // 移除文件扩展名，获取奇点ID
                    var singularityId = new ResourceLocation(id.getNamespace(),
                            id.getPath().substring("singularities/".length(), id.getPath().length() - ".json".length()));
                    var singularity = loadSingularity(resourceStack, singularityId, getConditionContext());
                    if (singularity != null) {
                        loadedSingularities.put(singularityId, singularity);
                    }
                } catch (Exception e) {
                    LOGGER.error("Failed to load singularity from data: {}", id, e);
                }
            }
        }
        return loadedSingularities;
    }

    @Override
    public void apply(@NotNull Map<ResourceLocation, Singularity> loadedSingularities, @NotNull ResourceManager resourceManager, @NotNull ProfilerFiller profiler) {
        this.singularities = loadedSingularities;

        // 更新缓存
        SingularityDataHandler.getInstance().getCachedSingularities().clear();
        SingularityDataHandler.getInstance().setCachedSingularities(this.getSingularities());
        LOGGER.info("Loaded {} singularities", loadedSingularities.size());
        // 通知其他组件
        // 通知其他组件奇点数据已更新
        onSingularitiesReloaded();
    }

    private Singularity loadSingularity(Collection<Resource> resources, ResourceLocation id, IContext context) throws IOException {
        if (resources.isEmpty()) {
            return null;
        }

        // 使用优先级最高的资源（通常是模组提供的资源）
        Resource resource = resources.iterator().next();

        try (Reader reader = resource.openAsReader()) {
            JsonObject json = GSON.fromJson(reader, JsonObject.class);
            if (json == null) {
                LOGGER.warn("Empty JSON file for singularity: {}", id);
                return null;
            }
            return SingularityUtils.loadFromJson(id, json, context);
        }
    }

    /**
     * 奇点数据重载完成后的回调
     */
    private void onSingularitiesReloaded() {
        MinecraftForge.EVENT_BUS.post(new SingularityDataHandler.SingularityReloadEvent(this.getSingularities()));
        // 这里可以添加通知其他组件的逻辑
        // 例如：刷新配方缓存、重新计算某些数据等
    }

    /**
     * 获取所有已加载的奇点
     */
    public Map<ResourceLocation, Singularity> getSingularities() {
        return new LinkedHashMap<>(this.singularities);
    }

    /**
     * 根据ID获取奇点
     */
    public Singularity getSingularity(ResourceLocation id) {
        return this.singularities.get(id);
    }

    /**
     * 检查奇点是否存在
     */
    public boolean hasSingularity(ResourceLocation id) {
        return this.singularities.containsKey(id);
    }

}