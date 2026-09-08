package committee.nova.mods.avaritia.gametest;

import committee.nova.mods.avaritia.Const;
import committee.nova.mods.avaritia.common.dimension.InfinityRingDimensions;
import committee.nova.mods.avaritia.common.dimension.InfinityRingKeys;
import committee.nova.mods.avaritia.common.dimension.InfinityRingSavedData;
import committee.nova.mods.avaritia.common.dimension.InfinityRingSettings;
import committee.nova.mods.avaritia.init.handler.AbilityHandler;
import committee.nova.mods.avaritia.init.registry.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.FunctionGameTestInstance;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.gametest.framework.TestData;
import net.minecraft.gametest.framework.TestEnvironmentDefinition;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.state.properties.SlabType;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.RegisterGameTestsEvent;
import net.neoforged.neoforge.event.tick.EntityTickEvent;
import net.neoforged.neoforge.registries.RegisterEvent;

import java.util.Set;
import java.util.UUID;

/** World lifecycle regressions for Infinity Ring dimensions. */
@EventBusSubscriber(modid = "avaritia_gametest")
public final class InfinityRingGameTests {
    private static final Identifier EMPTY_STRUCTURE = Identifier.withDefaultNamespace("empty");
    private static final Identifier SURFACE = Const.rl("infinity_ring_standing_feet_surface");
    private static final Identifier OBSTRUCTED = Const.rl("infinity_ring_standing_feet_obstructed");
    private static final Identifier RETURNS = Const.rl("infinity_ring_actor_return");
    private static final Identifier EVICT_FRIENDS = Const.rl("infinity_ring_public_to_friends_evicts");
    private static final Identifier EVICT_BAN = Const.rl("infinity_ring_ban_evicts");
    private static final Identifier DELETE_RECREATE = Const.rl("infinity_ring_delete_and_recreate");
    private static final Identifier FLIGHT = Const.rl("infinity_ring_flight_owned_then_revoked");

    private InfinityRingGameTests() {
    }

    @SubscribeEvent
    public static void registerFunctions(RegisterEvent event) {
        event.register(Registries.TEST_FUNCTION, SURFACE, () -> InfinityRingGameTests::standingFeetMatchTerrainSurface);
        event.register(Registries.TEST_FUNCTION, OBSTRUCTED, () -> InfinityRingGameTests::standingFeetSkipObstructingBlocks);
        event.register(Registries.TEST_FUNCTION, RETURNS, () -> InfinityRingGameTests::travelReturnsActorToRememberedPoint);
        event.register(Registries.TEST_FUNCTION, EVICT_FRIENDS, () -> InfinityRingGameTests::publicToFriendsEvictsVisitor);
        event.register(Registries.TEST_FUNCTION, EVICT_BAN, () -> InfinityRingGameTests::banEvictsOccupant);
        event.register(Registries.TEST_FUNCTION, DELETE_RECREATE, () -> InfinityRingGameTests::deleteUnregistersThenRecreateWorks);
        event.register(Registries.TEST_FUNCTION, FLIGHT, () -> InfinityRingGameTests::personalFlightGrantedThenRevokedWithoutChest);
    }

    @SubscribeEvent
    public static void register(RegisterGameTestsEvent event) {
        Holder<TestEnvironmentDefinition<?>> environment = event.registerEnvironment(
                Const.rl("infinity_ring"), new TestEnvironmentDefinition.AllOf());
        register(event, SURFACE, environment);
        register(event, OBSTRUCTED, environment);
        register(event, RETURNS, environment);
        register(event, EVICT_FRIENDS, environment);
        register(event, EVICT_BAN, environment);
        register(event, DELETE_RECREATE, environment);
        register(event, FLIGHT, environment);
    }

    private static void register(RegisterGameTestsEvent event, Identifier id,
                                 Holder<TestEnvironmentDefinition<?>> environment) {
        event.registerTest(id, new FunctionGameTestInstance(
                ResourceKey.create(Registries.TEST_FUNCTION, id),
                new TestData<>(environment, EMPTY_STRUCTURE, 100, 0, true)));
    }

