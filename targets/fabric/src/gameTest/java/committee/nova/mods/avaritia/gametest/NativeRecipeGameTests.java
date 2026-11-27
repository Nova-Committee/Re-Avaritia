package committee.nova.mods.avaritia.gametest;

import committee.nova.mods.avaritia.api.common.wrapper.ContainerItemHandler;
import committee.nova.mods.avaritia.common.crafting.recipe.EternalSingularityCraftRecipe;
import committee.nova.mods.avaritia.common.crafting.recipe.ExtremeSmithingRecipe;
import committee.nova.mods.avaritia.core.singularity.SingularityReloadListener;
import committee.nova.mods.avaritia.init.registry.ModItems;
import committee.nova.mods.avaritia.util.SingularityUtils;
import net.minecraft.core.NonNullList;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;

/** Native crafting boundaries used by both optional scripting integrations. */
public final class NativeRecipeGameTests {
    private static final String TEMPLATE = "avaritia:portable_ui_empty";

    private NativeRecipeGameTests() {}

    @GameTest(template = TEMPLATE)
    public static void eternalHonorsCountAndAdditionalIngredients(GameTestHelper helper) {
        var recipe = eternal("eternal_extra", 2);
        try {
            SimpleContainer inventory = eternalInventory();
            helper.assertTrue(recipe.matches(inventory, helper.getLevel()),
                    "every exact singularity plus the additional gold ingot must match");
            ItemStack result = recipe.assemble(inventory, helper.getLevel().registryAccess());
            helper.assertTrue(result.is(ModItems.eternal_singularity.get()) && result.getCount() == 2,
                    "scripted eternal output count must survive recipe assembly");
            inventory.setItem(0, ItemStack.EMPTY);
            helper.assertTrue(!recipe.matches(inventory, helper.getLevel()),
                    "all singularities alone must not replace a required extra ingredient");
            inventory.setItem(0, new ItemStack(Items.IRON_INGOT));
            helper.assertTrue(!recipe.matches(inventory, helper.getLevel()),
                    "a different ingot must not match the extra ingredient");
            helper.succeed();
        } finally {
            EternalSingularityCraftRecipe.INGREDIENTS_LOADED.removeBoolean(recipe);
        }
    }

    @GameTest(template = TEMPLATE)
    public static void freshEternalHandlerLoadsIngredientsAndRejectsDuplicateNbt(GameTestHelper helper) {
        var recipe = eternal("eternal_handler", 3);
        try {
            SimpleContainer inventory = eternalInventory();
            var handler = new ContainerItemHandler(inventory);
            helper.assertTrue(recipe.matches(handler),
                    "first item-handler match must load dynamic ingredients without a JEI/getIngredients pre-read");
            ItemStack result = recipe.assemble(handler);
            helper.assertTrue(result.getCount() == 3, "item-handler assembly must retain configured count");
            helper.assertTrue(!inventory.getItem(1).isEmpty() && !inventory.getItem(2).isEmpty(),
                    "native registry must supply distinct singularities for the NBT boundary");
            inventory.setItem(2, inventory.getItem(1).copy());
            helper.assertTrue(!recipe.matches(handler),
                    "a duplicate singularity ID must not satisfy a different exact-NBT ingredient");
            helper.succeed();
        } finally {
            EternalSingularityCraftRecipe.INGREDIENTS_LOADED.removeBoolean(recipe);
        }
    }

    @GameTest(template = TEMPLATE)
    public static void smithingSingleMaterialOccupiesAllThreeAdditionSlots(GameTestHelper helper) {
        var recipe = smithing("smithing_single", Ingredient.of(Items.GOLD_INGOT));
        var ingredients = recipe.getIngredients();
        helper.assertTrue(ingredients.size() == 5, "smithing must expose template, base and three addition slots");
        for (int slot = 2; slot < 5; slot++) {
            helper.assertTrue(ingredients.get(slot).test(new ItemStack(Items.GOLD_INGOT)),
                    "a single material must be displayed in each addition slot");
        }
        SimpleContainer inventory = smithingInventory(Items.GOLD_INGOT);
        inventory.getItem(1).getOrCreateTag().putString("scripting_marker", "preserved");
        helper.assertTrue(recipe.matches(inventory, helper.getLevel()), "complete smithing input must match");
        ItemStack result = recipe.assemble(inventory, helper.getLevel().registryAccess());
        helper.assertTrue(result.is(Items.DIAMOND_SWORD) && result.getCount() == 2
                        && "preserved".equals(result.getOrCreateTag().getString("scripting_marker")),
                "smithing assembly must preserve output quantity and base NBT");
        inventory.setItem(4, ItemStack.EMPTY);
        helper.assertTrue(!recipe.matches(inventory, helper.getLevel()), "the third addition is required");
        helper.succeed();
    }

    @GameTest(template = TEMPLATE)
    public static void smithingPreservesIngredientAlternativesInEveryAdditionSlot(GameTestHelper helper) {
        var recipe = smithing("smithing_alternatives", Ingredient.of(Items.GOLD_INGOT, Items.COPPER_INGOT));
        var ingredients = recipe.getIngredients();
        for (int slot = 2; slot < 5; slot++) {
            helper.assertTrue(ingredients.get(slot).test(new ItemStack(Items.GOLD_INGOT))
                            && ingredients.get(slot).test(new ItemStack(Items.COPPER_INGOT)),
                    "each smithing addition must retain every material alternative");
        }
        SimpleContainer inventory = smithingInventory(Items.COPPER_INGOT);
        inventory.setItem(3, new ItemStack(Items.GOLD_INGOT));
        helper.assertTrue(recipe.matches(inventory, helper.getLevel()), "mixed valid alternatives must match");
        inventory.setItem(2, new ItemStack(Items.IRON_INGOT));
        helper.assertTrue(!recipe.matches(inventory, helper.getLevel()), "an unrelated addition must fail");
        helper.succeed();
    }

    private static EternalSingularityCraftRecipe eternal(String path, int count) {
        return new EternalSingularityCraftRecipe(new ResourceLocation("avaritia_gametest", path),
                NonNullList.of(Ingredient.EMPTY, Ingredient.of(Items.GOLD_INGOT)), count);
    }

    private static SimpleContainer eternalInventory() {
        SimpleContainer inventory = new SimpleContainer(81);
        inventory.setItem(0, new ItemStack(Items.GOLD_INGOT));
        int slot = 1;
        for (var singularity : SingularityReloadListener.INSTANCE.getAllSingularities().values()) {
            if (singularity.getIngredient() != Ingredient.EMPTY) {
                inventory.setItem(slot++, SingularityUtils.getItemForSingularity(singularity));
            }
        }
        return inventory;
    }

    private static ExtremeSmithingRecipe smithing(String path, Ingredient additions) {
        return new ExtremeSmithingRecipe(new ResourceLocation("avaritia_gametest", path),
                Ingredient.of(Items.NETHERITE_UPGRADE_SMITHING_TEMPLATE), Ingredient.of(Items.IRON_SWORD),
                additions, new ItemStack(Items.DIAMOND_SWORD, 2));
    }

    private static SimpleContainer smithingInventory(net.minecraft.world.item.Item addition) {
        return new SimpleContainer(new ItemStack(Items.NETHERITE_UPGRADE_SMITHING_TEMPLATE),
                new ItemStack(Items.IRON_SWORD), new ItemStack(addition), new ItemStack(addition), new ItemStack(addition));
    }
}
