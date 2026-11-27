package committee.nova.mods.avaritia.gametest;

import com.mojang.authlib.GameProfile;
import committee.nova.mods.avaritia.Const;
import committee.nova.mods.avaritia.common.dimension.InfinityRingDimensions;
import committee.nova.mods.avaritia.common.dimension.InfinityRingKeys;
import committee.nova.mods.avaritia.common.dimension.InfinityRingSavedData;
import committee.nova.mods.avaritia.common.dimension.InfinityRingSettings;
import committee.nova.mods.avaritia.init.registry.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.TickTask;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.ai.village.poi.PoiTypes;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.storage.DerivedLevelData;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.util.FakePlayerFactory;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicBoolean;

@GameTestHolder(Const.MOD_ID)
@PrefixGameTestTemplate(false)
public final class InfinityRingLifecycleGameTests {
    private static final String TEMPLATE = "portable_ui_empty";

    private InfinityRingLifecycleGameTests() {
    }

    @GameTest(template = TEMPLATE, timeoutTicks = 200)
    public static void publicToFriendsEvictsVisitorToOwnReturn(GameTestHelper helper) {
        ServerPlayer owner = ringPlayer(helper, "IROwnPub");
        ServerPlayer visitor = ringPlayer(helper, "IRGstPub");
        BlockPos origin = helper.absolutePos(new BlockPos(5, 2, 5));
        visitor.moveTo(origin.getX() + 0.5, origin.getY(), origin.getZ() + 0.5, 0.0F, 0.0F);
        double returnX = visitor.getX();
        double returnY = visitor.getY();
        double returnZ = visitor.getZ();
        ResourceKey<Level> returnDim = visitor.level().dimension();

        helper.assertTrue(InfinityRingDimensions.create(owner,
                InfinityRingSettings.Terrain.VOID, InfinityRingSettings.TimeMode.DAY,
                InfinityRingSettings.WeatherMode.CLEAR, InfinityRingSettings.Access.PUBLIC),
                "owner should create a public dimension");
        helper.runAfterDelay(2, () -> {
            ServerLevel personal = InfinityRingDimensions.getOrCreateLevel(owner.server, owner.getUUID());
            InfinityRingSavedData.get(owner.server).rememberReturn(visitor.getUUID(),
                    new InfinityRingSavedData.TravelPoint(returnDim, returnX, returnY, returnZ, 0.0F, 0.0F));
            visitor.teleportTo(personal, 0.5, InfinityRingDimensions.standingFeetY(personal, personal.getSharedSpawnPos()),
                    0.5, 0.0F, 0.0F);
            helper.assertTrue(InfinityRingKeys.ownerOf(visitor.level().dimension()).filter(owner.getUUID()::equals).isPresent(),
                    "visitor should occupy the public personal dimension");

            InfinityRingDimensions.updateModes(owner, owner.getUUID(),
                    InfinityRingSettings.TimeMode.DAY, InfinityRingSettings.WeatherMode.CLEAR,
                    InfinityRingSettings.Access.FRIENDS);

            helper.assertTrue(visitor.level().dimension().equals(returnDim),
                    "PUBLIC->FRIENDS should send the unlisted visitor to their return dimension");
            helper.assertTrue(Math.abs(visitor.getX() - returnX) < 0.01
                            && Math.abs(visitor.getY() - returnY) < 0.01
                            && Math.abs(visitor.getZ() - returnZ) < 0.01,
                    "visitor should land on their own return coordinates");
            helper.assertTrue(InfinityRingDimensions.isPersonal(owner.level())
                            || owner.server.getLevel(InfinityRingKeys.levelKey(owner.getUUID())) != null,
                    "owner dimension should still exist after evicting the visitor");
            finish(helper, owner, visitor);
        });
    }

