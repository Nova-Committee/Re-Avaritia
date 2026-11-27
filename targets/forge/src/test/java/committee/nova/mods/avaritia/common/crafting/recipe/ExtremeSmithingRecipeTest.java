package committee.nova.mods.avaritia.common.crafting.recipe;

import net.minecraft.SharedConstants;
import net.minecraft.core.NonNullList;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.Bootstrap;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraftforge.network.NetworkHooks;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mockStatic;

class ExtremeSmithingRecipeTest {
    @BeforeAll
    static void bootstrapMinecraft() {
        SharedConstants.tryDetectVersion();
        try (MockedStatic<NetworkHooks> ignored = mockStatic(NetworkHooks.class)) {
            Bootstrap.bootStrap();
        }
    }

    @Test
    void exposesAllFiveInputsToRecipeViewers() {
        ExtremeSmithingRecipe recipe = new ExtremeSmithingRecipe(
                new ResourceLocation("avaritia", "issue_362"),
                Ingredient.of(Items.NETHERITE_UPGRADE_SMITHING_TEMPLATE),
                Ingredient.of(Items.DIAMOND_CHESTPLATE),
                Ingredient.of(Items.NETHER_STAR, Items.END_CRYSTAL, Items.ELYTRA),
                new ItemStack(Items.NETHERITE_CHESTPLATE)
        );

        NonNullList<Ingredient> inputs = recipe.getIngredients();

        assertEquals(5, inputs.size());
        assertTrue(inputs.get(0).test(new ItemStack(Items.NETHERITE_UPGRADE_SMITHING_TEMPLATE)));
        assertTrue(inputs.get(1).test(new ItemStack(Items.DIAMOND_CHESTPLATE)));
        assertTrue(inputs.get(2).test(new ItemStack(Items.NETHER_STAR)));
        assertTrue(inputs.get(3).test(new ItemStack(Items.END_CRYSTAL)));
        assertTrue(inputs.get(4).test(new ItemStack(Items.ELYTRA)));
    }
}
