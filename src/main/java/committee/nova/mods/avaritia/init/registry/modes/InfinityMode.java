package committee.nova.mods.avaritia.init.registry.modes;

import committee.nova.mods.avaritia.init.registry.enums.ModLang;
import com.mojang.serialization.Codec;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.ByIdMap;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.common.TranslatableEnum;
import org.jetbrains.annotations.NotNull;

import java.util.Locale;
import java.util.function.IntFunction;

/**
 * 无尽工具模式。
 */
public enum InfinityMode implements StringRepresentable, TranslatableEnum {
    DEFAULT(ModLang.DEFAULT_MODE),
    RANGE(ModLang.RANGE_MODE);

    public static final Codec<InfinityMode> CODEC = StringRepresentable.fromEnum(InfinityMode::values);
    public static final IntFunction<InfinityMode> BY_ID = ByIdMap.continuous(InfinityMode::ordinal, values(), ByIdMap.OutOfBoundsStrategy.WRAP);
    public static final StreamCodec<ByteBuf, InfinityMode> STREAM_CODEC = ByteBufCodecs.idMapper(BY_ID, InfinityMode::ordinal);

    private final ModLang langEntry;
    private final String serializedName;

    InfinityMode(ModLang langEntry) {
        this.serializedName = name().toLowerCase(Locale.ROOT);
        this.langEntry = langEntry;
    }

    @NotNull
    @Override
    public String getSerializedName() {
        return serializedName;
    }

    public String getTranslationKey() {
        return langEntry.getTranslationKey();
    }

    @NotNull
    @Override
    public Component getTranslatedName() {
        return Component.translatable(getTranslationKey());
    }

    public InfinityMode next(ItemStack stack) {
        return switch (this) {
            case DEFAULT -> RANGE;
            case RANGE -> DEFAULT;
        };
    }
}
