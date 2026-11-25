package committee.nova.mods.avaritia.common.dimension;

import committee.nova.mods.avaritia.Const;
import committee.nova.mods.avaritia.common.item.misc.InfinityRingItem;
import committee.nova.mods.avaritia.common.net.C2SInfinityRingSpawnPack;
import committee.nova.mods.avaritia.common.net.S2CInfinityRingOpenPack;
import committee.nova.mods.avaritia.common.net.S2CInfinityRingSpawnPack;
import committee.nova.mods.avaritia.common.net.S2CInfinityRingSpawnPack.Result;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.GameProfileCache;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SaplingBlock;
import net.minecraft.world.level.block.state.BlockState;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/** Create, enter, police, and delete Infinity Ring personal dimensions. */
public final class InfinityRingDimensions {
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
        return Boolean.TRUE.equals(Const.checkExtraSlots(player,
                stack -> stack.getItem() instanceof InfinityRingItem, false, stack -> true));
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
            player.connection.send(new S2CInfinityRingOpenPack(true, 0, 0, 0, 0, List.of(), player.getUUID(), true));
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
            player.connection.send(new S2CInfinityRingOpenPack(true, 0, 0, 0, 0, List.of(), player.getUUID(), true));
            return;
        }
        if (!settings.canManage(player.getUUID(), target)) {
            return;
        }
        player.connection.send(new S2CInfinityRingOpenPack(false,
                settings.terrain.ordinal(), settings.time.ordinal(), settings.weather.ordinal(),
                settings.access.ordinal(), friendNames(player.server, settings), target,
                target.equals(player.getUUID())));
    }

    public static S2CInfinityRingSpawnPack handleSpawnRequest(ServerPlayer actor, C2SInfinityRingSpawnPack request) {
        InfinityRingSettings settings = actor.isAlive() ? managedSettings(actor, request.owner()) : null;
        if (settings == null) {
            return S2CInfinityRingSpawnPack.reply(request, Result.DENIED, null);
        }
        if (!request.save()) {
            return S2CInfinityRingSpawnPack.reply(request, Result.STATUS, settings.spawn);
        }
        if (!actor.level().dimension().equals(InfinityRingKeys.levelKey(request.owner()))) {
            return S2CInfinityRingSpawnPack.reply(request, Result.DENIED, settings.spawn);
        }
        InfinityRingSpawnPoint point = new InfinityRingSpawnPoint(actor.getX(), actor.getY(), actor.getZ(),
                actor.getYRot(), actor.getXRot());
        if (!point.isSafe(actor.serverLevel(), actor, false)) {
            return S2CInfinityRingSpawnPack.reply(request, Result.UNSAFE, settings.spawn);
        }
        settings.spawn = point;
        InfinityRingSavedData.get(actor.server).touch();
        return S2CInfinityRingSpawnPack.reply(request, Result.SAVED, point);
    }

    public static boolean create(ServerPlayer player, InfinityRingSettings.Terrain terrain,
                                 InfinityRingSettings.TimeMode time, InfinityRingSettings.WeatherMode weather,
                                 InfinityRingSettings.Access access) {
        if (!holdingRing(player)) {
            return false;
        }
        InfinityRingSavedData data = InfinityRingSavedData.get(player.server);
        if (data.hasDimension(player.getUUID())) {
            player.sendSystemMessage(Component.translatable("message.avaritia.infinity_ring.already_exists"));
            return false;
        }
        InfinityRingSettings settings = new InfinityRingSettings();
        settings.terrain = terrain;
        settings.time = time;
        settings.weather = weather;
        settings.access = access;
        ServerLevel level = getOrCreateLevel(player.server, player.getUUID(), terrain);
        if (level.getLevelData() instanceof PersonalLevelData personal) {
            personal.apply(settings, player.server.overworld());
        }
        settings.spawn = defaultSpawn(level, terrain);
        buildSpawnPlatform(level, terrain, settings.spawn);
        data.create(player.getUUID(), settings);
        teleportToPersonalSpawn(player, level, settings, true);
        return true;
    }

    public static void travelOwn(ServerPlayer player) {
        InfinityRingSavedData data = InfinityRingSavedData.get(player.server);
        InfinityRingSettings settings = data.settings(player.getUUID());
        if (settings == null) {
            return;
        }
        if (player.level().dimension().equals(InfinityRingKeys.levelKey(player.getUUID()))) {
            teleportReturn(player);
            return;
        }
        ServerLevel level = getOrCreateLevel(player.server, player.getUUID(), settings.terrain);
        if (level.getLevelData() instanceof PersonalLevelData personal) {
            personal.apply(settings, player.server.overworld());
        }
        teleportToPersonalSpawn(player, level, settings, !isPersonal(player.level()));
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
        ServerLevel level = getOrCreateLevel(actor.server, owner.get(), settings.terrain);
        return teleportToPersonalSpawn(actor, level, settings, !isPersonal(actor.level()));
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

    public static boolean deleteOwn(ServerPlayer owner) {
        if (!holdingRing(owner)) {
            return false;
        }
        UUID uuid = owner.getUUID();
        InfinityRingSavedData data = InfinityRingSavedData.get(owner.server);
        if (!data.hasDimension(uuid)) {
            return false;
        }
        ResourceKey<Level> key = InfinityRingKeys.levelKey(uuid);
        ServerLevel level = owner.server.getLevel(key);
        if (level != null) {
            for (ServerPlayer occupant : List.copyOf(level.players())) {
                teleportReturn(occupant);
            }
            if (!level.players().isEmpty()) {
                ServerLevel overworld = owner.server.overworld();
                BlockPos spawn = overworld.getSharedSpawnPos();
                for (ServerPlayer occupant : List.copyOf(level.players())) {
                    occupant.teleportTo(overworld, spawn.getX() + 0.5, spawn.getY(), spawn.getZ() + 0.5, 0.0F, 0.0F);
                }
            }
            if (!level.players().isEmpty()) {
                owner.sendSystemMessage(Component.translatable("message.avaritia.infinity_ring.delete_failed"));
                return false;
            }
        }
        if (!DynamicDimensions.remove(owner.server, key)) {
            owner.sendSystemMessage(Component.translatable("message.avaritia.infinity_ring.delete_failed"));
            return false;
        }
        data.removeOwner(uuid);
        owner.sendSystemMessage(Component.translatable("message.avaritia.infinity_ring.deleted"));
        return true;
    }

    private static ServerLevel getOrCreateLevel(MinecraftServer server, UUID owner, InfinityRingSettings.Terrain terrain) {
        return DynamicDimensions.getOrCreate(server, InfinityRingKeys.levelKey(owner),
                () -> DynamicDimensions.stemFor(server, terrain));
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
        GameProfileCache cache = server.getProfileCache();
        List<String> names = new ArrayList<>();
        if (cache == null) {
            return names;
        }
        settings.players.forEach((id, role) -> cache.get(id).ifPresent(profile ->
                names.add(profile.getName() + "|" + role.name())));
        for (UUID banned : settings.banned) {
            cache.get(banned).ifPresent(profile -> names.add(profile.getName() + "|BANNED"));
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
        if (point == null || InfinityRingKeys.isPersonal(point.dimension())) {
            BlockPos spawn = dest.getSharedSpawnPos();
            player.teleportTo(dest, spawn.getX() + 0.5, spawn.getY(), spawn.getZ() + 0.5, player.getYRot(), player.getXRot());
            return;
        }
        player.teleportTo(dest, point.x(), point.y(), point.z(), point.yRot(), point.xRot());
    }

    private static Optional<UUID> resolvePlayer(MinecraftServer server, String name) {
        GameProfileCache cache = server.getProfileCache();
        if (cache == null) {
            return Optional.empty();
        }
        return cache.get(name).map(profile -> profile.getId());
    }

    private static boolean teleportToPersonalSpawn(ServerPlayer player, ServerLevel level,
                                                    InfinityRingSettings settings, boolean trackReturn) {
        InfinityRingSpawnPoint fallback = defaultSpawn(level, settings.terrain);
        InfinityRingSpawnPoint destination = settings.spawn;
        if (destination == null || !destination.isSafe(level, player, true)) {
            if (fallback.equals(destination) || !fallback.isSafe(level, player, true)) {
                player.sendSystemMessage(Component.translatable("message.avaritia.infinity_ring.spawn_unavailable"));
                return false;
            }
            if (destination != null) {
                player.sendSystemMessage(Component.translatable("message.avaritia.infinity_ring.spawn_fallback"));
            }
            destination = fallback;
        }
        if (trackReturn) {
            rememberReturn(player);
        }
        player.teleportTo(level, destination.x(), destination.y(), destination.z(), destination.yaw(), destination.pitch());
        return true;
    }

    private static InfinityRingSpawnPoint defaultSpawn(ServerLevel level, InfinityRingSettings.Terrain terrain) {
        int feetY = terrain == InfinityRingSettings.Terrain.FLAT
                ? level.getMinBuildHeight() + DynamicDimensions.flatLayerHeight() : 65;
        return new InfinityRingSpawnPoint(0.5, feetY, 0.5, 0.0F, 0.0F);
    }

    private static void buildSpawnPlatform(ServerLevel level, InfinityRingSettings.Terrain terrain,
                                           InfinityRingSpawnPoint spawn) {
        if (terrain == InfinityRingSettings.Terrain.FLAT) {
            return;
        }
        BlockPos origin = BlockPos.containing(spawn.x(), spawn.y(), spawn.z()).below();
        if (terrain == InfinityRingSettings.Terrain.VOID) {
            placePlatform(level, origin, 2);
            return;
        }
        placePlatform(level, origin, 4);
        BlockPos sapling = origin.offset(3, 1, 0);
        BlockState oak = Blocks.OAK_SAPLING.defaultBlockState();
        level.setBlock(sapling, oak, 3);
        if (oak.getBlock() instanceof SaplingBlock saplingBlock) {
            saplingBlock.advanceTree(level, sapling, oak, level.random);
        }
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
