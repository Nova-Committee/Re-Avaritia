package committee.nova.mods.avaritia.client.shader;

import com.mojang.blaze3d.buffers.GpuBufferSlice;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.systems.RenderPass;
import net.minecraft.client.renderer.DynamicUniformStorage;
import net.minecraft.client.renderer.rendertype.RenderType;
import org.jetbrains.annotations.Nullable;

import java.nio.ByteBuffer;
import java.util.EnumMap;
import java.util.IdentityHashMap;
import java.util.Map;

public final class AvaritiaShaderUniforms {
    public static final String UNIFORM_NAME = "AvaritiaCosmic";

    private static final int UV_COUNT = 10;
    private static final int UV_COMPONENTS = UV_COUNT * 4;
    private static final int UBO_SIZE = (2 + UV_COUNT) * 16;

    private static final Map<Effect, GpuBufferSlice> CURRENT_SLICES = new EnumMap<>(Effect.class);
    private static final Map<RenderType, GpuBufferSlice> RENDER_TYPE_SLICES = new IdentityHashMap<>();

    private static @Nullable DynamicUniformStorage<CosmicUniform> storage;
    private static @Nullable RenderPipeline activePipeline;
    private static @Nullable RenderType activeRenderType;

    private AvaritiaShaderUniforms() {
    }

    public static void set(Effect effect, float time, float yaw, float pitch, float externalScale, float opacity, float[] uvs) {
        CURRENT_SLICES.put(effect, storage().writeUniform(new CosmicUniform(
                time, yaw, pitch, externalScale, opacity, effect.substrateAlpha(), copyUvs(uvs)
        )));
    }

    public static void set(RenderType renderType, Effect effect, float time, float yaw, float pitch, float externalScale, float opacity, float[] uvs) {
        GpuBufferSlice slice = storage().writeUniform(new CosmicUniform(
                time, yaw, pitch, externalScale, opacity, effect.substrateAlpha(), copyUvs(uvs)
        ));
        CURRENT_SLICES.put(effect, slice);
        RENDER_TYPE_SLICES.put(renderType, slice);
    }

    public static void setActivePipeline(RenderPipeline pipeline) {
        activePipeline = pipeline;
    }

    public static void setActiveRenderType(RenderType renderType) {
        activeRenderType = renderType;
    }

    public static void clearActiveRenderType(RenderType renderType) {
        if (activeRenderType == renderType) {
            activeRenderType = null;
        }
    }

    public static void bindIfAvaritia(RenderPass renderPass) {
        Effect effect = Effect.fromPipeline(activePipeline);
        if (effect == null) {
            return;
        }

        GpuBufferSlice slice = activeRenderType != null ? RENDER_TYPE_SLICES.get(activeRenderType) : null;
        if (slice == null) {
            slice = CURRENT_SLICES.get(effect);
        }
        if (slice != null) {
            renderPass.setUniform(UNIFORM_NAME, slice);
        }
    }

    public static void endFrame() {
        CURRENT_SLICES.clear();
        RENDER_TYPE_SLICES.clear();
        activePipeline = null;
        activeRenderType = null;
        if (storage != null) {
            storage.endFrame();
        }
    }

    private static DynamicUniformStorage<CosmicUniform> storage() {
        if (storage == null) {
            storage = new DynamicUniformStorage<>("Avaritia cosmic uniforms", UBO_SIZE, 32);
        }
        return storage;
    }

    private static float[] copyUvs(float[] uvs) {
        float[] copy = new float[UV_COMPONENTS];
        if (uvs != null) {
            System.arraycopy(uvs, 0, copy, 0, Math.min(uvs.length, copy.length));
        }
        return copy;
    }

    public enum Effect {
        COSMIC(0.0F),
        COSMIC_ARMOR(1.0F),
        HELL(0.0F),
        ETERNAL(0.0F),
        UNSTABLE(0.0F),
        BLACK_HOLE(0.0F);

        private final float substrateAlpha;

        Effect(float substrateAlpha) {
            this.substrateAlpha = substrateAlpha;
        }

        float substrateAlpha() {
            return this.substrateAlpha;
        }

        private static @Nullable Effect fromPipeline(@Nullable RenderPipeline pipeline) {
            if (pipeline == AvaritiaShaders.COSMIC_SHADER) {
                return COSMIC;
            }
            if (pipeline == AvaritiaShaders.COSMIC_ARMOR_SHADER) {
                return COSMIC_ARMOR;
            }
            if (pipeline == AvaritiaShaders.HELL_SHADER) {
                return HELL;
            }
            if (pipeline == AvaritiaShaders.ETERNAL_SHADER) {
                return ETERNAL;
            }
            if (pipeline == AvaritiaShaders.UNSTABLE_SHADER) {
                return UNSTABLE;
            }
            if (pipeline == AvaritiaShaders.BLACK_HOLE_SHADER) {
                return BLACK_HOLE;
            }
            return null;
        }
    }

    private record CosmicUniform(float time, float yaw, float pitch, float externalScale, float opacity,
                                 float substrateAlpha, float[] uvs)
            implements DynamicUniformStorage.DynamicUniform {
        @Override
        public void write(ByteBuffer buffer) {
            buffer.putFloat(this.time);
            buffer.putFloat(this.yaw);
            buffer.putFloat(this.pitch);
            buffer.putFloat(this.externalScale);
            buffer.putFloat(this.opacity);
            buffer.putFloat(this.substrateAlpha);
            buffer.putFloat(0.0F);
            buffer.putFloat(0.0F);

            for (int i = 0; i < UV_COUNT; i++) {
                int index = i * 4;
                buffer.putFloat(this.uvs[index]);
                buffer.putFloat(this.uvs[index + 1]);
                buffer.putFloat(this.uvs[index + 2]);
                buffer.putFloat(this.uvs[index + 3]);
            }
        }
    }
}
