package committee.nova.mods.avaritia.gametest;

import committee.nova.mods.avaritia.Const;
import committee.nova.mods.avaritia.common.block.compressor.NeutronCompressorBlock;
import committee.nova.mods.avaritia.common.tile.NeutronCompressorTile;
import committee.nova.mods.avaritia.core.io.SideConfiguration.SideMode;
import committee.nova.mods.avaritia.init.registry.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.FunctionGameTestInstance;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.gametest.framework.TestData;
import net.minecraft.gametest.framework.TestEnvironmentDefinition;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.event.RegisterGameTestsEvent;
import net.neoforged.neoforge.registries.RegisterEvent;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.item.WorldlyContainerWrapper;
import net.neoforged.neoforge.transfer.transaction.Transaction;

import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;

@EventBusSubscriber(modid = "avaritia_gametest")
public final class CompressorSidedIOGameTests {
    private static final List<String> NAMES = List.of("compressor_relative_faces", "compressor_mixed_io", "compressor_all_modes", "compressor_active_output");
    private static final List<Consumer<GameTestHelper>> FUNCTIONS = List.of(CompressorSidedIOGameTests::relativeFaces, CompressorSidedIOGameTests::mixedIO, CompressorSidedIOGameTests::allModes, CompressorSidedIOGameTests::activeOutput);

    private CompressorSidedIOGameTests() {}

    @SubscribeEvent
    public static void registerFunctions(RegisterEvent event) {
        for (int i = 0; i < NAMES.size(); i++) {
            var function = FUNCTIONS.get(i);
            event.register(Registries.TEST_FUNCTION, Const.rl(NAMES.get(i)), () -> function);
        }
    }

    @SubscribeEvent
    public static void registerTests(RegisterGameTestsEvent event) {
        var environment = event.registerEnvironment(Const.rl("compressor_sided_io"), new TestEnvironmentDefinition.AllOf());
        for (var name : NAMES) {
            var id = Const.rl(name);
            var function = ResourceKey.<Consumer<GameTestHelper>>create(Registries.TEST_FUNCTION, id);
            event.registerTest(id, new FunctionGameTestInstance(function, new TestData<>(environment, Identifier.withDefaultNamespace("empty"), 100, 0, true)));
        }
    }

    private static void relativeFaces(GameTestHelper helper) {
        var tile = place(helper, Direction.SOUTH);
        tile.getSideConfiguration().setSideMode(Direction.NORTH, SideMode.ACTIVE_MIXIN);
        tile.getSideConfiguration().setSideMode(Direction.SOUTH, SideMode.ACTIVE_OUTPUT);
        var front = new WorldlyContainerWrapper(tile, Direction.SOUTH);
        try (var tx = Transaction.openRoot()) {
            helper.assertTrue(front.insert(0, ItemResource.of(Items.DEEPSLATE), 8, tx) == 8, "world SOUTH must insert into relative front");
            tx.commit();
        }
        tile.getInventory().setStackInSlot(0, new ItemStack(Items.BEDROCK, 4));
        var back = new WorldlyContainerWrapper(tile, Direction.NORTH);
        try (var tx = Transaction.openRoot()) {
            helper.assertTrue(back.extract(0, ItemResource.of(Items.BEDROCK), 4, tx) == 4, "world NORTH must extract relative back output");
            tx.commit();
        }
        helper.assertTrue(tile.getItem(1).getCount() == 8 && tile.getItem(0).isEmpty(), "committed transfers must change correct slots");
        helper.succeed();
    }