    @GameTest(template = TEMPLATE, timeoutTicks = 200)
    public static void banEvictsOccupant(GameTestHelper helper) {
        ServerPlayer owner = ringPlayer(helper, "IROwnBan");
        ServerPlayer visitor = ringPlayer(helper, "IRGstBan");
        helper.assertTrue(InfinityRingDimensions.create(owner,
                InfinityRingSettings.Terrain.VOID, InfinityRingSettings.TimeMode.DAY,
                InfinityRingSettings.WeatherMode.CLEAR, InfinityRingSettings.Access.PUBLIC),
                "owner should create a public dimension");
        helper.runAfterDelay(2, () -> {
            ServerLevel personal = InfinityRingDimensions.getOrCreateLevel(owner.server, owner.getUUID());
            visitor.teleportTo(personal, 0.5, InfinityRingDimensions.standingFeetY(personal, personal.getSharedSpawnPos()),
                    0.5, 0.0F, 0.0F);
            helper.assertTrue(InfinityRingDimensions.addFriend(owner, owner.getUUID(), visitor.getGameProfile().getName()),
                    "owner should be able to list the visitor");
            helper.assertTrue(InfinityRingDimensions.ban(owner, owner.getUUID(), visitor.getGameProfile().getName()),
                    "owner should be able to ban the occupant");
            helper.assertFalse(InfinityRingKeys.isPersonal(visitor.level().dimension()),
                    "ban should evict the occupant from the personal dimension");
            InfinityRingSettings settings = InfinityRingSavedData.get(owner.server).settings(owner.getUUID());
            helper.assertTrue(settings != null && !settings.canEnter(visitor.getUUID(), owner.getUUID()),
                    "banned visitor must not be allowed back in");
            finish(helper, owner, visitor);
        });
    }

    @GameTest(template = TEMPLATE, timeoutTicks = 200)
    public static void personalReturnFallsBackWhenDestinationIsPersonal(GameTestHelper helper) {
        ServerPlayer owner = ringPlayer(helper, "IROwnFb");
        ServerPlayer visitor = ringPlayer(helper, "IRGstFb");
        helper.assertTrue(InfinityRingDimensions.create(owner,
                InfinityRingSettings.Terrain.VOID, InfinityRingSettings.TimeMode.DAY,
                InfinityRingSettings.WeatherMode.CLEAR, InfinityRingSettings.Access.PUBLIC),
                "owner should create a public dimension");
        helper.runAfterDelay(2, () -> {
            ResourceKey<Level> personalKey = InfinityRingKeys.levelKey(owner.getUUID());
            ServerLevel personal = owner.server.getLevel(personalKey);
            helper.assertTrue(personal != null, "personal level should exist");
            InfinityRingSavedData.get(owner.server).rememberReturn(visitor.getUUID(),
                    new InfinityRingSavedData.TravelPoint(personalKey, 8, 70, 8, 0.0F, 0.0F));
            visitor.teleportTo(personal, 0.5, InfinityRingDimensions.standingFeetY(personal, personal.getSharedSpawnPos()),
                    0.5, 0.0F, 0.0F);
            InfinityRingDimensions.updateModes(owner, owner.getUUID(),
                    InfinityRingSettings.TimeMode.DAY, InfinityRingSettings.WeatherMode.CLEAR,
                    InfinityRingSettings.Access.PRIVATE);
            helper.assertTrue(visitor.level().dimension().equals(Level.OVERWORLD),
                    "a personal return destination must fall back to overworld spawn");
            helper.assertFalse(InfinityRingKeys.isPersonal(visitor.level().dimension()),
                    "evicted visitor must not remain in a personal dimension");
            finish(helper, owner, visitor);
        });
    }

    @GameTest(template = TEMPLATE, timeoutTicks = 400)
    public static void deleteThenSameSessionRecreate(GameTestHelper helper) {
        ServerPlayer owner = ringPlayer(helper, "IROwnDel");
        helper.assertTrue(InfinityRingDimensions.create(owner,
                InfinityRingSettings.Terrain.VOID, InfinityRingSettings.TimeMode.DAY,
                InfinityRingSettings.WeatherMode.CLEAR, InfinityRingSettings.Access.PRIVATE),
                "first create should succeed");
        helper.runAfterDelay(2, () -> {
            ResourceKey<Level> key = InfinityRingKeys.levelKey(owner.getUUID());
            ServerLevel personal = owner.server.getLevel(key);
            helper.assertTrue(personal != null, "created level must be loaded");
            AtomicBoolean queuedPoiFinished = new AtomicBoolean(false);
            owner.server.tell(new TickTask(owner.server.getTickCount(), () -> {
                personal.getPoiManager().add(personal.getSharedSpawnPos(),
                        PoiTypes.forState(Blocks.BELL.defaultBlockState()).orElseThrow());
                queuedPoiFinished.set(true);
            }));
            CompletableFuture<Boolean> deleted = InfinityRingDimensions.deleteOwn(owner);
            helper.assertTrue(owner.server.getLevel(key) == null,
                    "world map must drop before deferred close");
            helper.assertTrue(InfinityRingSavedData.get(owner.server).hasDimension(owner.getUUID()),
                    "owner settings must remain until close succeeds");
            deleted.whenComplete((ok, err) -> helper.runAfterDelay(1, () -> {
                helper.assertTrue(err == null && Boolean.TRUE.equals(ok),
                        "delete while inside should evacuate and remove");
                helper.assertTrue(queuedPoiFinished.get(),
                        "already-queued POI mailbox work must run before close");
                helper.assertTrue(owner.server.getLevel(key) == null, "deleted level must leave the world map");
                helper.assertFalse(InfinityRingSavedData.get(owner.server).hasDimension(owner.getUUID()),
                        "owner settings must be removed only after a successful delete");
                helper.assertTrue(InfinityRingDimensions.create(owner,
                        InfinityRingSettings.Terrain.VOID, InfinityRingSettings.TimeMode.NIGHT,
                        InfinityRingSettings.WeatherMode.CLEAR, InfinityRingSettings.Access.PRIVATE),
                        "same-session recreate must succeed");
                helper.runAfterDelay(2, () -> {
                    helper.assertTrue(owner.server.getLevel(key) != null, "recreated level must be loaded");
                    helper.assertTrue(InfinityRingSavedData.get(owner.server).hasDimension(owner.getUUID()),
                            "recreate must restore owner settings");
                    finish(helper, owner);
                });
            }));
        });
    }

