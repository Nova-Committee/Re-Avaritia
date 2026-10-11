package committee.nova.mods.avaritia.client.shader;

import committee.nova.mods.avaritia.Const;
import com.mojang.blaze3d.pipeline.BlendFunction;
import com.mojang.blaze3d.platform.DepthTestFunction;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.shaders.UniformType;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.neoforged.neoforge.client.event.RegisterRenderPipelinesEvent;

/**
 * Registers Avaritia's custom effect render pipelines.
 *
 * <p>Backported to 1.21.11: the 26.1 sources attached a {@code DepthStencilState}
 * ({@code CompareOp.LESS_THAN_OR_EQUAL}, depth write off) to each pipeline. 1.21.11
 * has no {@code DepthStencilState}; {@code RenderPipeline.Builder} expresses the same
 * state through {@link RenderPipeline.Builder#withDepthTestFunction} plus
 * {@link RenderPipeline.Builder#withDepthWrite}. Both 26.1 constants held identical
 * values, so they collapse into one pair of builder calls.
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
    public static RenderPipeline BLACK_HOLE_SHADER;

    public static void onRegisterShaders(RegisterRenderPipelinesEvent event) {
        COSMIC_SHADER = registerItemPipeline(event, "cosmic", DefaultVertexFormat.NEW_ENTITY, VertexFormat.Mode.QUADS);
        COSMIC_ARMOR_SHADER = registerPipeline(event, "cosmic_armor", "cosmic", DefaultVertexFormat.NEW_ENTITY, VertexFormat.Mode.QUADS);
        HELL_SHADER = registerItemPipeline(event, "hell", DefaultVertexFormat.NEW_ENTITY, VertexFormat.Mode.QUADS);
        ETERNAL_SHADER = registerItemPipeline(event, "eternal", DefaultVertexFormat.NEW_ENTITY, VertexFormat.Mode.QUADS);
        UNSTABLE_SHADER = registerItemPipeline(event, "unstable", DefaultVertexFormat.NEW_ENTITY, VertexFormat.Mode.QUADS);
        BLACK_HOLE_SHADER = registerPipeline(event, "black_hole", DefaultVertexFormat.NEW_ENTITY, VertexFormat.Mode.QUADS);
        AvaritiaRenderTypes.reloadEffectTypes();
    }

    private static RenderPipeline registerPipeline(RegisterRenderPipelinesEvent event, String name, VertexFormat vertexFormat, VertexFormat.Mode mode) {
        return registerPipeline(event, name, name, vertexFormat, mode);
    }

    private static RenderPipeline registerItemPipeline(RegisterRenderPipelinesEvent event, String name, VertexFormat vertexFormat, VertexFormat.Mode mode) {
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
                .withBlend(BlendFunction.TRANSLUCENT)
                .withDepthTestFunction(DepthTestFunction.LEQUAL_DEPTH_TEST)
                .withDepthWrite(false)
                .withCull(false)
                .withVertexFormat(vertexFormat, mode)
                .build();
        event.registerPipeline(pipeline);
        return pipeline;
    }
}
