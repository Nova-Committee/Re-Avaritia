package committee.nova.mods.avaritia.common.item.misc;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtIo;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Persistent Infinity Bucket storage. Fluids use native {@link FluidStack} NBT,
 * with a one-way reader for legacy {@code Id}/{@code Amount} entries. Creature
 * payloads keep {@code Id}/{@code Data} and fold legacy {@code Bucketable} into
 * {@code Data.BucketableData}. Unrelated root item NBT is merged, never replaced.
 */
public final class InfinityBucketContents {
    public static final String FLUIDS_NBT = "Fluids";
    public static final String CREATURES_NBT = "Creatures";
    public static final String CONTROL_NBT = "InfinityBucketControl";
    public static final String FORCE_PLACEMENT_NBT = "ForcePlacement";
    public static final String OUTPUT_KIND_NBT = "OutputKind";
    public static final String CREATURE_ID_KEY = "Id";
    public static final String CREATURE_DATA_KEY = "Data";
    public static final String CREATURE_BUCKETABLE_KEY = "Bucketable";
    public static final String BUCKETABLE_DATA_KEY = "BucketableData";
    public static final String LEGACY_FLUID_ID_KEY = "Id";
    public static final String LEGACY_FLUID_AMOUNT_KEY = "Amount";
    public static final String CONTROL_CREATURE_KEY = "creature";
    public static final String CONTROL_INDEX_KEY = "index";
    public static final byte OUTPUT_FLUID = 0;
    public static final byte OUTPUT_CREATURE = 1;
    public static final int BUCKET_VOLUME = 1000;
    public static final int MAX_STORAGE_BYTES = 256 * 1024;
    public static final int MAX_FLUIDS = 256;
    public static final int MAX_CREATURES = 64;
    public static final int MAX_CREATURE_BYTES = 32 * 1024;
    public static final int MAX_FLUID_ENTRIES = MAX_FLUIDS;
    public static final int MAX_CREATURE_ENTRIES = MAX_CREATURES;
    public static final int MAX_TOTAL_BYTES = MAX_STORAGE_BYTES;

    private InfinityBucketContents() {
    }

    public static boolean isBucket(ItemStack stack) {
        return !stack.isEmpty() && stack.getItem() instanceof InfinityBucketItem && stack.getCount() == 1;
    }

    @NotNull
    public static List<FluidStack> getFluids(ItemStack stack) {
        CompoundTag nbt = stack.getTag();
        if (nbt == null || !nbt.contains(FLUIDS_NBT, Tag.TAG_LIST)) {
            return new ArrayList<>();
        }
        List<FluidStack> fluids = new ArrayList<>();
        ListTag list = nbt.getList(FLUIDS_NBT, Tag.TAG_COMPOUND);
        for (int i = 0; i < list.size(); i++) {
            CompoundTag entry = list.getCompound(i);
            if (encodedSize(entry) == Integer.MAX_VALUE) {
                continue;
            }
            FluidStack fluid = loadFluidStack(entry);
            if (!fluid.isEmpty()) {
                fluids.add(fluid);
            }
        }
        return fluids;
    }

    public static boolean setFluids(ItemStack stack, List<FluidStack> fluids) {
        Control control = getControl(stack);
        List<FluidStack> previous = getFluids(stack);
        CompoundTag proposed = new CompoundTag();
        ListTag fluidList = fluidList(fluids);
        if (!fluidList.isEmpty()) {
            proposed.put(FLUIDS_NBT, fluidList);
        }
        copyRaw(stack, CREATURES_NBT, proposed);
        if (!allowed(stack, proposed, fluidList.size(), rawCount(stack, CREATURES_NBT))) {
            return false;
        }
        writeList(stack, FLUIDS_NBT, fluidList);
        List<FluidStack> stored = getFluids(stack);
        if (!control.creatureSelected() && control.selectedIndex() >= 0 && control.selectedIndex() < previous.size()) {
            FluidStack selected = previous.get(control.selectedIndex());
            if (control.selectedIndex() >= stored.size() || !selected.isFluidEqual(stored.get(control.selectedIndex()))) {
                for (int i = 0; i < stored.size(); i++) {
                    if (selected.isFluidEqual(stored.get(i))) {
                        control = control.selectFluid(i);
                        break;
                    }
                }
            }
        }
        setControl(stack, control);
        return true;
    }

