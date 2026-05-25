package net.minecraft.world.item.crafting;

import com.google.common.collect.Lists;
import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.mojang.serialization.codecs.RecordCodecBuilder.Instance;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntComparators;
import it.unimi.dsi.fastutil.ints.IntList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Stream;
import javax.annotation.Nullable;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.player.StackedContents;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;

public final class Ingredient implements Predicate<ItemStack> {
    public static final Ingredient EMPTY = new Ingredient(Stream.empty());
    public static final StreamCodec<RegistryFriendlyByteBuf, Ingredient> CONTENTS_STREAM_CODEC = new StreamCodec<>() {
        private static final StreamCodec<RegistryFriendlyByteBuf, net.neoforged.neoforge.common.crafting.ICustomIngredient> CUSTOM_INGREDIENT_CODEC = net.minecraft.network.codec.ByteBufCodecs.registry(net.neoforged.neoforge.registries.NeoForgeRegistries.Keys.INGREDIENT_TYPES)
                .dispatch(c -> c.getType(), t -> t.streamCodec());

        @Override
        public void encode(RegistryFriendlyByteBuf buf, Ingredient ingredient) {
            if (ingredient.isSimple()) {
                ItemStack.LIST_STREAM_CODEC.encode(buf, Arrays.asList(ingredient.getItems()));
            } else {
                buf.writeVarInt(-1);
                CUSTOM_INGREDIENT_CODEC.encode(buf, ingredient.customIngredient);
            }
        }

        @Override
        public Ingredient decode(RegistryFriendlyByteBuf buf) {
            var size = buf.readVarInt();
            if (size == -1) {
                return new Ingredient(CUSTOM_INGREDIENT_CODEC.decode(buf));
            }
            return fromValues(Stream.generate(() -> ItemStack.STREAM_CODEC.decode(buf)).limit(size).map(Ingredient.ItemValue::new));
        }
    };
    private final Ingredient.Value[] values;
    @Nullable
    private ItemStack[] itemStacks;
    @Nullable
    private IntList stackingIds;
    @Nullable
    private net.neoforged.neoforge.common.crafting.ICustomIngredient customIngredient = null;

    /**
     * This codec allows both the {@code {...}} and {@code [{...}, {...}, ...]} syntax.
     * {@code []} is allowed for empty ingredients, and will only match empty stacks.
     */
    public static final Codec<Ingredient> CODEC = net.neoforged.neoforge.common.crafting.CraftingHelper.makeIngredientCodec(true);
    /**
     * Same as {@link #CODEC} except that empty ingredients ({@code []}) are not allowed.
     */
    public static final Codec<Ingredient> CODEC_NONEMPTY = net.neoforged.neoforge.common.crafting.CraftingHelper.makeIngredientCodec(false);
    /**
     * This is a codec that only allows the {@code {...}} syntax.
     * Array ingredients are serialized using the CompoundIngredient custom ingredient type:
     * {@code { "type": "neoforge:compound", "ingredients": [{...}, {...}, ...] }}.
     */
    public static final com.mojang.serialization.MapCodec<Ingredient> MAP_CODEC_NONEMPTY = net.neoforged.neoforge.common.crafting.CraftingHelper.makeIngredientMapCodec();
    public static final Codec<List<Ingredient>> LIST_CODEC = MAP_CODEC_NONEMPTY.codec().listOf();
    public static final Codec<List<Ingredient>> LIST_CODEC_NONEMPTY = LIST_CODEC.validate(list -> list.isEmpty() ? DataResult.error(() -> "Item array cannot be empty, at least one item must be defined") : DataResult.success(list));

    private Ingredient(Stream<? extends Ingredient.Value> p_43907_) {
        this.values = p_43907_.toArray(Ingredient.Value[]::new);
    }

    private Ingredient(Ingredient.Value[] p_301044_) {
        this.values = p_301044_;
    }

    public Ingredient(net.neoforged.neoforge.common.crafting.ICustomIngredient customIngredient) {
        this(new Value[0]);
        this.customIngredient = customIngredient;
    }

    public ItemStack[] getItems() {
        if (this.itemStacks == null) {
            // Neo: vanilla used Stream.distinct() here which has basically no effect as ItemStack does not override hashCode() and equals().
            // Using ItemStackLinkedSet::createTypeAndComponentsSet instead for a real distinct result.
            final Stream<ItemStack> stream = this.customIngredient == null
                    ? Arrays.stream(this.values).flatMap(value -> value.getItems().stream())
                    : this.customIngredient.getItems();
            this.itemStacks = stream.collect(java.util.stream.Collectors.toCollection(net.minecraft.world.item.ItemStackLinkedSet::createTypeAndComponentsSet)).toArray(ItemStack[]::new);
        }

        return this.itemStacks;
    }

