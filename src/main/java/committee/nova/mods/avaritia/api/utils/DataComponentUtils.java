package committee.nova.mods.avaritia.api.utils;

import com.mojang.serialization.Codec;
import committee.nova.mods.avaritia.Const;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;

/**
 * @author cnlimiter
 */
public class DataComponentUtils {
    public static final Codec<DataComponentMap> COMPONENTS_CODEC = DataComponentMap.CODEC.optionalFieldOf("components", DataComponentMap.EMPTY).codec();


    public static DataComponentMap loadWithComponents(CompoundTag tag, HolderLookup.Provider registries) {
        return COMPONENTS_CODEC
                .parse(registries.createSerializationContext(NbtOps.INSTANCE), tag)
                .resultOrPartial(p_337987_ -> Const.LOGGER.warn("Failed to load components: {}", p_337987_))
                .orElse(DataComponentMap.EMPTY);
    }

    public static CompoundTag saveWithComponents(DataComponentMap components,HolderLookup.Provider registries) {
        CompoundTag compoundtag = new CompoundTag();
        COMPONENTS_CODEC
                .encodeStart(registries.createSerializationContext(NbtOps.INSTANCE), components)
                .resultOrPartial(p_337988_ -> Const.LOGGER.warn("Failed to save components: {}", p_337988_))
                .ifPresent(p_337994_ -> compoundtag.merge((CompoundTag)p_337994_));
        return compoundtag;
    }
}
