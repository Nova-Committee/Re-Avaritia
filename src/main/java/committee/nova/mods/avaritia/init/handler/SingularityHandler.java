package committee.nova.mods.avaritia.init.handler;

import committee.nova.mods.avaritia.Const;
import committee.nova.mods.avaritia.ModApi;
import committee.nova.mods.avaritia.api.utils.RecipeUtils;
import committee.nova.mods.avaritia.core.singularity.Singularity;
import committee.nova.mods.avaritia.core.singularity.SingularityDataManager;
import committee.nova.mods.avaritia.core.singularity.SingularityReloadEvent;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;

import static committee.nova.mods.avaritia.Const.LOGGER;

/**
 * @author cnlimiter
 */
@EventBusSubscriber(modid = Const.MOD_ID)
public class SingularityHandler {

    /**
     * 重新生成压缩机配方
     * 在奇点数据重载完成后调用，确保压缩机配方使用最新的奇点数据
     */
    @SubscribeEvent
    public static void onReloadSingularity(SingularityReloadEvent event) {
        try {
            SingularityDataManager.getInstance().setCachedSingularities(event.getSingularities());
            // 为新的奇点生成压缩机配方
            int generatedCount = 0;
            for (Singularity singularity : SingularityDataManager.getInstance().getSingularities()) {
                if (!singularity.isRecipeEnabled()) {
                    continue;
                }

                var compressorRecipe = ModApi.addSingularityRecipe(singularity);
                if (compressorRecipe != null) {
                    // 动态添加配方到配方管理器
                    RecipeUtils.addRecipe(compressorRecipe);
                    generatedCount++;
                }
            }

            LOGGER.info("Singularity: Regenerated {} recipes", generatedCount);

        } catch (Exception e) {
            LOGGER.error("Singularity: Failed to regenerate recipes", e);
        }
    }
}
