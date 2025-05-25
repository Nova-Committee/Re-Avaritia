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
public enum InfinityMode implements IModeEnum<InfinityMode> {
    DEFAULT(ModLang.DEFAULT_MODE),
    RANGE(ModLang.RANGE_MODE);

    public static final Codec<InfinityMode> CODEC = StringRepresentable.fromEnum(InfinityMode::values);
    public static final IntFunction<InfinityMode> BY_ID = ByIdMap.continuous(InfinityMode::ordinal, values(), ByIdMap.OutOfBoundsStrategy.WRAP);
    public static final StreamCodec<ByteBuf, InfinityMode> STREAM_CODEC = ByteBufCodecs.idMapper(BY_ID, InfinityMode::ordinal);

    private final IHasTranslationKey langEntry;
    private final String serializedName;

    InfinityMode(IHasTranslationKey langEntry) {
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
    public InfinityMode next(ItemStack stack) {
        return switch (this) {
            case DEFAULT -> RANGE;
            case RANGE -> DEFAULT;
        };
    }
}
