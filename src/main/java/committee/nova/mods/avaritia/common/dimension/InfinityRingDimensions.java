package committee.nova.mods.avaritia.common.dimension;

import committee.nova.mods.avaritia.Const;
import committee.nova.mods.avaritia.common.item.misc.InfinityRingItem;
import committee.nova.mods.avaritia.common.net.InfinityRingSpawnPayloads;
import committee.nova.mods.avaritia.common.net.InfinityRingSpawnPayloads.Result;
import committee.nova.mods.avaritia.common.net.S2CInfinityRingOpenPack;
import committee.nova.mods.avaritia.init.compat.trinkets.TrinketsIntegration;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.NameAndId;
import net.minecraft.server.players.UserNameToIdResolver;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SaplingBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.LevelData;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/** Create, enter, police, and delete Infinity Ring personal dimensions. */
public final class InfinityRingDimensions {
    static final BlockPos SPAWN_ORIGIN = new BlockPos(0, 64, 0);

    private InfinityRingDimensions() {
    }

    public static boolean isPersonal(Level level) {
        return InfinityRingKeys.isPersonal(level.dimension());
    }

    public static boolean holdingRing(Player player) {
        if (player.getMainHandItem().getItem() instanceof InfinityRingItem) {
            return true;
        }
        if (player.getOffhandItem().getItem() instanceof InfinityRingItem) {
            return true;
        }
        return Const.findInventoryItem(player,
                stack -> stack.getItem() instanceof InfinityRingItem, false, stack -> true);
    }

    public static UUID manageTarget(ServerPlayer player) {
        return InfinityRingKeys.ownerOf(player.level().dimension())
                .filter(owner -> {
                    InfinityRingSettings settings = InfinityRingSavedData.get(player.server).settings(owner);
                    return settings != null && settings.canManage(player.getUUID(), owner);
                })
                .orElse(player.getUUID());
    }

    private static InfinityRingSettings managedSettings(ServerPlayer actor, UUID target) {
        if (!holdingRing(actor)) {
            return null;
        }
        InfinityRingSettings settings = InfinityRingSavedData.get(actor.server).settings(target);
        if (settings == null || !settings.canManage(actor.getUUID(), target)) {
            return null;
        }
        return settings;
    }

    public static void travelOrCreatePrompt(ServerPlayer player) {
        if (!holdingRing(player)) {
            return;
        }
        InfinityRingSavedData data = InfinityRingSavedData.get(player.server);
        if (!data.hasDimension(player.getUUID())) {
            S2CInfinityRingOpenPack.openCreate(player);
            return;
        }
        travelOwn(player);
    }

    public static void openControl(ServerPlayer player) {
        if (!holdingRing(player)) {
            return;
        }
        UUID target = manageTarget(player);
        InfinityRingSettings settings = InfinityRingSavedData.get(player.server).settings(target);
        if (settings == null) {
            S2CInfinityRingOpenPack.openCreate(player);
            return;
        }
        if (!settings.canManage(player.getUUID(), target)) {
            return;
        }
        S2CInfinityRingOpenPack.openControl(player, settings, friendNames(player.server, settings), target,
                target.equals(player.getUUID()));
    }

    public static InfinityRingSpawnPayloads.Reply handleSpawnRequest(ServerPlayer actor,
                                                                  InfinityRingSpawnPayloads.Request request) {
        UUID owner = request.owner();
        UUID requestId = request.requestId();
        InfinityRingSettings settings = managedSettings(actor, owner);
        if (!actor.isAlive() || settings == null) {
            return new InfinityRingSpawnPayloads.Reply(owner, requestId, Result.DENIED, Optional.empty());
        }
        if (!request.save()) {
            return new InfinityRingSpawnPayloads.Reply(owner, requestId, Result.STATUS, settings.spawn);
        }
        if (!canSetSpawn(actor) || !actor.level().dimension().equals(InfinityRingKeys.levelKey(owner))) {
            return new InfinityRingSpawnPayloads.Reply(owner, requestId, Result.DENIED, settings.spawn);
        }
        InfinityRingSpawnPoint point = new InfinityRingSpawnPoint(actor.getX(), actor.getY(), actor.getZ(),
                actor.getYRot(), actor.getXRot());
        if (!point.isSafe(actor.level(), actor, false)) {
            return new InfinityRingSpawnPayloads.Reply(owner, requestId, Result.UNSAFE, settings.spawn);
        }
        settings.spawn = Optional.of(point);
        InfinityRingSavedData.get(actor.server).touch();
        return new InfinityRingSpawnPayloads.Reply(owner, requestId, Result.SAVED, settings.spawn);
    }

