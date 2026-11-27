package committee.nova.mods.avaritia.init.compat.emi.category;

import committee.nova.mods.avaritia.Const;
import committee.nova.mods.avaritia.api.common.crafting.ICompressorRecipe;
import committee.nova.mods.avaritia.init.registry.ModBlocks;
import dev.emi.emi.api.recipe.EmiRecipe;
import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.render.EmiTexture;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.widget.WidgetHolder;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public record CompressorCategory(ICompressorRecipe recipe) implements EmiRecipe {
    private static final EmiTexture TEXTURE = new EmiTexture(ResourceLocation.tryBuild(Const.MOD_ID, "textures/gui/jei/compressor.png"), 0, 0, 169, 62);
    public static final EmiStack WORKSTATION = EmiStack.of(ModBlocks.neutron_compressor.get());
    public static final EmiRecipeCategory CATEGORY = new EmiRecipeCategory(ResourceLocation.tryBuild(Const.MOD_ID, "compressor"), WORKSTATION);

    @Override
    public EmiRecipeCategory getCategory() {
        return CATEGORY;
    }

    @Override
    public @NotNull ResourceLocation getId() {
        return this.recipe.getId();
    }

    @Override
    public List<EmiIngredient> getInputs() {
        return this.recipe.getIngredients().stream().map(EmiIngredient::of).map(x -> x.setAmount(this.recipe.getInputCount())).toList();
    }

    @Override
    public List<EmiStack> getOutputs() {
        ClientLevel level = Minecraft.getInstance().level;
        assert level != null;
        return List.of(EmiStack.of(this.recipe.getResultItem(level.registryAccess())));
    }

    @Override
    public int getDisplayWidth() {
        return 171;
    }

    @Override
    public int getDisplayHeight() {
        return 64;
    }

    @Override
    public void addWidgets(WidgetHolder widgets) {
        ClientLevel level = Minecraft.getInstance().level;
        assert level != null;
        widgets.addTexture(TEXTURE, 1, 1);
        widgets.addSlot(this.getInputs().get(0), 37, 21).drawBack(false);
        widgets.addSlot(this.getOutputs().get(0), 117, 21).recipeContext(this).drawBack(false);
    }
}
