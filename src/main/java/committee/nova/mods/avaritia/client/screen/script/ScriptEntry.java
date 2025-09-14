package committee.nova.mods.avaritia.client.screen.script;

import com.google.gson.JsonObject;
import net.minecraft.world.item.crafting.Recipe;

/**
 * @author: cnlimiter
 * 脚本条目类
 */
public class ScriptEntry {
    private String name;
    private RecipeType recipeType;
    private int tier;
    private Recipe<?> recipe;
    private String scriptContent;

    public ScriptEntry(String name, RecipeType recipeType, int tier, Recipe<?> recipe, String scriptContent) {
        this.name = name;
        this.recipeType = recipeType;
        this.tier = tier;
        this.recipe = recipe;
        this.scriptContent = scriptContent;
    }

    // Getters and setters
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public RecipeType getRecipeType() {
        return recipeType;
    }

    public void setRecipeType(RecipeType recipeType) {
        this.recipeType = recipeType;
    }

    public int getTier() {
        return tier;
    }

    public void setTier(int tier) {
        this.tier = tier;
    }

    public Recipe<?> getRecipe() {
        return recipe;
    }

    public void setRecipe(Recipe<?> recipe) {
        this.recipe = recipe;
    }

    public String getScriptContent() {
        return scriptContent;
    }

    public void setScriptContent(String scriptContent) {
        this.scriptContent = scriptContent;
    }

    /**
     * 转换为JSON对象
     */
    public JsonObject toJson() {
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("name", this.name);
        jsonObject.addProperty("recipeType", this.recipeType.name());
        jsonObject.addProperty("tier", this.tier);
        jsonObject.addProperty("scriptContent", this.scriptContent);
        // 注意：Recipe对象不直接序列化到JSON中，因为它是复杂的Minecraft对象
        return jsonObject;
    }

    /**
     * 从JSON对象创建ScriptEntry
     */
    public static ScriptEntry fromJson(JsonObject jsonObject) {
        try {
            String name = jsonObject.get("name").getAsString();
            String recipeTypeStr = jsonObject.get("recipeType").getAsString();
            int tier = jsonObject.get("tier").getAsInt();
            String scriptContent = jsonObject.get("scriptContent").getAsString();

            RecipeType recipeType = RecipeType.valueOf(recipeTypeStr);

            return new ScriptEntry(name, recipeType, tier, null, scriptContent);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 创建副本
     */
    public ScriptEntry copy() {
        return new ScriptEntry(this.name, this.recipeType, this.tier, this.recipe, this.scriptContent);
    }

    @Override
    public String toString() {
        return "ScriptEntry{" +
                "name='" + name + '\'' +
                ", recipeType=" + recipeType +
                ", tier=" + tier +
                '}';
    }
}