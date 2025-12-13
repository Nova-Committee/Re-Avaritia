package committee.nova.mods.avaritia.init.handler;

import committee.nova.mods.avaritia.Const;
import committee.nova.mods.avaritia.ModApi;
import committee.nova.mods.avaritia.api.init.event.RegisterRecipesEvent;
import committee.nova.mods.avaritia.core.singularity.SingularityReloadListener;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * Description:
 * @author cnlimiter
 * Date: 2022/5/15 20:34
 * Version: 1.0
 */
@Mod.EventBusSubscriber
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

            if (compressorRecipe != null) {
                event.addRecipe(compressorRecipe);
                generatedCount++;
            }

        }
        Const.LOGGER.info("Singularity: Regenerated {} recipes", generatedCount);
    }
}
