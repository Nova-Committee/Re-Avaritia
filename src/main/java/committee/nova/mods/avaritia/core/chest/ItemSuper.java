package committee.nova.mods.avaritia.core.chest;

import appeng.api.stacks.AEItemKey;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import committee.nova.mods.avaritia.Const;
import committee.nova.mods.avaritia.core.singularity.Singularity;
import committee.nova.mods.avaritia.util.StorageUtils;
import lombok.Getter;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.jetbrains.annotations.Nullable;

/**
 * @author cnlimiter
 */
public class ItemSuper {
    public static final ItemSuper EMPTY = new ItemSuper(ItemStack.EMPTY, 0L);
    public static final MapCodec<ItemSuper> MAP_CODEC = RecordCodecBuilder.mapCodec(
            builder -> builder.group(
                            BuiltInRegistries.ITEM.holderByNameCodec().validate(
                                            item -> item.is(Items.AIR.builtInRegistryHolder())
                                                    ? DataResult.error(() -> "Item must not be minecraft:air")
                                                    : DataResult.success(item))
                                    .fieldOf("id").forGetter(itemSuper -> itemSuper.stack.getItemHolder()),
                            DataComponentPatch.CODEC.optionalFieldOf("components", DataComponentPatch.EMPTY)
                                    .forGetter(itemSuper -> itemSuper.stack.getComponentsPatch()),
                            Codec.LONG.optionalFieldOf("realCount", 0L).forGetter(recipe -> recipe.realCount)

            )
            .apply(builder, (item, componentPatch, realCount) -> new ItemSuper(new ItemStack(item, 1, componentPatch), realCount)));
    public static final Codec<ItemSuper> CODEC = MAP_CODEC.codec();

    public static final StreamCodec<RegistryFriendlyByteBuf, ItemSuper> STREAM_CODEC = StreamCodec.of(
            ItemSuper::writeToPacket, ItemSuper::fromPacket
    );

    @Getter private final ItemStack stack;
    @Getter private final long realCount;
    private final int hashCode;

    public ItemSuper(ItemStack stack, long realCount) {
        this.stack = stack;
        this.realCount = realCount;
        this.hashCode = ItemStack.hashItemAndComponents(stack);
    }

    @Nullable
    public static ItemSuper of(ItemStack stack) {
        if (stack.isEmpty()) {
            return null;
        }

        return new ItemSuper(stack.copy(), stack.getCount());
    }

    public boolean hasComponents() {
        return stack.getComponents().isEmpty();
    }

    @Nullable
    public static ItemSuper fromTag(HolderLookup.Provider registries, CompoundTag tag) {
        var ops = registries.createSerializationContext(NbtOps.INSTANCE);
        try {
            return CODEC.decode(ops, tag).getOrThrow().getFirst();
        } catch (Exception e) {
            Const.LOGGER.debug("Tried to load an invalid item key from NBT: {}", tag);
            return null;
        }
    }

    public CompoundTag toTag(HolderLookup.Provider registries) {
        var ops = registries.createSerializationContext(NbtOps.INSTANCE);
        return (CompoundTag) CODEC.encodeStart(ops, this)
                .getOrThrow();
    }

    public static void writeToPacket(RegistryFriendlyByteBuf data, ItemSuper itemSuper) {
        ItemStack.STREAM_CODEC.encode(data, itemSuper.stack);
        data.writeLong(itemSuper.realCount);
    }

    public static ItemSuper fromPacket(RegistryFriendlyByteBuf data) {
        var stack = ItemStack.STREAM_CODEC.decode(data);
        var realCount = data.readLong();
        return new ItemSuper(stack, realCount);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        ItemSuper itemSuper = (ItemSuper) o;
        return this.hashCode == itemSuper.hashCode && ItemStack.isSameItemSameComponents(stack, itemSuper.stack);
    }

    @Override
    public String toString() {
        var id = BuiltInRegistries.ITEM.getKey(stack.getItem());
        String idString = id != BuiltInRegistries.ITEM.getDefaultKey() ? id.toString()
                : stack.getItem().getClass().getName() + "(unregistered)";
        return stack.isComponentsPatchEmpty() ? idString : idString + "#" + hashCode;
    }

    @Override
    public int hashCode() {
        return hashCode;
    }
}
