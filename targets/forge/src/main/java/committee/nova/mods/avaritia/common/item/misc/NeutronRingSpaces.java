package committee.nova.mods.avaritia.common.item.misc;

import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.core.Vec3i;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraft.world.phys.AABB;

/** Capture/place volume from a floor-center base. X/Z cap at one chunk, Y clipped to world height. */
public final class NeutronRingSpaces {
    private NeutronRingSpaces() {
    }

    public static BlockPos minCorner(BlockPos base, NeutronRingContents.Size size) {
        NeutronRingContents.Size clamped = size.clamp();
        return new BlockPos(base.getX() - clamped.x() / 2, base.getY(), base.getZ() - clamped.z() / 2);
    }

    public static Vec3i size(Level level, BlockPos base, NeutronRingContents.Size size) {
        NeutronRingContents.Size clamped = size.clamp();
        BlockPos min = minCorner(base, clamped);
        int maxY = Math.min(level.getMaxBuildHeight() - 1, min.getY() + clamped.y() - 1);
        int sizeY = Math.max(1, maxY - min.getY() + 1);
        return new Vec3i(clamped.x(), sizeY, clamped.z());
    }

    public static AABB previewBox(Level level, BlockPos base, NeutronRingContents.Size size) {
        BlockPos min = minCorner(base, size);
        Vec3i extent = size(level, base, size);
        return new AABB(min.getX(), min.getY(), min.getZ(),
                min.getX() + extent.getX(), min.getY() + extent.getY(), min.getZ() + extent.getZ());
    }

    public static CompoundTag capture(ServerLevel level, BlockPos base, NeutronRingContents.Size size) {
        StructureTemplate template = new StructureTemplate();
        template.fillFromWorld(level, minCorner(base, size), size(level, base, size), false, null);
        return template.save(new CompoundTag());
    }

    public static void clear(ServerLevel level, BlockPos base, NeutronRingContents.Size size) {
        BlockPos min = minCorner(base, size);
        Vec3i extent = size(level, base, size);
        BlockPos max = min.offset(extent.getX() - 1, extent.getY() - 1, extent.getZ() - 1);
        int flags = Block.UPDATE_CLIENTS | Block.UPDATE_KNOWN_SHAPE | Block.UPDATE_SUPPRESS_DROPS;
        for (BlockPos pos : BlockPos.betweenClosed(min, max)) {
            level.removeBlockEntity(pos);
            level.setBlock(pos, Blocks.AIR.defaultBlockState(), flags);
        }
        BlockPos.betweenClosedStream(min.offset(-1, -1, -1), max.offset(1, 1, 1)).forEach(pos -> {
            BlockState state = level.getBlockState(pos);
            BlockState updated = Block.updateFromNeighbourShapes(state, level, pos);
            if (state != updated) {
                level.setBlock(pos, updated, Block.UPDATE_CLIENTS);
            }
        });
    }

    public static boolean place(ServerLevel level, BlockPos base, CompoundTag tag) {
        NeutronRingContents.Size size = sizeOf(tag);
        BlockPos min = minCorner(base, size);
        BlockPos max = min.offset(size.x() - 1, size.y() - 1, size.z() - 1);
        if (!level.isInWorldBounds(min) || !level.isInWorldBounds(max)) {
            return false;
        }
        StructureTemplate template = new StructureTemplate();
        template.load(level.registryAccess().lookupOrThrow(Registries.BLOCK), tag);
        StructurePlaceSettings settings = new StructurePlaceSettings().setIgnoreEntities(true);
        return template.placeInWorld(level, min, min, settings, RandomSource.create(), 3);
    }

    public static NeutronRingContents.Size sizeOf(CompoundTag tag) {
        var list = tag.getList("size", 3);
        if (list.size() < 3) {
            return NeutronRingContents.Size.DEFAULT;
        }
        return new NeutronRingContents.Size(list.getInt(0), list.getInt(1), list.getInt(2)).clamp();
    }

    public static boolean sameDimension(Level level, GlobalPos pos) {
        return pos.dimension().equals(level.dimension());
    }
}