    private static boolean canSetSpawn(ServerPlayer actor) {
        return actor.getMainHandItem().getItem() instanceof InfinityRingItem
                || actor.getOffhandItem().getItem() instanceof InfinityRingItem
                || !TrinketsIntegration.find(actor, stack -> stack.getItem() instanceof InfinityRingItem).isEmpty();
    }

    public static boolean create(ServerPlayer player, InfinityRingSettings.Terrain terrain,
                                 InfinityRingSettings.TimeMode time, InfinityRingSettings.WeatherMode weather,
                                 InfinityRingSettings.Access access) {
        if (!holdingRing(player)) {
            return false;
        }
        InfinityRingSavedData data = InfinityRingSavedData.get(player.server);
        if (data.hasDimension(player.getUUID())
                || DynamicDimensions.isClosing(InfinityRingKeys.levelKey(player.getUUID()))) {
            player.sendSystemMessage(Component.translatable("message.avaritia.infinity_ring.already_exists"));
            return false;
        }
        InfinityRingSettings settings = new InfinityRingSettings();
        settings.terrain = terrain;
        settings.time = time;
        settings.weather = weather;
        settings.access = access;
        ResourceKey<Level> key = InfinityRingKeys.levelKey(player.getUUID());
        ServerLevel level = DynamicDimensions.getOrCreate(player.server, key,
                () -> DynamicDimensions.stemFor(player.server, terrain));
        if (level == null) {
            return false;
        }
        PersonalLevelData personal = level.getLevelData() instanceof PersonalLevelData data2 ? data2 : null;
        if (personal != null) {
            personal.apply(settings, player.server.overworld());
        }
        BlockPos spawn = buildSpawn(level, terrain);
        // Initialize this dimension without a permanent spawn-region ticket or a global broadcast.
        if (personal != null) {
            personal.setSpawn(LevelData.RespawnData.of(level.dimension(), spawn, 0.0F, 0.0F));
        }
        settings.defaultSpawn = Optional.of(defaultAt(spawn.getY() + 1));
        data.create(player.getUUID(), settings);
        teleportToPersonalSpawn(player, level, settings);
        return true;
    }

    public static void travelOwn(ServerPlayer player) {
        InfinityRingSavedData data = InfinityRingSavedData.get(player.server);
        InfinityRingSettings settings = data.settings(player.getUUID());
        if (settings == null || DynamicDimensions.isClosing(InfinityRingKeys.levelKey(player.getUUID()))) {
            return;
        }
        if (isPersonal(player.level())
                && InfinityRingKeys.ownerOf(player.level().dimension()).filter(player.getUUID()::equals).isPresent()) {
            teleportReturn(player);
            return;
        }
        ServerLevel level = getOrCreateLevel(player.server, player.getUUID());
        if (level == null) {
            return;
        }
        if (level.getLevelData() instanceof PersonalLevelData personal) {
            personal.apply(settings, player.server.overworld());
        }
        teleportToPersonalSpawn(player, level, settings);
    }

    public static boolean visit(ServerPlayer actor, String ownerName) {
        if (!holdingRing(actor)) {
            return false;
        }
        Optional<UUID> owner = resolvePlayer(actor.server, ownerName);
        if (owner.isEmpty()) {
            actor.sendSystemMessage(Component.translatable("message.avaritia.infinity_ring.unknown_player", ownerName));
            return false;
        }
        InfinityRingSettings settings = InfinityRingSavedData.get(actor.server).settings(owner.get());
        if (settings == null) {
            actor.sendSystemMessage(Component.translatable("message.avaritia.infinity_ring.missing"));
            return false;
        }
        if (!settings.canEnter(actor.getUUID(), owner.get())) {
            actor.sendSystemMessage(Component.translatable("message.avaritia.infinity_ring.denied"));
            return false;
        }
        if (DynamicDimensions.isClosing(InfinityRingKeys.levelKey(owner.get()))) {
            actor.sendSystemMessage(Component.translatable("message.avaritia.infinity_ring.missing"));
            return false;
        }
        ServerLevel level = getOrCreateLevel(actor.server, owner.get());
        return level != null && teleportToPersonalSpawn(actor, level, settings);
    }

    public static boolean addFriend(ServerPlayer actor, UUID target, String name) {
        InfinityRingSettings settings = managedSettings(actor, target);
        if (settings == null) {
            return false;
        }
        Optional<UUID> friend = resolvePlayer(actor.server, name);
        if (friend.isEmpty()) {
            actor.sendSystemMessage(Component.translatable("message.avaritia.infinity_ring.unknown_player", name));
            return false;
        }
        if (friend.get().equals(target)) {
            return false;
        }
        settings.banned.remove(friend.get());
        settings.players.putIfAbsent(friend.get(), InfinityRingSettings.Role.VISITOR);
        InfinityRingSavedData.get(actor.server).touch();
        return true;
    }

