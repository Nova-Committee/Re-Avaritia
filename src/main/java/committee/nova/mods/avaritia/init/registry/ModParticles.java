package committee.nova.mods.avaritia.init.registry;

import com.mojang.serialization.Codec;
import committee.nova.mods.avaritia.Const;
import committee.nova.mods.avaritia.client.particle.ShockwaveParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import org.jetbrains.annotations.NotNull;

/**
 * @author cnlimiter
 */
public class ModParticles {
    public static final DeferredRegister<ParticleType<?>> PARTICLE_TYPE = DeferredRegister.create(ForgeRegistries.PARTICLE_TYPES, Const.MOD_ID);

    public static final RegistryObject<SimpleParticleType> CHARGE = PARTICLE_TYPE.register("charge", () -> new SimpleParticleType(false));


    public static final RegistryObject<ParticleType<ShockwaveParticleOptions>> SHOCKWAVE_PARTICLE = PARTICLE_TYPE.register("shockwave",
            () -> new ParticleType<>(false, ShockwaveParticleOptions.DESERIALIZER) {
        public @NotNull Codec<ShockwaveParticleOptions> codec() {
            return ShockwaveParticleOptions.CODEC;
        }
    });

}
