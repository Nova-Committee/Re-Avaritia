package committee.nova.mods.avaritia.gametest;

import committee.nova.mods.avaritia.Const;
import committee.nova.mods.avaritia.init.registry.ModBlocks;
import committee.nova.mods.avaritia.init.registry.ModItems;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.common.Tags;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

@GameTestHolder(Const.MOD_ID)
@PrefixGameTestTemplate(false)
public final class MaterialTagGameTests {
    private MaterialTagGameTests() {
    }

    @GameTest(template = "portable_ui_empty")
    public static void commonMaterialIngredientsMatch(GameTestHelper helper) {
        helper.assertTrue(new ItemStack(ModItems.infinity_nugget.get()).is(Tags.Items.NUGGETS), "Infinity nugget must match the common nugget tag");
        helper.assertTrue(new ItemStack(ModItems.infinity_ingot.get()).is(Tags.Items.INGOTS), "Infinity ingot must match the common ingot tag");
        helper.assertTrue(new ItemStack(ModItems.crystal_matrix_ingot.get()).is(Tags.Items.INGOTS), "Crystal matrix ingot must match the common ingot tag");
        for (var block : new net.minecraft.world.level.block.Block[]{ModBlocks.neutron.get(), ModBlocks.crystal_matrix.get(), ModBlocks.infinity.get()}) {
            helper.assertTrue(new ItemStack(block).is(Tags.Items.STORAGE_BLOCKS), "Storage block item must match the common storage tag");
            helper.assertTrue(block.defaultBlockState().is(Tags.Blocks.STORAGE_BLOCKS), "Storage block must match the common block tag");
        }
        helper.assertTrue(new ItemStack(ModBlocks.crystal_matrix.get()).is(Tags.Items.GEMS), "Crystal Matrix must match the common gem tag");
        helper.assertTrue(!new ItemStack(ModItems.infinity_ingot.get()).is(Tags.Items.GEMS), "Infinity ingot must not become a gem");
        helper.succeed();
    }
}