    @NotNull
    public static FluidStack loadFluidStack(CompoundTag nbt) {
        if (nbt == null || nbt.isEmpty()) {
            return FluidStack.EMPTY;
        }
        if (nbt.contains("FluidName", Tag.TAG_STRING)) {
            return FluidStack.loadFluidStackFromNBT(nbt);
        }
        if (!nbt.contains(LEGACY_FLUID_ID_KEY, Tag.TAG_STRING)) {
            return FluidStack.EMPTY;
        }
        ResourceLocation fluidName = new ResourceLocation(nbt.getString(LEGACY_FLUID_ID_KEY));
        var fluid = ForgeRegistries.FLUIDS.getValue(fluidName);
        if (fluid == null) {
            return FluidStack.EMPTY;
        }
        int amount = nbt.getInt(LEGACY_FLUID_AMOUNT_KEY);
        if (amount <= 0) {
            return FluidStack.EMPTY;
        }
        return new FluidStack(fluid, amount);
    }

    @NotNull
    public static List<CreatureRecord> getCreatures(ItemStack stack) {
        CompoundTag nbt = stack.getTag();
        if (nbt == null || !nbt.contains(CREATURES_NBT, Tag.TAG_LIST)) {
            return new ArrayList<>();
        }
        List<CreatureRecord> creatures = new ArrayList<>();
        ListTag list = nbt.getList(CREATURES_NBT, Tag.TAG_COMPOUND);
        for (int i = 0; i < list.size(); i++) {
            CompoundTag entry = list.getCompound(i);
            if (encodedSize(entry) == Integer.MAX_VALUE) {
                continue;
            }
            CreatureRecord record = CreatureRecord.load(entry);
            if (record != null) {
                creatures.add(record);
            }
        }
        return creatures;
    }

    public static boolean setCreatures(ItemStack stack, List<CreatureRecord> creatures) {
        Control control = getControl(stack);
        List<CreatureRecord> previous = getCreatures(stack);
        CompoundTag proposed = new CompoundTag();
        copyRaw(stack, FLUIDS_NBT, proposed);
        ListTag creatureList = creatureList(creatures);
        if (!creatureList.isEmpty()) {
            proposed.put(CREATURES_NBT, creatureList);
        }
        if (!allowed(stack, proposed, rawCount(stack, FLUIDS_NBT), creatureList.size())) {
            return false;
        }
        if (creatureList.size() > rawCount(stack, CREATURES_NBT)) {
            for (CreatureRecord creature : creatures) {
                if (encodedSize(creature.save()) > MAX_CREATURE_BYTES && !containsCreature(previous, creature)) {
                    return false;
                }
            }
        }
        writeList(stack, CREATURES_NBT, creatureList);
        List<CreatureRecord> stored = getCreatures(stack);
        if (control.creatureSelected() && control.selectedIndex() >= 0 && control.selectedIndex() < previous.size()) {
            CreatureRecord selected = previous.get(control.selectedIndex());
            if (control.selectedIndex() >= stored.size() || !selected.equals(stored.get(control.selectedIndex()))) {
                int newIndex = stored.indexOf(selected);
                if (newIndex >= 0) {
                    control = control.selectCreature(newIndex);
                }
            }
        }
        setControl(stack, control);
        return true;
    }

    public static boolean canAcceptFluids(ItemStack stack, List<FluidStack> fluids) {
        CompoundTag proposed = new CompoundTag();
        ListTag fluidList = fluidList(fluids);
        if (!fluidList.isEmpty()) {
            proposed.put(FLUIDS_NBT, fluidList);
        }
        copyRaw(stack, CREATURES_NBT, proposed);
        return allowed(stack, proposed, fluidList.size(), rawCount(stack, CREATURES_NBT));
    }

