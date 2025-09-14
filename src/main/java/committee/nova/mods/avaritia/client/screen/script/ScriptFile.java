package committee.nova.mods.avaritia.client.screen.script;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import net.minecraft.world.item.crafting.Recipe;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

/**
 * @author: cnlimiter
 * 脚本文件类，用于管理配方脚本文件
 */
public class ScriptFile {
    private final File file;
    private final List<ScriptEntry> scripts;
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    public ScriptFile(File file) {
        this.file = file;
        this.scripts = new ArrayList<>();
        this.loadScripts();
    }

    /**
     * 从文件中加载脚本条目
     */
    private void loadScripts() {
        try {
            if (!this.file.exists()) {
                return;
            }

            // 如果是JSON文件，使用JSON格式加载
            if (this.file.getName().endsWith(".json")) {
                loadFromJson();
            } else {
                // 如果是脚本文件(.js, .zs等)，解析脚本内容
                loadFromScript();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 从JSON文件加载脚本
     */
    private void loadFromJson() {
        try (FileReader reader = new FileReader(this.file)) {
            JsonObject jsonObject = GSON.fromJson(reader, JsonObject.class);

            if (jsonObject.has("scripts") && jsonObject.get("scripts").isJsonArray()) {
                JsonArray scriptsArray = jsonObject.getAsJsonArray("scripts");

                for (int i = 0; i < scriptsArray.size(); i++) {
                    JsonObject scriptObject = scriptsArray.get(i).getAsJsonObject();
                    ScriptEntry script = ScriptEntry.fromJson(scriptObject);
                    if (script != null) {
                        this.scripts.add(script);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 从脚本文件加载脚本
     */
    private void loadFromScript() {
        try (BufferedReader reader = Files.newBufferedReader(this.file.toPath())) {
            String line;
            ScriptEntry currentScript = null;
            StringBuilder scriptContent = new StringBuilder();

            while ((line = reader.readLine()) != null) {
                // 查找脚本开始标记
                if (line.trim().startsWith("// SCRIPT_START:")) {
                    if (currentScript != null) {
                        // 保存之前的脚本
                        currentScript.setScriptContent(scriptContent.toString());
                        this.scripts.add(currentScript);
                        scriptContent = new StringBuilder();
                    }

                    // 解析脚本名称和类型
                    String[] parts = line.trim().substring(15).split(",");
                    String name = parts.length > 0 ? parts[0].trim() : "Unnamed Script";
                    String type = parts.length > 1 ? parts[1].trim() : "UNKNOWN";
                    int tier = parts.length > 2 ? Integer.parseInt(parts[2].trim()) : 1;

                    currentScript = new ScriptEntry(name,
                            RecipeType.valueOf(type),
                            tier, null, "");
                } else if (line.trim().startsWith("// SCRIPT_END")) {
                    // 脚本结束标记
                    if (currentScript != null) {
                        currentScript.setScriptContent(scriptContent.toString());
                        this.scripts.add(currentScript);
                        currentScript = null;
                        scriptContent = new StringBuilder();
                    }
                } else {
                    // 添加脚本内容
                    if (currentScript != null) {
                        scriptContent.append(line).append("\n");
                    }
                }
            }

            // 保存最后一个脚本（如果没有结束标记）
            if (currentScript != null) {
                currentScript.setScriptContent(scriptContent.toString());
                this.scripts.add(currentScript);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 将脚本保存到文件
     */
    public void save() {
        try {
            // 创建目录（如果不存在）
            File parentDir = this.file.getParentFile();
            if (parentDir != null && !parentDir.exists()) {
                parentDir.mkdirs();
            }

            // 根据文件扩展名选择保存方式
            if (this.file.getName().endsWith(".json")) {
                saveToJson();
            } else {
                saveToScript();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 保存为JSON格式
     */
    private void saveToJson() {
        try (FileWriter writer = new FileWriter(this.file)) {
            JsonObject jsonObject = new JsonObject();
            JsonArray scriptsArray = new JsonArray();

            for (ScriptEntry script : this.scripts) {
                JsonObject scriptObject = script.toJson();
                scriptsArray.add(scriptObject);
            }

            jsonObject.add("scripts", scriptsArray);
            GSON.toJson(jsonObject, writer);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 保存为脚本格式
     */
    private void saveToScript() {
        try (FileWriter writer = new FileWriter(this.file)) {
            for (ScriptEntry script : this.scripts) {
                // 写入脚本开始标记
                writer.write("// SCRIPT_START:" + script.getName() + "," +
                        script.getRecipeType().name() + "," + script.getTier() + "\n");

                // 写入脚本内容
                writer.write(script.getScriptContent());

                // 写入脚本结束标记
                writer.write("\n// SCRIPT_END\n\n");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 添加脚本
     */
    public void addScript(ScriptEntry script) {
        this.scripts.add(script);
    }

    /**
     * 移除脚本
     */
    public void removeScript(ScriptEntry script) {
        this.scripts.remove(script);
    }

    /**
     * 获取文件
     */
    public File getFile() {
        return file;
    }

    /**
     * 获取脚本列表
     */
    public List<ScriptEntry> getScripts() {
        return scripts;
    }

    /**
     * 创建新的脚本文件
     */
    public static ScriptFile createNew(String fileName) {
        try {
            Path configPath = Paths.get("config", "avaritia", "recipe");
            if (!Files.exists(configPath)) {
                Files.createDirectories(configPath);
            }

            Path filePath = configPath.resolve(fileName);
            File file = filePath.toFile();

            // 如果文件不存在，创建空文件
            if (!file.exists()) {
                file.createNewFile();
            }

            return new ScriptFile(file);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 从配方创建脚本条目
     */
    public ScriptEntry createScriptFromRecipe(String name, RecipeType type,
                                              int tier, Recipe<?> recipe) {
        // 生成脚本内容（这里只是一个示例，实际实现需要根据配方类型生成具体脚本）
        StringBuilder content = new StringBuilder();
        content.append("// Generated recipe script\n");
        content.append("// Recipe type: ").append(type.name()).append("\n");
        content.append("// Tier: ").append(tier).append("\n");

        ScriptEntry script = new ScriptEntry(name, type, tier, recipe, content.toString());
        this.addScript(script);
        return script;
    }

    /**
     * 获取文件名（不含扩展名）
     */
    public String getName() {
        String fileName = this.file.getName();
        int dotIndex = fileName.lastIndexOf('.');
        return dotIndex > 0 ? fileName.substring(0, dotIndex) : fileName;
    }

    /**
     * 获取文件扩展名
     */
    public String getExtension() {
        String fileName = this.file.getName();
        int dotIndex = fileName.lastIndexOf('.');
        return dotIndex > 0 ? fileName.substring(dotIndex + 1) : "";
    }

    /**
     * 检查是否为JSON文件
     */
    public boolean isJsonFile() {
        return this.file.getName().endsWith(".json");
    }

    /**
     * 检查是否为脚本文件
     */
    public boolean isScriptFile() {
        String name = this.file.getName().toLowerCase();
        return name.endsWith(".js") || name.endsWith(".zs");
    }
}
