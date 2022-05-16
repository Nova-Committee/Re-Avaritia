package nova.committee.avaritia.init.compat.jei;

import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.registration.*;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import nova.committee.avaritia.Static;
import nova.committee.avaritia.client.screen.CompressorScreen;
import nova.committee.avaritia.init.compat.jei.category.CompressorCategory;
import nova.committee.avaritia.init.registry.ModBlocks;
import nova.committee.avaritia.init.registry.ModItems;
import nova.committee.avaritia.init.registry.ModRecipe;
import nova.committee.avaritia.util.SingularityUtils;
import org.jetbrains.annotations.NotNull;

/**
 * Description:
 * Author: cnlimiter
 * Date: 2022/5/15 23:09
 * Version: 1.0
 */
@JeiPlugin
public class JeiCompat implements IModPlugin {
    public static final ResourceLocation UID = new ResourceLocation(Static.MOD_ID, "jei_plugin");

    @Override
    public ResourceLocation getPluginUid() {
        return UID;
    }

    @Override
    public void registerCategories(IRecipeCategoryRegistration registration) {
        var helper = registration.getJeiHelpers().getGuiHelper();
        registration.addRecipeCategories(new CompressorCategory(helper));
    }

    @Override
    public void registerRecipes(@NotNull IRecipeRegistration registration) {
        var world = Minecraft.getInstance().level;
        if (world != null) {
            var manager = world.getRecipeManager();
            registration.addRecipes(manager.byType(ModRecipe.RecipeTypes.COMPRESSOR).values(), CompressorCategory.UID);

        }

    }

    @Override
    public void registerRecipeCatalysts(IRecipeCatalystRegistration registration) {
        registration.addRecipeCatalyst(new ItemStack(ModBlocks.compressor), CompressorCategory.UID);

    }

    @Override
    public void registerGuiHandlers(IGuiHandlerRegistration registration) {
        registration.addRecipeClickArea(CompressorScreen.class, 97, 47, 21, 14, CompressorCategory.UID);

    }

    @Override
    public void registerItemSubtypes(ISubtypeRegistration registration) {
        registration.registerSubtypeInterpreter(ModItems.singularity, (stack, context) -> {
            var singularity = SingularityUtils.getSingularity(stack);
            return singularity != null ? singularity.getId().toString() : "";
        });
    }
}
