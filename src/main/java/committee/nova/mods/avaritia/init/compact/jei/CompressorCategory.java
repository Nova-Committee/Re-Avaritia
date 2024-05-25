package committee.nova.mods.avaritia.init.compact.jei;

import committee.nova.mods.avaritia.Static;
import committee.nova.mods.avaritia.api.common.crafting.ICompressorRecipe;
import committee.nova.mods.avaritia.init.registry.ModBlocks;
import committee.nova.mods.avaritia.init.registry.ModTooltips;
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
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;

/**
 * Description:
 * Author: cnlimiter
 * Date: 2022/5/15 23:12
 * Version: 1.0
 */
public class CompressorCategory implements IRecipeCategory<ICompressorRecipe> {
    public static final RecipeType<ICompressorRecipe> RECIPE_TYPE = RecipeType.create(Static.MOD_ID, "compressor", ICompressorRecipe.class);
    private static final ResourceLocation TEXTURE = new ResourceLocation(Static.MOD_ID, "textures/gui/jei/compressor.png");
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
    public @NotNull IDrawable getBackground() {
        return this.background;
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
        builder.addSlot(RecipeIngredientRole.INPUT, 19, 11).addIngredients(inputs.get(0));
        builder.addSlot(RecipeIngredientRole.OUTPUT, 98, 11).addItemStack(output);

    }

    @Override
    public @NotNull List<Component> getTooltipStrings(@NotNull ICompressorRecipe recipe, @NotNull IRecipeSlotsView recipeSlotsView, double mouseX, double mouseY) {
        if (mouseX > 43 && mouseX < 58 && mouseY > 11 && mouseY < 26) {
            return Collections.singletonList(ModTooltips.NUM_ITEMS.args(recipe.getInputCount()).color(ChatFormatting.LIGHT_PURPLE).build());
        }

        if (mouseX > 66 && mouseX < 88 && mouseY > 11 && mouseY < 26) {
            return Collections.singletonList(ModTooltips.TIME_CONSUME.args(recipe.getTimeRequire()).color(ChatFormatting.BLUE).build());
        }
        return List.of();
    }


}
