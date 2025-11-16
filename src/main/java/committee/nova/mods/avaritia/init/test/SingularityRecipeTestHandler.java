package committee.nova.mods.avaritia.init.test;

import committee.nova.mods.avaritia.Const;
import committee.nova.mods.avaritia.api.util.recipe.RecipeUtils;
import committee.nova.mods.avaritia.core.singularity.Singularity;
import committee.nova.mods.avaritia.core.singularity.SingularityDataManager;
import committee.nova.mods.avaritia.init.registry.ModRecipeTypes;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraftforge.event.server.ServerStartedEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.util.Map;

/**
 * 奇点配方测试处理器
 * 用于验证奇点和压缩机配方的同步是否正常工作
 *
 * @author cnlimiter
 */
public class SingularityRecipeTestHandler {

    @SubscribeEvent
    public static void onServerStarted(ServerStartedEvent event) {
        // 服务器启动后进行测试验证
        validateSingularityRecipeSync();
    }

    /**
     * 验证奇点和压缩机配方的同步状态
     */
    private static void validateSingularityRecipeSync() {
        Const.LOGGER.info("开始验证奇点配方同步状态...");

        try {
            // 获取所有奇点
            var singularities = SingularityDataManager.getInstance().getSingularities();
            int totalSingularities = singularities.size();
            int enabledSingularities = (int) singularities.stream()
                    .filter(s -> !s.isRecipeDisabled())
                    .count();

            Const.LOGGER.info("总奇点数量: {}, 启用配方的奇点数量: {}", totalSingularities, enabledSingularities);

            // 获取压缩机配方
            RecipeManager recipeManager = RecipeUtils.getRecipeManager();
            if (recipeManager == null) {
                Const.LOGGER.warn("无法获取RecipeManager，跳过配方验证");
                return;
            }

            var compressorRecipes = recipeManager.byType(ModRecipeTypes.COMPRESSOR_RECIPE.get());
            int singularityRecipes = 0;
            int matchedRecipes = 0;

            // 统计奇点相关的压缩机配方
            for (Map.Entry<ResourceLocation, ?> entry : compressorRecipes.entrySet()) {
                ResourceLocation recipeId = entry.getKey();
                if (recipeId.getPath().endsWith("_singularity")) {
                    singularityRecipes++;

                    // 检查对应的奇点是否存在
                    String singularityPath = recipeId.getPath().replace("_singularity", "");
                    ResourceLocation singularityId = new ResourceLocation(recipeId.getNamespace(), singularityPath);

                    if (SingularityDataManager.getInstance().hasSingularity(singularityId)) {
                        matchedRecipes++;
                    } else {
                        Const.LOGGER.warn("发现孤立的压缩机配方: {}, 对应的奇点不存在", recipeId);
                    }
                }
            }

            Const.LOGGER.info("压缩机配方统计 - 奇点相关配方: {}, 匹配的配方: {}", singularityRecipes, matchedRecipes);

            // 验证结果
            if (enabledSingularities == matchedRecipes) {
                Const.LOGGER.info("✅ 奇点配方同步验证通过! 所有启用的奇点都有对应的压缩机配方");
            } else {
                Const.LOGGER.error("❌ 奇点配方同步验证失败! 启用的奇点数量({})与匹配的配方数量({})不一致",
                    enabledSingularities, matchedRecipes);

                // 输出详细信息以便调试
                for (Singularity singularity : singularities) {
                    if (!singularity.isRecipeDisabled()) {
                        ResourceLocation expectedRecipeId = new ResourceLocation(
                            singularity.getRegistryName().getNamespace(),
                            singularity.getRegistryName().getPath() + "_singularity"
                        );

                        boolean hasRecipe = compressorRecipes.containsKey(expectedRecipeId);
                        Const.LOGGER.info("奇点: {} -> 配方: {} ({})",
                            singularity.getRegistryName(),
                            expectedRecipeId,
                            hasRecipe ? "存在" : "缺失");
                    }
                }
            }

        } catch (Exception e) {
            Const.LOGGER.error("奇点配方同步验证过程中发生错误", e);
        }
    }
}