    public static boolean removeFriend(ServerPlayer actor, UUID target, String name) {
        InfinityRingSettings settings = managedSettings(actor, target);
        if (settings == null) {
            return false;
        }
        Optional<UUID> friend = resolvePlayer(actor.server, name);
        if (friend.isEmpty()) {
            return false;
        }
        boolean removed = settings.players.remove(friend.get()) != null;
        if (removed) {
            InfinityRingSavedData.get(actor.server).touch();
            evictUnauthorized(actor.server, target);
        }
        return removed;
    }

    public static boolean setRole(ServerPlayer actor, UUID target, String name, InfinityRingSettings.Role role) {
        InfinityRingSettings settings = managedSettings(actor, target);
        if (settings == null) {
            return false;
        }
        Optional<UUID> player = resolvePlayer(actor.server, name);
        if (player.isEmpty() || player.get().equals(target)) {
            return false;
        }
        settings.banned.remove(player.get());
        settings.players.put(player.get(), role);
        InfinityRingSavedData.get(actor.server).touch();
        return true;
    }

    public static boolean ban(ServerPlayer actor, UUID target, String name) {
        InfinityRingSettings settings = managedSettings(actor, target);
        if (settings == null) {
            return false;
        }
        Optional<UUID> player = resolvePlayer(actor.server, name);
        if (player.isEmpty() || player.get().equals(target)) {
            return false;
        }
        settings.players.remove(player.get());
        settings.banned.add(player.get());
        InfinityRingSavedData.get(actor.server).touch();
        evictUnauthorized(actor.server, target);
        return true;
    }

    public static boolean unban(ServerPlayer actor, UUID target, String name) {
        InfinityRingSettings settings = managedSettings(actor, target);
        if (settings == null) {
            return false;
        }
        Optional<UUID> player = resolvePlayer(actor.server, name);
        if (player.isEmpty()) {
            return false;
        }
        boolean removed = settings.banned.remove(player.get());
        if (removed) {
            InfinityRingSavedData.get(actor.server).touch();
        }
        return removed;
    }

    public static void updateModes(ServerPlayer actor, UUID target, InfinityRingSettings.TimeMode time,
                                   InfinityRingSettings.WeatherMode weather, InfinityRingSettings.Access access) {
        InfinityRingSettings settings = managedSettings(actor, target);
        if (settings == null) {
            return;
        }
        settings.time = time;
        settings.weather = weather;
        settings.access = access;
        InfinityRingSavedData.get(actor.server).touch();
        ServerLevel level = actor.server.getLevel(InfinityRingKeys.levelKey(target));
        if (level != null && level.getLevelData() instanceof PersonalLevelData personal) {
            personal.apply(settings, actor.server.overworld());
        }
        evictUnauthorized(actor.server, target);
    }

    public static CompletableFuture<Boolean> deleteOwn(ServerPlayer owner) {
        if (!holdingRing(owner)) {
            return CompletableFuture.completedFuture(false);
        }
        UUID uuid = owner.getUUID();
        InfinityRingSavedData data = InfinityRingSavedData.get(owner.server);
        if (!data.hasDimension(uuid) && !DynamicDimensions.isClosing(InfinityRingKeys.levelKey(uuid))) {
            return CompletableFuture.completedFuture(false);
        }
        ResourceKey<Level> key = InfinityRingKeys.levelKey(uuid);
        ServerLevel level = owner.server.getLevel(key);
        if (level != null) {
            for (ServerPlayer occupant : List.copyOf(level.players())) {
                teleportReturn(occupant);
            }
            if (!level.players().isEmpty()) {
                ServerLevel overworld = owner.server.overworld();
                BlockPos spawn = overworld.getLevelData().getRespawnData().pos();
                for (ServerPlayer occupant : List.copyOf(level.players())) {
                    occupant.teleportTo(overworld, spawn.getX() + 0.5, spawn.getY(), spawn.getZ() + 0.5, Set.of(), 0.0F, 0.0F, true);
                }
            }
            if (!level.players().isEmpty()) {
                owner.sendSystemMessage(Component.translatable("message.avaritia.infinity_ring.delete_failed"));
                return CompletableFuture.completedFuture(false);
            }
        }
        return DynamicDimensions.remove(owner.server, key).whenComplete((ok, err) -> {
            if (err != null || !Boolean.TRUE.equals(ok)) {
                owner.sendSystemMessage(Component.translatable("message.avaritia.infinity_ring.delete_failed"));
                return;
            }
            InfinityRingSavedData.get(owner.server).removeOwner(uuid);
            owner.sendSystemMessage(Component.translatable("message.avaritia.infinity_ring.deleted"));
        }).thenApply(Boolean.TRUE::equals);
    }

