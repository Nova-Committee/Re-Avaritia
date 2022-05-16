package nova.committee.avaritia.init.compat.jei.category;

import com.mojang.blaze3d.vertex.PoseStack;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.IRecipeLayout;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.ingredients.IIngredients;
import mezz.jei.api.recipe.category.IRecipeCategory;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import nova.committee.avaritia.Static;
import nova.committee.avaritia.common.recipe.ICraftRecipe;
import nova.committee.avaritia.common.recipe.ShapedExtremeCraftingRecipe;
import nova.committee.avaritia.common.recipe.ShapelessExtremeCraftingRecipe;
import nova.committee.avaritia.init.compat.jei.JeiCompat;
import nova.committee.avaritia.init.registry.ModBlocks;
import nova.committee.avaritia.util.lang.Localizable;

import java.util.Collections;
import java.util.List;

/**
 * Description:
 * Author: cnlimiter
 * Date: 2022/5/16 23:46
 * Version: 1.0
 */
public class ExtremeCraftingTableCategory implements IRecipeCategory<ICraftRecipe> {

    public static final ResourceLocation UID = new ResourceLocation(Static.MOD_ID, "extreme_crafting");
    private static final ResourceLocation TEXTURE = new ResourceLocation(Static.MOD_ID, "textures/gui/jei/extreme_jei.png");

    private final IDrawable background;
    private final IDrawable icon;
    private final IDrawable shapeless;

    public ExtremeCraftingTableCategory(IGuiHelper helper) {
        this.background = helper.createDrawable(TEXTURE, 0, 0, 190, 163);
        this.icon = helper.createDrawableIngredient(new ItemStack(ModBlocks.extreme_crafting_table));
        this.shapeless = helper.createDrawable(JeiCompat.ICONS, 17, 0, 19, 15);
    }

    @Override
    public ResourceLocation getUid() {
        return UID;
    }

    @Override
    public Class<? extends ICraftRecipe> getRecipeClass() {
        return ICraftRecipe.class;
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
    public void draw(ICraftRecipe recipe, PoseStack stack, double mouseX, double mouseY) {
        stack.pushPose();
        stack.scale(0.5F, 0.5F, 0.5F);

        var shapeless = recipe instanceof ShapelessExtremeCraftingRecipe;

        if (shapeless)
            this.shapeless.draw(stack, 306, 329);

        stack.popPose();
    }

    @Override
    public List<Component> getTooltipStrings(ICraftRecipe recipe, double mouseX, double mouseY) {
        var shapeless = recipe instanceof ShapelessExtremeCraftingRecipe;
        int sX = (shapeless ? 340 : 306) / 2, sY = 200 / 2;

        if (shapeless && mouseX > sX + 10 && mouseX < sX + 20 && mouseY > sY - 1 && mouseY < sY + 8) {
            return Collections.singletonList(Localizable.of("jei.tooltip.shapeless.recipe").build());
        }

        return Collections.emptyList();
    }

    @Override
    public void setIngredients(ICraftRecipe recipe, IIngredients ingredients) {
        ingredients.setOutput(VanillaTypes.ITEM, recipe.getResultItem());
        ingredients.setInputIngredients(recipe.getIngredients());
    }

    @Override
    public void setRecipe(IRecipeLayout layout, ICraftRecipe recipe, IIngredients ingredients) {
        var stacks = layout.getItemStacks();
        var inputs = ingredients.getInputs(VanillaTypes.ITEM);
        var outputs = ingredients.getOutputs(VanillaTypes.ITEM).get(0);

        stacks.init(0, false, 167, 73);
        stacks.set(0, outputs);

        for (int i = 0; i < 9; i++) {
            for (int j = 0; j < 9; j++) {
                int index = 1 + j + (i * 9);
                stacks.init(index, true, j * 18, i * 18);
            }
        }

        if (recipe instanceof ShapedExtremeCraftingRecipe shaped) {
            int stackIndex = 0;

            for (int i = 0; i < 9; i++) {
                for (int j = 0; j < 9; j++) {
                    int index = 1 + (i * 9) + j;

                    stacks.set(index, inputs.get(stackIndex));

                    stackIndex++;
                }
            }
        } else if (recipe instanceof ShapelessExtremeCraftingRecipe) {
            for (int i = 0; i < inputs.size(); i++) {
                stacks.set(i + 1, inputs.get(i));
            }
        }

        layout.moveRecipeTransferButton(170, 100);
    }
}
