package committee.nova.mods.avaritia.client.particle;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import committee.nova.mods.avaritia.init.registry.ModParticles;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.ExtraCodecs;
import org.jetbrains.annotations.NotNull;
import org.joml.Vector3f;

import java.util.Optional;

/**
 * @author cnlimiter
 */
public class ShockwaveParticleOptions implements ParticleOptions {
    protected final boolean fullbright;
    private final float scale;
    private final Vector3f color;
    protected final String trailParticleRaw;

    public ShockwaveParticleOptions(Vector3f color, float scale, boolean glowing, String trailParticle) {
        this.scale = scale;
        this.color = color;
        this.fullbright = glowing;
        this.trailParticleRaw = trailParticle;
    }

    public float getScale() {
        return this.scale;
    }

    public boolean isFullbright() {
        return this.fullbright;
    }


    public Vector3f color() {
        return color;
    }

    public Optional<ParticleOptions> trailParticle() {
        try {
            var type = BuiltInRegistries.PARTICLE_TYPE.get(ResourceLocation.tryParse(ShockwaveParticleOptions.this.trailParticleRaw));
            if (type instanceof ParticleOptions particleOptions) {
                return Optional.of(particleOptions);
            }
        } catch (Exception ignored) {

        }
        return Optional.empty();
    }

    public static final MapCodec<ShockwaveParticleOptions> CODEC = RecordCodecBuilder.mapCodec((optionsInstance) ->
            optionsInstance.group(ExtraCodecs.VECTOR3F.fieldOf("color").forGetter((option) -> option.color),
                    Codec.FLOAT.fieldOf("scale").forGetter((option) -> option.scale),
                    Codec.BOOL.fieldOf("fullbright").forGetter((option) -> option.fullbright),
                    Codec.STRING.fieldOf("particle").forGetter((option) -> option.trailParticleRaw)
            ).apply(optionsInstance, ShockwaveParticleOptions::new));

    public static StreamCodec<? super FriendlyByteBuf, ShockwaveParticleOptions> STREAM_CODEC = StreamCodec.of(
            (buf, option) -> {
                buf.writeFloat(option.color.x);
                buf.writeFloat(option.color.y);
                buf.writeFloat(option.color.z);
                buf.writeFloat(option.scale);
                buf.writeBoolean(option.fullbright);
                buf.writeUtf(option.trailParticleRaw);
            },
            (buf) -> {
                return new ShockwaveParticleOptions(new Vector3f(buf.readFloat(), buf.readFloat(), buf.readFloat()), buf.readFloat(), buf.readBoolean(), buf.readUtf());
            }
    );

    public @NotNull ParticleType<ShockwaveParticleOptions> getType() {
        return ModParticles.SHOCKWAVE_PARTICLE.get();
    }
}