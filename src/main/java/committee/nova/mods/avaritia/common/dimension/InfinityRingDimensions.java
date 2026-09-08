package committee.nova.mods.avaritia.common.dimension;

import committee.nova.mods.avaritia.Const;
import committee.nova.mods.avaritia.common.item.misc.InfinityRingItem;
import committee.nova.mods.avaritia.common.net.S2CInfinityRingOpenPack;
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
        MinecraftServer server = player.level().getServer();
        return InfinityRingKeys.ownerOf(player.level().dimension())
                .filter(owner -> {
                    InfinityRingSettings settings = InfinityRingSavedData.get(server).settings(owner);
                    return settings != null && settings.canManage(player.getUUID(), owner);
                })
                .orElse(player.getUUID());
    }

    private static InfinityRingSettings managedSettings(ServerPlayer actor, UUID target) {
        if (!holdingRing(actor)) {
            return null;
        }
        MinecraftServer server = actor.level().getServer();
        InfinityRingSettings settings = InfinityRingSavedData.get(server).settings(target);
        if (settings == null || !settings.canManage(actor.getUUID(), target)) {
            return null;
        }
        return settings;
    }

    public static void travelOrCreatePrompt(ServerPlayer player) {
        if (!holdingRing(player)) {
            return;
        }
        MinecraftServer server = player.level().getServer();
        InfinityRingSavedData data = InfinityRingSavedData.get(server);
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
        MinecraftServer server = player.level().getServer();
        UUID target = manageTarget(player);
        InfinityRingSettings settings = InfinityRingSavedData.get(server).settings(target);
        if (settings == null) {
            S2CInfinityRingOpenPack.openCreate(player);
            return;
        }
        if (!settings.canManage(player.getUUID(), target)) {
            return;
        }
        S2CInfinityRingOpenPack.openControl(player, settings, friendNames(server, settings), target,
                target.equals(player.getUUID()));
    }

    public static boolean create(ServerPlayer player, InfinityRingSettings.Terrain terrain,
                                 InfinityRingSettings.TimeMode time, InfinityRingSettings.WeatherMode weather,
                                 InfinityRingSettings.Access access) {
        if (!holdingRing(player)) {
            return false;
        }
        MinecraftServer server = player.level().getServer();
        InfinityRingSavedData data = InfinityRingSavedData.get(server);
        if (data.hasDimension(player.getUUID())) {
            player.sendSystemMessage(Component.translatable("message.avaritia.infinity_ring.already_exists"));
            return false;
        }
        InfinityRingSettings settings = new InfinityRingSettings();
        settings.terrain = terrain;
        settings.time = time;
        settings.weather = weather;
        settings.access = access;
        data.create(player.getUUID(), settings);
        ServerLevel level = getOrCreateLevel(server, player.getUUID());
        if (level.getLevelData() instanceof PersonalLevelData personal) {
            personal.apply(settings, server.overworld(), level);
        }
        BlockPos spawn = buildSpawn(level, terrain);
        level.setRespawnData(LevelData.RespawnData.of(level.dimension(), spawn, 0.0F, 0.0F));
        rememberReturn(player);
        teleportToPersonalSpawn(player, level);
        return true;
    }

    public static void travelOwn(ServerPlayer player) {
        MinecraftServer server = player.level().getServer();
        InfinityRingSavedData data = InfinityRingSavedData.get(server);
        if (!data.hasDimension(player.getUUID())) {
            return;
        }
        if (isPersonal(player.level())
                && InfinityRingKeys.ownerOf(player.level().dimension()).filter(player.getUUID()::equals).isPresent()) {
            teleportReturn(player);
            return;
        }
        if (!isPersonal(player.level())) {
            rememberReturn(player);
        }
        ServerLevel level = getOrCreateLevel(server, player.getUUID());
        InfinityRingSettings settings = data.settings(player.getUUID());
        if (level.getLevelData() instanceof PersonalLevelData personal && settings != null) {
            personal.apply(settings, server.overworld(), level);
        }
        teleportToPersonalSpawn(player, level);
    }

    public static boolean visit(ServerPlayer actor, String ownerName) {
        if (!holdingRing(actor)) {
            return false;
        }
        MinecraftServer server = actor.level().getServer();
        Optional<UUID> owner = resolvePlayer(server, ownerName);
        if (owner.isEmpty()) {
            actor.sendSystemMessage(Component.translatable("message.avaritia.infinity_ring.unknown_player", ownerName));
            return false;
        }
        InfinityRingSettings settings = InfinityRingSavedData.get(server).settings(owner.get());
        if (settings == null) {
            actor.sendSystemMessage(Component.translatable("message.avaritia.infinity_ring.missing"));
            return false;
        }
        if (!settings.canEnter(actor.getUUID(), owner.get())) {
            actor.sendSystemMessage(Component.translatable("message.avaritia.infinity_ring.denied"));
            return false;
        }
        if (!isPersonal(actor.level())) {
            rememberReturn(actor);
        }
        ServerLevel level = getOrCreateLevel(server, owner.get());
        teleportToPersonalSpawn(actor, level);
        return true;
    }

    public static boolean addFriend(ServerPlayer actor, UUID target, String name) {
        InfinityRingSettings settings = managedSettings(actor, target);
        if (settings == null) {
            return false;
        }
        Optional<UUID> friend = resolvePlayer(actor.level().getServer(), name);
        if (friend.isEmpty()) {
            actor.sendSystemMessage(Component.translatable("message.avaritia.infinity_ring.unknown_player", name));
            return false;
        }
        if (friend.get().equals(target)) {
            return false;
        }
        settings.banned.remove(friend.get());
        settings.players.putIfAbsent(friend.get(), InfinityRingSettings.Role.VISITOR);
        InfinityRingSavedData.get(actor.level().getServer()).touch();
        return true;
    }

    public static boolean removeFriend(ServerPlayer actor, UUID target, String name) {
        InfinityRingSettings settings = managedSettings(actor, target);
        if (settings == null) {
            return false;
        }
        Optional<UUID> friend = resolvePlayer(actor.level().getServer(), name);
        if (friend.isEmpty()) {
            return false;
        }
        boolean removed = settings.players.remove(friend.get()) != null;
        if (removed) {
            InfinityRingSavedData.get(actor.level().getServer()).touch();
            evictUnauthorized(actor.level().getServer(), target);
        }
        return removed;
    }

    public static boolean setRole(ServerPlayer actor, UUID target, String name, InfinityRingSettings.Role role) {
        InfinityRingSettings settings = managedSettings(actor, target);
        if (settings == null) {
            return false;
        }
        Optional<UUID> player = resolvePlayer(actor.level().getServer(), name);
        if (player.isEmpty() || player.get().equals(target)) {
            return false;
        }
        settings.banned.remove(player.get());
        settings.players.put(player.get(), role);
        InfinityRingSavedData.get(actor.level().getServer()).touch();
        return true;
    }

    public static boolean ban(ServerPlayer actor, UUID target, String name) {
        InfinityRingSettings settings = managedSettings(actor, target);
        if (settings == null) {
            return false;
        }
        Optional<UUID> player = resolvePlayer(actor.level().getServer(), name);
        if (player.isEmpty() || player.get().equals(target)) {
            return false;
        }
        settings.players.remove(player.get());
        settings.banned.add(player.get());
        InfinityRingSavedData.get(actor.level().getServer()).touch();
        evictUnauthorized(actor.level().getServer(), target);
        return true;
    }

    public static boolean unban(ServerPlayer actor, UUID target, String name) {
        InfinityRingSettings settings = managedSettings(actor, target);
        if (settings == null) {
            return false;
        }
        Optional<UUID> player = resolvePlayer(actor.level().getServer(), name);
        if (player.isEmpty()) {
            return false;
        }
        boolean removed = settings.banned.remove(player.get());
        if (removed) {
            InfinityRingSavedData.get(actor.level().getServer()).touch();
        }
        return removed;
    }

    public static void updateModes(ServerPlayer actor, UUID target, InfinityRingSettings.TimeMode time,
                                   InfinityRingSettings.WeatherMode weather, InfinityRingSettings.Access access) {
        InfinityRingSettings settings = managedSettings(actor, target);
        if (settings == null) {
            return;
        }
        MinecraftServer server = actor.level().getServer();
        settings.time = time;
        settings.weather = weather;
        settings.access = access;
        InfinityRingSavedData.get(server).touch();
        ServerLevel level = server.getLevel(InfinityRingKeys.levelKey(target));
        if (level != null && level.getLevelData() instanceof PersonalLevelData personal) {
            personal.apply(settings, server.overworld(), level);
        }
        evictUnauthorized(server, target);
    }

    public static boolean deleteOwn(ServerPlayer owner) {
        if (!holdingRing(owner)) {
            return false;
        }
        UUID uuid = owner.getUUID();
        MinecraftServer server = owner.level().getServer();
        InfinityRingSavedData data = InfinityRingSavedData.get(server);
        if (!data.hasDimension(uuid)) {
            return false;
        }
        ResourceKey<Level> key = InfinityRingKeys.levelKey(uuid);
        ServerLevel level = server.getLevel(key);
        if (level != null) {
            for (ServerPlayer occupant : List.copyOf(level.players())) {
                teleportReturn(occupant);
            }
            if (!level.players().isEmpty()) {
                ServerLevel overworld = server.overworld();
                BlockPos spawn = overworld.getRespawnData().pos();
                for (ServerPlayer occupant : List.copyOf(level.players())) {
                    occupant.teleportTo(overworld, spawn.getX() + 0.5, spawn.getY(), spawn.getZ() + 0.5,
                            Set.of(), 0.0F, 0.0F, false);
                }
            }
            if (!level.players().isEmpty()) {
                owner.sendSystemMessage(Component.translatable("message.avaritia.infinity_ring.delete_failed"));
                return false;
            }
        }
        if (!DynamicDimensions.remove(server, key)) {
            owner.sendSystemMessage(Component.translatable("message.avaritia.infinity_ring.delete_failed"));
            return false;
        }
        data.removeOwner(uuid);
        owner.sendSystemMessage(Component.translatable("message.avaritia.infinity_ring.deleted"));
        return true;
    }

    public static ServerLevel getOrCreateLevel(MinecraftServer server, UUID owner) {
        InfinityRingSettings settings = InfinityRingSavedData.get(server).settings(owner);
        InfinityRingSettings.Terrain terrain = settings == null ? InfinityRingSettings.Terrain.VOID : settings.terrain;
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
                        personal.apply(settings, server.overworld(), level);
                    }
                });
            }
        });
    }

    public static void evictIfUnauthorized(ServerPlayer player) {
        InfinityRingKeys.ownerOf(player.level().dimension()).ifPresent(owner -> {
            InfinityRingSettings settings = InfinityRingSavedData.get(player.level().getServer()).settings(owner);
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
        InfinityRingSavedData.get(player.level().getServer()).rememberReturn(player.getUUID(),
                new InfinityRingSavedData.TravelPoint(
                        player.level().dimension(),
                        player.getX(),
                        player.getY(),
                        player.getZ(),
                        player.getYRot(),
                        player.getXRot()));
    }

    private static void teleportReturn(ServerPlayer player) {
        MinecraftServer server = player.level().getServer();
        InfinityRingSavedData.TravelPoint point = InfinityRingSavedData.get(server).takeReturn(player.getUUID());
        ServerLevel dest = point == null ? null : server.getLevel(point.dimension());
        if (dest == null || InfinityRingKeys.isPersonal(dest.dimension())) {
            dest = server.overworld();
        }
        if (point == null || InfinityRingKeys.isPersonal(point.dimension())) {
            BlockPos spawn = dest.getRespawnData().pos();
            player.teleportTo(dest, spawn.getX() + 0.5, spawn.getY(), spawn.getZ() + 0.5,
                    Set.of(), player.getYRot(), player.getXRot(), false);
            return;
        }
        player.teleportTo(dest, point.x(), point.y(), point.z(),
                Set.of(), point.yRot(), point.xRot(), false);
    }

    private static Optional<UUID> resolvePlayer(MinecraftServer server, String name) {
        if (name == null || name.isBlank()) {
            return Optional.empty();
        }
        String trimmed = name.trim();
        for (ServerPlayer online : server.getPlayerList().getPlayers()) {
            if (online.getGameProfile().name().equalsIgnoreCase(trimmed)) {
                return Optional.of(online.getUUID());
            }
        }
        return server.services().nameToIdCache().get(trimmed).map(NameAndId::id);
    }

    public static double standingFeetY(ServerLevel level, BlockPos spawn) {
        int x = spawn.getX();
        int z = spawn.getZ();
        level.getChunkAt(spawn);
        int min = level.getMinY() + 1;
        int max = level.getMinY() + level.getHeight() - 2;
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

    private static void teleportToPersonalSpawn(ServerPlayer player, ServerLevel level) {
        MinecraftServer server = player.level().getServer();
        BlockPos spawn = level.getRespawnData().pos();
        double y = standingFeetY(level, spawn);
        server.execute(() -> player.teleportTo(level, spawn.getX() + 0.5, y, spawn.getZ() + 0.5,
                Set.of(), player.getYRot(), player.getXRot(), false));
    }

    private static BlockPos buildSpawn(ServerLevel level, InfinityRingSettings.Terrain terrain) {
        if (terrain == InfinityRingSettings.Terrain.FLAT) {
            BlockPos origin = BlockPos.ZERO;
            level.getChunkAt(origin);
            int feet = (int) Math.floor(standingFeetY(level, origin));
            return new BlockPos(0, Math.max(level.getMinY(), feet - 1), 0);
        }
        BlockPos origin = new BlockPos(0, 64, 0);
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
