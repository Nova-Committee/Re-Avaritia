package committee.nova.mods.avaritia.gametest;

import com.mojang.authlib.GameProfile;
import committee.nova.mods.avaritia.Const;
import committee.nova.mods.avaritia.common.dimension.InfinityRingDimensions;
import committee.nova.mods.avaritia.common.dimension.InfinityRingSavedData;
import committee.nova.mods.avaritia.common.dimension.InfinityRingSettings;
import committee.nova.mods.avaritia.common.dimension.InfinityRingSpawnPoint;
import committee.nova.mods.avaritia.common.net.C2SInfinityRingSpawnPack;
import committee.nova.mods.avaritia.common.net.S2CInfinityRingSpawnPack.Result;
import committee.nova.mods.avaritia.init.registry.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.state.properties.SlabType;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

import java.util.UUID;

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

    @GameTest(template = TEMPLATE, timeoutTicks = 100)
    public static void exactSpawnRequiresSafeStandingSpace(GameTestHelper helper) {
        BlockPos ground = new BlockPos(8, 3, 5);
        BlockPos world = helper.absolutePos(ground);
        var level = helper.getLevel();
        var player = new ServerPlayer(level.getServer(), level, new GameProfile(UUID.randomUUID(), "SpawnCollision"));
        var point = new InfinityRingSpawnPoint(world.getX() + 0.4, world.getY() + 0.5, world.getZ() + 0.65, 25, -10);
        helper.setBlock(ground, Blocks.OAK_SLAB);
        helper.setBlock(ground.above(), Blocks.AIR);
        helper.setBlock(ground.above(2), Blocks.AIR);
        helper.assertTrue(point.isSafe(level, player, true), "a bottom slab supports the exact saved position");
        helper.setBlock(ground.above(2), Blocks.STONE);
        helper.assertFalse(point.isSafe(level, player, true), "a standing player needs headroom");
        helper.setBlock(ground.above(2), Blocks.AIR);
        helper.setBlock(ground, Blocks.AIR);
        helper.assertFalse(point.isSafe(level, player, true), "a position without support is unsafe");
        var fullBlockPoint = new InfinityRingSpawnPoint(point.x(), world.getY() + 1, point.z(), point.yaw(), point.pitch());
        helper.setBlock(ground, Blocks.MAGMA_BLOCK);
        helper.assertFalse(fullBlockPoint.isSafe(level, player, true), "a hazardous floor is unsafe");
        helper.setBlock(ground, Blocks.STONE);
        helper.setBlock(ground.above(), Blocks.LAVA);
        helper.assertFalse(fullBlockPoint.isSafe(level, player, true), "fluid at the destination is unsafe");
        helper.succeed();
    }

    @GameTest(template = TEMPLATE, timeoutTicks = 100)
    public static void spawnRequestChecksRingPermissionAndDimension(GameTestHelper helper) {
        var level = helper.getLevel();
        var actor = new ServerPlayer(level.getServer(), level, new GameProfile(UUID.randomUUID(), "SpawnPermission"));
        UUID owner = UUID.randomUUID();
        var data = InfinityRingSavedData.get(level.getServer());
        var settings = new InfinityRingSettings();
        var original = new InfinityRingSpawnPoint(0.5, 65, 0.5, 0, 0);
        settings.spawn = original;
        settings.players.put(actor.getUUID(), InfinityRingSettings.Role.VISITOR);
        data.create(owner, settings);
        var status = new C2SInfinityRingSpawnPack(owner, UUID.randomUUID(), false);
        var save = new C2SInfinityRingSpawnPack(owner, UUID.randomUUID(), true);
        try {
            actor.getInventory().setItem(0, new ItemStack(ModItems.infinity_ring.get()));
            helper.assertTrue(InfinityRingDimensions.handleSpawnRequest(actor, save).result() == Result.DENIED,
                    "a visitor cannot set the owner's spawn");
            settings.players.put(actor.getUUID(), InfinityRingSettings.Role.ADMIN);
            helper.assertTrue(InfinityRingDimensions.handleSpawnRequest(actor, status).result() == Result.STATUS,
                    "an administrator with a ring can read the status outside the dimension");
            helper.assertTrue(InfinityRingDimensions.handleSpawnRequest(actor, save).result() == Result.DENIED,
                    "an administrator cannot save a position in another world");
            actor.getInventory().setItem(0, ItemStack.EMPTY);
            helper.assertTrue(InfinityRingDimensions.handleSpawnRequest(actor, status).result() == Result.DENIED,
                    "an administrator without a ring cannot read management status");
            helper.assertTrue(original.equals(settings.spawn), "rejected requests must keep the saved entry point");
        } finally {
            data.removeOwner(owner);
        }
        helper.succeed();
    }
}
