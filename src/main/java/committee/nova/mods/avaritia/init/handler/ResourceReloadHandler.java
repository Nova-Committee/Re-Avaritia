package committee.nova.mods.avaritia.init.handler;

import committee.nova.mods.avaritia.Const;
import committee.nova.mods.avaritia.core.singularity.Singularity;
import committee.nova.mods.avaritia.core.singularity.SingularityDataManager;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManagerReloadListener;
import net.minecraftforge.event.AddReloadListenerEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.Map;

/**
 * Description:
 * Author: cnlimiter
 * Date: 2022/5/15 11:40
 * Version: 1.0
 */
@Mod.EventBusSubscriber
public class ResourceReloadHandler {
    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onAddReloadListeners(AddReloadListenerEvent event) {
        event.addListener((ResourceManagerReloadListener) resourceManager -> {
            var singularities = SingularityDataManager.loadSingularities(resourceManager, event.getConditionContext());

            SingularityDataManager.INSTANCE.getCachedSingularities().clear();
            SingularityDataManager.INSTANCE.setCachedSingularities(singularities);
            Const.LOGGER.info("Loaded {} singularities", singularities.size());
            // 通知其他组件奇点数据已更新
            SingularityDataManager.INSTANCE.onSingularitiesReloaded(singularities);
        });
    }
}