    private static void mixedIO(GameTestHelper helper) {
        var tile = place(helper, Direction.SOUTH);
        for (var mode : new SideMode[]{SideMode.PASSIVE_MIXIN, SideMode.ACTIVE_MIXIN}) {
            for (var side : Direction.values()) tile.getSideConfiguration().setSideMode(side, mode);
            tile.getInventory().setStackInSlot(0, new ItemStack(Items.BEDROCK, 4));
            tile.getInventory().setStackInSlot(1, ItemStack.EMPTY);
            var handler = helper.getLevel().getCapability(Capabilities.Item.BLOCK, tile.getBlockPos(), Direction.NORTH);
            helper.assertTrue(handler != null, "registered item capability must exist");
            int extracted = 0;
            try (var tx = Transaction.openRoot()) {
                for (int slot = 0; slot < handler.size(); slot++) extracted += handler.extract(slot, ItemResource.of(Items.BEDROCK), 4, tx);
                helper.assertTrue(extracted == 4, "mixed capability must extract output: " + mode);
                helper.assertTrue(handler.insert(0, ItemResource.of(Items.DEEPSLATE), 8, tx) == 8, "mixed input must be first exposed slot");
                tx.commit();
            }
            System.out.println("ISSUE395 " + mode + " slots=" + Arrays.toString(tile.getSlotsForFace(Direction.NORTH)) + " extracted=" + extracted);
            helper.assertTrue(tile.getItem(0).isEmpty() && tile.getItem(1).getCount() == 8, "committed mixed transfers");
        }
        helper.succeed();
    }

    private static void allModes(GameTestHelper helper) {
        var tile = place(helper, Direction.NORTH);
        for (var facing : Direction.Plane.HORIZONTAL) {
            helper.getLevel().setBlockAndUpdate(tile.getBlockPos(), tile.getBlockState().setValue(NeutronCompressorBlock.FACING, facing));
            for (var mode : SideMode.values()) {
                for (var side : Direction.values()) tile.getSideConfiguration().setSideMode(side, SideMode.OFF);
                tile.getSideConfiguration().setSideMode(Direction.NORTH, mode);
                tile.getInventory().setStackInSlot(0, new ItemStack(Items.BEDROCK, 4));
                tile.getInventory().setStackInSlot(1, ItemStack.EMPTY);
                var handler = new WorldlyContainerWrapper(tile, facing);
                helper.assertTrue(handler.size() == (mode.isMixed() ? 2 : mode == SideMode.OFF ? 0 : 1), "slot count " + facing + "/" + mode);
                int inserted = 0, extracted = 0;
                try (var tx = Transaction.openRoot()) {
                    for (int slot = 0; slot < handler.size(); slot++) {
                        inserted += handler.insert(slot, ItemResource.of(Items.DEEPSLATE), 8, tx);
                        extracted += handler.extract(slot, ItemResource.of(Items.BEDROCK), 4, tx);
                    }
                    helper.assertTrue(inserted == (mode.canInput() ? 8 : 0) && extracted == (mode.canOutput() ? 4 : 0), "mode permissions " + facing + "/" + mode);
                }
                helper.assertTrue(new WorldlyContainerWrapper(tile, facing.getOpposite()).size() == 0, "OFF back exposes no slots");
                helper.assertTrue(tile.getItem(0).getCount() == 4 && tile.getItem(1).isEmpty(), "uncommitted transaction must roll back");
            }
        }
        helper.succeed();
    }

    private static void activeOutput(GameTestHelper helper) {
        var tile = place(helper, Direction.SOUTH);
        helper.setBlock(new BlockPos(1, 1, 0), Blocks.CHEST);
        tile.getSideConfiguration().setSideMode(Direction.SOUTH, SideMode.ACTIVE_OUTPUT);
        tile.getInventory().setStackInSlot(0, new ItemStack(Items.BEDROCK, 4));
        helper.runAfterDelay(25, () -> {
            var chest = (ChestBlockEntity) helper.getLevel().getBlockEntity(tile.getBlockPos().north());
            helper.assertTrue(chest.getItem(0).getCount() == 4 && tile.getItem(0).isEmpty(), "active back must push to world NORTH chest");
            helper.succeed();
        });
    }

    private static NeutronCompressorTile place(GameTestHelper helper, Direction facing) {
        var pos = new BlockPos(1, 1, 1);
        helper.setBlock(pos, ModBlocks.densest_neutron_compressor.get().defaultBlockState().setValue(NeutronCompressorBlock.FACING, facing));
        return (NeutronCompressorTile) helper.getLevel().getBlockEntity(helper.absolutePos(pos));
    }
}