    public static boolean canAcceptCreatures(ItemStack stack, List<CreatureRecord> creatures) {
        CompoundTag proposed = new CompoundTag();
        copyRaw(stack, FLUIDS_NBT, proposed);
        ListTag creatureList = creatureList(creatures);
        if (!creatureList.isEmpty()) {
            proposed.put(CREATURES_NBT, creatureList);
        }
        if (!allowed(stack, proposed, rawCount(stack, FLUIDS_NBT), creatureList.size())) {
            return false;
        }
        if (creatureList.size() <= rawCount(stack, CREATURES_NBT)) {
            return true;
        }
        List<CreatureRecord> existing = getCreatures(stack);
        for (CreatureRecord creature : creatures) {
            if (encodedSize(creature.save()) > MAX_CREATURE_BYTES && !containsCreature(existing, creature)) {
                return false;
            }
        }
        return true;
    }

    public static boolean canStore(List<FluidStack> fluids, List<CreatureRecord> creatures) {
        if (fluids.size() > MAX_FLUID_ENTRIES || creatures.size() > MAX_CREATURE_ENTRIES) {
            return false;
        }
        int total = 0;
        for (FluidStack fluid : fluids) {
            total += encodedFluidSize(fluid);
            if (total > MAX_TOTAL_BYTES) {
                return false;
            }
        }
        for (CreatureRecord creature : creatures) {
            int creatureSize = encodedCreatureSize(creature);
            if (creatureSize > MAX_CREATURE_BYTES) {
                return false;
            }
            total += creatureSize;
            if (total > MAX_TOTAL_BYTES) {
                return false;
            }
        }
        return true;
    }

    public static int encodedFluidSize(FluidStack fluid) {
        if (fluid == null || fluid.isEmpty()) {
            return 0;
        }
        return encodedSize(fluid.writeToNBT(new CompoundTag()));
    }

    public static int encodedCreatureSize(@Nullable CreatureRecord creature) {
        return creature == null ? 0 : encodedSize(creature.save());
    }

    public static Control getControl(ItemStack stack) {
        CompoundTag tag = stack.getTag();
        if (tag != null && tag.contains(CONTROL_NBT, Tag.TAG_COMPOUND)) {
            CompoundTag control = tag.getCompound(CONTROL_NBT);
            return new Control(control.getBoolean(CONTROL_CREATURE_KEY), control.getInt(CONTROL_INDEX_KEY))
                    .clamp(getFluids(stack).size(), getCreatures(stack).size());
        }
        if (tag != null && tag.contains(OUTPUT_KIND_NBT, Tag.TAG_BYTE)
                && tag.getByte(OUTPUT_KIND_NBT) == OUTPUT_CREATURE) {
            return new Control(true, 0).clamp(getFluids(stack).size(), getCreatures(stack).size());
        }
        return Control.DEFAULT.clamp(getFluids(stack).size(), getCreatures(stack).size());
    }

    public static void setControl(ItemStack stack, Control control) {
        Control clamped = control.clamp(getFluids(stack).size(), getCreatures(stack).size());
        CompoundTag tag = stack.getOrCreateTag();
        CompoundTag stored = new CompoundTag();
        stored.putBoolean(CONTROL_CREATURE_KEY, clamped.creatureSelected());
        stored.putInt(CONTROL_INDEX_KEY, clamped.selectedIndex());
        tag.put(CONTROL_NBT, stored);
        tag.remove(FORCE_PLACEMENT_NBT);
        tag.remove(OUTPUT_KIND_NBT);
        stripEmptyTag(stack, tag);
    }

    public static FluidStack getSelectedFluid(ItemStack stack) {
        Control control = getControl(stack);
        if (control.creatureSelected()) {
            return FluidStack.EMPTY;
        }
        List<FluidStack> fluids = getFluids(stack);
        if (fluids.isEmpty()) {
            return FluidStack.EMPTY;
        }
        int index = Math.min(control.selectedIndex(), fluids.size() - 1);
        return fluids.get(Math.max(0, index));
    }

    @Nullable
    public static CreatureRecord getSelectedCreature(ItemStack stack) {
        Control control = getControl(stack);
        if (!control.creatureSelected()) {
            return null;
        }
        List<CreatureRecord> creatures = getCreatures(stack);
        if (creatures.isEmpty()) {
            return null;
        }
        int index = Math.min(control.selectedIndex(), creatures.size() - 1);
        return creatures.get(Math.max(0, index));
    }

