package committee.nova.mods.avaritia.common.component;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.fluids.FluidStack;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Legacy combined Infinity Bucket component. Kept so old saves still load;
 * live storage uses {@link InfinityBucketFluids}, {@link InfinityBucketCreatures}
 * and {@link InfinityBucketControl}.
 */
public record InfinityBucketContents(
        List<FluidStack> fluids,
        List<CapturedCreature> creatures,
        int selectedFluid,
        int selectedCreature,
        boolean forceEnvironment,
        boolean creatureTab
) {
    public static final InfinityBucketContents EMPTY = new InfinityBucketContents(
            List.of(), List.of(), 0, 0, false, false);

    private static final Codec<InfinityBucketContents> RAW_CODEC = RecordCodecBuilder.create(instance -> instance.group(
            FluidStack.CODEC.sizeLimitedListOf(InfinityBucketBudget.MAX_FLUID_ENTRIES).optionalFieldOf("fluids", List.of())
                    .forGetter(InfinityBucketContents::fluids),
            CapturedCreature.CODEC.sizeLimitedListOf(InfinityBucketBudget.MAX_CREATURE_ENTRIES).optionalFieldOf("creatures", List.of())
                    .forGetter(InfinityBucketContents::creatures),
            Codec.INT.optionalFieldOf("selected_fluid", 0).forGetter(InfinityBucketContents::selectedFluid),
            Codec.INT.optionalFieldOf("selected_creature", 0).forGetter(InfinityBucketContents::selectedCreature),
            Codec.BOOL.optionalFieldOf("force", false).forGetter(InfinityBucketContents::forceEnvironment),
            Codec.BOOL.optionalFieldOf("creature_tab", false).forGetter(InfinityBucketContents::creatureTab)
    ).apply(instance, InfinityBucketContents::new));

    public static final Codec<InfinityBucketContents> CODEC = RAW_CODEC.validate(contents ->
            toCreatures(contents.creatures).size() > InfinityBucketBudget.MAX_CREATURE_ENTRIES
                    || contents.fluids.size() > InfinityBucketBudget.MAX_FLUID_ENTRIES
                    ? DataResult.error(() -> "Infinity bucket payload exceeds storage budget")
                    : DataResult.success(contents));

    public static final StreamCodec<RegistryFriendlyByteBuf, InfinityBucketContents> STREAM_CODEC = StreamCodec.of(
            InfinityBucketContents::encode,
            InfinityBucketContents::decode
    );

    public InfinityBucketContents {
        fluids = copyFluids(fluids);
        creatures = List.copyOf(creatures);
        selectedFluid = fluids.isEmpty() ? 0 : Mth.clamp(selectedFluid, 0, fluids.size() - 1);
        selectedCreature = creatures.isEmpty() ? 0 : Mth.clamp(selectedCreature, 0, creatures.size() - 1);
    }

    public List<FluidStack> copyFluids() {
        return copyFluids(fluids);
    }

    public InfinityBucketFluids toFluids() {
        return new InfinityBucketFluids(fluids);
    }

    public InfinityBucketCreatures toCreatureList() {
        return new InfinityBucketCreatures(toCreatures(creatures));
    }

    public InfinityBucketControl toControl() {
        return creatureTab
                ? new InfinityBucketControl(true, selectedCreature)
                : new InfinityBucketControl(false, selectedFluid);
    }

    public void writeSplitComponents(ItemStack stack) {
        InfinityBucketFluids storedFluids = toFluids();
        InfinityBucketCreatures storedCreatures = toCreatureList();
        InfinityBucketControl control = toControl().clamp(storedFluids.size(), storedCreatures.size());
        if (storedFluids.isEmpty()) {
            stack.remove(committee.nova.mods.avaritia.init.registry.ModDataComponents.INFINITY_BUCKET_FLUIDS.get());
        } else {
            stack.set(committee.nova.mods.avaritia.init.registry.ModDataComponents.INFINITY_BUCKET_FLUIDS.get(), storedFluids);
        }
        if (storedCreatures.isEmpty()) {
            stack.remove(committee.nova.mods.avaritia.init.registry.ModDataComponents.INFINITY_BUCKET_CREATURES.get());
        } else {
            stack.set(committee.nova.mods.avaritia.init.registry.ModDataComponents.INFINITY_BUCKET_CREATURES.get(), storedCreatures);
        }
        if (control.equals(InfinityBucketControl.DEFAULT)) {
            stack.remove(committee.nova.mods.avaritia.init.registry.ModDataComponents.INFINITY_BUCKET_CONTROL.get());
        } else {
            stack.set(committee.nova.mods.avaritia.init.registry.ModDataComponents.INFINITY_BUCKET_CONTROL.get(), control);
        }
    }

    private static List<FluidStack> copyFluids(List<FluidStack> source) {
        List<FluidStack> copied = new ArrayList<>(source.size());
        for (FluidStack fluid : source) {
            if (fluid != null && !fluid.isEmpty()) {
                copied.add(fluid.copy());
            }
        }
        return List.copyOf(copied);
    }

    private static List<InfinityBucketCreature> toCreatures(List<CapturedCreature> source) {
        List<InfinityBucketCreature> next = new ArrayList<>(source.size());
        for (CapturedCreature creature : source) {
            CompoundTag data = creature.data().copy();
            if (!creature.bucketData().isEmpty()) {
                data.put("BucketableData", creature.bucketData().copy());
            }
            creature.customName().ifPresent(name ->
                    ComponentSerialization.CODEC.encodeStart(NbtOps.INSTANCE, name).result().ifPresent(tag -> data.put("CustomName", tag)));
            next.add(new InfinityBucketCreature(creature.typeId(), data));
        }
        return next;
    }

    private static void encode(RegistryFriendlyByteBuf buf, InfinityBucketContents contents) {
        FluidStack.OPTIONAL_STREAM_CODEC.apply(ByteBufCodecs.list(InfinityBucketBudget.MAX_FLUID_ENTRIES)).encode(buf, contents.fluids);
        CapturedCreature.NETWORK_STREAM_CODEC.apply(ByteBufCodecs.list(InfinityBucketBudget.MAX_CREATURE_ENTRIES)).encode(buf, contents.creatures);
        ByteBufCodecs.VAR_INT.encode(buf, contents.selectedFluid);
        ByteBufCodecs.VAR_INT.encode(buf, contents.selectedCreature);
        ByteBufCodecs.BOOL.encode(buf, contents.forceEnvironment);
        ByteBufCodecs.BOOL.encode(buf, contents.creatureTab);
    }

    private static InfinityBucketContents decode(RegistryFriendlyByteBuf buf) {
        List<FluidStack> fluids = FluidStack.OPTIONAL_STREAM_CODEC.apply(ByteBufCodecs.list(InfinityBucketBudget.MAX_FLUID_ENTRIES)).decode(buf);
        List<CapturedCreature> creatures = CapturedCreature.NETWORK_STREAM_CODEC.apply(ByteBufCodecs.list(InfinityBucketBudget.MAX_CREATURE_ENTRIES)).decode(buf);
        int selectedFluid = ByteBufCodecs.VAR_INT.decode(buf);
        int selectedCreature = ByteBufCodecs.VAR_INT.decode(buf);
        boolean force = ByteBufCodecs.BOOL.decode(buf);
        boolean creatureTab = ByteBufCodecs.BOOL.decode(buf);
        return new InfinityBucketContents(fluids, creatures, selectedFluid, selectedCreature, force, creatureTab);
    }

    public record CapturedCreature(
            Identifier typeId,
            CompoundTag data,
            CompoundTag bucketData,
            Optional<Component> customName
    ) {
        public static final Codec<CapturedCreature> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                Identifier.CODEC.fieldOf("id").forGetter(CapturedCreature::typeId),
                CompoundTag.CODEC.optionalFieldOf("data", new CompoundTag()).forGetter(CapturedCreature::data),
                CompoundTag.CODEC.optionalFieldOf("bucket_data", new CompoundTag()).forGetter(CapturedCreature::bucketData),
                ComponentSerialization.CODEC.optionalFieldOf("name").forGetter(CapturedCreature::customName)
        ).apply(instance, CapturedCreature::new));

        public static final StreamCodec<RegistryFriendlyByteBuf, CapturedCreature> NETWORK_STREAM_CODEC = StreamCodec.composite(
                Identifier.STREAM_CODEC, CapturedCreature::typeId,
                ComponentSerialization.OPTIONAL_STREAM_CODEC, CapturedCreature::customName,
                (id, name) -> new CapturedCreature(id, new CompoundTag(), new CompoundTag(), name)
        );

        public CapturedCreature {
            data = data == null ? new CompoundTag() : data.copy();
            bucketData = bucketData == null ? new CompoundTag() : bucketData.copy();
        }
    }
}
