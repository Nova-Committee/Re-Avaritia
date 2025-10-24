package committee.nova.mods.avaritia.init.handler;

import committee.nova.mods.avaritia.Const;
import committee.nova.mods.avaritia.ModApi;
import committee.nova.mods.avaritia.api.init.event.RegisterRecipesEvent;
import committee.nova.mods.avaritia.core.singularity.Singularity;
import committee.nova.mods.avaritia.core.singularity.SingularityDataManager;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.Collection;
import java.util.List;

/**
 * Description:
 * Author: cnlimiter
 * Date: 2022/5/15 20:34
 * Version: 1.0
 */
@Mod.EventBusSubscriber(modid = Const.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class InternalRecipeHandler {
    @SubscribeEvent
    public static void onRegisterRecipes(RegisterRecipesEvent event) {
        List<Singularity> allSingularities = SingularityDataManager.INSTANCE.getSingularities();
        for (var singularity : allSingularities) {
            if (singularity.isRecipeDisabled()) {
                continue;
            }

            var compressorRecipe = ModApi.addSingularityRecipe(singularity);

            if (compressorRecipe != null)
                event.addRecipe(compressorRecipe);
        }
    }
}
