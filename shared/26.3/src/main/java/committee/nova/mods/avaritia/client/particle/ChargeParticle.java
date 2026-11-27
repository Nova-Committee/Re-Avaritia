package committee.nova.mods.avaritia.client.particle;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.SimpleAnimatedParticle;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.util.RandomSource;
import org.jetbrains.annotations.NotNull;

public class ChargeParticle extends SimpleAnimatedParticle {
    public ChargeParticle(ClientLevel world, double x, double y, double z, SpriteSet sprites) {
        super(world, x, y, z, sprites, 0.0F);
        this.lifetime = 8 + this.random.nextInt(4);
        this.setParticleSpeed(0D, 0D, 0D);
        this.scale(1.25F);
        this.setSpriteFromAge(sprites);
    }

    @Override
    public int getLightCoords(float partialTick) {
        return 15728880;
    }

    public record Factory(SpriteSet sprites) implements ParticleProvider<SimpleParticleType> {
        @Override
        public Particle createParticle(@NotNull SimpleParticleType simpleParticleType, @NotNull ClientLevel world, double x, double y, double z, double velX, double velY, double velZ, @NotNull RandomSource random) {
            return new ChargeParticle(world, x, y, z, sprites);
        }
    }
}
