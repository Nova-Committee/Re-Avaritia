package committee.nova.mods.avaritia.init.registry.enums;

import net.minecraft.util.StringRepresentable;
import org.jetbrains.annotations.NotNull;

/**
 * @author: cnlimiter
 */
public enum CompressorTier implements StringRepresentable {
    DEFAULT("neutron_compressor", 1f, 1f, 1),
    DENSE("dense_neutron_compressor", 1/2f, 1f, 1),
    DENSER("denser_neutron_compressor", 1/4f, 3/4f, 1),
    DENSEST("densest_neutron_compressor", 1/8f, 1/2f, 2);

    public final String name;
    public final float timeAmplifier;
    public final float inputAmplifier;
    public final int outputAmplifier;

    CompressorTier(String name, float timeAmplifier, float inputAmplifier, int outputAmplifier) {
        this.name = name;
        this.timeAmplifier = timeAmplifier;
        this.inputAmplifier = inputAmplifier;
        this.outputAmplifier = outputAmplifier;
    }

    @Override
    public String toString() {
        return this.getSerializedName();
    }

    @Override
    public @NotNull String getSerializedName() {
        return this.name;
    }
}
