package com.avaritia.init.handler;

import com.avaritia.Const;
import com.avaritia.ModApi;
import com.avaritia.api.init.event.RegisterRecipesEvent;
import com.avaritia.core.singularity.SingularityReloadListener;
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
        var allSingularities = SingularityReloadListener.INSTANCE.getAllSingularities().values();

        int generatedCount = 0;
        for (var singularity : allSingularities) {
            if (!singularity.isEnabled() || !singularity.isRecipeEnabled()) {
                continue;
            }

            var compressorRecipe = ModApi.addSingularityRecipe(singularity);

            // 默认奇点配方已经由数据包 JSON 加载；这里仅补充第三方运行时新增的奇点配方。
            if (compressorRecipe != null && !event.hasRecipe(compressorRecipe.id())) {
                event.addRecipe(compressorRecipe);
                generatedCount++;
            }

        }
        Const.LOGGER.info("Singularity: Regenerated {} recipes", generatedCount);
    }
}