    public boolean test(@Nullable ItemStack p_43914_) {
        if (p_43914_ == null) {
            return false;
        } else if (this.customIngredient != null) {
            return this.customIngredient.test(p_43914_);
        } else if (this.isEmpty()) {
            return p_43914_.isEmpty();
        } else {
            for (ItemStack itemstack : this.getItems()) {
                if (itemstack.is(p_43914_.getItem())) {
                    return true;
                }
            }

            return false;
        }
    }

    public IntList getStackingIds() {
        if (this.stackingIds == null) {
            ItemStack[] aitemstack = this.getItems();
            this.stackingIds = new IntArrayList(aitemstack.length);

            for (ItemStack itemstack : aitemstack) {
                this.stackingIds.add(StackedContents.getStackingIndex(itemstack));
            }

            this.stackingIds.sort(IntComparators.NATURAL_COMPARATOR);
        }

        return this.stackingIds;
    }

    /**
     * Returns {@code true} if this ingredient is explicitly chosen to be empty, i.e. using {@code []}.
     */
    public boolean isEmpty() {
        return this.values.length == 0 && !isCustom();
    }

    /**
     * Returns {@code true} if this ingredient has an empty stack list.
     * Unlike {@link #isEmpty()}, this will catch "accidentally empty" ingredients,
     * for example a tag ingredient that has an empty tag.
     */
    public boolean hasNoItems() {
        ItemStack[] items = getItems();
        if (items.length == 0)
            return true;
        if (items.length == 1) {
            // If we potentially added a barrier due to the ingredient being an empty tag, try and check if it is the stack we added
            ItemStack item = items[0];
            return item.getItem() == net.minecraft.world.item.Items.BARRIER && item.getHoverName() instanceof net.minecraft.network.chat.MutableComponent hoverName && hoverName.getString().startsWith("Empty Tag: ");
        }
        return false;
    }

    @Override
    public boolean equals(Object p_301003_) {
        return p_301003_ instanceof Ingredient ingredient ? java.util.Objects.equals(this.customIngredient, ingredient.customIngredient) && Arrays.equals((Object[])this.values, (Object[])ingredient.values) : false;
    }

    @Override
    public int hashCode() {
        if (this.customIngredient != null) {
            return this.customIngredient.hashCode();
        }
        return Arrays.hashCode(this.values);
    }

    /**
     * Retrieves the underlying values of this ingredient.
     * If this is a {@linkplain #isCustom custom ingredient}, an exception is thrown.
     */
    public Value[] getValues() {
        if (isCustom()) {
            throw new IllegalStateException("Cannot retrieve values from custom ingredient!");
        }
        return this.values;
    }

    public boolean isSimple() {
        return this.customIngredient == null || this.customIngredient.isSimple();
    }

    @Nullable
    public net.neoforged.neoforge.common.crafting.ICustomIngredient getCustomIngredient() {
        return this.customIngredient;
    }

    public boolean isCustom() {
        return this.customIngredient != null;
    }

    public static Ingredient fromValues(Stream<? extends Ingredient.Value> p_43939_) {
        Ingredient ingredient = new Ingredient(p_43939_);
        return ingredient.isEmpty() ? EMPTY : ingredient;
    }

    public static Ingredient of() {
        return EMPTY;
    }

    public static Ingredient of(ItemLike... p_43930_) {
        return of(Arrays.stream(p_43930_).map(ItemStack::new));
    }

    public static Ingredient of(ItemStack... p_43928_) {
        return of(Arrays.stream(p_43928_));
    }

    public static Ingredient of(Stream<ItemStack> p_43922_) {
        return fromValues(p_43922_.filter(p_43944_ -> !p_43944_.isEmpty()).map(Ingredient.ItemValue::new));
    }

    public static Ingredient of(TagKey<Item> p_204133_) {
        return fromValues(Stream.of(new Ingredient.TagValue(p_204133_)));
    }

