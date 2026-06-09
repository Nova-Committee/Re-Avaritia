package com.avaritia.api.common.crafting;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.ShapedRecipePattern;

import java.util.List;
import java.util.function.Function;

/**
 * @Project: Avaritia
 * @Author: cnlimiter
 * @CreateTime: 2025/5/15 13:49
 * @Description:
 */
public final class ShapedRecipePatternCodecs {
    private static final Codec<List<String>> PATTERN_CODEC;
    public static final Codec<Character> SYMBOL_CODEC;
    private static final MapCodec<ShapedRecipePattern.Data> DATA_MAP_CODEC;
    public static final MapCodec<ShapedRecipePattern> MAP_CODEC;

    public ShapedRecipePatternCodecs() {
    }

    static {
        PATTERN_CODEC = Codec.STRING.listOf().comapFlatMap((pattern) -> {
            if (pattern.isEmpty()) {
                return DataResult.error(() -> "Invalid pattern: empty pattern not allowed");
            } else {
                int length = pattern.getFirst().length();

                for(String line : pattern) {
                    if (length != line.length()) {
                        return DataResult.error(() -> "Invalid pattern: each row must be the same width");
                    }
                }

                return DataResult.success(pattern);
            }
        }, Function.identity());
        SYMBOL_CODEC = Codec.STRING.comapFlatMap((symbol) -> {
            if (symbol.length() != 1) {
                return DataResult.error(() -> "Invalid key entry: '" + symbol + "' is an invalid symbol (must be 1 character only).");
            } else {
                return " ".equals(symbol) ? DataResult.error(() -> "Invalid key entry: ' ' is a reserved symbol.") : DataResult.success(symbol.charAt(0));
            }
        }, String::valueOf);
        DATA_MAP_CODEC = RecordCodecBuilder.mapCodec((builder) -> builder.group(ExtraCodecs.strictUnboundedMap(SYMBOL_CODEC, RecipeCodecs.LEGACY_INGREDIENT).fieldOf("key").forGetter(ShapedRecipePattern.Data::key), PATTERN_CODEC.fieldOf("pattern").forGetter(ShapedRecipePattern.Data::pattern)).apply(builder, ShapedRecipePattern.Data::new));
        MAP_CODEC = DATA_MAP_CODEC.flatXmap(ShapedRecipePattern::unpack, (pattern) -> pattern.data.map(DataResult::success).orElseGet(() -> DataResult.error(() -> "Cannot encode unpacked recipe")));
    }
}
