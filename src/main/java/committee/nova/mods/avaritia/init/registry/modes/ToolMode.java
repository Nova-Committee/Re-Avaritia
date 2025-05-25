package committee.nova.mods.avaritia.init.registry.modes;

import com.mojang.serialization.Codec;
import committee.nova.mods.avaritia.api.common.item.iface.mode.IModeEnum;
import committee.nova.mods.avaritia.api.utils.text.IHasTranslationKey;
import committee.nova.mods.avaritia.init.registry.enums.ModLang;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.ByIdMap;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.Locale;
import java.util.function.IntFunction;

/**
 * @project: Avaritia
 * @author: cnlimiter
 * @createTime: 2025/5/24 20:41
 * @apiNote:
 */
public enum ToolMode implements IModeEnum<ToolMode> {
    DEFAULT(ModLang.DEFAULT_MODE),
    ADVANCE(ModLang.ADVANCE_MODE);

    public static final Codec<ToolMode> CODEC = StringRepresentable.fromEnum(ToolMode::values);
    public static final IntFunction<ToolMode> BY_ID = ByIdMap.continuous(ToolMode::ordinal, values(), ByIdMap.OutOfBoundsStrategy.WRAP);
    public static final StreamCodec<ByteBuf, ToolMode> STREAM_CODEC = ByteBufCodecs.idMapper(BY_ID, ToolMode::ordinal);

    private final IHasTranslationKey langEntry;
    private final String serializedName;

    ToolMode(IHasTranslationKey langEntry) {
        this.serializedName = name().toLowerCase(Locale.ROOT);
        this.langEntry = langEntry;
    }

    @NotNull
    @Override
    public String getSerializedName() {
        return serializedName;
    }

    @Override
    public String getTranslationKey() {
        return langEntry.getTranslationKey();
    }

    @Override
    public ToolMode next(ItemStack stack) {
        return switch (this) {
            case ADVANCE -> DEFAULT;
            case DEFAULT -> ADVANCE;
        };
    }
}
