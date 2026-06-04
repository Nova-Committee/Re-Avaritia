package com.avaritia.client.shader;

import com.avaritia.Const;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.neoforged.neoforge.client.event.RegisterRenderPipelinesEvent;

/**
 * Name: Avaritia-forge / AvaritiaShaders
 * Author: cnlimiter
 * CreateTime: 2023/9/18 1:37
 * Description:
 */

public class AvaritiaShaders {
    public static final float[] COSMIC_UVS = new float[40];
    public static TextureAtlasSprite[] COSMIC_SPRITES = new TextureAtlasSprite[10];
    public static final float[] ETERNAL_UVS = new float[40];
    public static TextureAtlasSprite[] ETERNAL_SPRITES = new TextureAtlasSprite[10];

    public static RenderPipeline COSMIC_SHADER;
    public static RenderPipeline COSMIC_ARMOR_SHADER;
    public static RenderPipeline HELL_SHADER;
    public static RenderPipeline ETERNAL_SHADER;
    public static RenderPipeline UNSTABLE_SHADER;

    public static ShaderUniform cosmicTime = new ShaderUniform();
    public static ShaderUniform cosmicYaw = new ShaderUniform();
    public static ShaderUniform cosmicPitch = new ShaderUniform();
    public static ShaderUniform cosmicExternalScale = new ShaderUniform();
    public static ShaderUniform cosmicOpacity = new ShaderUniform();
    public static ShaderUniform cosmicUVs = new ShaderUniform();

    public static ShaderUniform cosmicArmorTime = new ShaderUniform();
    public static ShaderUniform cosmicArmorYaw = new ShaderUniform();
    public static ShaderUniform cosmicArmorPitch = new ShaderUniform();
    public static ShaderUniform cosmicArmorExternalScale = new ShaderUniform();
    public static ShaderUniform cosmicArmorOpacity = new ShaderUniform();
    public static ShaderUniform cosmicArmorUVs = new ShaderUniform();

    public static ShaderUniform hellTime = new ShaderUniform();
    public static ShaderUniform hellYaw = new ShaderUniform();
    public static ShaderUniform hellPitch = new ShaderUniform();
    public static ShaderUniform hellExternalScale = new ShaderUniform();
    public static ShaderUniform hellOpacity = new ShaderUniform();
    public static ShaderUniform hellUVs = new ShaderUniform();

    public static ShaderUniform eternalTime = new ShaderUniform();
    public static ShaderUniform eternalYaw = new ShaderUniform();
    public static ShaderUniform eternalPitch = new ShaderUniform();
    public static ShaderUniform eternalExternalScale = new ShaderUniform();
    public static ShaderUniform eternalOpacity = new ShaderUniform();
    public static ShaderUniform eternalUVs = new ShaderUniform();

    public static ShaderUniform unstableTime = new ShaderUniform();
    public static ShaderUniform unstableYaw = new ShaderUniform();
    public static ShaderUniform unstablePitch = new ShaderUniform();
    public static ShaderUniform unstableExternalScale = new ShaderUniform();
    public static ShaderUniform unstableOpacity = new ShaderUniform();
    public static ShaderUniform unstableUVs = new ShaderUniform();

    public static void onRegisterShaders(RegisterRenderPipelinesEvent event) {
        COSMIC_SHADER = registerPipeline(event, "cosmic", DefaultVertexFormat.BLOCK, VertexFormat.Mode.QUADS);
        COSMIC_ARMOR_SHADER = registerPipeline(event, "cosmic_armor", DefaultVertexFormat.ENTITY, VertexFormat.Mode.QUADS);
        HELL_SHADER = registerPipeline(event, "hell", DefaultVertexFormat.BLOCK, VertexFormat.Mode.QUADS);
        ETERNAL_SHADER = registerPipeline(event, "eternal", DefaultVertexFormat.BLOCK, VertexFormat.Mode.QUADS);
        UNSTABLE_SHADER = registerPipeline(event, "unstable", DefaultVertexFormat.BLOCK, VertexFormat.Mode.QUADS);
    }

    private static RenderPipeline registerPipeline(RegisterRenderPipelinesEvent event, String name, VertexFormat vertexFormat, VertexFormat.Mode mode) {
        RenderPipeline pipeline = RenderPipeline.builder()
                .withLocation(Const.rl(name))
                .withVertexShader(Const.rl(name))
                .withFragmentShader(Const.rl(name))
                .withSampler("Sampler0")
                .withSampler("Sampler2")
                .withVertexFormat(vertexFormat, mode)
                .build();
        event.registerPipeline(pipeline);
        return pipeline;
    }

    /**
     * 兼容旧版 {@code AbstractUniform#set(...)} 调用的占位句柄。
     * <p>MC 26.1.2 已移除旧 ShaderInstance/AbstractUniform 管线，真实上传需要后续接入 UBO/管线修改器。</p>
     */
    public static class ShaderUniform {
        public void set(float value) {
        }

        public void set(long value) {
        }

        public void set(float[] values) {
        }
    }
}
