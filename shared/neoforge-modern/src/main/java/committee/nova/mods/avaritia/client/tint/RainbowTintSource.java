package committee.nova.mods.avaritia.client.tint;

import com.mojang.serialization.MapCodec;
import net.minecraft.client.color.item.ItemTintSource;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.awt.Color;

public record RainbowTintSource() implements ItemTintSource {
    private static final long CYCLE_MILLIS = 18_000L;
    public static final MapCodec<RainbowTintSource> CODEC = MapCodec.unit(RainbowTintSource::new);

    public static int currentColor() {
        return colorAt(System.currentTimeMillis());
    }

    static int colorAt(long timeMillis) {
        float hue = Math.floorMod(timeMillis, CYCLE_MILLIS) / (float) CYCLE_MILLIS;
        return Color.HSBtoRGB(hue, 1.0F, 1.0F);
    }

    @Override
    public int calculate(ItemStack stack, @Nullable ClientLevel level, @Nullable LivingEntity entity) {
        return currentColor();
    }

    @Override
    public MapCodec<? extends ItemTintSource> type() {
        return CODEC;
    }
}
