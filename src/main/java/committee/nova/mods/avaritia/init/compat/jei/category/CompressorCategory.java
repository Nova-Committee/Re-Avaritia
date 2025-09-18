package committee.nova.mods.avaritia.init.compat.jei.category;

import committee.nova.mods.avaritia.Const;
import committee.nova.mods.avaritia.api.common.crafting.ICompressorRecipe;
import committee.nova.mods.avaritia.api.util.lang.Localizable;
import committee.nova.mods.avaritia.init.registry.ModBlocks;
import committee.nova.mods.avaritia.init.registry.ModTooltips;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.builder.ITooltipBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.category.IRecipeCategory;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;

/**
 * Description:
 * Author: cnlimiter
 * Date: 2022/5/15 23:12
 * Version: 1.0
 */
public class CompressorCategory implements IRecipeCategory<ICompressorRecipe> {
    public static final RecipeType<ICompressorRecipe> RECIPE_TYPE = RecipeType.create(Const.MOD_ID, "compressor", ICompressorRecipe.class);
    private static final ResourceLocation TEXTURE = new ResourceLocation(Const.MOD_ID, "textures/gui/jei/compressor.png");
    private final IDrawable background;
    private final IDrawable icon;


    public CompressorCategory(IGuiHelper helper) {
        this.background = helper.createDrawable(TEXTURE, 0, 0, 170, 63);
        this.icon = helper.createDrawableIngredient(VanillaTypes.ITEM_STACK, new ItemStack(ModBlocks.neutron_compressor.get()));
    }


    @Override
    public @NotNull RecipeType<ICompressorRecipe> getRecipeType() {
        return RECIPE_TYPE;
    }

    @Override
    public @NotNull Component getTitle() {
        return Localizable.of("jei.category.avaritia.compressor").build();
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
    public void draw(@NotNull ICompressorRecipe recipe, @NotNull IRecipeSlotsView recipeSlotsView, @NotNull GuiGraphics guiGraphics, double mouseX, double mouseY) {
        this.background.draw(guiGraphics);
    }

    @Override
    public @NotNull IDrawable getIcon() {
        return this.icon;
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, ICompressorRecipe recipe, @NotNull IFocusGroup focuses) {
        var level = Minecraft.getInstance().level;
        assert level != null;
        var inputs = recipe.getIngredients();
        var output = recipe.getResultItem(level.registryAccess());
        builder.addSlot(RecipeIngredientRole.INPUT, 37, 21).addIngredients(inputs.get(0));
        builder.addSlot(RecipeIngredientRole.OUTPUT, 117, 21).addItemStack(output);
        builder.moveRecipeTransferButton(160, 68);
    }

    @Override
    public void getTooltip(@NotNull ITooltipBuilder tooltip, @NotNull ICompressorRecipe recipe, @NotNull IRecipeSlotsView recipeSlotsView, double mouseX, double mouseY) {
        if (mouseX > 62 && mouseX < 77 && mouseY > 21 && mouseY < 36) {
            tooltip.addAll(Collections.singletonList(ModTooltips.NUM_ITEMS.args(recipe.getInputCount()).color(ChatFormatting.LIGHT_PURPLE).build()));
        }

        if (mouseX > 86 && mouseX < 107 && mouseY > 22 && mouseY < 36) {
            tooltip.addAll(Collections.singletonList(ModTooltips.TIME_CONSUME.args(recipe.getTimeCost()).color(ChatFormatting.BLUE).build()));
        }
    }
}
