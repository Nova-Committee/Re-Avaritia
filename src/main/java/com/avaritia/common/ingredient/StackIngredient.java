package com.avaritia.common.ingredient;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.avaritia.init.registry.ModIngredients;
import net.minecraft.core.Holder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.common.crafting.ICustomIngredient;
import net.neoforged.neoforge.common.crafting.IngredientType;
import org.jetbrains.annotations.NotNull;
import org.jspecify.annotations.NonNull;

import java.util.stream.Stream;

/**
 * RawValue
 *
 * @author cnlimiter
 * @version 1.0
 * @description
 * @date 2024/6/16 下午2:02
 */
public record StackIngredient(ItemStack item) implements ICustomIngredient {
    public static final MapCodec<StackIngredient> MAP_CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            ItemStack.OPTIONAL_CODEC.fieldOf("item").forGetter(StackIngredient::item)
    ).apply(instance, StackIngredient::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, StackIngredient> STREAM_CODEC = ItemStack.STREAM_CODEC.map(StackIngredient::new, StackIngredient::item);
    @Override
    public boolean test(@NotNull ItemStack stack) {
        return ItemStack.isSameItem(stack, this.item);
    }

    @Override
    public @NonNull Stream<Holder<Item>> items() {
        return Stream.of(this.item.typeHolder());
    }

    @Override
    public boolean isSimple() {
        return false;
    }

    @Override
    public @NotNull IngredientType<?> getType() {
        return ModIngredients.STACK_ITEM.get();
    }
}
