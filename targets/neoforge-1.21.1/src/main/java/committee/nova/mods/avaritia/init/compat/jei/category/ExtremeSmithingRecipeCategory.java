package committee.nova.mods.avaritia.init.compat.jei.category;

import committee.nova.mods.avaritia.Const;
import committee.nova.mods.avaritia.api.utils.lang.Localizable;
import committee.nova.mods.avaritia.common.crafting.recipe.ExtremeSmithingRecipe;
import committee.nova.mods.avaritia.init.registry.ModBlocks;
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
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;

/**
 * @Project: Avaritia
 * @Author: cnlimiter
 * @CreateTime: 2024/12/21 14:27
 * @Description:
 */
public class ExtremeSmithingRecipeCategory implements IRecipeCategory<RecipeHolder<ExtremeSmithingRecipe>> {
    public static final RecipeType<RecipeHolder<ExtremeSmithingRecipe>> RECIPE_TYPE = RecipeType.createRecipeHolderType(Const.rl("extreme_smithing"));
    private static final ResourceLocation TEXTURE = Const.rl( "textures/gui/jei/extreme_smithing_jei.png");
    private final IDrawable icon;
    private final IDrawable background;

    public ExtremeSmithingRecipeCategory(IGuiHelper helper) {
        this.background = helper.createDrawable(TEXTURE, 0, 0, 170, 64);
        this.icon = helper.createDrawableItemStack(new ItemStack(ModBlocks.extreme_smithing_table.get()));
    }

    @Override
    public @NotNull RecipeType<RecipeHolder<ExtremeSmithingRecipe>> getRecipeType() {
        return RECIPE_TYPE;
    }

    @Override
    public @NotNull Component getTitle() {
        return Localizable.of("jei.category.avaritia.extreme_smithing_table").build();
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
    public void draw(@NotNull RecipeHolder<ExtremeSmithingRecipe> recipe, @NotNull IRecipeSlotsView recipeSlotsView, @NotNull GuiGraphics guiGraphics, double mouseX, double mouseY) {
        this.background.draw(guiGraphics);
    }
    @Override
    public @NotNull IDrawable getIcon() {
        return icon;
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, @NotNull RecipeHolder<ExtremeSmithingRecipe> recipeHolder, @NotNull IFocusGroup focuses) {
        var recipe = recipeHolder.value();
        builder.addSlot(RecipeIngredientRole.INPUT, 27, 23)
                .addIngredients(recipe.template);

        builder.addSlot(RecipeIngredientRole.INPUT, 45, 23)
                .addIngredients(recipe.base);

        builder.addSlot(RecipeIngredientRole.INPUT, 45, 5)
                .addIngredients(Ingredient.of(Arrays.stream(recipe.additions.getItems()).toList().get(0)));
        builder.addSlot(RecipeIngredientRole.INPUT, 63, 23)
                .addIngredients(Ingredient.of(Arrays.stream(recipe.additions.getItems()).toList().get(1)));
        builder.addSlot(RecipeIngredientRole.INPUT, 45, 41)
                .addIngredients(Ingredient.of(Arrays.stream(recipe.additions.getItems()).toList().get(2)));


        builder.addSlot(RecipeIngredientRole.OUTPUT, 117, 23)
                .addItemStack(getResultItem(recipe));
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
        var recipe = recipeHolder.value();
        if (recipe.isIncomplete()) {
            return false;
        }
        return recipe instanceof ExtremeSmithingRecipe;
    }
}
