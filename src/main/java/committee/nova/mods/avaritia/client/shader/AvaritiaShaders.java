package committee.nova.mods.avaritia.client.shader;

import committee.nova.mods.avaritia.Const;
import com.mojang.blaze3d.pipeline.BlendFunction;
import com.mojang.blaze3d.pipeline.ColorTargetState;
import com.mojang.blaze3d.pipeline.DepthStencilState;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.platform.CompareOp;
import com.mojang.blaze3d.shaders.UniformType;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.neoforged.neoforge.client.event.RegisterRenderPipelinesEvent;

/**
 * Registers Avaritia's custom effect render pipelines.
 */
public class AvaritiaShaders {
    private static final DepthStencilState EFFECT_OVERLAY_DEPTH =
            new DepthStencilState(CompareOp.LESS_THAN_OR_EQUAL, false);

    public static final float[] COSMIC_UVS = new float[40];
    public static TextureAtlasSprite[] COSMIC_SPRITES = new TextureAtlasSprite[10];
    public static final float[] ETERNAL_UVS = new float[40];
    public static TextureAtlasSprite[] ETERNAL_SPRITES = new TextureAtlasSprite[10];

    public static RenderPipeline COSMIC_SHADER;
    public static RenderPipeline COSMIC_ARMOR_SHADER;
    public static RenderPipeline HELL_SHADER;
    public static RenderPipeline ETERNAL_SHADER;
    public static RenderPipeline UNSTABLE_SHADER;
    public static RenderPipeline BLACK_HOLE_SHADER;

    public static void onRegisterShaders(RegisterRenderPipelinesEvent event) {
        COSMIC_SHADER = registerPipeline(event, "cosmic", DefaultVertexFormat.ENTITY, VertexFormat.Mode.QUADS);
        COSMIC_ARMOR_SHADER = registerPipeline(event, "cosmic_armor", "cosmic", DefaultVertexFormat.ENTITY, VertexFormat.Mode.QUADS);
        HELL_SHADER = registerPipeline(event, "hell", DefaultVertexFormat.ENTITY, VertexFormat.Mode.QUADS);
        ETERNAL_SHADER = registerPipeline(event, "eternal", DefaultVertexFormat.ENTITY, VertexFormat.Mode.QUADS);
        UNSTABLE_SHADER = registerPipeline(event, "unstable", DefaultVertexFormat.ENTITY, VertexFormat.Mode.QUADS);
        BLACK_HOLE_SHADER = registerPipeline(event, "black_hole", DefaultVertexFormat.ENTITY, VertexFormat.Mode.QUADS);
        AvaritiaRenderTypes.reloadEffectTypes();
    }

    private static RenderPipeline registerPipeline(RegisterRenderPipelinesEvent event, String name, VertexFormat vertexFormat, VertexFormat.Mode mode) {
        return registerPipeline(event, name, name, vertexFormat, mode);
    }

    private static RenderPipeline registerPipeline(RegisterRenderPipelinesEvent event, String name, String shaderName, VertexFormat vertexFormat, VertexFormat.Mode mode) {
        var shader = Const.rl("core/" + shaderName);
        RenderPipeline pipeline = RenderPipeline.builder(RenderPipelines.ENTITY_SNIPPET)
                .withLocation(Const.rl(name))
                .withVertexShader(shader)
                .withFragmentShader(shader)
                .withSampler("Sampler2")
                .withUniform(AvaritiaShaderUniforms.UNIFORM_NAME, UniformType.UNIFORM_BUFFER)
                .withColorTargetState(new ColorTargetState(BlendFunction.TRANSLUCENT))
                .withDepthStencilState(EFFECT_OVERLAY_DEPTH)
                .withCull(false)
                .withVertexFormat(vertexFormat, mode)
                .build();
        event.registerPipeline(pipeline);
        return pipeline;
    }
}
