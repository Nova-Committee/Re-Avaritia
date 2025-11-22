package committee.nova.mods.avaritia.init.compat.jei.category.tables;

import committee.nova.mods.avaritia.Const;
import committee.nova.mods.avaritia.api.common.crafting.ITierCraftingRecipe;
import committee.nova.mods.avaritia.api.utils.lang.Localizable;
import committee.nova.mods.avaritia.common.crafting.recipe.EternalSingularityCraftRecipe;
import committee.nova.mods.avaritia.common.crafting.recipe.InfinityCatalystCraftRecipe;
import committee.nova.mods.avaritia.common.crafting.recipe.ShapedTableCraftingRecipe;
import committee.nova.mods.avaritia.common.crafting.recipe.ShapelessTableCraftingRecipe;
import committee.nova.mods.avaritia.init.registry.ModBlocks;
import committee.nova.mods.avaritia.init.registry.ModItems;
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
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.core.NonNullList;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeHolder;
import org.jetbrains.annotations.NotNull;

/**
 * Description:
 * Author: cnlimiter
 * Date: 2022/5/16 23:46
 * Version: 1.0
 */
public class NetherCraftingTableCategory implements IRecipeCategory<RecipeHolder<ITierCraftingRecipe>> {

    public static final RecipeType<RecipeHolder<ITierCraftingRecipe>> RECIPE_TYPE = RecipeType.createRecipeHolderType(Const.rl("nether_craft"));
    private static final ResourceLocation TEXTURE = Const.rl( "textures/gui/jei/tables/nether_jei.png");
    private final IDrawable background;
    private final IDrawable icon;

    public NetherCraftingTableCategory(IGuiHelper helper) {
        this.background = helper.createDrawable(TEXTURE, 0, 0, 158, 101);
        this.icon = helper.createDrawableIngredient(VanillaTypes.ITEM_STACK, new ItemStack(ModBlocks.nether_crafting_table.get()));
    }


    @Override
    public @NotNull RecipeType<RecipeHolder<ITierCraftingRecipe>> getRecipeType() {
        return RECIPE_TYPE;
    }

    @Override
    public @NotNull Component getTitle() {
        return Localizable.of("jei.category.avaritia.nether_crafting_table").build();
    }

    @Override
    public int getWidth() {
        return 158;
    }

    @Override
    public int getHeight() {
        return 101;
    }

    @Override
    public void draw(@NotNull RecipeHolder<ITierCraftingRecipe> recipe, @NotNull IRecipeSlotsView recipeSlotsView, @NotNull GuiGraphics guiGraphics, double mouseX, double mouseY) {
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
            int heightOffset = Math.floorDiv(5 - shaped.getHeight(), 2);
            int widthOffset = Math.floorDiv(5 - shaped.getWidth(), 2);

            for (int i = heightOffset; i < shaped.getHeight() + heightOffset; i++) {
                for (int j = widthOffset; j < shaped.getWidth() + widthOffset; j++) {
                    builder.addSlot(RecipeIngredientRole.INPUT, j * 18 + 5, i * 18 + 5).addIngredients(inputs.get(stackIndex));
                    stackIndex++;
                }
            }
            builder.addSlot(RecipeIngredientRole.OUTPUT, 133, 40).addItemStack(output);
        } else if (recipe instanceof ShapelessTableCraftingRecipe) {
            shapelessRecipe(builder, inputs);
            builder.addSlot(RecipeIngredientRole.OUTPUT, 133, 40).addItemStack(output);
        } else if (recipe instanceof InfinityCatalystCraftRecipe) {
            shapelessRecipe(builder, inputs);
            builder.addSlot(RecipeIngredientRole.OUTPUT, 133, 40).addItemStack(new ItemStack(ModItems.infinity_catalyst.get()));
        } else if (recipe instanceof EternalSingularityCraftRecipe) {
            shapelessRecipe(builder, inputs);
            builder.addSlot(RecipeIngredientRole.OUTPUT, 133, 40).addItemStack(new ItemStack(ModItems.eternal_singularity.get()));
        }
        builder.moveRecipeTransferButton(145, 105);
    }

    private void shapelessRecipe(@NotNull IRecipeLayoutBuilder builder, NonNullList<Ingredient> inputs) {
        for (int i = 0; i < 5; i++) {
            for (int j = 0; j < 5; j++) {
                int index = j + (i * 5);

                if (index < inputs.size()) {
                    builder.addSlot(RecipeIngredientRole.INPUT, j * 18 + 2, i * 18 + 2).addIngredients(inputs.get(index));
                }
            }
        }
        builder.setShapeless(99, 85);
    }

    @Override
    public void getTooltip(@NotNull ITooltipBuilder tooltip, @NotNull RecipeHolder<ITierCraftingRecipe> recipeHolder, @NotNull IRecipeSlotsView recipeSlotsView, double mouseX, double mouseY) {
        var recipe = recipeHolder.value();
        var shapeless = recipe instanceof ShapelessTableCraftingRecipe;
        int sX = (shapeless ? 340 : 306) / 2, sY = 200 / 2;

        if (shapeless && mouseX > sX + 10 && mouseX < sX + 20 && mouseY > sY - 1 && mouseY < sY + 8) {
            tooltip.add(Localizable.of("jei.tooltip.shapeless.recipe").build());
        }
    }

}
