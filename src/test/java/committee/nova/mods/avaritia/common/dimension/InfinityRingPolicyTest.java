package committee.nova.mods.avaritia.common.dimension;

import net.minecraft.SharedConstants;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.Bootstrap;
import net.minecraft.world.level.Level;
import net.minecraftforge.network.NetworkHooks;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mockStatic;

class InfinityRingPolicyTest {
    private static final UUID OWNER = UUID.fromString("00000000-0000-0000-0000-000000000001");
    private static final UUID ACTOR = UUID.fromString("00000000-0000-0000-0000-000000000002");
    private static final UUID OTHER = UUID.fromString("00000000-0000-0000-0000-000000000003");

    @BeforeAll
    static void bootstrapMinecraft() {
        SharedConstants.tryDetectVersion();
        try (MockedStatic<NetworkHooks> ignored = mockStatic(NetworkHooks.class)) {
            Bootstrap.bootStrap();
        }
    }

    @Test
    void ownerAlwaysEntersAndManages() {
        InfinityRingSettings settings = new InfinityRingSettings();
        settings.access = InfinityRingSettings.Access.PRIVATE;
        assertTrue(settings.canEnter(OWNER, OWNER));
        assertTrue(settings.canManage(OWNER, OWNER));
    }

    @Test
    void privateDeniesNonOwnerEvenIfListed() {
        InfinityRingSettings settings = new InfinityRingSettings();
        settings.access = InfinityRingSettings.Access.PRIVATE;
        settings.players.put(ACTOR, InfinityRingSettings.Role.ADMIN);
        assertFalse(settings.canEnter(ACTOR, OWNER));
        assertTrue(settings.canManage(ACTOR, OWNER));
    }

    @Test
    void friendsRequiresListedPlayerAndBanOverridesPublic() {
        InfinityRingSettings settings = new InfinityRingSettings();
        settings.access = InfinityRingSettings.Access.FRIENDS;
        assertFalse(settings.canEnter(ACTOR, OWNER));
        settings.players.put(ACTOR, InfinityRingSettings.Role.VISITOR);
        assertTrue(settings.canEnter(ACTOR, OWNER));
        settings.access = InfinityRingSettings.Access.PUBLIC;
        assertTrue(settings.canEnter(OTHER, OWNER));
        settings.banned.add(OTHER);
        assertFalse(settings.canEnter(OTHER, OWNER));
        assertTrue(settings.canEnter(OWNER, OWNER));
    }

    @Test
    void publicToFriendsDeniesUnlistedActors() {
        InfinityRingSettings settings = new InfinityRingSettings();
        settings.access = InfinityRingSettings.Access.PUBLIC;
        assertTrue(settings.canEnter(ACTOR, OWNER));
        settings.access = InfinityRingSettings.Access.FRIENDS;
        assertFalse(settings.canEnter(ACTOR, OWNER));
        settings.players.put(ACTOR, InfinityRingSettings.Role.MEMBER);
        assertTrue(settings.canEnter(ACTOR, OWNER));
    }

    @Test
    void onlyAdminRoleCanManageBesidesOwner() {
        InfinityRingSettings settings = new InfinityRingSettings();
        settings.players.put(ACTOR, InfinityRingSettings.Role.MEMBER);
        assertFalse(settings.canManage(ACTOR, OWNER));
        settings.players.put(ACTOR, InfinityRingSettings.Role.ADMIN);
        assertTrue(settings.canManage(ACTOR, OWNER));
        assertFalse(settings.canManage(OTHER, OWNER));
    }