    @GameTest(template = TEMPLATE, timeoutTicks = 200)
    public static void personalFlightGrantedAndRevokedOnLeave(GameTestHelper helper) {
        ServerPlayer owner = ringPlayer(helper, "IROwnFly");
        owner.getAbilities().mayfly = false;
        owner.getAbilities().flying = false;
        owner.onUpdateAbilities();
        helper.assertTrue(InfinityRingDimensions.create(owner,
                InfinityRingSettings.Terrain.VOID, InfinityRingSettings.TimeMode.DAY,
                InfinityRingSettings.WeatherMode.CLEAR, InfinityRingSettings.Access.PRIVATE),
                "owner should create a private dimension");
        helper.runAfterDelay(2, () -> {
            helper.assertTrue(InfinityRingDimensions.isPersonal(owner.level()),
                    "owner should be inside the personal dimension before flight is granted");
            assertStandingOnPersonalGround(helper, owner);
            tickAbilities(owner);
            helper.assertTrue(owner.getAbilities().mayfly,
                    "survival personal dimension must grant mayfly from AbilityHandler");
            InfinityRingDimensions.travelOwn(owner);
            helper.assertFalse(InfinityRingDimensions.isPersonal(owner.level()),
                    "second use should return the owner");
            tickAbilities(owner);
            helper.assertFalse(owner.getAbilities().mayfly,
                    "leaving without a chest must revoke only the flight we granted");
            finish(helper, owner);
        });
    }

    @GameTest(template = TEMPLATE, timeoutTicks = 400)
    public static void skyIslandArrivalSurvivesReturnVisitAndReload(GameTestHelper helper) {
        checkTerrainArrivals(helper, InfinityRingSettings.Terrain.SKY_ISLAND);
    }

    @GameTest(template = TEMPLATE, timeoutTicks = 400)
    public static void flatArrivalSurvivesReturnVisitAndReload(GameTestHelper helper) {
        checkTerrainArrivals(helper, InfinityRingSettings.Terrain.FLAT);
    }

