package committee.nova.mods.avaritia.common.item.misc;

import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import java.util.Optional;
import java.util.UUID;

/** Lightweight Neutron Ring state. Templates live in server SavedData keyed by player UUID. */
public record NeutronRingContents(UUID storageId, Optional<GlobalPos> captureBase, Optional<GlobalPos> placeBase,
                                  Optional<String> selectedId, Size size, boolean playerBound) {
    public static final int MAX_XZ = 16;
    public static final int MAX_Y = 384;
    public static final String TAG = "NeutronRing";

    public record Size(int x, int y, int z) {
        public static final Size DEFAULT = new Size(MAX_XZ, MAX_XZ, MAX_XZ);

        public Size clamp() {
            return new Size(Mth.clamp(x, 1, MAX_XZ), Mth.clamp(y, 1, MAX_Y), Mth.clamp(z, 1, MAX_XZ));
        }

        public void write(FriendlyByteBuf buf) {
            buf.writeVarInt(x);
            buf.writeVarInt(y);
            buf.writeVarInt(z);
        }

        public static Size read(FriendlyByteBuf buf) {
            return new Size(buf.readVarInt(), buf.readVarInt(), buf.readVarInt()).clamp();
        }

        public CompoundTag save() {
            CompoundTag tag = new CompoundTag();
            tag.putInt("x", x);
            tag.putInt("y", y);
            tag.putInt("z", z);
            return tag;
        }

        public static Size load(CompoundTag tag) {
            return new Size(tag.getInt("x"), tag.getInt("y"), tag.getInt("z")).clamp();
        }
    }

    public static NeutronRingContents owned(UUID owner) {
        return new NeutronRingContents(owner, Optional.empty(), Optional.empty(), Optional.empty(), Size.DEFAULT, true);
    }

    public static NeutronRingContents create() {
        return new NeutronRingContents(UUID.randomUUID(), Optional.empty(), Optional.empty(), Optional.empty(),
                Size.DEFAULT, false);
    }

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

    public void save(ItemStack stack) {
        stack.getOrCreateTag().put(TAG, save());
    }

    public CompoundTag save() {
        CompoundTag tag = new CompoundTag();
        tag.putUUID("storage", storageId);
        captureBase.ifPresent(pos -> tag.put("capture", writePos(pos)));
        placeBase.ifPresent(pos -> tag.put("place", writePos(pos)));
        selectedId.ifPresent(id -> tag.putString("selected", id));
        tag.put("size", size.save());
        tag.putBoolean("playerBound", playerBound);
        return tag;
    }

    public static NeutronRingContents load(CompoundTag tag) {
        UUID storage = tag.hasUUID("storage") ? tag.getUUID("storage") : UUID.randomUUID();
        Optional<GlobalPos> capture = readPos(tag, "capture");
        Optional<GlobalPos> place = readPos(tag, "place");
        Optional<String> selected = tag.contains("selected", Tag.TAG_STRING)
                ? Optional.of(tag.getString("selected")) : Optional.empty();
        Size size = tag.contains("size", Tag.TAG_COMPOUND) ? Size.load(tag.getCompound("size")) : Size.DEFAULT;
        boolean bound = tag.getBoolean("playerBound");
        return new NeutronRingContents(storage, capture, place, selected, size.clamp(), bound);
    }

    public static NeutronRingContents of(ItemStack stack) {
        CompoundTag tag = stack.getTagElement(TAG);
        if (tag != null) {
            return load(tag);
        }
        NeutronRingContents data = create();
        data.save(stack);
        return data;
    }

    public void write(FriendlyByteBuf buf) {
        buf.writeUUID(storageId);
        writeOptionalPos(buf, captureBase);
        writeOptionalPos(buf, placeBase);
        buf.writeBoolean(selectedId.isPresent());
        selectedId.ifPresent(buf::writeUtf);
        size.write(buf);
        buf.writeBoolean(playerBound);
    }

    public static NeutronRingContents read(FriendlyByteBuf buf) {
        UUID storage = buf.readUUID();
        Optional<GlobalPos> capture = readOptionalPos(buf);
        Optional<GlobalPos> place = readOptionalPos(buf);
        Optional<String> selected = buf.readBoolean() ? Optional.of(buf.readUtf()) : Optional.empty();
        Size size = Size.read(buf);
        boolean bound = buf.readBoolean();
        return new NeutronRingContents(storage, capture, place, selected, size, bound);
    }

    private static CompoundTag writePos(GlobalPos pos) {
        CompoundTag tag = new CompoundTag();
        tag.putString("dimension", pos.dimension().location().toString());
        tag.putInt("x", pos.pos().getX());
        tag.putInt("y", pos.pos().getY());
        tag.putInt("z", pos.pos().getZ());
        return tag;
    }

    private static Optional<GlobalPos> readPos(CompoundTag parent, String key) {
        if (!parent.contains(key, Tag.TAG_COMPOUND)) {
            return Optional.empty();
        }
        CompoundTag tag = parent.getCompound(key);
        ResourceLocation dimension = ResourceLocation.tryParse(tag.getString("dimension"));
        if (dimension == null) {
            return Optional.empty();
        }
        return Optional.of(GlobalPos.of(ResourceKey.create(Registries.DIMENSION, dimension),
                new BlockPos(tag.getInt("x"), tag.getInt("y"), tag.getInt("z"))));
    }

    private static void writeOptionalPos(FriendlyByteBuf buf, Optional<GlobalPos> pos) {
        buf.writeBoolean(pos.isPresent());
        pos.ifPresent(value -> {
            buf.writeResourceLocation(value.dimension().location());
            buf.writeBlockPos(value.pos());
        });
    }

    private static Optional<GlobalPos> readOptionalPos(FriendlyByteBuf buf) {
        if (!buf.readBoolean()) {
            return Optional.empty();
        }
        ResourceKey<Level> dimension = ResourceKey.create(Registries.DIMENSION, buf.readResourceLocation());
        return Optional.of(GlobalPos.of(dimension, buf.readBlockPos()));
    }
}