    public static ServerLevel getOrCreateLevel(MinecraftServer server, UUID owner) {
        ResourceKey<Level> key = InfinityRingKeys.levelKey(owner);
        if (DynamicDimensions.isClosing(key)) {
            return server.getLevel(key);
        }
        InfinityRingSettings settings = InfinityRingSavedData.get(server).settings(owner);
        InfinityRingSettings.Terrain terrain = settings == null ? InfinityRingSettings.Terrain.VOID : settings.terrain;
        return DynamicDimensions.getOrCreate(server, key, () -> DynamicDimensions.stemFor(server, terrain));
    }

    public static void restoreMissingLevels(MinecraftServer server) {
        InfinityRingSavedData data = InfinityRingSavedData.get(server);
        server.getAllLevels().forEach(level -> {
            if (InfinityRingKeys.isPersonal(level.dimension())) {
                DynamicDimensions.rebind(level);
                InfinityRingKeys.ownerOf(level.dimension()).ifPresent(owner -> {
                    InfinityRingSettings settings = data.settings(owner);
                    if (settings != null && level.getLevelData() instanceof PersonalLevelData personal) {
                        personal.apply(settings, server.overworld());
                    }
                });
            }
        });
    }

    public static void evictIfUnauthorized(ServerPlayer player) {
        InfinityRingKeys.ownerOf(player.level().dimension()).ifPresent(owner -> {
            InfinityRingSettings settings = InfinityRingSavedData.get(player.server).settings(owner);
            if (settings == null || !settings.canEnter(player.getUUID(), owner)) {
                teleportReturn(player);
            }
        });
    }

    public static List<String> friendNames(MinecraftServer server, InfinityRingSettings settings) {
        UserNameToIdResolver cache = server.services().nameToIdCache();
        List<String> names = new ArrayList<>();
        settings.players.forEach((id, role) -> cache.get(id).ifPresent(profile ->
                names.add(profile.name() + "|" + role.name())));
        for (UUID banned : settings.banned) {
            cache.get(banned).ifPresent(profile -> names.add(profile.name() + "|BANNED"));
        }
        return names;
    }

    private static void evictUnauthorized(MinecraftServer server, UUID owner) {
        ServerLevel level = server.getLevel(InfinityRingKeys.levelKey(owner));
        if (level == null) {
            return;
        }
        InfinityRingSettings settings = InfinityRingSavedData.get(server).settings(owner);
        if (settings == null) {
            return;
        }
        for (ServerPlayer occupant : List.copyOf(level.players())) {
            if (!settings.canEnter(occupant.getUUID(), owner)) {
                teleportReturn(occupant);
            }
        }
    }

    private static void rememberReturn(ServerPlayer player) {
        if (isPersonal(player.level())) {
            return;
        }
        InfinityRingSavedData.get(player.server).rememberReturn(player.getUUID(),
                new InfinityRingSavedData.TravelPoint(
                        player.level().dimension(),
                        player.getX(),
                        player.getY(),
                        player.getZ(),
                        player.getYRot(),
                        player.getXRot()));
    }

    private static void teleportReturn(ServerPlayer player) {
        InfinityRingSavedData.TravelPoint point = InfinityRingSavedData.get(player.server).takeReturn(player.getUUID());
        ServerLevel dest = point == null ? null : player.server.getLevel(point.dimension());
        if (dest == null || InfinityRingKeys.isPersonal(dest.dimension())) {
            dest = player.server.overworld();
        }
        if (point == null || player.server.getLevel(point.dimension()) == null || InfinityRingKeys.isPersonal(point.dimension())) {
            BlockPos spawn = dest.getLevelData().getRespawnData().pos();
            player.teleportTo(dest, spawn.getX() + 0.5, spawn.getY(), spawn.getZ() + 0.5, Set.of(), player.getYRot(), player.getXRot(), true);
            return;
        }
        player.teleportTo(dest, point.x(), point.y(), point.z(), Set.of(), point.yRot(), point.xRot(), true);
    }

