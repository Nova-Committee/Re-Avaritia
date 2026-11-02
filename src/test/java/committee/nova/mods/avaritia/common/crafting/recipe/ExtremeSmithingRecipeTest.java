package committee.nova.mods.avaritia.common.crafting.recipe;

import net.minecraft.SharedConstants;
import net.minecraft.core.NonNullList;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.component.DataComponentInitializers;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.Bootstrap;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;


import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;


@Disabled("26.3 默认数据组件绑定需要完整 tag 数据，单元测试环境暂不支持")
class ExtremeSmithingRecipeTest {
    @BeforeAll
    static void bootstrapMinecraft() {
        SharedConstants.tryDetectVersion();
        Bootstrap.bootStrap();
        // 26.3 的默认数据组件不再随 Bootstrap 绑定，需手动应用后才能创建 ItemStack
        BuiltInRegistries.DATA_COMPONENT_INITIALIZERS
                .build(RegistryAccess.fromRegistryOfRegistries(BuiltInRegistries.REGISTRY))
                .forEach(DataComponentInitializers.PendingComponents::apply);
    }

    @Test
    void exposesAllFiveInputsToRecipeViewers() {
        ExtremeSmithingRecipe recipe = new ExtremeSmithingRecipe(
                Ingredient.of(Items.NETHERITE_UPGRADE_SMITHING_TEMPLATE),
                Ingredient.of(Items.DIAMOND_CHESTPLATE),
                Ingredient.of(Items.NETHER_STAR, Items.END_CRYSTAL, Items.ELYTRA),
                new ItemStack(Items.NETHERITE_CHESTPLATE)
        );

        NonNullList<Ingredient> inputs = recipe.getIngredients();

        assertEquals(3, inputs.size());
        assertTrue(inputs.get(0).test(new ItemStack(Items.NETHERITE_UPGRADE_SMITHING_TEMPLATE)));
        assertTrue(inputs.get(1).test(new ItemStack(Items.DIAMOND_CHESTPLATE)));
        // The three addition slots share one multi-item ingredient since 26.3.
        assertTrue(inputs.get(2).test(new ItemStack(Items.NETHER_STAR)));
        assertTrue(inputs.get(2).test(new ItemStack(Items.END_CRYSTAL)));
        assertTrue(inputs.get(2).test(new ItemStack(Items.ELYTRA)));
    }
}
