package com.avaritia.compat.jei.category.tables;

import com.avaritia.common.crafting.recipe.ShapedTableCraftingRecipe;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.builder.IRecipeSlotBuilder;
import mezz.jei.api.recipe.RecipeIngredientRole;
import net.minecraft.core.NonNullList;
import net.minecraft.world.item.crafting.Ingredient;
import org.jetbrains.annotations.NotNull;

final class TableRecipeLayouts {
    private static final int SLOT_SIZE = 18;

    private TableRecipeLayouts() {
    }

    static void addShapedInputs(@NotNull IRecipeLayoutBuilder builder, ShapedTableCraftingRecipe shaped,
                                int gridSize, int slotX, int slotY) {
        int heightOffset = Math.floorDiv(gridSize - shaped.getHeight(), 2);
        int widthOffset = Math.floorDiv(gridSize - shaped.getWidth(), 2);
        var ingredients = shaped.pattern.ingredients();

        for (int row = 0; row < gridSize; row++) {
            for (int column = 0; column < gridSize; column++) {
                IRecipeSlotBuilder slot = builder.addSlot(RecipeIngredientRole.INPUT,
                        column * SLOT_SIZE + slotX, row * SLOT_SIZE + slotY);
                if (row >= heightOffset && row < heightOffset + shaped.getHeight()
                        && column >= widthOffset && column < widthOffset + shaped.getWidth()) {
                    // shaped pattern 保留了空格位置；getIngredients() 会压缩空槽，直接按矩形读取会让 JEI 越界。
                    int index = (row - heightOffset) * shaped.getWidth() + column - widthOffset;
                    if (index < ingredients.size()) {
                        ingredients.get(index).ifPresent(slot::add);
                    }
                }
            }
        }
    }

    static void addShapelessInputs(@NotNull IRecipeLayoutBuilder builder, NonNullList<Ingredient> inputs,
                                   int gridSize, int slotX, int slotY) {
        for (int row = 0; row < gridSize; row++) {
            for (int column = 0; column < gridSize; column++) {
                int index = column + row * gridSize;
                if (index < inputs.size()) {
                    builder.addSlot(RecipeIngredientRole.INPUT, column * SLOT_SIZE + slotX, row * SLOT_SIZE + slotY)
                            .add(inputs.get(index));
                }
            }
        }
    }
}
