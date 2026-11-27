package committee.nova.mods.avaritia.init.handler;

import committee.nova.mods.avaritia.Const;
import committee.nova.mods.avaritia.ModApi;
import committee.nova.mods.avaritia.api.init.event.RegisterRecipesEvent;
import committee.nova.mods.avaritia.core.singularity.Singularity;
import committee.nova.mods.avaritia.core.singularity.SingularityReloadListener;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;

/**
 * Description:
 * Author: cnlimiter
 * Date: 2022/5/15 20:34
 * Version: 1.0
 */
@EventBusSubscriber(modid = Const.MOD_ID)
public class InternalRecipeHandler {
    @SubscribeEvent
    public static void onRegisterRecipes(RegisterRecipesEvent event) {
        SingularityReloadListener.Snapshot snapshot = SingularityReloadListener.INSTANCE.getSnapshot();
        int removedCount = removeDisabledSingularityRecipes(event, snapshot);

        int generatedCount = 0;
        for (var singularity : snapshot.singularities().values()) {
            if (!singularity.isEnabled() || !singularity.isRecipeEnabled()) {
                continue;
            }

            var compressorRecipe = ModApi.addSingularityRecipe(singularity);

            // 默认奇点配方已经由数据包 JSON 加载，这里只补充脚本运行时新增的奇点配方。
            if (compressorRecipe != null && !event.hasRecipe(compressorRecipe.id())) {
                event.addRecipe(compressorRecipe);
                generatedCount++;
            }

        }
        Const.LOGGER.info("Singularity: Regenerated {} recipes, removed {} recipes", generatedCount, removedCount);
    }

    private static int removeDisabledSingularityRecipes(RegisterRecipesEvent event, SingularityReloadListener.Snapshot snapshot) {
        int removedCount = 0;
        for (var singularityId : snapshot.recipeRemovals()) {
            removedCount += event.removeRecipe(Singularity.recipeKey(singularityId));
        }
        return removedCount;
    }
}