    private static void checkTerrainArrivals(GameTestHelper helper, InfinityRingSettings.Terrain terrain) {
        ServerPlayer owner = ringPlayer(helper, "SpawnOwn" + terrain.ordinal());
        ServerPlayer visitor = ringPlayer(helper, "SpawnGuest" + terrain.ordinal());
        ServerLevel overworld = owner.server.overworld();
        BlockPos overworldSpawn = overworld.getSharedSpawnPos();
        float overworldAngle = overworld.getSharedSpawnAngle();
        helper.assertTrue(InfinityRingDimensions.create(owner, terrain,
                        InfinityRingSettings.TimeMode.DAY, InfinityRingSettings.WeatherMode.CLEAR,
                        InfinityRingSettings.Access.PUBLIC),
                "terrain creation should succeed");
        helper.assertTrue(overworldSpawn.equals(overworld.getSharedSpawnPos())
                        && overworldAngle == overworld.getSharedSpawnAngle(),
                "personal spawn setup must not change the overworld spawn");

        helper.runAfterDelay(2, () -> {
            assertStandingOnPersonalGround(helper, owner);
            ServerLevel personal = owner.serverLevel();
            BlockPos editedGround = owner.blockPosition().below().offset(2, 0, 2);
            personal.setBlock(editedGround, Blocks.GOLD_BLOCK.defaultBlockState(), 3);
            InfinityRingDimensions.travelOwn(owner);
            helper.assertFalse(InfinityRingDimensions.isPersonal(owner.level()), "owner should return before re-entry");
            InfinityRingDimensions.travelOwn(owner);
            helper.assertTrue(InfinityRingDimensions.visit(visitor, owner.getGameProfile().getName()),
                    "visitor should enter the public personal dimension");

            helper.runAfterDelay(2, () -> {
                assertStandingOnPersonalGround(helper, owner);
                assertStandingOnPersonalGround(helper, visitor);
                helper.assertTrue(visitor.serverLevel() == personal, "visitor should arrive on the owner's island");
                InfinityRingDimensions.travelOwn(owner);

                // A restored runtime level initially has vanilla derived metadata. Exercise the
                // real rebind path on entry without rebuilding or deleting the saved terrain.
                DerivedLevelData restored = new DerivedLevelData(
                        owner.server.getWorldData(), owner.server.getWorldData().overworldData());
                personal.levelData = restored;
                personal.serverLevelData = restored;
                InfinityRingDimensions.travelOwn(owner);

                helper.runAfterDelay(2, () -> {
                    assertStandingOnPersonalGround(helper, owner);
                    helper.assertTrue(owner.serverLevel() == personal, "re-entry should reuse the restored level");
                    helper.assertTrue(personal.getBlockState(editedGround).is(Blocks.GOLD_BLOCK),
                            "restoring spawn metadata must preserve player-edited terrain");
                    helper.assertTrue(overworldSpawn.equals(overworld.getSharedSpawnPos())
                                    && overworldAngle == overworld.getSharedSpawnAngle(),
                            "return and reload must not leak personal spawn into the overworld");
                    finish(helper, owner, visitor);
                });
            });
        });
    }

    private static void assertStandingOnPersonalGround(GameTestHelper helper, ServerPlayer player) {
        helper.assertTrue(InfinityRingDimensions.isPersonal(player.level()), "player must arrive in a personal dimension");
        BlockPos feet = player.blockPosition();
        ServerLevel level = player.serverLevel();
        helper.assertTrue(level.getBlockState(feet.below()).is(Blocks.GRASS_BLOCK),
                "arrival must stand on the generated terrain, not in the void");
        helper.assertTrue(level.getBlockState(feet).getCollisionShape(level, feet).isEmpty()
                        && level.getBlockState(feet.above()).getCollisionShape(level, feet.above()).isEmpty(),
                "arrival must leave room for the player's feet and head");
        helper.assertTrue(Math.abs(player.getY() - feet.getY()) < 0.01,
                "feet must align with the ground surface");
    }

    private static void tickAbilities(ServerPlayer player) {
        MinecraftForge.EVENT_BUS.post(new LivingEvent.LivingTickEvent(player));
    }

    private static ServerPlayer ringPlayer(GameTestHelper helper, String name) {
        ServerLevel level = helper.getLevel();
        GameProfile profile = new GameProfile(UUID.randomUUID(), name);
        ServerPlayer player = FakePlayerFactory.get(level, profile);
        if (level.getServer().getProfileCache() != null) {
            level.getServer().getProfileCache().add(profile);
        }
        player.setGameMode(GameType.SURVIVAL);
        player.getAbilities().mayfly = false;
        player.getAbilities().flying = false;
        player.onUpdateAbilities();
        BlockPos pos = helper.absolutePos(new BlockPos(4, 2, 4));
        player.moveTo(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5, 0.0F, 0.0F);
        if (!level.players().contains(player)) {
            level.addNewPlayer(player);
        }
        player.getInventory().setItem(0, new ItemStack(ModItems.infinity_ring.get()));
        player.getInventory().selected = 0;
        return player;
    }

    private static void finish(GameTestHelper helper, ServerPlayer... players) {
        CompletableFuture<Void> cleanup = CompletableFuture.completedFuture(null);
        for (ServerPlayer player : players) {
            cleanup = cleanup.thenCompose(ignored -> {
                if (!InfinityRingSavedData.get(player.server).hasDimension(player.getUUID())) {
                    return CompletableFuture.completedFuture(null);
                }
                return InfinityRingDimensions.deleteOwn(player).thenAccept(ok ->
                        helper.assertTrue(ok, "test cleanup must delete leftover personal dimensions"));
            });
        }
        cleanup.whenComplete((ignored, err) -> {
            for (ServerPlayer player : players) {
                if (!player.isRemoved()) {
                    player.serverLevel().removePlayerImmediately(player, Entity.RemovalReason.DISCARDED);
                }
            }
            if (err != null) {
                helper.fail(err.getMessage() == null ? err.toString() : err.getMessage());
            } else {
                helper.succeed();
            }
        });
    }
}
