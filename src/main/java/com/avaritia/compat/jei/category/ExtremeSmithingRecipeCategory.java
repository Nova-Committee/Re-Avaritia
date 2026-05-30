package com.avaritia.compat.jei.category;

import com.avaritia.Avaritia;
import com.avaritia.common.crafting.recipe.ExtremeSmithingRecipe;
import com.avaritia.init.registry.ModBlocks;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.category.IRecipeCategory;
import mezz.jei.api.recipe.types.IRecipeHolderType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;

public class ExtremeSmithingRecipeCategory implements IRecipeCategory<RecipeHolder<ExtremeSmithingRecipe>> {
    public static final IRecipeHolderType<ExtremeSmithingRecipe> RECIPE_TYPE = IRecipeHolderType.create(Identifier.of(Const.MOD_ID, "extreme_smithing"));
    private static final Identifier TEXTURE = Identifier.of(Const.MOD_ID, "textures/gui/jei/extreme_smithing_jei.png");
    private final IDrawable icon;
    private final IDrawable background;

    public ExtremeSmithingRecipeCategory(IGuiHelper helper) {
        this.background = helper.createDrawable(TEXTURE, 0, 0, 170, 64);
        this.icon = helper.createDrawableItemStack(new ItemStack(ModBlocks.extreme_smithing_table.get()));
    }

    @Override
    public @NotNull IRecipeHolderType<ExtremeSmithingRecipe> getRecipeType() {
        return RECIPE_TYPE;
    }

    @Override
    public @NotNull Component getTitle() {
        return Component.translatable("jei.category.avaritia.extreme_smithing_table");
    }

    @Override
    public int getWidth() {
        return 170;
    }

    @Override
    public int getHeight() {
        return 64;
    }

    @Override
    public void draw(@NotNull RecipeHolder<ExtremeSmithingRecipe> recipe, @NotNull IRecipeSlotsView recipeSlotsView, @NotNull GuiGraphicsExtractor guiGraphics, double mouseX, double mouseY) {
        this.background.draw(guiGraphics);
    }

    @Override
    public @NotNull IDrawable getIcon() {
        return icon;
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, @NotNull RecipeHolder<ExtremeSmithingRecipe> recipeHolder, @NotNull IFocusGroup focuses) {
        var recipe = recipeHolder.value();
        builder.addSlot(RecipeIngredientRole.INPUT, 27, 23).add(recipe.template);
        builder.addSlot(RecipeIngredientRole.INPUT, 45, 23).add(recipe.base);
        builder.addSlot(RecipeIngredientRole.INPUT, 45, 5).add(Ingredient.of(Arrays.stream(recipe.additions.getItems()).toList().get(0)));
        builder.addSlot(RecipeIngredientRole.INPUT, 63, 23).add(Ingredient.of(Arrays.stream(recipe.additions.getItems()).toList().get(1)));
        builder.addSlot(RecipeIngredientRole.INPUT, 45, 41).add(Ingredient.of(Arrays.stream(recipe.additions.getItems()).toList().get(2)));
        builder.addSlot(RecipeIngredientRole.OUTPUT, 117, 23).add(getResultItem(recipe));
        builder.moveRecipeTransferButton(160, 68);
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

    @Override
    public boolean isHandled(@NotNull RecipeHolder<ExtremeSmithingRecipe> recipeHolder) {
        return !recipeHolder.value().isIncomplete();
    }
}
