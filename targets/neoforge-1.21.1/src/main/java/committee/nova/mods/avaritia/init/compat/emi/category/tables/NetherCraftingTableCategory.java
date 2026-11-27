package committee.nova.mods.avaritia.init.compat.emi.category.tables;

import committee.nova.mods.avaritia.Const;
import committee.nova.mods.avaritia.api.common.crafting.ITierCraftingRecipe;
import committee.nova.mods.avaritia.common.crafting.recipe.EternalSingularityCraftRecipe;
import committee.nova.mods.avaritia.common.crafting.recipe.InfinityCatalystCraftRecipe;
import committee.nova.mods.avaritia.common.crafting.recipe.ShapedTableCraftingRecipe;
import committee.nova.mods.avaritia.common.crafting.recipe.ShapelessTableCraftingRecipe;
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

public record NetherCraftingTableCategory(RecipeHolder<ITierCraftingRecipe> recipe) implements EmiRecipe {
    private static final EmiTexture TEXTURE = new EmiTexture(ResourceLocation.tryBuild(Const.MOD_ID, "textures/gui/jei/tables/nether_jei.png"), 0, 0, 157, 100);
    public static final EmiStack WORKSTATION = EmiStack.of(ModBlocks.nether_crafting_table.get());
    public static final EmiRecipeCategory CATEGORY = new EmiRecipeCategory(ResourceLocation.tryBuild(Const.MOD_ID, "nether_crafting_table"), WORKSTATION);

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
        return 159;
    }

    @Override
    public int getDisplayHeight() {
        return 102;
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
            int heightOffset = Math.floorDiv(5 - shaped.getHeight(), 2);
            int widthOffset = Math.floorDiv(5 - shaped.getWidth(), 2);

            for (int i = heightOffset; i < shaped.getHeight() + heightOffset; i++)
                for (int j = widthOffset; j < shaped.getWidth() + widthOffset; j++) {
                    widgets.addSlot(EmiIngredient.of(inputs.get(stackIndex)), j * 18 + 5, i * 18 + 5).drawBack(false);
                    stackIndex++;
                }
            widgets.addSlot(EmiStack.of(output), 133, 40).recipeContext(this).drawBack(false);
        } else if (recipe.value() instanceof ShapelessTableCraftingRecipe) {
            shapelessRecipe(widgets, inputs);
            widgets.addSlot(EmiStack.of(output), 133, 40).recipeContext(this).drawBack(false);
        } else if (recipe.value() instanceof InfinityCatalystCraftRecipe) {
            shapelessRecipe(widgets, inputs);
            widgets.addSlot(EmiStack.of(ModItems.infinity_catalyst.get()), 133, 40).recipeContext(this).drawBack(false);
        } else if (recipe.value() instanceof EternalSingularityCraftRecipe) {
            shapelessRecipe(widgets, inputs);
            widgets.addSlot(EmiStack.of(ModItems.eternal_singularity.get()), 133, 40).recipeContext(this).drawBack(false);
        }
    }

    private void shapelessRecipe(@NotNull WidgetHolder widgetHolder, NonNullList<Ingredient> inputs) {
        for (int i = 0; i < 5; i++) {
            for (int j = 0; j < 5; j++) {
                int index = j + (i * 5);
                if (index < inputs.size())
                    widgetHolder.addSlot(EmiIngredient.of(inputs.get(index)), j * 18 + 2, i * 18 + 2).drawBack(false);
            }
        }
        widgetHolder.addTexture(EmiTexture.SHAPELESS, 102, 85);
    }
}