    private static void standingFeetMatchTerrainSurface(GameTestHelper helper) {
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

    private static void standingFeetSkipObstructingBlocks(GameTestHelper helper) {
        BlockPos groundRel = new BlockPos(8, 2, 8);
        helper.setBlock(groundRel, Blocks.GRASS_BLOCK);
        helper.setBlock(groundRel.above(), Blocks.OAK_SLAB.defaultBlockState().setValue(SlabBlock.TYPE, SlabType.TOP));
        helper.setBlock(groundRel.above(2), Blocks.AIR);
        helper.setBlock(groundRel.above(3), Blocks.AIR);
        BlockPos ground = helper.absolutePos(groundRel);
        helper.getLevel().getChunkAt(ground);
        double feet = InfinityRingDimensions.standingFeetY(helper.getLevel(), ground);
        helper.assertTrue(feet >= ground.getY() + 2, "feet should rise above the obstructing slab");
        helper.assertTrue(helper.getLevel().getBlockState(new BlockPos(ground.getX(), (int) feet, ground.getZ())).isAir(),
                "standing position must be air");
        helper.succeed();
    }

    private static void travelReturnsActorToRememberedPoint(GameTestHelper helper) {
        ServerPlayer owner = survivalWithRing(helper);
        BlockPos origin = helper.absolutePos(new BlockPos(4, 2, 4));
        owner.snapTo(origin.getX() + 0.5, origin.getY(), origin.getZ() + 0.5, 33.0F, 7.0F);
        helper.assertTrue(InfinityRingDimensions.create(owner,
                InfinityRingSettings.Terrain.VOID, InfinityRingSettings.TimeMode.DAY,
                InfinityRingSettings.WeatherMode.CLEAR, InfinityRingSettings.Access.PRIVATE),
                "owner should create a personal dimension");
        helper.runAtTickTime(1, () -> {
            ResourceKey<Level> personal = InfinityRingKeys.levelKey(owner.getUUID());
            helper.assertTrue(owner.level().dimension().equals(personal), "create should move the owner into the personal dim");
            InfinityRingDimensions.travelOwn(owner);
            helper.assertTrue(!InfinityRingKeys.isPersonal(owner.level().dimension()),
                    "second use should leave the personal dim");
            helper.assertTrue(Math.abs(owner.getX() - (origin.getX() + 0.5)) < 0.01
                            && Math.abs(owner.getY() - origin.getY()) < 0.01
                            && Math.abs(owner.getZ() - (origin.getZ() + 0.5)) < 0.01,
                    "return should restore the actor's remembered overworld point");
            helper.assertTrue(Math.abs(owner.getYRot() - 33.0F) < 0.01F && Math.abs(owner.getXRot() - 7.0F) < 0.01F,
                    "return should restore the actor's remembered rotation");
            cleanup(owner);
            helper.succeed();
        });
    }

    private static void publicToFriendsEvictsVisitor(GameTestHelper helper) {
        ServerPlayer owner = survivalWithRing(helper);
        ServerPlayer visitor = survivalWithRing(helper);
        helper.assertTrue(!owner.getUUID().equals(visitor.getUUID()), "owner and visitor must be distinct actors");
        helper.assertTrue(InfinityRingDimensions.create(owner,
                InfinityRingSettings.Terrain.VOID, InfinityRingSettings.TimeMode.DAY,
                InfinityRingSettings.WeatherMode.CLEAR, InfinityRingSettings.Access.PUBLIC),
                "owner should create a public personal dimension");
        helper.runAtTickTime(1, () -> {
            ServerLevel personal = personalLevel(helper, owner.getUUID());
            enterPersonal(visitor, personal);
            helper.assertTrue(visitor.level().dimension().equals(personal.dimension()),
                    "visitor should occupy the public personal dim");
            InfinityRingDimensions.updateModes(owner, owner.getUUID(),
                    InfinityRingSettings.TimeMode.DAY, InfinityRingSettings.WeatherMode.CLEAR,
                    InfinityRingSettings.Access.FRIENDS);
            helper.assertTrue(!visitor.level().dimension().equals(personal.dimension()),
                    "PUBLIC to FRIENDS must evict a visitor who is not on the list");
            helper.assertTrue(personal.players().stream().noneMatch(player -> player.getUUID().equals(visitor.getUUID())),
                    "evicted visitor must not remain in the personal player list");
            cleanup(owner);
            helper.succeed();
        });
    }

    private static void banEvictsOccupant(GameTestHelper helper) {
        ServerPlayer owner = survivalWithRing(helper);
        ServerPlayer visitor = survivalWithRing(helper);
        helper.assertTrue(InfinityRingDimensions.create(owner,
                InfinityRingSettings.Terrain.VOID, InfinityRingSettings.TimeMode.DAY,
                InfinityRingSettings.WeatherMode.CLEAR, InfinityRingSettings.Access.PUBLIC),
                "owner should create a public personal dimension");
        helper.runAtTickTime(1, () -> {
            ServerLevel personal = personalLevel(helper, owner.getUUID());
            enterPersonal(visitor, personal);
            helper.assertTrue(visitor.level().dimension().equals(personal.dimension()),
                    "visitor should occupy the public personal dim");
            InfinityRingSettings settings = InfinityRingSavedData.get(owner.level().getServer()).settings(owner.getUUID());
            settings.players.remove(visitor.getUUID());
            settings.banned.add(visitor.getUUID());
            InfinityRingSavedData.get(owner.level().getServer()).touch();
            helper.assertTrue(!settings.canEnter(visitor.getUUID(), owner.getUUID()),
                    "banned actors cannot enter");
            InfinityRingDimensions.evictIfUnauthorized(visitor);
            helper.assertTrue(!visitor.level().dimension().equals(personal.dimension()),
                    "ban must evict an occupant immediately");
            cleanup(owner);
            helper.succeed();
        });
    }

    private static void deleteUnregistersThenRecreateWorks(GameTestHelper helper) {
        ServerPlayer owner = survivalWithRing(helper);
        UUID uuid = owner.getUUID();
        helper.assertTrue(InfinityRingDimensions.create(owner,
                InfinityRingSettings.Terrain.VOID, InfinityRingSettings.TimeMode.DAY,
                InfinityRingSettings.WeatherMode.CLEAR, InfinityRingSettings.Access.PRIVATE),
                "first create should succeed");
        helper.runAtTickTime(1, () -> {
            MinecraftServer server = owner.level().getServer();
            ResourceKey<Level> key = InfinityRingKeys.levelKey(uuid);
            helper.assertTrue(server.getLevel(key) != null, "personal level must exist after create");
            helper.assertTrue(InfinityRingSavedData.get(server).hasDimension(uuid), "saved owner must exist after create");
            helper.assertTrue(InfinityRingDimensions.deleteOwn(owner), "owner holding the ring should delete the dim");
            helper.assertTrue(server.getLevel(key) == null, "deleted personal level must leave the world map");
            helper.assertTrue(!InfinityRingSavedData.get(server).hasDimension(uuid), "saved owner must be removed after delete");
            helper.assertTrue(InfinityRingDimensions.create(owner,
                    InfinityRingSettings.Terrain.FLAT, InfinityRingSettings.TimeMode.NIGHT,
                    InfinityRingSettings.WeatherMode.CLEAR, InfinityRingSettings.Access.PRIVATE),
                    "same-session recreate after delete must succeed");
            helper.runAtTickTime(2, () -> {
                helper.assertTrue(server.getLevel(key) != null, "recreated personal level must exist");
                helper.assertTrue(InfinityRingSavedData.get(server).hasDimension(uuid), "recreated owner settings must exist");
                helper.assertTrue(owner.level().dimension().equals(key), "owner should enter the recreated dim");
                cleanup(owner);
                helper.succeed();
            });
        });
    }

    private static void personalFlightGrantedThenRevokedWithoutChest(GameTestHelper helper) {
        ServerPlayer owner = survivalWithRing(helper);
        owner.getAbilities().mayfly = false;
        owner.getAbilities().flying = false;
        helper.assertTrue(InfinityRingDimensions.create(owner,
                InfinityRingSettings.Terrain.VOID, InfinityRingSettings.TimeMode.DAY,
                InfinityRingSettings.WeatherMode.CLEAR, InfinityRingSettings.Access.PRIVATE),
                "owner should create a personal dimension");
        helper.runAtTickTime(1, () -> {
            helper.assertTrue(InfinityRingKeys.isPersonal(owner.level().dimension()), "owner must be inside the personal dim");
            tickFlight(owner);
            helper.assertTrue(owner.getAbilities().mayfly, "personal dim should grant mayfly in survival");
            AbilityHandler.FlightInfo info = AbilityHandler.entitiesWithFlight.get(flightKey(owner));
            helper.assertTrue(info != null && info.grantedByUs, "AbilityHandler must own the granted flight");
            InfinityRingDimensions.travelOwn(owner);
            helper.assertTrue(!InfinityRingKeys.isPersonal(owner.level().dimension()), "owner should have left the personal dim");
            tickFlight(owner);
            helper.assertTrue(!owner.getAbilities().mayfly,
                    "leaving without infinity chest must revoke only AbilityHandler-owned flight");
            cleanup(owner);
            helper.succeed();
        });
    }

    private static ServerPlayer survivalWithRing(GameTestHelper helper) {
        ServerPlayer player = GameTestPlayers.occupy(helper, "AvaritiaRing-" + UUID.randomUUID().toString().substring(0, 8));
        player.setGameMode(GameType.SURVIVAL);
        player.getAbilities().mayfly = false;
        player.getAbilities().flying = false;
        player.getInventory().clearContent();
        player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(ModItems.infinity_ring.get()));
        BlockPos pos = helper.absolutePos(new BlockPos(4, 2, 4));
        player.setPos(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5);
        return player;
    }

    private static ServerLevel personalLevel(GameTestHelper helper, UUID owner) {
        ServerLevel level = helper.getLevel().getServer().getLevel(InfinityRingKeys.levelKey(owner));
        helper.assertTrue(level != null, "personal level must exist");
        return level;
    }

    private static void enterPersonal(ServerPlayer visitor, ServerLevel personal) {
        BlockPos spawn = personal.getRespawnData().pos();
        double y = InfinityRingDimensions.standingFeetY(personal, spawn);
        visitor.teleportTo(personal, spawn.getX() + 0.5, y, spawn.getZ() + 0.5, Set.of(), visitor.getYRot(), visitor.getXRot(), false);
    }

    private static void tickFlight(ServerPlayer player) {
        NeoForge.EVENT_BUS.post(new EntityTickEvent.Post(player));
    }

    private static String flightKey(ServerPlayer player) {
        return player.getGameProfile().name() + ":" + player.level().isClientSide();
    }

    private static void cleanup(ServerPlayer owner) {
        if (InfinityRingDimensions.holdingRing(owner)) {
            InfinityRingDimensions.deleteOwn(owner);
        }
    }
}
