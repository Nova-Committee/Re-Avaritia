package committee.nova.mods.avaritia.api.init.data;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.tags.TagManager;
import net.minecraft.util.GsonHelper;
import java.util.Collection;

/** Loader-independent datapack gates, evaluated against this reload's item tags. */
public final class ResourceConditions {
    private static volatile TagManager tagManager;
    public static void setTagManager(TagManager manager) { tagManager = manager; }

    public static JsonObject tagNotEmpty(String tag) {
        JsonObject empty = new JsonObject();
        empty.addProperty("type", "avaritia:tag_empty");
        empty.addProperty("tag", tag);
        JsonObject not = new JsonObject();
        not.addProperty("type", "avaritia:not");
        not.add("value", empty);
        return not;
    }

    public static boolean processConditions(JsonObject resource) {
        return evaluateResource(resource, true) != Boolean.FALSE;
    }

    /** Used before loot parsing, when tag preparation may still be running. */
    public static boolean modDependenciesSatisfied(JsonObject resource) {
        return evaluateResource(resource, false) != Boolean.FALSE;
    }

    private static Boolean evaluateResource(JsonObject resource, boolean tagsReady) {
        JsonElement conditions = resource.get("avaritia:conditions");
        if (conditions == null) conditions = resource.get("conditions");
        if (conditions == null || !conditions.isJsonArray()) return true;
        Boolean result = true;
        for (JsonElement element : conditions.getAsJsonArray()) {
            // Loot predicates use 'condition', not resource gate 'type'.
            if (!element.isJsonObject() || !element.getAsJsonObject().has("type")) continue;
            Boolean value = evaluate(element.getAsJsonObject(), tagsReady);
            if (Boolean.FALSE.equals(value)) return false;
            if (value == null) result = null;
        }
        return result;
    }

    public static boolean test(JsonObject condition) {
        return Boolean.TRUE.equals(evaluate(condition, true));
    }

    private static Boolean evaluate(JsonObject condition, boolean tagsReady) {
        String type = GsonHelper.getAsString(condition, "type");
        return switch (type) {
            case "avaritia:mod_loaded" -> FabricLoader.getInstance().isModLoaded(GsonHelper.getAsString(condition, "modid"));
            case "avaritia:item_exists" -> BuiltInRegistries.ITEM.containsKey(new ResourceLocation(GsonHelper.getAsString(condition, "item")));
            case "avaritia:tag_empty" -> tagsReady ? tagEmpty(new ResourceLocation(GsonHelper.getAsString(condition, "tag"))) : null;
            case "avaritia:true", "avaritia:infinity_catalyst_recipe" -> true;
            case "avaritia:false" -> false;
            case "avaritia:not" -> {
                Boolean value = evaluate(GsonHelper.getAsJsonObject(condition, "value"), tagsReady);
                yield value == null ? null : !value;
            }
            case "avaritia:and", "avaritia:or" -> {
                JsonArray values = GsonHelper.getAsJsonArray(condition, "values");
                boolean and = type.equals("avaritia:and");
                Boolean result = and;
                for (JsonElement element : values) {
                    Boolean value = evaluate(element.getAsJsonObject(), tagsReady);
                    if (value == null) result = null;
                    else if (value != and) { result = value; break; }
                }
                yield result;
            }
            default -> throw new JsonSyntaxException("Unknown resource condition: " + type);
        };
    }

    private static boolean tagEmpty(ResourceLocation id) {
        TagManager manager = tagManager;
        if (manager != null) {
            for (TagManager.LoadResult<?> result : manager.getResult()) {
                if (result.key().equals(Registries.ITEM)) {
                    Collection<?> values = result.tags().get(id);
                    return values == null || values.isEmpty();
                }
            }
            return true;
        }
        return BuiltInRegistries.ITEM.getTag(TagKey.create(Registries.ITEM, id)).map(tag -> tag.size() == 0).orElse(true);
    }

    private ResourceConditions() {}
}
