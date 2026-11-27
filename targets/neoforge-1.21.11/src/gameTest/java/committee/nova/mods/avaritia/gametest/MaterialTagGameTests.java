package committee.nova.mods.avaritia.gametest;

import committee.nova.mods.avaritia.Const;
import committee.nova.mods.avaritia.init.registry.ModBlocks;
import committee.nova.mods.avaritia.init.registry.ModItems;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.FunctionGameTestInstance;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.gametest.framework.TestData;
import net.minecraft.gametest.framework.TestEnvironmentDefinition;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.common.Tags;
import net.neoforged.neoforge.event.RegisterGameTestsEvent;
import net.neoforged.neoforge.registries.RegisterEvent;

import java.util.function.Consumer;

@EventBusSubscriber(modid = "avaritia_gametest")
public final class MaterialTagGameTests {
    private static final Identifier ID = Const.rl("common_material_ingredients");
    private static final ResourceKey<Consumer<GameTestHelper>> FUNCTION = ResourceKey.create(Registries.TEST_FUNCTION, ID);

    private MaterialTagGameTests() {
    }

    @SubscribeEvent
    public static void registerFunction(RegisterEvent event) {
        event.register(Registries.TEST_FUNCTION, ID, () -> MaterialTagGameTests::commonMaterialIngredientsMatch);
    }

    @SubscribeEvent
    public static void registerTest(RegisterGameTestsEvent event) {
        Holder<TestEnvironmentDefinition> environment = event.registerEnvironment(Const.rl("material_tags"), new TestEnvironmentDefinition.AllOf(java.util.List.of()));
        event.registerTest(ID, new FunctionGameTestInstance(FUNCTION, new TestData<>(environment, Identifier.withDefaultNamespace("empty"), 40, 0, true)));
    }

    private static void commonMaterialIngredientsMatch(GameTestHelper helper) {
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
