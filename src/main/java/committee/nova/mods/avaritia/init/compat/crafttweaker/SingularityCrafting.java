package committee.nova.mods.avaritia.init.compat.crafttweaker;

import com.blamejared.crafttweaker.api.annotation.ZenRegister;
import com.blamejared.crafttweaker.api.ingredient.IIngredient;
import committee.nova.mods.avaritia.Const;
import committee.nova.mods.avaritia.core.singularity.Singularity;
import committee.nova.mods.avaritia.core.singularity.SingularityReloadListener;
import committee.nova.mods.avaritia.core.singularity.SingularityValidationException;
import net.minecraft.resources.ResourceLocation;
import org.openzen.zencode.java.ZenCodeType;

/**
 * @author cnlimiter
 */
@ZenCodeType.Name("mods.avaritia.Singularity")
@ZenRegister
public class SingularityCrafting {
    @ZenCodeType.Method
    public static void register(String key, String displayName, int overlayColor, int underlayColor,
                         int count, int timeCost, IIngredient ingredient, boolean enabled, boolean recipeEnable) {
        ResourceLocation id = parseId(key);
        if (id == null) {
            return;
        }
        var vanillaIngredient = ingredient.asVanillaIngredient();
        try {
            Singularity singularity = new Singularity(id, displayName, overlayColor, underlayColor,
                    count, timeCost, vanillaIngredient, enabled, recipeEnable);
            SingularityReloadListener.INSTANCE.registerScriptSingularity(
                    SingularityReloadListener.ScriptSource.CRAFT_TWEAKER, singularity);
        } catch (SingularityValidationException exception) {
            Const.LOGGER.error("Singularity: Invalid CraftTweaker singularity {}; skipping this entry", key, exception);
        }
    }

    @ZenCodeType.Method
    public static void remove(String id) {
        ResourceLocation parsedId = parseId(id);
        if (parsedId != null) {
            SingularityReloadListener.INSTANCE.removeSingularity(
                    SingularityReloadListener.ScriptSource.CRAFT_TWEAKER, parsedId);
        }
    }

    @ZenCodeType.Method
    public static void removeAll() {
        SingularityReloadListener.INSTANCE.setRemoveAll(
                SingularityReloadListener.ScriptSource.CRAFT_TWEAKER, true);
    }

    @ZenCodeType.Method
    public static void removeRecipe(String id) {
        ResourceLocation parsedId = parseId(id);
        if (parsedId != null) {
            SingularityReloadListener.INSTANCE.removeSingularityRecipe(
                    SingularityReloadListener.ScriptSource.CRAFT_TWEAKER, parsedId);
        }
    }

    @ZenCodeType.Method
    public static void removeAllRecipe() {
        SingularityReloadListener.INSTANCE.setRemoveAllRecipes(
                SingularityReloadListener.ScriptSource.CRAFT_TWEAKER, true);
    }

    private static ResourceLocation parseId(String id) {
        ResourceLocation parsedId = ResourceLocation.tryParse(id);
        if (parsedId == null) {
            Const.LOGGER.error("Singularity: Invalid CraftTweaker singularity id {}; skipping this operation", id);
        }
        return parsedId;
    }
}
