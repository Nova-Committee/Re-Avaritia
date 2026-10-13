package committee.nova.mods.avaritia.init.registry;

import com.mojang.serialization.MapCodec;
import committee.nova.mods.avaritia.Const;
import committee.nova.mods.avaritia.client.particle.ShockwaveParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import committee.nova.mods.avaritia.api.init.registry.RegistryEntries;
import net.minecraft.core.registries.BuiltInRegistries;
import committee.nova.mods.avaritia.api.init.registry.RegistryEntry;
import org.jetbrains.annotations.NotNull;

/**
 * @author cnlimiter
 */
public class ModParticles {
    public static final RegistryEntries<ParticleType<?>> PARTICLE_TYPE = RegistryEntries.create(BuiltInRegistries.PARTICLE_TYPE, Const.MOD_ID);

    public static final RegistryEntry<SimpleParticleType> CHARGE = PARTICLE_TYPE.register("charge", () -> new SimpleParticleType(false));


    public static final RegistryEntry<ParticleType<ShockwaveParticleOptions>> SHOCKWAVE_PARTICLE = PARTICLE_TYPE.register("shockwave",
            () -> new ParticleType<>(false) {
        @Override
        public @NotNull MapCodec<ShockwaveParticleOptions> codec() {
            return ShockwaveParticleOptions.CODEC;
        }

        @Override
        public @NotNull StreamCodec<? super RegistryFriendlyByteBuf, ShockwaveParticleOptions> streamCodec() {
            return ShockwaveParticleOptions.STREAM_CODEC;
        }
    });

}
