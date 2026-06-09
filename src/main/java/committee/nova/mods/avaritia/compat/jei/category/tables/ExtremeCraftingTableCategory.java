package committee.nova.mods.avaritia.compat.jei.category.tables;

import committee.nova.mods.avaritia.Const;
import committee.nova.mods.avaritia.api.common.crafting.ITierCraftingRecipe;
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
            TableRecipeLayouts.addShapedInputs(builder, shaped, 9, 2, 2);
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
        TableRecipeLayouts.addShapelessInputs(builder, inputs, 9, 2, 2);
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
