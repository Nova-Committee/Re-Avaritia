package committee.nova.mods.avaritia.api.util.recipe;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

/**
 * @author cnlimiter
 */
public class ConfigRecipeManager {
    private static final Logger LOGGER = LogManager.getLogger();
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();
    private static final String CONFIG_PATH = "config/avaritia/recipes";

    public enum RecipeAction {
        ADD,    // 添加新配方
        REMOVE, // 删除现有配方
        REPLACE // 替换现有配方
    }

    public static class RecipeConfig {
        public RecipeAction action;
        public String recipeType;
        public JsonObject recipeData;

        public RecipeConfig(RecipeAction action, String recipeType, JsonObject recipeData) {
            this.action = action;
            this.recipeType = recipeType;
            this.recipeData = recipeData;
        }
    }

    public static Map<ResourceLocation, RecipeConfig> loadConfigRecipes() {
        Map<ResourceLocation, RecipeConfig> configs = new HashMap<>();
        Path configDir = Paths.get(CONFIG_PATH);

        if (!Files.exists(configDir)) {
            try {
                Files.createDirectories(configDir);
            } catch (IOException e) {
                LOGGER.error("Failed to create config directory: {}", CONFIG_PATH, e);
                return configs;
            }
        }

        try {
            Files.walk(configDir)
                    .filter(path -> path.toString().endsWith(".json"))
                    .forEach(path -> {
                        try (BufferedReader reader = Files.newBufferedReader(path)) {
                            JsonObject json = GsonHelper.fromJson(GSON, reader, JsonObject.class);
                            ResourceLocation recipeId = getRecipeIdFromPath(path, configDir);
                            RecipeAction action = parseAction(json);
                            String recipeType = GsonHelper.getAsString(json, "type", "");
                            JsonObject recipeData = GsonHelper.getAsJsonObject(json, "recipe", new JsonObject());

                            configs.put(recipeId, new RecipeConfig(action, recipeType, recipeData));
                        } catch (Exception e) {
                            LOGGER.error("Failed to load recipe config: {}", path, e);
                        }
                    });
        } catch (IOException e) {
            LOGGER.error("Failed to walk config directory: {}", CONFIG_PATH, e);
        }

        return configs;
    }

    private static ResourceLocation getRecipeIdFromPath(Path path, Path root) {
        String relativePath = root.relativize(path).toString();
        String name = relativePath.substring(0, relativePath.length() - 5); // Remove .json
        return new ResourceLocation("avaritia", name.replace('\\', '/'));
    }

    private static RecipeAction parseAction(JsonObject json) {
        String actionStr = GsonHelper.getAsString(json, "action", "ADD");
        try {
            return RecipeAction.valueOf(actionStr.toUpperCase());
        } catch (IllegalArgumentException e) {
            LOGGER.warn("Unknown recipe action: {}, defaulting to ADD", actionStr);
            return RecipeAction.ADD;
        }
    }
}
