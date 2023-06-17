package committee.nova.mods.avaritia.init.compat.jei.category;

import committee.nova.mods.avaritia.Static;
import committee.nova.mods.avaritia.common.crafting.recipe.ICraftRecipe;
import committee.nova.mods.avaritia.common.crafting.recipe.ShapedExtremeCraftingRecipe;
import committee.nova.mods.avaritia.common.crafting.recipe.ShapelessExtremeCraftingRecipe;
import committee.nova.mods.avaritia.init.compat.jei.JeiCompat;
import committee.nova.mods.avaritia.init.registry.ModBlocks;
import committee.nova.mods.avaritia.util.lang.Localizable;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.category.IRecipeCategory;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

import java.util.Collections;
import java.util.List;

/**
 * Description:
 * Author: cnlimiter
 * Date: 2022/5/16 23:46
 * Version: 1.0
 */
public class ExtremeCraftingTableCategory implements IRecipeCategory<ICraftRecipe> {

    public static final RecipeType<ICraftRecipe> RECIPE_TYPE = RecipeType.create(Static.MOD_ID, "extreme_craft", ICraftRecipe.class);
    private static final ResourceLocation TEXTURE = new ResourceLocation(Static.MOD_ID, "textures/gui/jei/extreme_jei.png");

    private final IDrawable background;
    private final IDrawable icon;
    private final IDrawable shapeless;

    public ExtremeCraftingTableCategory(IGuiHelper helper) {
        this.background = helper.createDrawable(TEXTURE, 0, 0, 190, 163);
        this.icon = helper.createDrawableIngredient(VanillaTypes.ITEM_STACK, new ItemStack(ModBlocks.extreme_crafting_table.get()));
        this.shapeless = helper.createDrawable(JeiCompat.ICONS, 17, 0, 19, 15);
    }


    @Override
    public RecipeType<ICraftRecipe> getRecipeType() {
        return RECIPE_TYPE;
    }

    @Override
    public Component getTitle() {
        return Localizable.of("jei.category.avaritia.extreme_crafting_table").build();
    }

    @Override
    public IDrawable getBackground() {
        return this.background;
    }

    @Override
    public IDrawable getIcon() {
        return this.icon;
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, ICraftRecipe recipe, IFocusGroup focuses) {
        var level = Minecraft.getInstance().level;
        assert level != null;
        var inputs = recipe.getIngredients();
        var output = recipe.getResultItem(level.registryAccess());


        if (recipe instanceof ShapedExtremeCraftingRecipe shaped) {
            int stackIndex = 0;
            int heightOffset = Math.floorDiv(9 - shaped.getHeight(), 2);
            int widthOffset = Math.floorDiv(9 - shaped.getWidth(), 2);

            for (int i = heightOffset; i < shaped.getHeight() + heightOffset; i++) {
                for (int j = widthOffset; j < shaped.getWidth() + widthOffset; j++) {
                    builder.addSlot(RecipeIngredientRole.INPUT, j * 18 + 1, i * 18 + 1).addIngredients(inputs.get(stackIndex));

                    stackIndex++;
                }
            }
        } else if (recipe instanceof ShapelessExtremeCraftingRecipe) {
            for (int i = 0; i < 9; i++) {
                for (int j = 0; j < 9; j++) {
                    int index = j + (i * 9);

                    if (index < inputs.size()) {
                        builder.addSlot(RecipeIngredientRole.INPUT, j * 18 + 1, i * 18 + 1).addIngredients(inputs.get(index));
                    }
                }
            }
            builder.setShapeless(152, 164);
        }
        builder.addSlot(RecipeIngredientRole.OUTPUT, 167, 73).addItemStack(output);
        builder.moveRecipeTransferButton(170, 100);
    }

    @Override
    public List<Component> getTooltipStrings(ICraftRecipe recipe, IRecipeSlotsView recipeSlotsView, double mouseX, double mouseY) {
        var shapeless = recipe instanceof ShapelessExtremeCraftingRecipe;
        int sX = (shapeless ? 340 : 306) / 2, sY = 200 / 2;

        if (shapeless && mouseX > sX + 10 && mouseX < sX + 20 && mouseY > sY - 1 && mouseY < sY + 8) {
            return Collections.singletonList(Localizable.of("jei.tooltip.shapeless.recipe").build());
        }

        return Collections.emptyList();
    }

    @Override
    public void draw(ICraftRecipe recipe, IRecipeSlotsView recipeSlotsView, GuiGraphics gfx, double mouseX, double mouseY) {
        var matrix = gfx.pose();
        matrix.pushPose();
        matrix.scale(0.5F, 0.5F, 0.5F);

        var shapeless = recipe instanceof ShapelessExtremeCraftingRecipe;

        if (shapeless)
            this.shapeless.draw(gfx, 306, 329);

        matrix.popPose();
    }


}
