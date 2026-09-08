package committee.nova.mods.avaritia.gametest;

import committee.nova.mods.avaritia.Const;
import committee.nova.mods.avaritia.common.dimension.InfinityRingDimensions;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.state.properties.SlabType;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

@GameTestHolder(Const.MOD_ID)
@PrefixGameTestTemplate(false)
public final class InfinityRingSpawnGameTests {
    private static final String TEMPLATE = "portable_ui_empty";

    private InfinityRingSpawnGameTests() {
    }

    @GameTest(template = TEMPLATE, timeoutTicks = 100)
    public static void standingFeetMatchTerrainSurface(GameTestHelper helper) {
        BlockPos groundRel = new BlockPos(8, 3, 8);
        helper.setBlock(groundRel, Blocks.GRASS_BLOCK);
        helper.setBlock(groundRel.above(), Blocks.AIR);
        helper.setBlock(groundRel.above(2), Blocks.AIR);
        BlockPos ground = helper.absolutePos(groundRel);
        helper.getLevel().getChunkAt(ground);
        int feet = (int) InfinityRingDimensions.standingFeetY(helper.getLevel(), ground);
        BlockPos feetPos = new BlockPos(ground.getX(), feet, ground.getZ());
        helper.assertTrue(helper.getLevel().getBlockState(feetPos).isAir(), "feet must be in air");
        helper.assertTrue(helper.getLevel().getBlockState(feetPos.below()).is(Blocks.GRASS_BLOCK),
                "feet should stand on the grass, not at a fixed world height");
        helper.succeed();
    }

    @GameTest(template = TEMPLATE, timeoutTicks = 100)
    public static void standingFeetSkipObstructingBlocks(GameTestHelper helper) {
        BlockPos groundRel = new BlockPos(8, 2, 8);
        helper.setBlock(groundRel, Blocks.GRASS_BLOCK);
        helper.setBlock(groundRel.above(), Blocks.OAK_SLAB.defaultBlockState().setValue(SlabBlock.TYPE, SlabType.TOP));
        helper.setBlock(groundRel.above(2), Blocks.AIR);
        helper.setBlock(groundRel.above(3), Blocks.AIR);
        BlockPos ground = helper.absolutePos(groundRel);
        helper.getLevel().getChunkAt(ground);
        double feet = InfinityRingDimensions.standingFeetY(helper.getLevel(), ground);
        helper.assertTrue(feet >= ground.getY() + 2, "feet should rise above the obstructing slab");
        helper.assertTrue(helper.getLevel().getBlockState(BlockPos.containing(ground.getX(), feet, ground.getZ())).isAir(),
                "standing position must be air");
        helper.succeed();
    }
}
