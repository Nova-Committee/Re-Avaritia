package committee.nova.mods.avaritia.common.dimension;

import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
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
import org.jetbrains.annotations.Nullable;

/** Exact feet position and rotation for entering an Infinity Ring world. */
public record InfinityRingSpawnPoint(double x, double y, double z, float yaw, float pitch) {
    private static final double SUPPORT_CHECK = 1.0E-5;

    CompoundTag save() {
        CompoundTag tag = new CompoundTag();
        tag.putDouble("X", x);
        tag.putDouble("Y", y);
        tag.putDouble("Z", z);
        tag.putFloat("Yaw", yaw);
        tag.putFloat("Pitch", pitch);
        return tag;
    }

    @Nullable
    static InfinityRingSpawnPoint load(CompoundTag tag) {
        if (!tag.contains("X", Tag.TAG_DOUBLE) || !tag.contains("Y", Tag.TAG_DOUBLE)
                || !tag.contains("Z", Tag.TAG_DOUBLE) || !tag.contains("Yaw", Tag.TAG_FLOAT)
                || !tag.contains("Pitch", Tag.TAG_FLOAT)) {
            return null;
        }
        return new InfinityRingSpawnPoint(tag.getDouble("X"), tag.getDouble("Y"), tag.getDouble("Z"),
                tag.getFloat("Yaw"), tag.getFloat("Pitch"));
    }

    public boolean isSafe(ServerLevel level, Player player, boolean loadChunks) {
        if (!Double.isFinite(x) || !Double.isFinite(y) || !Double.isFinite(z)
                || !Float.isFinite(yaw) || !Float.isFinite(pitch)
                || Math.abs(x) >= Level.MAX_LEVEL_SIZE || Math.abs(z) >= Level.MAX_LEVEL_SIZE) {
            return false;
        }
        AABB body = player.getDimensions(Pose.STANDING).makeBoundingBox(x, y, z);
        var border = level.getWorldBorder();
        if (body.minY - SUPPORT_CHECK < level.getMinBuildHeight() || body.maxY > level.getMaxBuildHeight()
                || body.minX < border.getMinX() || body.maxX > border.getMaxX()
                || body.minZ < border.getMinZ() || body.maxZ > border.getMaxZ()) {
            return false;
        }

        // Include the adjacent blocks consulted by the block-collision iterator.
        int minChunkX = SectionPos.blockToSectionCoord(Mth.floor(body.minX) - 1);
        int maxChunkX = SectionPos.blockToSectionCoord(Mth.floor(body.maxX) + 1);
        int minChunkZ = SectionPos.blockToSectionCoord(Mth.floor(body.minZ) - 1);
        int maxChunkZ = SectionPos.blockToSectionCoord(Mth.floor(body.maxZ) + 1);
        if (maxChunkX - minChunkX > 1 || maxChunkZ - minChunkZ > 1) {
            return false;
        }
        for (int chunkX = minChunkX; chunkX <= maxChunkX; chunkX++) {
            for (int chunkZ = minChunkZ; chunkZ <= maxChunkZ; chunkZ++) {
                if (loadChunks) {
                    level.getChunk(chunkX, chunkZ);
                } else if (!level.getChunkSource().hasChunk(chunkX, chunkZ)) {
                    return false;
                }
            }
        }
        if (level.getBlockCollisions(player, body).iterator().hasNext()
                || !level.getBlockCollisions(player, body.move(0, -SUPPORT_CHECK, 0)).iterator().hasNext()) {
            return false;
        }
        for (BlockPos pos : BlockPos.betweenClosed(BlockPos.containing(body.minX, body.minY - SUPPORT_CHECK, body.minZ),
                BlockPos.containing(Math.nextDown(body.maxX), Math.nextDown(body.maxY), Math.nextDown(body.maxZ)))) {
            BlockState state = level.getBlockState(pos);
            if (!state.getFluidState().isEmpty() || isHazard(state)) {
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
