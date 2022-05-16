package nova.committee.avaritia.init.compat.jei.category;

import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.IRecipeLayout;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.ingredients.IIngredients;
import mezz.jei.api.recipe.category.IRecipeCategory;
import net.minecraft.ChatFormatting;
import net.minecraft.core.NonNullList;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import nova.committee.avaritia.Static;
import nova.committee.avaritia.common.recipe.ICompressorRecipe;
import nova.committee.avaritia.init.ModTooltips;
import nova.committee.avaritia.init.registry.ModBlocks;
import nova.committee.avaritia.util.Localizable;

import java.util.Collections;
import java.util.List;

/**
 * Description:
 * Author: cnlimiter
 * Date: 2022/5/15 23:12
 * Version: 1.0
 */
public class CompressorCategory implements IRecipeCategory<ICompressorRecipe> {
    public static final ResourceLocation UID = new ResourceLocation(Static.MOD_ID, "compressor");
    private static final ResourceLocation TEXTURE = new ResourceLocation(Static.MOD_ID, "textures/gui/jei/compressor.png");
    private final IDrawable background;
    private final IDrawable icon;


    public CompressorCategory(IGuiHelper helper) {
        this.background = helper.createDrawable(TEXTURE, 0, 0, 170, 75);
        this.icon = helper.createDrawableIngredient(new ItemStack(ModBlocks.compressor));
    }

    @Override
    public ResourceLocation getUid() {
        return UID;
    }

    @Override
    public Class<? extends ICompressorRecipe> getRecipeClass() {
        return ICompressorRecipe.class;
    }

    @Override
    public Component getTitle() {
        return Localizable.of("jei.category.avaritia.compressor").build();
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
    public List<Component> getTooltipStrings(ICompressorRecipe recipe, double mouseX, double mouseY) {


        if (mouseX > 36 && mouseX < 53 && mouseY > 32 && mouseY < 49) {
            return Collections.singletonList(ModTooltips.NUM_ITEMS.args(recipe.getInputCount()).color(ChatFormatting.WHITE).build());
        }

        return Collections.emptyList();
    }

    @Override
    public void setIngredients(ICompressorRecipe recipe, IIngredients ingredients) {
        ingredients.setOutput(VanillaTypes.ITEM, recipe.getResultItem());

        NonNullList<Ingredient> inputs = NonNullList.create();

        inputs.addAll(recipe.getIngredients());

        ingredients.setInputIngredients(inputs);
    }

    @Override
    public void setRecipe(IRecipeLayout layout, ICompressorRecipe recipe, IIngredients ingredients) {
        var stacks = layout.getItemStacks();
        var inputs = ingredients.getInputs(VanillaTypes.ITEM);
        var outputs = ingredients.getOutputs(VanillaTypes.ITEM).get(0);

        stacks.init(0, true, 36, 32);
        stacks.init(1, false, 116, 32);

        stacks.set(0, inputs.get(0));
        stacks.set(1, outputs);
    }
}
