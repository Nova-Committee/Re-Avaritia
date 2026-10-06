package committee.nova.mods.avaritia.init.compat.emi.category;

import committee.nova.mods.avaritia.Const;
import committee.nova.mods.avaritia.common.crafting.recipe.ExtremeSmithingRecipe;
import committee.nova.mods.avaritia.init.registry.ModBlocks;
import dev.emi.emi.api.recipe.EmiRecipe;
import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.render.EmiTexture;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.widget.WidgetHolder;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.List;

public record ExtremeSmithingRecipeCategory(ExtremeSmithingRecipe recipe) implements EmiRecipe {
    private static final EmiTexture TEXTURE = new EmiTexture(ResourceLocation.tryBuild(Const.MOD_ID, "textures/gui/jei/extreme_smithing_jei.png"), 0, 0, 169, 63);
    public static final EmiStack WORKSTATION = EmiStack.of(ModBlocks.extreme_smithing_table.get());
    public static final EmiRecipeCategory CATEGORY = new EmiRecipeCategory(ResourceLocation.tryBuild(Const.MOD_ID, "extreme_smithing_table"), WORKSTATION);

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
        return this.recipe.getIngredients().stream().map(EmiIngredient::of).toList();
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
        return 65;
    }

    @Override
    public void addWidgets(WidgetHolder widgets) {
        widgets.addTexture(TEXTURE, 1, 1);
        widgets.addSlot(EmiIngredient.of(recipe.template), 27, 23).drawBack(false);
        widgets.addSlot(EmiIngredient.of(recipe.base), 45, 23).drawBack(false);
        widgets.addSlot(EmiIngredient.of(Ingredient.of(Arrays.stream(recipe.additions.getItems()).toList().get(0))), 45, 5).drawBack(false);
        widgets.addSlot(EmiIngredient.of(Ingredient.of(Arrays.stream(recipe.additions.getItems()).toList().get(1))), 63, 23).drawBack(false);
        widgets.addSlot(EmiIngredient.of(Ingredient.of(Arrays.stream(recipe.additions.getItems()).toList().get(2))), 45, 41).drawBack(false);
        widgets.addSlot(EmiStack.of(getResultItem(recipe)), 117, 23).recipeContext(this).drawBack(false);
    }

    public static ItemStack getResultItem(Recipe<?> recipe) {
        Minecraft minecraft = Minecraft.getInstance();
        ClientLevel level = minecraft.level;
        if (level == null) {
            throw new NullPointerException("level must not be null.");
        }
        RegistryAccess registryAccess = level.registryAccess();
        return recipe.getResultItem(registryAccess);
    }
}
