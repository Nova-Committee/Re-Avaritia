package committee.nova.mods.avaritia.init.compat.jei.category;

import committee.nova.mods.avaritia.Const;
import committee.nova.mods.avaritia.api.common.crafting.ICompressorRecipe;
import committee.nova.mods.avaritia.api.utils.lang.Localizable;
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
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import org.jetbrains.annotations.NotNull;

/**
 * Description:
 * Author: cnlimiter
 * Date: 2022/5/15 23:12
 * Version: 1.0
 */
public class CompressorCategory implements IRecipeCategory<RecipeHolder<ICompressorRecipe>> {
    public static final RecipeType<RecipeHolder<ICompressorRecipe>> RECIPE_TYPE = RecipeType.createRecipeHolderType(Const.rl("compressor"));
    private static final ResourceLocation TEXTURE = Const.rl( "textures/gui/jei/compressor.png");
    private final IDrawable icon;
    private final IDrawable background;


    public CompressorCategory(IGuiHelper helper) {
        this.icon = helper.createDrawableIngredient(VanillaTypes.ITEM_STACK, new ItemStack(ModBlocks.neutron_compressor.get()));
        this.background = helper.createDrawable(TEXTURE, 0, 0, 170, 63);
    }


    @Override
    public @NotNull RecipeType<RecipeHolder<ICompressorRecipe>> getRecipeType() {
        return RECIPE_TYPE;
    }

    @Override
    public @NotNull Component getTitle() {
        return Localizable.of("jei.category.avaritia.compressor").build();
    }

    @Override
    public @NotNull IDrawable getBackground() {
        return this.background;
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
        builder.addSlot(RecipeIngredientRole.INPUT, 37, 21).addIngredients(inputs.get(0));
        builder.addSlot(RecipeIngredientRole.OUTPUT, 117, 21).addItemStack(output);
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
