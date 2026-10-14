package committee.nova.mods.avaritia.client.shader;

import com.mojang.renderpearl.api.pipeline.BindGroupLayout;
import com.mojang.renderpearl.api.pipeline.PrimitiveTopology;
import committee.nova.mods.avaritia.Const;
import com.mojang.renderpearl.api.pipeline.BlendFunction;
import com.mojang.renderpearl.api.pipeline.ColorTargetState;
import com.mojang.renderpearl.api.pipeline.DepthStencilState;
import com.mojang.renderpearl.api.pipeline.RenderPipeline;
import com.mojang.renderpearl.api.pipeline.CompareOp;
import com.mojang.renderpearl.api.pipeline.UniformType;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.renderpearl.api.vertex.VertexFormat;
import net.minecraft.client.renderer.BindGroupLayouts;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;

/**
 * Registers Avaritia's custom effect render pipelines (26.3 RenderPipeline system).
 * <p>26.3 Fabric：管线经 {@link RenderPipelines#register(RenderPipeline)} 注册
 * （fabric-transitive-access-wideners 已拓宽），替代 Neo 的 RegisterRenderPipelinesEvent。</p>
 */
public class AvaritiaShaders {
    // 26.3 RenderPearl 使用 reversed-Z 深度（近处片元深度值更大，DepthStencilState.DEFAULT 即
    // GREATER_THAN_OR_EQUAL）。必须用 GEQUAL：LEQUAL 只在已有相等/更近深度时通过——GUI 与有基础
    // 模型的手持物品恰好满足（基础层先写同平面深度），但手持三叉戟跳过 wrapped.update 后遮罩平面
    // 无基础深度，"Item in hand" pass 深度清为 0.0（远平面），LEQUAL 全部丢弃导致星空不可见。
    private static final DepthStencilState TRANSLUCENT_EFFECT_DEPTH =
            new DepthStencilState(CompareOp.GREATER_THAN_OR_EQUAL, false);
    static final DepthStencilState ITEM_EFFECT_OVERLAY_DEPTH =
            new DepthStencilState(CompareOp.GREATER_THAN_OR_EQUAL, false);

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

    public static void register() {
        COSMIC_SHADER = registerItemPipeline("cosmic", DefaultVertexFormat.ENTITY, PrimitiveTopology.QUADS);
        COSMIC_ARMOR_SHADER = registerPipeline("cosmic_armor", "cosmic", DefaultVertexFormat.ENTITY, PrimitiveTopology.QUADS);
        HELL_SHADER = registerItemPipeline("hell", DefaultVertexFormat.ENTITY, PrimitiveTopology.QUADS);
        ETERNAL_SHADER = registerItemPipeline("eternal", DefaultVertexFormat.ENTITY, PrimitiveTopology.QUADS);
        UNSTABLE_SHADER = registerItemPipeline("unstable", DefaultVertexFormat.ENTITY, PrimitiveTopology.QUADS);
        BLACK_HOLE_SHADER = registerPipeline("black_hole", DefaultVertexFormat.ENTITY, PrimitiveTopology.QUADS);
        AvaritiaRenderTypes.reloadEffectTypes();
    }

    private static RenderPipeline registerPipeline(String name, VertexFormat vertexFormat, PrimitiveTopology mode) {
        return registerPipeline(name, name, vertexFormat, mode, TRANSLUCENT_EFFECT_DEPTH);
    }

    private static RenderPipeline registerItemPipeline(String name, VertexFormat vertexFormat, PrimitiveTopology mode) {
        return registerPipeline(name, name, vertexFormat, mode, ITEM_EFFECT_OVERLAY_DEPTH);
    }

    private static RenderPipeline registerPipeline(String name, String shaderName, VertexFormat vertexFormat, PrimitiveTopology mode) {
        return registerPipeline(name, shaderName, vertexFormat, mode, TRANSLUCENT_EFFECT_DEPTH);
    }

    private static RenderPipeline registerPipeline(String name, String shaderName,
                                                   VertexFormat vertexFormat, PrimitiveTopology mode,
                                                   DepthStencilState depthStencilState) {
        var shader = Const.rl("core/" + shaderName);
        RenderPipeline pipeline = RenderPipeline.builder(RenderPipelines.ENTITY_SNIPPET)
                .withLocation(Const.rl(name))
                .withVertexShader(shader)
                .withFragmentShader(shader)
                // 26.3 起采样器与自定义 uniform 通过 BindGroupLayout 声明（ENTITY_SNIPPET 已含 Sampler0/Sampler2，这里显式补齐以免依赖父片段）。
                .withBindGroupLayout(BindGroupLayouts.SAMPLER2)
                .withBindGroupLayout(BindGroupLayout.builder()
                        .withUniform(AvaritiaShaderUniforms.UNIFORM_NAME, UniformType.UNIFORM_BUFFER)
                        .build())
                .withColorTargetState(new ColorTargetState(BlendFunction.TRANSLUCENT))
                .withDepthStencilState(depthStencilState)
                .withCull(false)
                .withVertexBinding(0, vertexFormat)
                .withPrimitiveTopology(mode)
                .build();
        RenderPipelines.register(pipeline);
        return pipeline;
    }
}
