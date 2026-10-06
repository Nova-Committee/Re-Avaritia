package committee.nova.mods.avaritia.gametest;

import committee.nova.mods.avaritia.init.registry.ModBlocks;
import committee.nova.mods.avaritia.init.registry.ModItems;
import io.netty.buffer.Unpooled;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.game.ClientboundBlockUpdatePacket;
import net.minecraft.world.item.BlockItem;

public final class NativeBlockStateGameTests {
    private NativeBlockStateGameTests() {}

    @GameTest(template = "avaritia:portable_ui_empty")
    public static void modBlockItemsSurviveVanillaCloneStack(GameTestHelper helper) {
        BlockPos pos = helper.absolutePos(new BlockPos(4, 2, 4));
        for (var entry : ModItems.ITEMS.getEntries()) {
            if (entry.get() instanceof BlockItem blockItem) {
                var block = blockItem.getBlock();
                var state = block.defaultBlockState();
                helper.getLevel().setBlockAndUpdate(pos, state);
                var picked = block.getCloneItemStack(helper.getLevel(), pos, state);
                helper.assertTrue(picked.is(blockItem),
                        "vanilla clone/pick-block must preserve registered item " + entry.getId());
            }
        }
        helper.succeed();
    }

    @GameTest(template = "avaritia:portable_ui_empty")
    public static void modBlocksSurviveVanillaBlockUpdatePackets(GameTestHelper helper) {
        BlockPos pos = helper.absolutePos(new BlockPos(4, 2, 4));
        FriendlyByteBuf buffer = new FriendlyByteBuf(Unpooled.buffer());
        try {
            for (var entry : ModBlocks.BLOCKS.getEntries()) {
                for (var state : entry.get().getStateDefinition().getPossibleStates()) {
                    helper.getLevel().setBlockAndUpdate(pos, state);
                    buffer.clear();
                    new ClientboundBlockUpdatePacket(helper.getLevel(), pos).write(buffer);
                    var decoded = new ClientboundBlockUpdatePacket(buffer);
                    helper.assertTrue(decoded.getBlockState() == state,
                            "vanilla network update must preserve " + entry.getId() + " state " + state);
                }
            }
        } finally {
            buffer.release();
        }
        helper.succeed();
    }
}
