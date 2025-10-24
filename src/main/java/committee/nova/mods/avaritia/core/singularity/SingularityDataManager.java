package committee.nova.mods.avaritia.core.singularity;

import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import committee.nova.mods.avaritia.Const;
import committee.nova.mods.avaritia.common.crafting.recipe.EternalSingularityCraftRecipe;
import committee.nova.mods.avaritia.util.SingularityUtils;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.resources.FileToIdConverter;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.GsonHelper;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.crafting.conditions.ICondition;
import net.minecraftforge.eventbus.api.Event;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.io.Reader;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

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
    public static final SingularityDataManager INSTANCE = new SingularityDataManager();

    @Getter @Setter private List<Singularity> singularities = new CopyOnWriteArrayList<>();
    public SingularityDataManager() {
    }

    public static List<Singularity> loadSingularities(ResourceManager resourceManager, ICondition.IContext conditionContext) {
        List<Singularity> singularities = new CopyOnWriteArrayList<>();

        Map<ResourceLocation, JsonElement> map = new ConcurrentHashMap<>();
        FileToIdConverter filetoidconverter = FileToIdConverter.json("singularities");

        for(Map.Entry<ResourceLocation, Resource> entry : filetoidconverter.listMatchingResources(resourceManager).entrySet()) {
            ResourceLocation resourcelocation = entry.getKey();
            ResourceLocation resourcelocation1 = filetoidconverter.fileToId(resourcelocation);

            try (Reader reader = entry.getValue().openAsReader()) {
                JsonElement jsonelement = GsonHelper.fromJson(Const.GSON, reader, JsonElement.class);
                JsonElement jsonelement1 = map.put(resourcelocation1, jsonelement);
                if (jsonelement1 != null) {
                    throw new IllegalStateException("Duplicate data file ignored with ID " + resourcelocation1);
                }
            } catch (IllegalArgumentException | IOException | JsonParseException jsonparseexception) {
                LOGGER.error("Couldn't parse data file {} from {}", resourcelocation1, resourcelocation, jsonparseexception);
            }
        }


        for(Map.Entry<ResourceLocation, JsonElement> entry : map.entrySet()) {
            var singularity = SingularityUtils.loadFromJson(entry.getKey(), entry.getValue().getAsJsonObject(), conditionContext);
            if (singularity != null) {
                singularities.add(singularity);
            }
        }

        return singularities;
    }

    public void onSingularitiesReloaded(List<Singularity> singularities) {
        MinecraftForge.EVENT_BUS.post(new SingularityDataManager.SingularityReloadEvent(singularities));
        // 这里可以添加通知其他组件的逻辑
        // 例如：刷新配方缓存、重新计算某些数据等
    }

    /**
     * 根据ID获取奇点（优先运行时奇点）
     */
    public Singularity getSingularity(ResourceLocation id) {
        for (Singularity singularity : this.singularities) {
            if(singularity.getId().equals(id)) {
                return singularity;
            }
        }
        return null;
    }
    /**
     * 注册运行时奇点
     */
    public void registerSingularity(Singularity singularity) {
        if (singularity != null && singularity.getId() != null) {
            this.singularities.add(singularity);
            // 使EternalSingularityCraftRecipe缓存失效
            LOGGER.info("Registered singularity: {}", singularity.getId());
            EternalSingularityCraftRecipe.invalidate();
            // 通知奇点更新
            MinecraftForge.EVENT_BUS.post(new SingularityReloadEvent(getSingularities()));
        }
    }

    /**
     * 奇点重载事件
     */
    public static class SingularityReloadEvent extends Event {
        private final List<Singularity> singularities;

        public SingularityReloadEvent(List<Singularity> singularities) {
            this.singularities = new CopyOnWriteArrayList<>(singularities);
        }

        public List<Singularity> getSingularities() {
            return new CopyOnWriteArrayList<>(this.singularities);
        }
    }
}