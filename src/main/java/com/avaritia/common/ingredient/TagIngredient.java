package com.avaritia.common.ingredient;

import com.avaritia.init.registry.ModIngredients;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.common.crafting.ICustomIngredient;
import net.neoforged.neoforge.common.crafting.IngredientType;
import org.jetbrains.annotations.NotNull;
import org.jspecify.annotations.NonNull;

import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public record TagIngredient(TagKey<Item> tag) implements ICustomIngredient {
    public static final MapCodec<TagIngredient> MAP_CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            TagKey.codec(Registries.ITEM).fieldOf("tag").forGetter(TagIngredient::tag)
    ).apply(instance, TagIngredient::new));

    public static final StreamCodec<ByteBuf, TagIngredient> STREAM_CODEC = TagKey.streamCodec(Registries.ITEM).map(TagIngredient::new, TagIngredient::tag);

    @Override
    public boolean test(@NotNull ItemStack stack) {
        return stack.is(this.tag);
    }

    @Override
    public @NonNull Stream<Holder<Item>> items() {
        return StreamSupport.stream(BuiltInRegistries.ITEM.getTagOrEmpty(this.tag).spliterator(), false);
    }

    @Override
    public boolean isSimple() {
        return true;
    }

    @Override
    public @NotNull IngredientType<?> getType() {
        return ModIngredients.TAG_ITEM.get();
    }
}
