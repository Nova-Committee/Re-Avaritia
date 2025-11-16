package committee.nova.mods.avaritia.init.registry;

import com.mojang.serialization.MapCodec;
import committee.nova.mods.avaritia.Const;
import committee.nova.mods.avaritia.client.particle.ShockwaveParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.neoforged.neoforge.registries.DeferredRegister;
import org.jetbrains.annotations.NotNull;

import java.util.function.Supplier;

/**
 * @author cnlimiter
 */
public class ModParticles {
    public static final DeferredRegister<ParticleType<?>> PARTICLE_TYPES = DeferredRegister.create(Registries.PARTICLE_TYPE, Const.MOD_ID);

    public static final Supplier<SimpleParticleType> CHARGE = PARTICLE_TYPES.register("charge", () -> new SimpleParticleType(false));


    public static final Supplier<ParticleType<ShockwaveParticleOptions>> SHOCKWAVE_PARTICLE = PARTICLE_TYPES.register("shockwave", () -> new ParticleType<>(false) {
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
