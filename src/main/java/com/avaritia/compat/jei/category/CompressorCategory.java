package com.avaritia.compat.jei.category;

import com.avaritia.Avaritia;
import com.avaritia.api.common.crafting.ICompressorRecipe;
import com.avaritia.init.registry.ModBlocks;
import com.avaritia.init.registry.ModTooltips;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.builder.ITooltipBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.types.IRecipeHolderType;
import mezz.jei.api.recipe.category.IRecipeCategory;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import org.jetbrains.annotations.NotNull;

public class CompressorCategory implements IRecipeCategory<RecipeHolder<ICompressorRecipe>> {
    public static final IRecipeHolderType<ICompressorRecipe> RECIPE_TYPE = IRecipeHolderType.create(Identifier.of(Avaritia.MOD_ID, "compressor"));
    private static final Identifier TEXTURE = Identifier.of(Avaritia.MOD_ID, "textures/gui/jei/compressor.png");
    private final IDrawable icon;
    private final IDrawable background;

    public CompressorCategory(IGuiHelper helper) {
        this.background = helper.createDrawable(TEXTURE, 0, 0, 170, 63);
        this.icon = helper.createDrawableIngredient(VanillaTypes.ITEM_STACK, new ItemStack(ModBlocks.neutron_compressor.get()));
    }

    @Override
    public @NotNull IRecipeHolderType<ICompressorRecipe> getRecipeType() {
        return RECIPE_TYPE;
    }

    @Override
    public @NotNull Component getTitle() {
        return Component.translatable("jei.category.avaritia.compressor");
    }

    @Override
    public int getWidth() {
        return 170;
    }

    @Override
    public int getHeight() {
        return 63;
    }

    @Override
    public void draw(@NotNull RecipeHolder<ICompressorRecipe> recipe, @NotNull IRecipeSlotsView recipeSlotsView, @NotNull GuiGraphicsExtractor guiGraphics, double mouseX, double mouseY) {
        this.background.draw(guiGraphics);
    }

    @Override
    public @NotNull IDrawable getIcon() {
        return this.icon;
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, RecipeHolder<ICompressorRecipe> recipeHolder, @NotNull IFocusGroup focuses) {
        var recipe = recipeHolder.value();
        var level = Minecraft.getInstance().level;
        assert level != null;
        var inputs = recipe.getIngredients();
        var output = recipe.getResultItem(level.registryAccess());
        builder.addSlot(RecipeIngredientRole.INPUT, 37, 21).add(inputs.get(0));
        builder.addSlot(RecipeIngredientRole.OUTPUT, 117, 21).add(output);
        builder.moveRecipeTransferButton(160, 68);
    }

    @Override
    public void getTooltip(@NotNull ITooltipBuilder tooltip, @NotNull RecipeHolder<ICompressorRecipe> recipeHolder, @NotNull IRecipeSlotsView recipeSlotsView, double mouseX, double mouseY) {
        var recipe = recipeHolder.value();
        if (mouseX > 62 && mouseX < 77 && mouseY > 21 && mouseY < 36) {
            tooltip.add(ModTooltips.NUM_ITEMS.args(recipe.getInputCount()).color(ChatFormatting.LIGHT_PURPLE).build());
        }

        if (mouseX > 86 && mouseX < 107 && mouseY > 22 && mouseY < 36) {
            tooltip.add(ModTooltips.TIME_CONSUME.args(recipe.getTimeCost()).color(ChatFormatting.BLUE).build());
        }
    }
}
