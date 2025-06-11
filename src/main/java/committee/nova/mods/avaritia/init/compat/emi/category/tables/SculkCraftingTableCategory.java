package committee.nova.mods.avaritia.init.compat.emi.category.tables;

import committee.nova.mods.avaritia.Const;
import committee.nova.mods.avaritia.api.common.crafting.ITierCraftingRecipe;
import committee.nova.mods.avaritia.common.crafting.recipe.*;
import committee.nova.mods.avaritia.init.registry.ModBlocks;
import committee.nova.mods.avaritia.init.registry.ModItems;
import dev.emi.emi.api.recipe.EmiRecipe;
import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.render.EmiTexture;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.widget.WidgetHolder;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.NonNullList;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeHolder;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public record SculkCraftingTableCategory(RecipeHolder<ITierCraftingRecipe> recipe) implements EmiRecipe {
    private static final EmiTexture TEXTURE = new EmiTexture(ResourceLocation.tryBuild(Const.MOD_ID, "textures/gui/jei/tables/sculk_jei.png"), 0, 0, 116, 55);
    public static final EmiStack WORKSTATION = EmiStack.of(ModBlocks.sculk_crafting_table.get());
    public static final EmiRecipeCategory CATEGORY = new EmiRecipeCategory(ResourceLocation.tryBuild(Const.MOD_ID, "sculk_crafting_table"), WORKSTATION);

    @Override
    public EmiRecipeCategory getCategory() {
        return CATEGORY;
    }

    @Override
    public @NotNull ResourceLocation getId() {
        return this.recipe.id();
    }

    @Override
    public List<EmiIngredient> getInputs() {
        return this.recipe.value().getIngredients().stream().map(EmiIngredient::of).toList();
    }

    @Override
    public List<EmiStack> getOutputs() {
        var level = Minecraft.getInstance().level;
        assert level != null;
        return List.of(EmiStack.of(this.recipe.value().getResultItem(level.registryAccess())));
    }

    @Override
    public int getDisplayWidth() {
        return 118;
    }

    @Override
    public int getDisplayHeight() {
        return 57;
    }

    @Override
    public void addWidgets(WidgetHolder widgets) {
        ClientLevel level = Minecraft.getInstance().level;
        assert level != null;
        NonNullList<Ingredient> inputs = recipe.value().getIngredients();
        ItemStack output = recipe.value().getResultItem(level.registryAccess());
        widgets.addTexture(TEXTURE, 1, 1);
        if (recipe.value() instanceof ShapedTableCraftingRecipe shaped) {
            int stackIndex = 0;
            int heightOffset = Math.floorDiv(3 - shaped.getHeight(), 2);
            int widthOffset = Math.floorDiv(3 - shaped.getWidth(), 2);

            for (int i = heightOffset; i < shaped.getHeight() + heightOffset; i++)
                for (int j = widthOffset; j < shaped.getWidth() + widthOffset; j++) {
                    widgets.addSlot(EmiIngredient.of(inputs.get(stackIndex)), j * 18 + 2, i * 18 + 2).drawBack(false);
                    stackIndex++;
                }
            widgets.addSlot(EmiStack.of(output), 96, 20).recipeContext(this).drawBack(false);
        } else if (recipe.value() instanceof ShapelessTableCraftingRecipe) {
            shapelessRecipe(widgets, inputs);
            widgets.addSlot(EmiStack.of(output), 96, 20).recipeContext(this).drawBack(false);
        } else if (recipe.value() instanceof InfinityCatalystCraftRecipe) {
            shapelessRecipe(widgets, inputs);
            widgets.addSlot(EmiStack.of(ModItems.infinity_catalyst.get()), 96, 20).recipeContext(this).drawBack(false);
        } else if (recipe.value() instanceof EternalSingularityCraftRecipe) {
            shapelessRecipe(widgets, inputs);
            widgets.addSlot(EmiStack.of(ModItems.eternal_singularity.get()), 96, 20).recipeContext(this).drawBack(false);
        }
    }

    private void shapelessRecipe(@NotNull WidgetHolder widgetHolder, NonNullList<Ingredient> inputs) {
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                int index = j + (i * 3);
                if (index < inputs.size())
                    widgetHolder.addSlot(EmiIngredient.of(inputs.get(index)), j * 18 + 2, i * 18 + 2).drawBack(false);
            }
        }
        widgetHolder.addTexture(EmiTexture.SHAPELESS, 60, 45);
    }
}
