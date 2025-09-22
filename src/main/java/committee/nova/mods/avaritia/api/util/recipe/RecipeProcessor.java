package committee.nova.mods.avaritia.api.util.recipe;

import com.google.gson.JsonElement;
import net.minecraft.resources.ResourceLocation;

import java.util.Map;

/**
 * @author: cnlimiter
 */
public class RecipeProcessor {
    public static void processRecipes(Map<ResourceLocation, JsonElement> recipes,
                                      Map<ResourceLocation, ConfigRecipeManager.RecipeConfig> configRecipes) {
        // 按优先级处理配方
        configRecipes.entrySet().stream()
                .sorted((e1, e2) -> {
                    // 可以根据文件名或其他属性定义优先级
                    return e1.getKey().toString().compareTo(e2.getKey().toString());
                })
                .forEach(entry -> {
                    ResourceLocation id = entry.getKey();
                    ConfigRecipeManager.RecipeConfig config = entry.getValue();

                    switch (config.action) {
                        case ADD:
                            recipes.put(id, config.recipeData);
                            break;
                        case REMOVE:
                            recipes.remove(id);
                            break;
                        case REPLACE:
                            recipes.put(id, config.recipeData);
                            break;
                    }
                });
    }
}
