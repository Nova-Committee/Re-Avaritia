package com.avaritia.compat.jei.category.tables;

import com.avaritia.Const;
import com.avaritia.api.common.crafting.ITierCraftingRecipe;
import com.avaritia.common.crafting.recipe.EternalSingularityCraftRecipe;
import com.avaritia.common.crafting.recipe.InfinityCatalystCraftRecipe;
import com.avaritia.common.crafting.recipe.ShapedTableCraftingRecipe;
import com.avaritia.common.crafting.recipe.ShapelessTableCraftingRecipe;
import com.avaritia.init.registry.ModBlocks;
import com.avaritia.init.registry.ModItems;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.builder.ITooltipBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.category.IRecipeCategory;
import mezz.jei.api.recipe.types.IRecipeHolderType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.core.NonNullList;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeHolder;
import org.jetbrains.annotations.NotNull;

public class ExtremeCraftingTableCategory implements IRecipeCategory<RecipeHolder<ITierCraftingRecipe>> {
    public static final IRecipeHolderType<ITierCraftingRecipe> RECIPE_TYPE = IRecipeHolderType.create(Identifier.fromNamespaceAndPath(Const.MOD_ID, "extreme_craft"));
    private static final Identifier TEXTURE = Identifier.fromNamespaceAndPath(Const.MOD_ID, "textures/gui/jei/tables/extreme_jei.png");
    private final IDrawable background;
    private final IDrawable icon;

    public ExtremeCraftingTableCategory(IGuiHelper helper) {
        this.background = helper.createDrawable(TEXTURE, 0, 0, 189, 163);
        this.icon = helper.createDrawableIngredient(VanillaTypes.ITEM_STACK, new ItemStack(ModBlocks.extreme_crafting_table.get()));
    }

    @Override
    public @NotNull IRecipeHolderType<ITierCraftingRecipe> getRecipeType() {
        return RECIPE_TYPE;
    }

    @Override
    public @NotNull Component getTitle() {
        return Component.translatable("jei.category.avaritia.extreme_crafting_table");
    }

    @Override
    public int getWidth() {
        return 189;
    }

    @Override
    public int getHeight() {
        return 163;
    }

    @Override
    public void draw(@NotNull RecipeHolder<ITierCraftingRecipe> recipe, @NotNull IRecipeSlotsView recipeSlotsView, @NotNull GuiGraphicsExtractor guiGraphics, double mouseX, double mouseY) {
        this.background.draw(guiGraphics);
    }

    @Override
    public @NotNull IDrawable getIcon() {
        return this.icon;
    }

    @Override
    public void setRecipe(@NotNull IRecipeLayoutBuilder builder, RecipeHolder<ITierCraftingRecipe> recipeHolder, @NotNull IFocusGroup focuses) {
        var recipe = recipeHolder.value();
        var level = Minecraft.getInstance().level;
        assert level != null;
        var inputs = recipe.getIngredients();
        var output = recipe.getResultItem(level.registryAccess());
        if (recipe instanceof ShapedTableCraftingRecipe shaped) {
            int stackIndex = 0;
            int heightOffset = Math.floorDiv(9 - shaped.getHeight(), 2);
            int widthOffset = Math.floorDiv(9 - shaped.getWidth(), 2);
            for (int i = 0; i < 9; i++) {
                for (int j = 0; j < 9; j++) {
                    var slot = builder.addSlot(RecipeIngredientRole.INPUT, j * 18 + 2, i * 18 + 2);
                    if (i >= heightOffset && i < heightOffset + shaped.getHeight()
                            && j >= widthOffset && j < widthOffset + shaped.getWidth()) {
                        slot.add(inputs.get(stackIndex));
                        stackIndex++;
                    }
                }
            }
            builder.addSlot(RecipeIngredientRole.OUTPUT, 167, 73).add(output);
        } else if (recipe instanceof ShapelessTableCraftingRecipe) {
            shapelessRecipe(builder, inputs);
            builder.addSlot(RecipeIngredientRole.OUTPUT, 167, 73).add(output);
        } else if (recipe instanceof InfinityCatalystCraftRecipe) {
            shapelessRecipe(builder, inputs);
            builder.addSlot(RecipeIngredientRole.OUTPUT, 167, 73).add(new ItemStack(ModItems.infinity_catalyst.get()));
        } else if (recipe instanceof EternalSingularityCraftRecipe) {
            shapelessRecipe(builder, inputs);
            builder.addSlot(RecipeIngredientRole.OUTPUT, 167, 73).add(new ItemStack(ModItems.eternal_singularity.get()));
        }
        builder.moveRecipeTransferButton(170, 120);
    }

    private void shapelessRecipe(@NotNull IRecipeLayoutBuilder builder, NonNullList<Ingredient> inputs) {
        for (int i = 0; i < 9; i++) {
            for (int j = 0; j < 9; j++) {
                int index = j + (i * 9);
                if (index < inputs.size()) {
                    builder.addSlot(RecipeIngredientRole.INPUT, j * 18 + 2, i * 18 + 2).add(inputs.get(index));
                }
            }
        }
        builder.setShapeless(163, 150);
    }

    @Override
    public void getTooltip(@NotNull ITooltipBuilder tooltip, @NotNull RecipeHolder<ITierCraftingRecipe> recipeHolder, @NotNull IRecipeSlotsView recipeSlotsView, double mouseX, double mouseY) {
        var recipe = recipeHolder.value();
        var shapeless = recipe instanceof ShapelessTableCraftingRecipe;
        int sX = (shapeless ? 340 : 306) / 2, sY = 200 / 2;
        if (shapeless && mouseX > sX + 10 && mouseX < sX + 20 && mouseY > sY - 1 && mouseY < sY + 8) {
            tooltip.add(Component.translatable("jei.tooltip.shapeless.recipe"));
        }
    }
}
