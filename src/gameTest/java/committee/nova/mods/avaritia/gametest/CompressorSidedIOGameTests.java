package committee.nova.mods.avaritia.gametest;

import committee.nova.mods.avaritia.Const;
import committee.nova.mods.avaritia.common.block.compressor.NeutronCompressorBlock;
import committee.nova.mods.avaritia.common.tile.NeutronCompressorTile;
import committee.nova.mods.avaritia.core.io.SideConfiguration.SideMode;
import committee.nova.mods.avaritia.init.registry.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;
import net.neoforged.neoforge.items.wrapper.SidedInvWrapper;

import java.util.Arrays;

@GameTestHolder(Const.MOD_ID)
@PrefixGameTestTemplate(false)
public final class CompressorSidedIOGameTests {
    private CompressorSidedIOGameTests() {}

    @GameTest(template = "portable_ui_empty")
    public static void southFacingUsesRelativeFrontAndBack(GameTestHelper helper) {
        var tile = place(helper, new BlockPos(1, 1, 1), Direction.SOUTH);
        tile.getSideConfiguration().setSideMode(Direction.NORTH, SideMode.ACTIVE_MIXIN);
        tile.getSideConfiguration().setSideMode(Direction.SOUTH, SideMode.ACTIVE_OUTPUT);
        var slots = tile.getSlotsForFace(Direction.SOUTH);
        System.out.println("ISSUE395 front world=SOUTH facing=SOUTH slots=" + Arrays.toString(slots)
                + " allowsInput=" + tile.canPlaceItemThroughFace(1, new ItemStack(Items.DEEPSLATE), Direction.SOUTH));
        var front = new SidedInvWrapper(tile, Direction.SOUTH);
        var remainder = front.insertItem(0, new ItemStack(Items.DEEPSLATE, 8), false);
        helper.assertTrue(remainder.isEmpty() && tile.getItem(1).getCount() == 8,
                "world SOUTH must insert into the configured relative front input");
        tile.getInventory().setStackInSlot(0, new ItemStack(Items.BEDROCK, 4));
        var back = new SidedInvWrapper(tile, Direction.NORTH);
        helper.assertTrue(back.getSlots() == 1 && back.extractItem(0, 4, false).getCount() == 4,
                "world NORTH must expose only the configured relative back output");
        helper.succeed();
    }

    @GameTest(template = "portable_ui_empty")
    public static void mixedModesExposeBothSlots(GameTestHelper helper) {
        var tile = place(helper, new BlockPos(1, 1, 1), Direction.SOUTH);
        for (var mode : new SideMode[]{SideMode.PASSIVE_MIXIN, SideMode.ACTIVE_MIXIN}) {
            for (Direction side : Direction.values()) tile.getSideConfiguration().setSideMode(side, mode);
            tile.getInventory().setStackInSlot(0, new ItemStack(Items.BEDROCK, 4));
            tile.getInventory().setStackInSlot(1, ItemStack.EMPTY);
            var handler = helper.getLevel().getCapability(Capabilities.ItemHandler.BLOCK, tile.getBlockPos(), Direction.NORTH);
            helper.assertTrue(handler != null, "registered sided capability must exist");
            System.out.println("ISSUE395 mode=" + mode + " slots=" + Arrays.toString(tile.getSlotsForFace(Direction.NORTH)));
            int extracted = 0;
            for (int slot = 0; slot < handler.getSlots(); slot++) extracted += handler.extractItem(slot, 4, false).getCount();
            helper.assertTrue(extracted == 4 && tile.getItem(0).isEmpty(), "mixed mode must expose and extract four output items: " + mode);
            helper.assertTrue(handler.insertItem(0, new ItemStack(Items.DEEPSLATE, 8), false).isEmpty()
                    && tile.getItem(1).getCount() == 8, "mixed input must use the first exposed slot");
        }
        helper.succeed();
    }

    @GameTest(template = "portable_ui_empty")
    public static void singleModesAndOffRespectEveryFacing(GameTestHelper helper) {
        var tile = place(helper, new BlockPos(1, 1, 1), Direction.NORTH);
        for (Direction facing : Direction.Plane.HORIZONTAL) {
            helper.getLevel().setBlockAndUpdate(tile.getBlockPos(), tile.getBlockState().setValue(NeutronCompressorBlock.FACING, facing));
            for (SideMode mode : SideMode.values()) {
                for (Direction side : Direction.values()) tile.getSideConfiguration().setSideMode(side, SideMode.OFF);
                tile.getSideConfiguration().setSideMode(Direction.NORTH, mode);
                tile.getInventory().setStackInSlot(0, new ItemStack(Items.BEDROCK, 4));
                tile.getInventory().setStackInSlot(1, ItemStack.EMPTY);
                var handler = new SidedInvWrapper(tile, facing);
                int expected = mode.isMixed() ? 2 : mode == SideMode.OFF ? 0 : 1;
                helper.assertTrue(handler.getSlots() == expected, "slot count for " + facing + "/" + mode);
                int inserted = 0;
                int extracted = 0;
                for (int slot = 0; slot < handler.getSlots(); slot++) {
                    inserted += 8 - handler.insertItem(slot, new ItemStack(Items.DEEPSLATE, 8), true).getCount();
                    extracted += handler.extractItem(slot, 4, true).getCount();
                }
                helper.assertTrue(inserted == (mode.canInput() ? 8 : 0) && extracted == (mode.canOutput() ? 4 : 0),
                        "mode direction and permissions for " + facing + "/" + mode);
                var blockedBack = new SidedInvWrapper(tile, facing.getOpposite());
                helper.assertTrue(blockedBack.getSlots() == 0, "OFF back must not expose slots");
                helper.assertTrue(tile.getItem(0).getCount() == 4 && tile.getItem(1).isEmpty(), "simulation must not mutate inventory");
            }
        }
        helper.succeed();
    }

    @GameTest(template = "portable_ui_empty", timeoutTicks = 100)
    public static void activeBackOutputStillPushesIntoChest(GameTestHelper helper) {
        var tile = place(helper, new BlockPos(1, 1, 1), Direction.SOUTH);
        helper.setBlock(new BlockPos(1, 1, 0), Blocks.CHEST);
        tile.getSideConfiguration().setSideMode(Direction.SOUTH, SideMode.ACTIVE_OUTPUT);
        tile.getInventory().setStackInSlot(0, new ItemStack(Items.BEDROCK, 4));
        helper.runAfterDelay(25, () -> {
            var chest = (ChestBlockEntity) helper.getLevel().getBlockEntity(tile.getBlockPos().north());
            helper.assertTrue(chest.getItem(0).getCount() == 4 && tile.getItem(0).isEmpty(), "active relative back must push all four items to world NORTH chest");
            helper.succeed();
        });
    }

    private static NeutronCompressorTile place(GameTestHelper helper, BlockPos pos, Direction facing) {
        helper.setBlock(pos, ModBlocks.densest_neutron_compressor.get().defaultBlockState().setValue(NeutronCompressorBlock.FACING, facing));
        return (NeutronCompressorTile) helper.getLevel().getBlockEntity(helper.absolutePos(pos));
    }
}
