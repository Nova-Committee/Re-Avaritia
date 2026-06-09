package committee.nova.mods.avaritia.client.tint;

import com.mojang.serialization.MapCodec;
import net.minecraft.client.color.item.ItemTintSource;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.awt.Color;

public record RainbowTintSource() implements ItemTintSource {
    public static final MapCodec<RainbowTintSource> CODEC = MapCodec.unit(RainbowTintSource::new);

    @Override
    public int calculate(ItemStack stack, @Nullable ClientLevel level, @Nullable LivingEntity entity) {
        float hue = (System.currentTimeMillis() % 18000L) / 18000.0F;
        return Color.HSBtoRGB(hue, 1.0F, 1.0F);
    }

    @Override
    public MapCodec<? extends ItemTintSource> type() {
        return CODEC;
    }
}
