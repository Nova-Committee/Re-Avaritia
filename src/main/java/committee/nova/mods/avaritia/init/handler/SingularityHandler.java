package committee.nova.mods.avaritia.init.handler;

import committee.nova.mods.avaritia.ModApi;
import committee.nova.mods.avaritia.api.util.recipe.RecipeUtils;
import committee.nova.mods.avaritia.core.singularity.Singularity;
import committee.nova.mods.avaritia.core.singularity.SingularityRuntimeEvent;
import committee.nova.mods.avaritia.core.singularity.SingularityReloadEvent;
import committee.nova.mods.avaritia.init.registry.ModRecipeTypes;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import static committee.nova.mods.avaritia.Const.LOGGER;

/**
 * @author cnlimiter
 */
@Mod.EventBusSubscriber
public class SingularityHandler {
    @SubscribeEvent
    public static void onAddSingularity(SingularityRuntimeEvent.Add event) {
        if (event.getSingularity().isRecipeDisabled()) return;
        try {
            var compressorRecipe = ModApi.addSingularityRecipe(event.getSingularity());
            if (compressorRecipe != null) {
                // 尝试动态添加配方到配方管理器
                RecipeUtils.addRecipe(compressorRecipe);
                LOGGER.info("Singularity: Added compressor recipe for runtime singularity: {}", event.getSingularity().getId());
            }
        } catch (Exception e) {
            LOGGER.error("Singularity: Failed to add compressor recipe for runtime singularity: {}", event.getSingularity().getId(), e);
        }
    }

    @SubscribeEvent
    public static void onRemoveSingularity(SingularityRuntimeEvent.Remove event) {
        try {
            // 构建配方ID（奇点ID + _singularity后缀）
            ResourceLocation recipeId = new ResourceLocation(
                    event.getSingularityId().getNamespace(),
                    event.getSingularityId().getPath() + "_singularity"
            );

            RecipeUtils.removeRecipe(ModRecipeTypes.COMPRESSOR_RECIPE.get(), recipeId);
            LOGGER.info("Singularity: Removed compressor recipe {} for singularity: {}", recipeId, event.getSingularityId());
        } catch (Exception e) {
            LOGGER.warn("Singularity: Failed to remove compressor recipe for singularity: {}", event.getSingularityId(), e);
        }
    }

    /**
     * 重新生成压缩机配方
     * 在奇点数据重载完成后调用，确保压缩机配方使用最新的奇点数据
     */
    @SubscribeEvent
    public static void onReloadSingularity(SingularityReloadEvent event) {
        try {
            // 清理旧的压缩机配方
            RecipeUtils.byType(ModRecipeTypes.COMPRESSOR_RECIPE.get()).forEach((id, recipe) -> {
                if (id.getPath().endsWith("_singularity")) {
                    RecipeUtils.removeRecipe(ModRecipeTypes.COMPRESSOR_RECIPE.get(),id);
                }
            });

            // 为新的奇点生成压缩机配方
            int generatedCount = 0;
            for (Singularity singularity : event.getSingularities().values()) {
                if (singularity.isRecipeDisabled()) {
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
