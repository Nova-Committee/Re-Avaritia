package committee.nova.mods.avaritia.common.dimension;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.serialization.JsonOps;
import net.minecraft.core.UUIDUtil;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class InfinityRingSettingsTest {
    @Test
    void ownerAlwaysEntersAndManages() {
        InfinityRingSettings settings = new InfinityRingSettings();
        settings.access = InfinityRingSettings.Access.PRIVATE;
        UUID owner = UUID.randomUUID();
        assertTrue(settings.canEnter(owner, owner));
        assertTrue(settings.canManage(owner, owner));
    }

    @Test
    void bannedActorCannotEnterEvenWhenPublic() {
        InfinityRingSettings settings = new InfinityRingSettings();
        settings.access = InfinityRingSettings.Access.PUBLIC;
        UUID owner = UUID.randomUUID();
        UUID banned = UUID.randomUUID();
        settings.banned.add(banned);
        assertFalse(settings.canEnter(banned, owner));
        assertTrue(settings.canEnter(UUID.randomUUID(), owner));
    }

    @Test
    void friendsAccessRequiresListedPlayerAndEvictsAfterRemoval() {
        InfinityRingSettings settings = new InfinityRingSettings();
        settings.access = InfinityRingSettings.Access.FRIENDS;
        UUID owner = UUID.randomUUID();
        UUID visitor = UUID.randomUUID();
        assertFalse(settings.canEnter(visitor, owner));
        settings.players.put(visitor, InfinityRingSettings.Role.VISITOR);
        assertTrue(settings.canEnter(visitor, owner));
        assertFalse(settings.canManage(visitor, owner));
        settings.players.remove(visitor);
        assertFalse(settings.canEnter(visitor, owner));
    }

    @Test
    void onlyAdminRoleCanManageForNonOwner() {
        InfinityRingSettings settings = new InfinityRingSettings();
        UUID owner = UUID.randomUUID();
        UUID admin = UUID.randomUUID();
        UUID member = UUID.randomUUID();
        settings.players.put(admin, InfinityRingSettings.Role.ADMIN);
        settings.players.put(member, InfinityRingSettings.Role.MEMBER);
        assertTrue(settings.canManage(admin, owner));
        assertFalse(settings.canManage(member, owner));
    }

    @Test
    void legacyFriendsCodecLoadsAsMemberWithoutDroppingRoles() {
        UUID legacyFriend = UUID.randomUUID();
        UUID admin = UUID.randomUUID();
        JsonObject json = new JsonObject();
        json.addProperty("access", InfinityRingSettings.Access.FRIENDS.ordinal());
        json.add("friends", UUIDUtil.CODEC.listOf().encodeStart(JsonOps.INSTANCE, List.of(legacyFriend)).getOrThrow());
        JsonObject adminEntry = new JsonObject();
        adminEntry.add("id", UUIDUtil.CODEC.encodeStart(JsonOps.INSTANCE, admin).getOrThrow());
        adminEntry.addProperty("role", InfinityRingSettings.Role.ADMIN.ordinal());
        JsonArray players = new JsonArray();
        players.add(adminEntry);
        json.add("players", players);

        InfinityRingSettings loaded = InfinityRingSettings.CODEC.parse(JsonOps.INSTANCE, json).getOrThrow();
        assertEquals(InfinityRingSettings.Role.MEMBER, loaded.players.get(legacyFriend));
        assertEquals(InfinityRingSettings.Role.ADMIN, loaded.players.get(admin));
        assertTrue(loaded.canEnter(legacyFriend, UUID.randomUUID()));
        assertTrue(loaded.canManage(admin, UUID.randomUUID()));
    }

    @Test
    void roundTripKeepsRolesAndBansAndDoesNotNeedLegacyFriends() {
        InfinityRingSettings settings = new InfinityRingSettings();
        settings.access = InfinityRingSettings.Access.FRIENDS;
        UUID member = UUID.randomUUID();
        UUID banned = UUID.randomUUID();
        settings.players.put(member, InfinityRingSettings.Role.MEMBER);
        settings.banned.add(banned);

        JsonElement encoded = InfinityRingSettings.CODEC.encodeStart(JsonOps.INSTANCE, settings).getOrThrow();
        InfinityRingSettings loaded = InfinityRingSettings.CODEC.parse(JsonOps.INSTANCE, encoded).getOrThrow();
        assertEquals(InfinityRingSettings.Role.MEMBER, loaded.players.get(member));
        assertTrue(loaded.banned.contains(banned));
        assertFalse(loaded.canEnter(banned, UUID.randomUUID()));
        assertTrue(encoded.isJsonObject());
        JsonArray friends = encoded.getAsJsonObject().has("friends")
                ? encoded.getAsJsonObject().getAsJsonArray("friends")
                : new JsonArray();
        assertTrue(friends.isEmpty());
    }
}
