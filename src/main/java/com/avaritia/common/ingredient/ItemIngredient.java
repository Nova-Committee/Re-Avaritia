package com.avaritia.common.ingredient;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.avaritia.init.registry.ModIngredients;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.common.crafting.ICustomIngredient;
import net.neoforged.neoforge.common.crafting.IngredientType;
import org.jetbrains.annotations.NotNull;

import java.util.stream.Stream;

/**
 * RawValue
 *
 * @author cnlimiter
 * @version 1.0
 * @description
 * @date 2024/6/16 下午2:02
 */
public record ItemIngredient(Identifier item) implements ICustomIngredient {
    public static final MapCodec<ItemIngredient> MAP_CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Identifier.CODEC.fieldOf("item").forGetter(ItemIngredient::item)
    ).apply(instance, ItemIngredient::new));

    public static final StreamCodec<ByteBuf, ItemIngredient> STREAM_CODEC = Identifier.STREAM_CODEC.map(ItemIngredient::new, ItemIngredient::item);
    @Override
    public boolean test(@NotNull ItemStack stack) {
        return stack.is(BuiltInRegistries.ITEM.get(this.item));
    }

    @Override
    public @NotNull Stream<ItemStack> getItems() {
        return Stream.of(BuiltInRegistries.ITEM.containsKey(this.item) ? new ItemStack(BuiltInRegistries.ITEM.get(this.item)) : ItemStack.EMPTY);
    }

    @Override
    public boolean isSimple() {
        return false;
    }

    @Override
    public @NotNull IngredientType<?> getType() {
        return ModIngredients.NBT_ITEM.get();
    }
}
