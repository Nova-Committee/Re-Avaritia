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
import net.minecraft.server.level.ClientInformation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

import java.util.UUID;

@GameTestHolder(Const.MOD_ID)
@PrefixGameTestTemplate(false)
public final class InfinityRingSpawnGameTests {
    private static final String TEMPLATE = "portable_ui_empty";

    private InfinityRingSpawnGameTests() {
    }

    @GameTest(template = TEMPLATE, timeoutTicks = 100)
    public static void exactSpawnRequiresSupportAndHeadroom(GameTestHelper helper) {
        BlockPos ground = new BlockPos(8, 3, 5);
        BlockPos world = helper.absolutePos(ground);
        var player = helper.makeMockPlayer(GameType.SURVIVAL);
        var spawn = new InfinityRingSpawnPoint(world.getX() + 0.4, world.getY() + 0.5, world.getZ() + 0.65, 25, -10);
        helper.setBlock(ground, Blocks.OAK_SLAB);
        helper.setBlock(ground.above(), Blocks.AIR);
        helper.setBlock(ground.above(2), Blocks.AIR);
        helper.assertTrue(spawn.isSafe(helper.getLevel(), player, true), "bottom slab supports the exact saved feet height");
        helper.setBlock(ground.above(2), Blocks.STONE);
        helper.assertTrue(!spawn.isSafe(helper.getLevel(), player, true), "standing headroom is required");
        helper.setBlock(ground.above(2), Blocks.AIR);
        helper.setBlock(ground, Blocks.AIR);
        helper.assertTrue(!spawn.isSafe(helper.getLevel(), player, true), "an unsupported destination must be rejected");
        helper.succeed();
    }

    @GameTest(template = TEMPLATE, timeoutTicks = 100)
    public static void spawnRequestChecksAuthorityAndDimension(GameTestHelper helper) {
        var level = helper.getLevel();
        var actor = new ServerPlayer(level.getServer(), level, new GameProfile(UUID.randomUUID(), "ring-test"),
                ClientInformation.createDefault());
        UUID owner = UUID.randomUUID();
        var data = InfinityRingSavedData.get(level.getServer());
        var settings = new InfinityRingSettings();
        settings.players.put(actor.getUUID(), InfinityRingSettings.Role.VISITOR);
        data.create(owner, settings);
        try {
            actor.getInventory().setItem(0, new ItemStack(ModItems.infinity_ring.get()));
            var status = new C2SInfinityRingSpawnPack(owner, UUID.randomUUID(), false);
            helper.assertTrue(InfinityRingDimensions.handleSpawnRequest(actor, status).result() == Result.DENIED,
                    "visitors may not read the management state");
            settings.players.put(actor.getUUID(), InfinityRingSettings.Role.ADMIN);
            helper.assertTrue(InfinityRingDimensions.handleSpawnRequest(actor, status).result() == Result.STATUS,
                    "administrators may read the management state from outside the target dimension");
            var save = new C2SInfinityRingSpawnPack(owner, UUID.randomUUID(), true);
            helper.assertTrue(InfinityRingDimensions.handleSpawnRequest(actor, save).result() == Result.DENIED,
                    "authority and a held ring cannot save a position in the wrong dimension");
            helper.assertTrue(!settings.save().contains("Spawn"), "no spawn should have been stored");
        } finally {
            data.removeOwner(owner);
        }
        helper.succeed();
    }

    @GameTest(template = TEMPLATE, timeoutTicks = 100)
    public static void exactSpawnRejectsHazardousFloorAndFluid(GameTestHelper helper) {
        BlockPos ground = new BlockPos(8, 3, 5);
        BlockPos world = helper.absolutePos(ground);
        var player = helper.makeMockPlayer(GameType.SURVIVAL);
        var spawn = new InfinityRingSpawnPoint(world.getX() + 0.5, world.getY() + 1, world.getZ() + 0.5, 0, 0);
        helper.setBlock(ground, Blocks.MAGMA_BLOCK);
        helper.assertTrue(!spawn.isSafe(helper.getLevel(), player, true), "magma must not be accepted as support");
        helper.setBlock(ground, Blocks.STONE);
        helper.setBlock(ground.above(), Blocks.WATER);
        helper.assertTrue(!spawn.isSafe(helper.getLevel(), player, true), "fluid at the exact destination must be rejected");
        helper.succeed();
    }
}
