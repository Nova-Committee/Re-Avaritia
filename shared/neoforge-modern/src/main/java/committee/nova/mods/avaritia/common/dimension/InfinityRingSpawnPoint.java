package committee.nova.mods.avaritia.common.dimension;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CampfireBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;

/** The player's feet position and orientation on entry to a personal dimension. */
public record InfinityRingSpawnPoint(double x, double y, double z, float yaw, float pitch) {
    private static final double SUPPORT_CHECK = 1.0E-5;

    public static final Codec<InfinityRingSpawnPoint> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.DOUBLE.fieldOf("x").forGetter(InfinityRingSpawnPoint::x),
            Codec.DOUBLE.fieldOf("y").forGetter(InfinityRingSpawnPoint::y),
            Codec.DOUBLE.fieldOf("z").forGetter(InfinityRingSpawnPoint::z),
            Codec.FLOAT.fieldOf("yaw").forGetter(InfinityRingSpawnPoint::yaw),
            Codec.FLOAT.fieldOf("pitch").forGetter(InfinityRingSpawnPoint::pitch)
    ).apply(instance, InfinityRingSpawnPoint::new));

    public static final StreamCodec<ByteBuf, InfinityRingSpawnPoint> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.DOUBLE, InfinityRingSpawnPoint::x,
            ByteBufCodecs.DOUBLE, InfinityRingSpawnPoint::y,
            ByteBufCodecs.DOUBLE, InfinityRingSpawnPoint::z,
            ByteBufCodecs.FLOAT, InfinityRingSpawnPoint::yaw,
            ByteBufCodecs.FLOAT, InfinityRingSpawnPoint::pitch,
            InfinityRingSpawnPoint::new);

    public boolean isSafe(ServerLevel level, Player player, boolean loadChunks) {
        if (!Double.isFinite(x) || !Double.isFinite(y) || !Double.isFinite(z)
                || !Float.isFinite(yaw) || !Float.isFinite(pitch)
                || Math.abs(x) >= Level.MAX_LEVEL_SIZE || Math.abs(z) >= Level.MAX_LEVEL_SIZE) {
            return false;
        }
        AABB body = player.getDimensions(Pose.STANDING).makeBoundingBox(x, y, z);
        if (body.minY - SUPPORT_CHECK < level.getMinY() || body.maxY > level.getMaxY() + 1.0
                || !level.getWorldBorder().isWithinBounds(body)) {
            return false;
        }

        // Block collision checks can consult blocks bordering the player's bounds.
        int minX = SectionPos.blockToSectionCoord(Mth.floor(body.minX) - 1);
        int maxX = SectionPos.blockToSectionCoord(Mth.floor(body.maxX) + 1);
        int minZ = SectionPos.blockToSectionCoord(Mth.floor(body.minZ) - 1);
        int maxZ = SectionPos.blockToSectionCoord(Mth.floor(body.maxZ) + 1);
        if (maxX - minX > 1 || maxZ - minZ > 1) {
            return false;
        }
        for (int chunkX = minX; chunkX <= maxX; chunkX++) {
            for (int chunkZ = minZ; chunkZ <= maxZ; chunkZ++) {
                if (loadChunks) {
                    level.getChunk(chunkX, chunkZ);
                } else if (!level.getChunkSource().hasChunk(chunkX, chunkZ)) {
                    return false;
                }
            }
        }

        if (!level.noBlockCollision(player, body)
                || level.noBlockCollision(player, body.move(0, -SUPPORT_CHECK, 0))) {
            return false;
        }
        for (BlockPos pos : BlockPos.betweenClosed(BlockPos.containing(body.minX, body.minY - SUPPORT_CHECK, body.minZ),
                BlockPos.containing(Math.nextDown(body.maxX), Math.nextDown(body.maxY), Math.nextDown(body.maxZ)))) {
            if (!level.getFluidState(pos).isEmpty() || isHazard(level.getBlockState(pos))) {
                return false;
            }
        }
        return true;
    }

    private static boolean isHazard(BlockState state) {
        return state.is(BlockTags.FIRE) || CampfireBlock.isLitCampfire(state)
                || state.is(Blocks.MAGMA_BLOCK) || state.is(Blocks.CACTUS) || state.is(Blocks.POWDER_SNOW)
                || state.is(Blocks.SWEET_BERRY_BUSH) || state.is(Blocks.WITHER_ROSE) || state.is(Blocks.POINTED_DRIPSTONE);
    }
}
