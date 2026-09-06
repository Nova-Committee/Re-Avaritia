package committee.nova.mods.avaritia.common.item.misc;

import committee.nova.mods.avaritia.common.component.NeutronRingContents;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.MapColor;

/** Compact top-down map plus per-column height for GUI 3D preview. 0 height = empty. */
public record NeutronSpacePreview(int sizeX, int sizeY, int sizeZ, int blocks, int[] top, int[] height) {
    public static final NeutronSpacePreview EMPTY = new NeutronSpacePreview(1, 1, 1, 0, new int[]{0}, new int[]{0});

    public static final StreamCodec<RegistryFriendlyByteBuf, int[]> COLORS = StreamCodec.of(
            (buf, arr) -> {
                buf.writeVarInt(arr.length);
                for (int value : arr) {
                    buf.writeInt(value);
                }
            },
            buf -> {
                int count = Math.min(256, buf.readVarInt());
                int[] values = new int[count];
                for (int i = 0; i < count; i++) {
                    values[i] = buf.readInt();
                }
                return values;
            });

    public static final StreamCodec<RegistryFriendlyByteBuf, int[]> HEIGHTS = StreamCodec.of(
            (buf, arr) -> {
                buf.writeVarInt(arr.length);
                for (int value : arr) {
                    buf.writeVarInt(value);
                }
            },
            buf -> {
                int count = Math.min(256, buf.readVarInt());
                int[] values = new int[count];
                for (int i = 0; i < count; i++) {
                    values[i] = buf.readVarInt();
                }
                return values;
            });

    public static final StreamCodec<RegistryFriendlyByteBuf, NeutronSpacePreview> STREAM_CODEC = StreamCodec.of(
            (buf, data) -> {
                ByteBufCodecs.VAR_INT.encode(buf, data.sizeX());
                ByteBufCodecs.VAR_INT.encode(buf, data.sizeY());
                ByteBufCodecs.VAR_INT.encode(buf, data.sizeZ());
                ByteBufCodecs.VAR_INT.encode(buf, data.blocks());
                COLORS.encode(buf, data.top());
                HEIGHTS.encode(buf, data.height());
            },
            buf -> new NeutronSpacePreview(
                    ByteBufCodecs.VAR_INT.decode(buf),
                    ByteBufCodecs.VAR_INT.decode(buf),
                    ByteBufCodecs.VAR_INT.decode(buf),
                    ByteBufCodecs.VAR_INT.decode(buf),
                    COLORS.decode(buf),
                    HEIGHTS.decode(buf)));
    public static NeutronSpacePreview capture(ServerLevel level, BlockPos base, NeutronRingContents.Size size) {
        Vec3i extent = NeutronRingSpaces.size(level, base, size);
        BlockPos min = NeutronRingSpaces.minCorner(base, size);
        int cells = extent.getX() * extent.getZ();
        int[] top = new int[cells];
        int[] height = new int[cells];
        int[] worldY = new int[cells];
        java.util.Arrays.fill(worldY, Integer.MIN_VALUE);
        int blocks = 0;
        BlockPos max = min.offset(extent.getX() - 1, extent.getY() - 1, extent.getZ() - 1);
        for (BlockPos pos : BlockPos.betweenClosed(min, max)) {
            BlockState state = level.getBlockState(pos);
            if (state.isAir()) {
                continue;
            }
            blocks++;
            int index = (pos.getX() - min.getX()) + (pos.getZ() - min.getZ()) * extent.getX();
            if (pos.getY() >= worldY[index]) {
                worldY[index] = pos.getY();
                height[index] = pos.getY() - min.getY() + 1;
                MapColor color = state.getMapColor(level, pos);
                top[index] = color.col == 0 ? 0xFF2B2B2B : 0xFF000000 | color.col;
            }
        }
        return new NeutronSpacePreview(extent.getX(), extent.getY(), extent.getZ(), blocks, top, height);
    }

    public int columnHeight(int index) {
        if (index < 0 || index >= top.length || top[index] == 0) {
            return 0;
        }
        if (index >= height.length || height[index] <= 0) {
            return 1;
        }
        return height[index];
    }

    public CompoundTag save() {
        CompoundTag tag = new CompoundTag();
        tag.putInt("X", sizeX);
        tag.putInt("Y", sizeY);
        tag.putInt("Z", sizeZ);
        tag.putInt("Blocks", blocks);
        tag.putIntArray("Top", top);
        tag.putIntArray("Height", height);
        return tag;
    }

    public static NeutronSpacePreview load(CompoundTag tag) {
        if (tag.isEmpty()) {
            return EMPTY;
        }
        int[] top = tag.getIntArray("Top");
        int[] height = tag.contains("Height") ? tag.getIntArray("Height") : legacyHeights(top);
        return new NeutronSpacePreview(tag.getInt("X"), tag.getInt("Y"), tag.getInt("Z"),
                tag.getInt("Blocks"), top, height);
    }

    private static int[] legacyHeights(int[] top) {
        int[] height = new int[top.length];
        for (int i = 0; i < top.length; i++) {
            height[i] = top[i] == 0 ? 0 : 1;
        }
        return height;
    }
}
