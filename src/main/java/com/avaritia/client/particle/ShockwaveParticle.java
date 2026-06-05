package com.avaritia.client.particle;

import com.mojang.math.Axis;
import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.SingleQuadParticle;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.state.level.QuadParticleRenderState;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.util.ARGB;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.util.Util;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.util.Optional;
import java.util.function.Consumer;

public class ShockwaveParticle extends SingleQuadParticle {
    private static final Vector3f ROTATION_VECTOR = Util.make(new Vector3f(0.5F, 0.5F, 0.5F), Vector3f::normalize);
    private static final float DEGREES_90 = Mth.PI / 2f;
    static final int MAX_PARTICLES = 30;

    private final float targetSize;
    private final boolean isFullbright;
    private final Optional<ParticleOptions> trailParticle;

    ShockwaveParticle(ClientLevel pLevel, double pX, double pY, double pZ, double xd, double yd, double zd, ShockwaveParticleOptions options, SpriteSet sprites, RandomSource random) {
        super(pLevel, pX, pY, pZ, 0, 0, 0, sprites.get(random));

        this.xd = xd;
        this.yd = yd;
        this.zd = zd;

        this.targetSize = options.getScale();
        this.quadSize = 0;
        this.lifetime = targetSize < 0 ? (int) (targetSize * -20) : (int) (Math.abs(targetSize) * 2.5);
        this.gravity = .1f;

        float f = random.nextFloat() * 0.14F + 0.85F;
        this.rCol = options.color().x() * f;
        this.gCol = options.color().y() * f;
        this.bCol = options.color().z() * f;
        this.friction = 1;
        this.isFullbright = options.isFullbright();
        this.trailParticle = options.trailParticle();
    }

    @Override
    public float getQuadSize(float partialTick) {
        var f = (partialTick + this.age) / (float) this.lifetime;
        if (targetSize < 0) {
            return Mth.lerp((1 - f) * (1 - f), 0, -targetSize);
        } else {
            return Mth.lerp(1 - (1 - f) * (1 - f), 0, targetSize);
        }
    }

    @Override
    public void tick() {
        this.xo = this.x;
        this.yo = this.y;
        this.zo = this.z;
        if (this.age++ >= this.lifetime) {
            this.remove();
        } else {
            this.move(this.xd, this.yd, this.zd);
            this.yd *= .85f;
            this.xd *= .94f;
            this.zd *= .94f;
            if (this.trailParticle.isPresent()) {
                float radius = getQuadSize(1);
                float circumference = radius * 2 * Mth.PI;
                int particles = (int) Mth.clamp(circumference / 5, 5, MAX_PARTICLES);
                float degreesPerParticle = 360f / particles;
                for (int i = 0; i < particles; i++) {
                    float f = degreesPerParticle * i + this.random.nextInt((int) degreesPerParticle);
                    float x = Mth.cos(f * Mth.DEG_TO_RAD) * radius;
                    float z = Mth.sin(f * Mth.DEG_TO_RAD) * radius;
                    this.level.addParticle(trailParticle.get(), this.x + x, this.y, this.z + z, 0, .05, 0);
                }
            }
        }
    }

    @Override
    public void extract(QuadParticleRenderState renderState, Camera camera, float partialticks) {
        this.alpha = 1.0F - Mth.clamp((this.age + partialticks) / (float) this.lifetime, 0, 1F);
        this.extractRotatedParticle(renderState, camera, partialticks, (p_234005_) -> {
            p_234005_.mul(Axis.YP.rotation(0));
            p_234005_.mul(Axis.XP.rotation(-DEGREES_90));
        });
        this.extractRotatedParticle(renderState, camera, partialticks, (p_234000_) -> {
            p_234000_.mul(Axis.YP.rotation(-(float) Math.PI));
            p_234000_.mul(Axis.XP.rotation(DEGREES_90));
        });
    }

    private void extractRotatedParticle(QuadParticleRenderState renderState, Camera camera, float partialTick, Consumer<Quaternionf> pQuaternion) {
        Vec3 vec3 = camera.position();
        float f = (float) (Mth.lerp(partialTick, this.xo, this.x) - vec3.x());
        float f1 = (float) (Mth.lerp(partialTick, this.yo, this.y) - vec3.y()) + .08f;
        float f2 = (float) (Mth.lerp(partialTick, this.zo, this.z) - vec3.z());
        Quaternionf quaternion = (new Quaternionf()).setAngleAxis(0.0F, ROTATION_VECTOR.x(), ROTATION_VECTOR.y(), ROTATION_VECTOR.z());

        pQuaternion.accept(quaternion);
        renderState.add(this.getLayer(), f, f1, f2, quaternion.x, quaternion.y, quaternion.z, quaternion.w, this.getQuadSize(partialTick), this.getU0(), this.getU1(), this.getV0(), this.getV1(), ARGB.colorFromFloat(this.alpha, this.rCol, this.gCol, this.bCol), this.getLightCoords(partialTick));
    }

    @Override
    protected @NotNull Layer getLayer() {
        return Layer.TRANSLUCENT;
    }

    @Override
    protected int getLightCoords(float pPartialTick) {
        if (isFullbright) {
            return 15728880;
        }
        BlockPos blockpos = BlockPos.containing(this.x, this.y, this.z).above();
        return this.level.hasChunkAt(blockpos) ? LevelRenderer.getLightCoords(this.level, blockpos) : 15728640;
    }

    public static class Provider implements ParticleProvider<ShockwaveParticleOptions> {
        private final SpriteSet sprite;

        public Provider(SpriteSet pSprite) {
            this.sprite = pSprite;
        }

        @Override
        public Particle createParticle(@NotNull ShockwaveParticleOptions options, @NotNull ClientLevel pLevel, double pX, double pY, double pZ, double pXSpeed, double pYSpeed, double pZSpeed, @NotNull RandomSource random) {
            ShockwaveParticle shockwaveParticle = new ShockwaveParticle(pLevel, pX, pY, pZ, pXSpeed, pYSpeed, pZSpeed, options, this.sprite, random);
            shockwaveParticle.setAlpha(1.0F);
            return shockwaveParticle;
        }
    }
}
