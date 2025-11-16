package committee.nova.mods.avaritia.core.singularity;

import committee.nova.mods.avaritia.Const;
import committee.nova.mods.avaritia.common.crafting.recipe.EternalSingularityCraftRecipe;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.common.MinecraftForge;
import org.apache.logging.log4j.Logger;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 奇点数据管理器 - 新的数据包系统
 * <p>
 * 使用标准的Minecraft数据包系统替代旧的配置文件方式
 * 支持动态重载、条件加载、网络同步等功能
 *
 * @author cnlimiter
 * @version 1.0
 */
public class SingularityDataManager {
    private static final Logger LOGGER = Const.LOGGER;
    private static SingularityDataManager INSTANCE;

    @Getter @Setter private Map<ResourceLocation, Singularity> cachedSingularities = new LinkedHashMap<>();
    @Getter @Setter private Map<ResourceLocation, Singularity> runtimeSingularities = new LinkedHashMap<>();

    public SingularityDataManager() {
    }

    public static SingularityDataManager getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new SingularityDataManager();
        }
        return INSTANCE;
    }

    /**
     * 获取所有奇点（包括运行时奇点）
     */
    public Collection<Singularity> getSingularities() {
        Map<ResourceLocation, Singularity> allSingularities = new LinkedHashMap<>(this.cachedSingularities);
        allSingularities.putAll(this.runtimeSingularities);
        return allSingularities.values();
    }

    /**
     * 只获取数据包奇点（不包括运行时奇点）
     */
    public Collection<Singularity> getDataPackSingularities() {
        return this.cachedSingularities.values();
    }


    /**
     * 根据ID获取奇点（优先运行时奇点）
     */
    public Singularity getSingularity(ResourceLocation id) {
        // 优先返回运行时奇点
        Singularity runtimeSingularity = this.runtimeSingularities.get(id);
        if (runtimeSingularity != null) {
            return runtimeSingularity;
        }

        return this.cachedSingularities.get(id);
    }

    /**
     * 注册运行时奇点（用于KubeJS）
     */
    public void registerRuntimeSingularity(Singularity singularity) {
        if (singularity != null && singularity.getRegistryName() != null) {
            var oldSingularity = this.runtimeSingularities.put(singularity.getRegistryName(), singularity);
            if (oldSingularity == null) {
                LOGGER.info("Singularity: Registered runtime singularity: {}", singularity.getRegistryName());
            } else {
                LOGGER.info("Singularity: Updated runtime singularity: {}", singularity.getRegistryName());
            }

            // 使EternalSingularityCraftRecipe缓存失效
            EternalSingularityCraftRecipe.invalidate();
            // 通知奇点更新
            MinecraftForge.EVENT_BUS.post(new SingularityRuntimeEvent.Add(getAllSingularities(), singularity));
        }
    }

    /**
     * 移除运行时奇点
     */
    public Singularity removeRuntimeSingularity(ResourceLocation id) {
        var removed = this.runtimeSingularities.remove(id);
        if (removed != null) {
            LOGGER.info("Singularity: Removed runtime singularity: {}", id);

            // 使EternalSingularityCraftRecipe缓存失效
            EternalSingularityCraftRecipe.invalidate();

            // 通知奇点更新
            MinecraftForge.EVENT_BUS.post(new SingularityRuntimeEvent.Remove(getAllSingularities(), id));
        }
        return removed;
    }


    /**
     * 获取所有奇点的合并映射
     */
    private Map<ResourceLocation, Singularity> getAllSingularities() {
        Map<ResourceLocation, Singularity> all = new LinkedHashMap<>(this.cachedSingularities);
        all.putAll(this.runtimeSingularities);
        return all;
    }

    /**
     * 检查奇点是否存在
     */
    public boolean hasSingularity(ResourceLocation id) {
        return this.cachedSingularities.containsKey(id) || this.runtimeSingularities.containsKey(id);
    }

    /**
     * 检查是否为运行时奇点
     */
    public boolean isRuntimeSingularity(ResourceLocation id) {
        return this.runtimeSingularities.containsKey(id);
    }

    /**
     * 获取奇点数量（包括运行时奇点）
     */
    public int getSingularityCount() {
        return this.cachedSingularities.size() + this.runtimeSingularities.size();
    }

    /**
     * 获取数据包奇点数量
     */
    public int getDataPackSingularityCount() {
        return this.cachedSingularities.size();
    }

    /**
     * 获取运行时奇点数量
     */
    public int getRuntimeSingularityCount() {
        return this.runtimeSingularities.size();
    }

}