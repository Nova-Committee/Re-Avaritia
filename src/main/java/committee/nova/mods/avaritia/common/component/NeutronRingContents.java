package committee.nova.mods.avaritia.common.component;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.GlobalPos;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.Mth;

import java.util.Optional;
import java.util.UUID;

/** Lightweight Neutron Ring state. Templates live in server SavedData keyed by player UUID. */
public record NeutronRingContents(UUID storageId, Optional<GlobalPos> captureBase, Optional<GlobalPos> placeBase,
                                  Optional<String> selectedId, Size size, boolean playerBound) {
    public static final int MAX_XZ = 16;
    public static final int MAX_Y = 384;

    public record Size(int x, int y, int z) {
        public static final Size DEFAULT = new Size(MAX_XZ, MAX_XZ, MAX_XZ);
        public static final Codec<Size> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                Codec.INT.fieldOf("x").forGetter(Size::x),
                Codec.INT.fieldOf("y").forGetter(Size::y),
                Codec.INT.fieldOf("z").forGetter(Size::z)
        ).apply(instance, Size::new));
        public static final StreamCodec<RegistryFriendlyByteBuf, Size> STREAM_CODEC = StreamCodec.composite(
                ByteBufCodecs.VAR_INT, Size::x,
                ByteBufCodecs.VAR_INT, Size::y,
                ByteBufCodecs.VAR_INT, Size::z,
                Size::new);

        public Size clamp() {
            return new Size(Mth.clamp(x, 1, MAX_XZ), Mth.clamp(y, 1, MAX_Y), Mth.clamp(z, 1, MAX_XZ));
        }
    }

    public static NeutronRingContents owned(UUID owner) {
        return new NeutronRingContents(owner, Optional.empty(), Optional.empty(), Optional.empty(), Size.DEFAULT, true);
    }

    public static NeutronRingContents create() {
        return new NeutronRingContents(UUID.randomUUID(), Optional.empty(), Optional.empty(), Optional.empty(),
                Size.DEFAULT, false);
    }

    public static final Codec<NeutronRingContents> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            UUIDUtil.CODEC.fieldOf("storage").forGetter(NeutronRingContents::storageId),
            GlobalPos.CODEC.optionalFieldOf("capture").forGetter(NeutronRingContents::captureBase),
            GlobalPos.CODEC.optionalFieldOf("place").forGetter(NeutronRingContents::placeBase),
            Codec.STRING.optionalFieldOf("selected").forGetter(NeutronRingContents::selectedId),
            Size.CODEC.optionalFieldOf("size", Size.DEFAULT).forGetter(NeutronRingContents::size),
            Codec.BOOL.optionalFieldOf("playerBound", false).forGetter(NeutronRingContents::playerBound)
    ).apply(instance, NeutronRingContents::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, NeutronRingContents> STREAM_CODEC = StreamCodec.of(
            (buf, data) -> {
                UUIDUtil.STREAM_CODEC.encode(buf, data.storageId());
                ByteBufCodecs.optional(GlobalPos.STREAM_CODEC).encode(buf, data.captureBase());
                ByteBufCodecs.optional(GlobalPos.STREAM_CODEC).encode(buf, data.placeBase());
                ByteBufCodecs.optional(ByteBufCodecs.STRING_UTF8).encode(buf, data.selectedId());
                Size.STREAM_CODEC.encode(buf, data.size());
                buf.writeBoolean(data.playerBound());
            },
            buf -> new NeutronRingContents(
                    UUIDUtil.STREAM_CODEC.decode(buf),
                    ByteBufCodecs.optional(GlobalPos.STREAM_CODEC).decode(buf),
                    ByteBufCodecs.optional(GlobalPos.STREAM_CODEC).decode(buf),
                    ByteBufCodecs.optional(ByteBufCodecs.STRING_UTF8).decode(buf),
                    Size.STREAM_CODEC.decode(buf),
                    buf.readBoolean()));

    public NeutronRingContents withCapture(GlobalPos pos) {
        return new NeutronRingContents(storageId, Optional.of(pos), Optional.empty(), selectedId, size, playerBound);
    }

    public NeutronRingContents withPlace(GlobalPos pos) {
        return new NeutronRingContents(storageId, captureBase, Optional.of(pos), selectedId, size, playerBound);
    }

    public NeutronRingContents clearCapture() {
        return new NeutronRingContents(storageId, Optional.empty(), placeBase, selectedId, size, playerBound);
    }

    public NeutronRingContents withSize(Size next) {
        return new NeutronRingContents(storageId, captureBase, placeBase, selectedId, next.clamp(), playerBound);
    }

    public NeutronRingContents select(String id, Size spaceSize) {
        return new NeutronRingContents(storageId, Optional.empty(), Optional.empty(), Optional.of(id), spaceSize.clamp(),
                playerBound);
    }

    public NeutronRingContents deselect() {
        return new NeutronRingContents(storageId, captureBase, Optional.empty(), Optional.empty(), Size.DEFAULT,
                playerBound);
    }
}