    private static Optional<UUID> resolvePlayer(MinecraftServer server, String name) {
        if (name == null || name.isBlank()) {
            return Optional.empty();
        }
        String trimmed = name.trim();
        ServerPlayer listed = server.getPlayerList().getPlayerByName(trimmed);
        if (listed != null) {
            return Optional.of(listed.getUUID());
        }
        for (ServerLevel level : server.getAllLevels()) {
            for (ServerPlayer occupant : level.players()) {
                if (occupant.nameAndId().name().equalsIgnoreCase(trimmed)) {
                    return Optional.of(occupant.getUUID());
                }
            }
        }
        return server.services().nameToIdCache().get(trimmed).map(NameAndId::id);
    }

    public static double standingFeetY(ServerLevel level, BlockPos spawn) {
        int x = spawn.getX();
        int z = spawn.getZ();
        level.getChunkAt(spawn);
        int min = level.getMinY() + 1;
        int max = level.getMaxY() - 2;
        int start = Math.max(min, Math.min(max, spawn.getY()));
        BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos(x, start, z);
        if (!level.getBlockState(cursor).getCollisionShape(level, cursor).isEmpty()) {
            for (int y = start + 1; y <= max; y++) {
                cursor.setY(y);
                if (level.getBlockState(cursor).isAir() && level.getBlockState(cursor.above()).isAir()) {
                    return y;
                }
            }
            return start + 1;
        }
        for (int y = start; y >= min; y--) {
            cursor.setY(y - 1);
            if (level.getBlockState(cursor).getCollisionShape(level, cursor).isEmpty()) {
                continue;
            }
            cursor.setY(y);
            if (level.getBlockState(cursor).isAir() && level.getBlockState(cursor.above()).isAir()) {
                return y;
            }
            for (int up = y + 1; up <= max; up++) {
                cursor.setY(up);
                if (level.getBlockState(cursor).isAir() && level.getBlockState(cursor.above()).isAir()) {
                    return up;
                }
            }
            return y;
        }
        return start;
    }

    private static boolean teleportToPersonalSpawn(ServerPlayer player, ServerLevel level, InfinityRingSettings settings) {
        InfinityRingSpawnPoint point = settings.spawn.orElse(null);
        if (point == null || !point.isSafe(level, player, true)) {
            InfinityRingSpawnPoint fallback = settings.defaultSpawn.orElseGet(() -> defaultAt(
                    settings.terrain == InfinityRingSettings.Terrain.FLAT
                            ? level.getMinY() + DynamicDimensions.flatLayerHeight() : SPAWN_ORIGIN.getY() + 1));
            if (!fallback.isSafe(level, player, true)) {
                player.sendSystemMessage(Component.translatable("message.avaritia.infinity_ring.spawn_unavailable"));
                return false;
            }
            if (point != null) {
                player.sendSystemMessage(Component.translatable("message.avaritia.infinity_ring.spawn_fallback"));
            }
            point = fallback;
        }
        rememberReturn(player);
        return player.teleportTo(level, point.x(), point.y(), point.z(), Set.of(), point.yaw(), point.pitch(), true);
    }

    private static InfinityRingSpawnPoint defaultAt(double feetY) {
        return new InfinityRingSpawnPoint(SPAWN_ORIGIN.getX() + 0.5, feetY,
                SPAWN_ORIGIN.getZ() + 0.5, 0.0F, 0.0F);
    }

    private static BlockPos buildSpawn(ServerLevel level, InfinityRingSettings.Terrain terrain) {
        if (terrain == InfinityRingSettings.Terrain.FLAT) {
            BlockPos origin = BlockPos.ZERO;
            level.getChunkAt(origin);
            int feet = (int) Math.floor(standingFeetY(level, origin));
            return new BlockPos(0, Math.max(level.getMinY(), feet - 1), 0);
        }
        BlockPos origin = SPAWN_ORIGIN;
        if (terrain == InfinityRingSettings.Terrain.VOID) {
            placePlatform(level, origin, 2);
            return origin;
        }
        placePlatform(level, origin, 4);
        BlockPos sapling = origin.offset(3, 1, 0);
        BlockState oak = Blocks.OAK_SAPLING.defaultBlockState();
        level.setBlock(sapling, oak, 3);
        if (oak.getBlock() instanceof SaplingBlock saplingBlock) {
            saplingBlock.advanceTree(level, sapling, oak, level.getRandom());
        }
        return origin;
    }

    private static void placePlatform(ServerLevel level, BlockPos origin, int radius) {
        for (int x = -radius; x <= radius; x++) {
            for (int z = -radius; z <= radius; z++) {
                level.setBlock(origin.offset(x, 0, z), Blocks.GRASS_BLOCK.defaultBlockState(), 3);
                level.setBlock(origin.offset(x, -1, z), Blocks.DIRT.defaultBlockState(), 3);
            }
        }
    }
}
