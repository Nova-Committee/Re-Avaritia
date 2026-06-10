package committee.nova.mods.avaritia.init.handler;

import committee.nova.mods.avaritia.Const;
import committee.nova.mods.avaritia.ModApi;
import committee.nova.mods.avaritia.api.init.event.RegisterRecipesEvent;
import committee.nova.mods.avaritia.core.singularity.SingularityReloadListener;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.crafting.Recipe;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;

import java.util.LinkedHashSet;
import java.util.Set;

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
        SingularityReloadListener listener = SingularityReloadListener.INSTANCE;
        int removedCount = removeDisabledSingularityRecipes(event, listener);
        var allSingularities = listener.getAllSingularities().values();

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
        Const.LOGGER.info("Singularity: Regenerated {} recipes, removed {} recipes", generatedCount, removedCount);
    }

    private static int removeDisabledSingularityRecipes(RegisterRecipesEvent event, SingularityReloadListener listener) {
        Set<Identifier> singularities = new LinkedHashSet<>();
        if (listener.isRemoveAll() || listener.isRemoveAllRecipes()) {
            singularities.addAll(listener.getDataSingularities().keySet());
            singularities.addAll(listener.getRunSingularities().keySet());
        }
        singularities.addAll(listener.getRemoveRecipes());
        singularities.addAll(listener.getRemoveSingularities());

        int removedCount = 0;
        for (Identifier singularityId : singularities) {
            removedCount += event.removeRecipe(singularityRecipeKey(singularityId));
        }
        return removedCount;
    }

    private static ResourceKey<Recipe<?>> singularityRecipeKey(Identifier singularityId) {
        return ResourceKey.create(Registries.RECIPE, Identifier.fromNamespaceAndPath(singularityId.getNamespace(), singularityId.getPath() + "_singularity"));
    }
}