    @Deprecated // Neo: We take over the codec creation entirely to support custom ingredients - see CraftingHelper
    private static Codec<Ingredient> codec(boolean p_301074_) {
        Codec<Ingredient.Value[]> codec = Codec.list(Ingredient.Value.CODEC)
            .comapFlatMap(
                p_300810_ -> !p_301074_ && p_300810_.size() < 1
                        ? DataResult.error(() -> "Item array cannot be empty, at least one item must be defined")
                        : DataResult.success(p_300810_.toArray(new Ingredient.Value[0])),
                List::of
            );
        return Codec.either(codec, Ingredient.Value.CODEC)
            .flatComapMap(
                p_300805_ -> p_300805_.map(Ingredient::new, p_300806_ -> new Ingredient(new Ingredient.Value[]{p_300806_})),
                p_300808_ -> {
                    if (p_300808_.values.length == 1) {
                        return DataResult.success(Either.right(p_300808_.values[0]));
                    } else {
                        return p_300808_.values.length == 0 && !p_301074_
                            ? DataResult.error(() -> "Item array cannot be empty, at least one item must be defined")
                            : DataResult.success(Either.left(p_300808_.values));
                    }
                }
            );
    }

    public static record ItemValue(ItemStack item) implements Ingredient.Value {
        static final com.mojang.serialization.MapCodec<Ingredient.ItemValue> MAP_CODEC = RecordCodecBuilder.mapCodec(
            p_330109_ -> p_330109_.group(ItemStack.SIMPLE_ITEM_CODEC.fieldOf("item").forGetter(p_300919_ -> p_300919_.item))
                    .apply(p_330109_, Ingredient.ItemValue::new)
        );
        static final Codec<Ingredient.ItemValue> CODEC = MAP_CODEC.codec();

        @Override
        public boolean equals(Object p_301316_) {
            return !(p_301316_ instanceof Ingredient.ItemValue ingredient$itemvalue)
                ? false
                : ingredient$itemvalue.item.getItem().equals(this.item.getItem()) && ingredient$itemvalue.item.getCount() == this.item.getCount();
        }

        // Neo: Add a hashCode() implementation that matches equals()
        @Override
        public int hashCode() {
            return 31 * item.getItem().hashCode() + item.getCount();
        }

        @Override
        public Collection<ItemStack> getItems() {
            return Collections.singleton(this.item);
        }
    }

    public static record TagValue(TagKey<Item> tag) implements Ingredient.Value {
        static final com.mojang.serialization.MapCodec<Ingredient.TagValue> MAP_CODEC = RecordCodecBuilder.mapCodec(
            p_301118_ -> p_301118_.group(TagKey.codec(Registries.ITEM).fieldOf("tag").forGetter(p_301154_ -> p_301154_.tag))
                    .apply(p_301118_, Ingredient.TagValue::new)
        );
        static final Codec<Ingredient.TagValue> CODEC = MAP_CODEC.codec();

        @Override
        public boolean equals(Object p_301162_) {
            return p_301162_ instanceof Ingredient.TagValue ingredient$tagvalue ? ingredient$tagvalue.tag.location().equals(this.tag.location()) : false;
        }

        @Override
        public Collection<ItemStack> getItems() {
            List<ItemStack> list = Lists.newArrayList();

            for (Holder<Item> holder : BuiltInRegistries.ITEM.getTagOrEmpty(this.tag)) {
                list.add(new ItemStack(holder));
            }

            if (list.isEmpty()) {
                net.minecraft.world.item.ItemStack itemStack = new net.minecraft.world.item.ItemStack(net.minecraft.world.level.block.Blocks.BARRIER);
                itemStack.set(net.minecraft.core.component.DataComponents.CUSTOM_NAME, net.minecraft.network.chat.Component.literal("Empty Tag: " + this.tag.location()));
                list.add(itemStack);
            }
            return list;
        }
    }

    // Neo: Do not extend this interface. For custom ingredient behaviors see ICustomIngredient.
    public interface Value {
        com.mojang.serialization.MapCodec<Ingredient.Value> MAP_CODEC = net.neoforged.neoforge.common.util.NeoForgeExtraCodecs.xor(Ingredient.ItemValue.MAP_CODEC, Ingredient.TagValue.MAP_CODEC)
            .xmap(p_300956_ -> p_300956_.map(p_300932_ -> p_300932_, p_301313_ -> p_301313_), p_301304_ -> {
                if (p_301304_ instanceof Ingredient.TagValue ingredient$tagvalue) {
                    return Either.right(ingredient$tagvalue);
                } else if (p_301304_ instanceof Ingredient.ItemValue ingredient$itemvalue) {
                    return Either.left(ingredient$itemvalue);
                } else {
                    throw new UnsupportedOperationException("This is neither an item value nor a tag value.");
                }
            });
        Codec<Ingredient.Value> CODEC = MAP_CODEC.codec();

        Collection<ItemStack> getItems();
    }
}