    @Test
    void legacyFriendsNbtBecomesMemberAndDoesNotOverrideRole() {
        CompoundTag tag = new CompoundTag();
        tag.putInt("Access", InfinityRingSettings.Access.FRIENDS.ordinal());
        ListTag friends = new ListTag();
        friends.add(NbtUtils.createUUID(ACTOR));
        tag.put("Friends", friends);
        CompoundTag player = new CompoundTag();
        player.put("Id", NbtUtils.createUUID(ACTOR));
        player.putInt("Role", InfinityRingSettings.Role.ADMIN.ordinal());
        ListTag players = new ListTag();
        players.add(player);
        tag.put("Players", players);

        InfinityRingSettings settings = InfinityRingSettings.load(tag);
        assertSame(InfinityRingSettings.Role.ADMIN, settings.players.get(ACTOR));
        assertTrue(settings.canEnter(ACTOR, OWNER));

        CompoundTag legacyOnly = new CompoundTag();
        legacyOnly.putInt("Access", InfinityRingSettings.Access.FRIENDS.ordinal());
        legacyOnly.put("Friends", friends);
        InfinityRingSettings migrated = InfinityRingSettings.load(legacyOnly);
        assertSame(InfinityRingSettings.Role.MEMBER, migrated.players.get(ACTOR));
        assertTrue(migrated.canEnter(ACTOR, OWNER));
    }

    @Test
    void settingsRoundTripKeepsRolesAndBans() {
        InfinityRingSettings settings = new InfinityRingSettings();
        settings.terrain = InfinityRingSettings.Terrain.FLAT;
        settings.time = InfinityRingSettings.TimeMode.CYCLE;
        settings.weather = InfinityRingSettings.WeatherMode.RAIN;
        settings.access = InfinityRingSettings.Access.FRIENDS;
        settings.players.put(ACTOR, InfinityRingSettings.Role.ADMIN);
        settings.banned.add(OTHER);

        InfinityRingSettings loaded = InfinityRingSettings.load(settings.save());
        assertEquals(InfinityRingSettings.Terrain.FLAT, loaded.terrain);
        assertEquals(InfinityRingSettings.TimeMode.CYCLE, loaded.time);
        assertEquals(InfinityRingSettings.WeatherMode.RAIN, loaded.weather);
        assertEquals(InfinityRingSettings.Access.FRIENDS, loaded.access);
        assertSame(InfinityRingSettings.Role.ADMIN, loaded.players.get(ACTOR));
        assertTrue(loaded.banned.contains(OTHER));
        assertFalse(loaded.canEnter(OTHER, OWNER));
    }

    @Test
    void perActorReturnsAreTakenOnceAndMissingIsNull() {
        InfinityRingSavedData data = new InfinityRingSavedData();
        ResourceKey<Level> overworld = Level.OVERWORLD;
        InfinityRingSavedData.TravelPoint point =
                new InfinityRingSavedData.TravelPoint(overworld, 10.5, 64.0, -3.25, 90.0F, 15.0F);
        data.rememberReturn(ACTOR, point);
        assertEquals(point, data.peekReturn(ACTOR));
        assertEquals(point, data.takeReturn(ACTOR));
        assertNull(data.takeReturn(ACTOR));
        assertNull(data.peekReturn(OTHER));
    }

    @Test
    void personalDimensionKeysEncodeOwnerUuid() {
        ResourceKey<Level> personal = InfinityRingKeys.levelKey(OWNER);
        assertTrue(InfinityRingKeys.isPersonal(personal));
        assertEquals(OWNER, InfinityRingKeys.ownerOf(personal).orElseThrow());
        InfinityRingSavedData data = new InfinityRingSavedData();
        data.rememberReturn(ACTOR, new InfinityRingSavedData.TravelPoint(personal, 1, 2, 3, 0, 0));
        InfinityRingSavedData.TravelPoint stored = data.peekReturn(ACTOR);
        assertTrue(InfinityRingKeys.isPersonal(stored.dimension()));
    }

    @Test
    void removeOwnerDeletesSettingsButKeepsUnrelatedReturns() {
        InfinityRingSavedData data = new InfinityRingSavedData();
        InfinityRingSettings settings = new InfinityRingSettings();
        data.create(OWNER, settings);
        assertTrue(data.hasDimension(OWNER));
        data.rememberReturn(ACTOR, new InfinityRingSavedData.TravelPoint(Level.NETHER, 0, 80, 0, 0, 0));
        data.removeOwner(OWNER);
        assertFalse(data.hasDimension(OWNER));
        assertNull(data.settings(OWNER));
        assertEquals(Level.NETHER, data.peekReturn(ACTOR).dimension());
    }
}