    public static boolean hasContents(ItemStack stack) {
        return !getFluids(stack).isEmpty() || !getCreatures(stack).isEmpty();
    }

    public static boolean clearContents(ItemStack stack) {
        if (!setFluids(stack, List.of()) || !setCreatures(stack, List.of())) {
            return false;
        }
        setControl(stack, Control.DEFAULT);
        return true;
    }

    public static void copyStoredState(ItemStack from, ItemStack to) {
        copyOrRemove(from, to, FLUIDS_NBT);
        copyOrRemove(from, to, CREATURES_NBT);
        copyOrRemove(from, to, CONTROL_NBT);
        CompoundTag tag = to.getTag();
        if (tag != null) {
            tag.remove(FORCE_PLACEMENT_NBT);
            tag.remove(OUTPUT_KIND_NBT);
            stripEmptyTag(to, tag);
        }
    }

    public static int encodedSize(@Nullable Tag tag) {
        if (tag == null) {
            return 0;
        }
        try {
            ByteArrayOutputStream bytes = new ByteArrayOutputStream();
            DataOutputStream output = new DataOutputStream(bytes);
            if (tag instanceof CompoundTag compound) {
                NbtIo.write(compound, output);
            } else {
                CompoundTag wrap = new CompoundTag();
                wrap.put("v", tag.copy());
                NbtIo.write(wrap, output);
            }
            return bytes.size();
        } catch (IOException e) {
            return Integer.MAX_VALUE;
        }
    }

    private static boolean allowed(ItemStack stack, CompoundTag proposed, int fluidCount, int creatureCount) {
        int proposedBytes = encodedSize(proposed);
        int currentBytes = encodedSize(storageSnapshot(stack));
        boolean shrinking = proposedBytes <= currentBytes
                && fluidCount <= rawCount(stack, FLUIDS_NBT)
                && creatureCount <= rawCount(stack, CREATURES_NBT);
        if (shrinking) {
            return proposedBytes != Integer.MAX_VALUE;
        }
        return fluidCount <= MAX_FLUIDS
                && creatureCount <= MAX_CREATURES
                && proposedBytes <= MAX_STORAGE_BYTES;
    }

    private static void copyRaw(ItemStack stack, String key, CompoundTag proposed) {
        CompoundTag tag = stack.getTag();
        if (tag != null && tag.contains(key)) {
            proposed.put(key, tag.get(key).copy());
        }
    }

    private static void copyOrRemove(ItemStack from, ItemStack to, String key) {
        CompoundTag source = from.getTag();
        if (source != null && source.contains(key)) {
            to.getOrCreateTag().put(key, source.get(key).copy());
            return;
        }
        CompoundTag dest = to.getTag();
        if (dest != null) {
            dest.remove(key);
            stripEmptyTag(to, dest);
        }
    }

    private static boolean writeList(ItemStack stack, String key, ListTag list) {
        CompoundTag tag = stack.getOrCreateTag();
        if (list.isEmpty()) {
            tag.remove(key);
            stripEmptyTag(stack, tag);
        } else {
            tag.put(key, list);
        }
        return true;
    }

    private static ListTag fluidList(List<FluidStack> fluids) {
        ListTag list = new ListTag();
        if (fluids == null) {
            return list;
        }
        for (FluidStack fluid : fluids) {
            if (fluid != null && !fluid.isEmpty()) {
                list.add(fluid.writeToNBT(new CompoundTag()));
            }
        }
        return list;
    }

    private static ListTag creatureList(List<CreatureRecord> creatures) {
        ListTag list = new ListTag();
        if (creatures == null) {
            return list;
        }
        for (CreatureRecord creature : creatures) {
            if (creature != null) {
                list.add(creature.save());
            }
        }
        return list;
    }

    private static CompoundTag storageSnapshot(ItemStack stack) {
        CompoundTag storage = new CompoundTag();
        CompoundTag tag = stack.getTag();
        if (tag == null) {
            return storage;
        }
        if (tag.contains(FLUIDS_NBT)) {
            storage.put(FLUIDS_NBT, tag.get(FLUIDS_NBT).copy());
        }
        if (tag.contains(CREATURES_NBT)) {
            storage.put(CREATURES_NBT, tag.get(CREATURES_NBT).copy());
        }
        return storage;
    }

