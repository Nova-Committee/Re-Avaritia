package committee.nova.mods.avaritia.init.handler;

import committee.nova.mods.avaritia.Const;
import committee.nova.mods.avaritia.common.crafting.recipe.EternalSingularityCraftRecipe;
import committee.nova.mods.avaritia.common.item.singularity.Singularity;
import committee.nova.mods.avaritia.init.registry.ModSingularities;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.Event;
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
public class SingularityDataHandler {

    private static final Logger LOGGER = Const.LOGGER;
    private static SingularityDataHandler INSTANCE;

    @Getter @Setter private Map<ResourceLocation, Singularity> cachedSingularities = new LinkedHashMap<>();
    @Getter @Setter private Map<ResourceLocation, Singularity> runtimeSingularities = new LinkedHashMap<>();
    private boolean isInitialized = false;

    public SingularityDataHandler() {
    }

    public static SingularityDataHandler getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new SingularityDataHandler();
        }
        return INSTANCE;
    }

    /**
     * 初始化数据管理器
     */
    public static void onCommonSetup() {
        getInstance().initialize();
        LOGGER.info("SingularityDataManager initialized");
    }

    /**
     * 初始化管理器
     */
    private void initialize() {
        if (this.isInitialized) {
            return;
        }

        this.isInitialized = true;
        this.loadFallbackSingularities();

        LOGGER.info("SingularityDataManager initialized with {} singularities",
                   this.cachedSingularities.size());
    }

    /**
     * 加载备用奇点（当数据包中没有奇点定义时）
     */
    private void loadFallbackSingularities() {
        this.cachedSingularities.clear();

        // 加载默认奇点
        for (var singularity : ModSingularities.getDefaults()) {
            this.cachedSingularities.put(singularity.getId(), singularity);
        }

        LOGGER.info("Loaded {} fallback singularities", this.cachedSingularities.size());
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
        if (singularity != null && singularity.getId() != null) {
            var oldSingularity = this.runtimeSingularities.put(singularity.getId(), singularity);
            if (oldSingularity == null) {
                LOGGER.info("Registered runtime singularity: {}", singularity.getId());
            } else {
                LOGGER.info("Updated runtime singularity: {}", singularity.getId());
            }

            // 使EternalSingularityCraftRecipe缓存失效
            EternalSingularityCraftRecipe.invalidate();

            // 通知奇点更新
            MinecraftForge.EVENT_BUS.post(new SingularityReloadEvent(getAllSingularities()));
        }
    }

    /**
     * 移除运行时奇点
     */
    public Singularity removeRuntimeSingularity(ResourceLocation id) {
        var removed = this.runtimeSingularities.remove(id);
        if (removed != null) {
            LOGGER.info("Removed runtime singularity: {}", id);

            // 使EternalSingularityCraftRecipe缓存失效
            EternalSingularityCraftRecipe.invalidate();

            // 通知奇点更新
            MinecraftForge.EVENT_BUS.post(new SingularityReloadEvent(getAllSingularities()));
        }
        return removed;
    }

    /**
     * 清空所有运行时奇点
     */
    public void clearRuntimeSingularities() {
        int count = this.runtimeSingularities.size();
        this.runtimeSingularities.clear();
        LOGGER.info("Cleared {} runtime singularities", count);

        if (count > 0) {
            // 使EternalSingularityCraftRecipe缓存失效
            EternalSingularityCraftRecipe.invalidate();

            // 通知奇点更新
            MinecraftForge.EVENT_BUS.post(new SingularityReloadEvent(getAllSingularities()));
        }
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

    /**
     * 检查管理器是否已初始化
     */
    public boolean isInitialized() {
        return this.isInitialized;
    }

    /**
     * 奇点重载事件
     */
    public static class SingularityReloadEvent extends Event {
        private final Map<ResourceLocation, Singularity> singularities;

        public SingularityReloadEvent(Map<ResourceLocation, Singularity> singularities) {
            this.singularities = new LinkedHashMap<>(singularities);
        }

        public Map<ResourceLocation, Singularity> getSingularities() {
            return new LinkedHashMap<>(this.singularities);
        }
    }
}