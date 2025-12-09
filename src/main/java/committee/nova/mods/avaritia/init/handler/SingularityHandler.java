package committee.nova.mods.avaritia.init.handler;

import committee.nova.mods.avaritia.Const;
import committee.nova.mods.avaritia.ModApi;
import committee.nova.mods.avaritia.api.util.recipe.RecipeUtils;
import committee.nova.mods.avaritia.core.singularity.SingularityEvent;
import committee.nova.mods.avaritia.core.singularity.SingularityReloadListener;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * @author cnlimiter
 */
@Mod.EventBusSubscriber
public class SingularityHandler {

    /**
     * 重新生成压缩机配方
     * 在奇点数据重载完成后调用，确保压缩机配方使用最新的奇点数据
     */
    @SubscribeEvent
    public static void onReloadSingularity(SingularityEvent.Reload event) {
        var allSingularities = event.getAllSingularities().values();

        int generatedCount = 0;
        for (var singularity : allSingularities) {
            if (!singularity.isEnabled() || !singularity.isRecipeEnabled()) {
                continue;
            }

            var compressorRecipe = ModApi.addSingularityRecipe(singularity);

            if (compressorRecipe != null) {
                RecipeUtils.addRecipe(compressorRecipe);
                generatedCount++;
            }

        }
        Const.LOGGER.info("Singularity: Regenerated {} recipes", generatedCount);
    }
}
