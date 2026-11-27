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
 * 普通工具模式。
 */
public enum ToolMode implements StringRepresentable, TranslatableEnum {
    DEFAULT(ModLang.DEFAULT_MODE),
    ADVANCE(ModLang.ADVANCE_MODE);

    public static final Codec<ToolMode> CODEC = StringRepresentable.fromEnum(ToolMode::values);
    public static final IntFunction<ToolMode> BY_ID = ByIdMap.continuous(ToolMode::ordinal, values(), ByIdMap.OutOfBoundsStrategy.WRAP);
    public static final StreamCodec<ByteBuf, ToolMode> STREAM_CODEC = ByteBufCodecs.idMapper(BY_ID, ToolMode::ordinal);

    private final ModLang langEntry;
    private final String serializedName;

    ToolMode(ModLang langEntry) {
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

    public ToolMode next(ItemStack stack) {
        return switch (this) {
            case ADVANCE -> DEFAULT;
            case DEFAULT -> ADVANCE;
        };
    }
}
