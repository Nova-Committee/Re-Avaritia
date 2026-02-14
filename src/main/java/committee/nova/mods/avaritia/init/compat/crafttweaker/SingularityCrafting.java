package committee.nova.mods.avaritia.init.compat.crafttweaker;

import com.blamejared.crafttweaker.api.annotation.ZenRegister;
import com.blamejared.crafttweaker.api.ingredient.IIngredient;
import committee.nova.mods.avaritia.core.singularity.Singularity;
import committee.nova.mods.avaritia.core.singularity.SingularityReloadListener;
import net.minecraft.resources.ResourceLocation;
import org.openzen.zencode.java.ZenCodeType;

/**
 * @author cnlimiter
 */
@ZenCodeType.Name("mods.avaritia.Singularity")
@ZenRegister
public class SingularityCrafting {
    @ZenCodeType.Method
    public void register(String key, String displayName, int overlayColor, int underlayColor,
                         int count, int timeCost, IIngredient ingredient, boolean enabled, boolean recipeEnable) {
        Singularity singularity = new Singularity(ResourceLocation.parse(key), displayName, overlayColor, underlayColor, count, timeCost, ingredient.asVanillaIngredient(), enabled, recipeEnable);
        SingularityReloadListener.INSTANCE.registerSingularity(singularity);
    }

    @ZenCodeType.Method
    public static void remove(String id) {
        SingularityReloadListener.INSTANCE.removeSingularity(ResourceLocation.parse(id));
    }

    @ZenCodeType.Method
    public static void removeAll() {
        SingularityReloadListener.INSTANCE.setRemoveAll(true);
    }

    @ZenCodeType.Method
    public static void removeRecipe(String id) {
        SingularityReloadListener.INSTANCE.removeSingularityRecipe(ResourceLocation.parse(id));
    }

    @ZenCodeType.Method
    public static void removeAllRecipe() {
        SingularityReloadListener.INSTANCE.setRemoveAllRecipes(true);
    }
}