    private static int rawCount(ItemStack stack, String key) {
        CompoundTag tag = stack.getTag();
        if (tag == null || !tag.contains(key, Tag.TAG_LIST)) {
            return 0;
        }
        return tag.getList(key, Tag.TAG_COMPOUND).size();
    }

    private static boolean containsCreature(List<CreatureRecord> existing, CreatureRecord candidate) {
        for (CreatureRecord record : existing) {
            if (record.equals(candidate)) {
                return true;
            }
        }
        return false;
    }

    private static void stripEmptyTag(ItemStack stack, CompoundTag tag) {
        if (tag.isEmpty()) {
            stack.setTag(null);
        }
    }

    public record Control(boolean creatureSelected, int selectedIndex) {
        public static final Control DEFAULT = new Control(false, 0);

        public Control selectFluid(int index) {
            return new Control(false, Math.max(0, index));
        }

        public Control selectCreature(int index) {
            return new Control(true, Math.max(0, index));
        }

        public Control clamp(int fluidCount, int creatureCount) {
            if (creatureSelected) {
                if (creatureCount <= 0) {
                    return new Control(false, fluidCount <= 0 ? 0 : Mth.clamp(selectedIndex, 0, fluidCount - 1));
                }
                return new Control(true, Mth.clamp(selectedIndex, 0, creatureCount - 1));
            }
            if (fluidCount <= 0) {
                return new Control(creatureCount > 0, 0);
            }
            return new Control(false, Mth.clamp(selectedIndex, 0, fluidCount - 1));
        }
    }

    public static final class CreatureRecord {
        public final ResourceLocation typeId;
        public final CompoundTag entityData;

        public CreatureRecord(ResourceLocation typeId, CompoundTag entityData) {
            this.typeId = Objects.requireNonNull(typeId, "typeId");
            this.entityData = entityData == null ? new CompoundTag() : entityData.copy();
        }

        public CompoundTag save() {
            CompoundTag tag = new CompoundTag();
            tag.putString(CREATURE_ID_KEY, typeId.toString());
            tag.put(CREATURE_DATA_KEY, entityData.copy());
            return tag;
        }

        public static CreatureRecord load(CompoundTag tag) {
            if (tag == null || !tag.contains(CREATURE_ID_KEY, Tag.TAG_STRING)) {
                return null;
            }
            ResourceLocation id = ResourceLocation.tryParse(tag.getString(CREATURE_ID_KEY));
            if (id == null) {
                return null;
            }
            CompoundTag data = tag.contains(CREATURE_DATA_KEY, Tag.TAG_COMPOUND)
                    ? tag.getCompound(CREATURE_DATA_KEY).copy()
                    : new CompoundTag();
            if (tag.contains(CREATURE_BUCKETABLE_KEY, Tag.TAG_COMPOUND)
                    && !data.contains(BUCKETABLE_DATA_KEY, Tag.TAG_COMPOUND)) {
                data.put(BUCKETABLE_DATA_KEY, tag.getCompound(CREATURE_BUCKETABLE_KEY).copy());
            }
            return new CreatureRecord(id, data);
        }

        @Nullable
        public EntityType<?> entityType() {
            return ForgeRegistries.ENTITY_TYPES.getValue(typeId);
        }

        public Component displayName() {
            if (entityData.contains("CustomName", Tag.TAG_STRING)) {
                try {
                    Component parsed = Component.Serializer.fromJson(entityData.getString("CustomName"));
                    if (parsed != null) {
                        return parsed;
                    }
                } catch (RuntimeException ignored) {
                }
            }
            EntityType<?> type = entityType();
            return type == null ? Component.literal(typeId.toString()) : type.getDescription();
        }

        public CompoundTag entityData() {
            return entityData.copy();
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (!(obj instanceof CreatureRecord other)) {
                return false;
            }
            return typeId.equals(other.typeId) && entityData.equals(other.entityData);
        }

        @Override
        public int hashCode() {
            return Objects.hash(typeId, entityData);
        }
    }
}
