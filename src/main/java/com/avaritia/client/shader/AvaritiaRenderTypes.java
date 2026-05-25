package com.avaritia.client.shader;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.avaritia.Avaritia;
import com.avaritia.Res;
import com.avaritia.api.utils.RenderUtils;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.Identifier;

import static net.minecraft.client.renderer.RenderStateShard.*;

/**
 * @Project: Avaritia
 * @Author: cnlimiter
 * @CreateTime: 2025/5/21 00:31
 * @Description:
 */
public class AvaritiaRenderTypes {
    public static RenderType VOID = RenderType.create(
            Avaritia.rl("void_hemisphere").toString(),
            DefaultVertexFormat.NEW_ENTITY, VertexFormat.Mode.TRIANGLES, 256,
            RenderType.CompositeState.builder()
                    .setShaderState(RENDERTYPE_ENTITY_SHADOW_SHADER)
                    .setTextureState(new RenderStateShard.TextureStateShard(Res.VOID, false, false))
                    .setCullState(RenderType.NO_CULL)
                    .createCompositeState(false));

    public static RenderType VOID_HALO = RenderType.create(
            Avaritia.rl("void_halo").toString(),
            DefaultVertexFormat.POSITION_TEX_COLOR, VertexFormat.Mode.QUADS, 256,
            RenderType.CompositeState.builder()
                    .setShaderState(POSITION_COLOR_TEX_LIGHTMAP_SHADER)
                    .setTextureState(new RenderStateShard.TextureStateShard(Res.VOID_HALO, false, false))
                    .setTransparencyState(RenderType.TRANSLUCENT_TRANSPARENCY)
                    .setWriteMaskState(RenderType.COLOR_WRITE)
                    .createCompositeState(false));

    public static RenderType COSMIC = RenderType.create(
            Avaritia.rl("cosmic").toString(), DefaultVertexFormat.NEW_ENTITY,
            VertexFormat.Mode.QUADS, 2097152, true, false,
            RenderType.CompositeState.builder()
                    .setShaderState(new RenderStateShard.ShaderStateShard(() -> AvaritiaShaders.COSMIC_SHADER))
                    .setDepthTestState(RenderStateShard.LEQUAL_DEPTH_TEST)
                    .setLightmapState(RenderStateShard.LIGHTMAP)
                    .setWriteMaskState(RenderStateShard.COLOR_WRITE)
                    .setTransparencyState(RenderStateShard.TRANSLUCENT_TRANSPARENCY)
                    .setTextureState(RenderUtils.COSMIC_TEXTURE_ISOLATED)
                    .setLayeringState(RenderUtils.POLYGON_OFFSET_LAYERING)
                    .createCompositeState(true)
    );

    public static final RenderType COSMIC_ARMOR = RenderType.create(
            Avaritia.rl("cosmic").toString(), DefaultVertexFormat.NEW_ENTITY,
            VertexFormat.Mode.QUADS, 2097152, true, false,
            RenderType.CompositeState.builder()
                    .setShaderState(new RenderStateShard.ShaderStateShard(() -> AvaritiaShaders.COSMIC_ARMOR_SHADER))
                    .setDepthTestState(RenderStateShard.LEQUAL_DEPTH_TEST)
                    .setLightmapState(RenderStateShard.LIGHTMAP)
                    .setTransparencyState(RenderStateShard.TRANSLUCENT_TRANSPARENCY)
                    .setWriteMaskState(RenderStateShard.COLOR_WRITE)
                    .setCullState(RenderStateShard.NO_CULL)
                    .setLayeringState(RenderType.VIEW_OFFSET_Z_LAYERING)
                    .setTextureState(RenderStateShard.BLOCK_SHEET)
                    .createCompositeState(true));

    public static final RenderType BLADE_SLASH = RenderType.create("blade_slash",
            DefaultVertexFormat.NEW_ENTITY, VertexFormat.Mode.QUADS, 256, true, true,
            RenderType.CompositeState.builder().setTextureState(new RenderStateShard.TextureStateShard(Res.BLADE_SLASH, false, false))
                    .setShaderState(RENDERTYPE_TEXT_SEE_THROUGH_SHADER)
                    .setTransparencyState(TRANSLUCENT_TRANSPARENCY)
                    .setCullState(NO_CULL)
                    .createCompositeState(true));

    public static RenderType Glow(Identifier Identifier) {
        return RenderType.create(Avaritia.rl("glow").toString(),
                DefaultVertexFormat.NEW_ENTITY, VertexFormat.Mode.QUADS, 2097152, true, false,
                RenderType.CompositeState.builder().setShaderState(RenderStateShard.POSITION_COLOR_TEX_LIGHTMAP_SHADER)
                        .setDepthTestState(RenderStateShard.LEQUAL_DEPTH_TEST)
                        .setTransparencyState(RenderStateShard.TRANSLUCENT_TRANSPARENCY)
                        .setCullState(RenderStateShard.NO_CULL)
                        .setLayeringState(RenderStateShard.VIEW_OFFSET_Z_LAYERING)
                        .setTextureState(new RenderStateShard.TextureStateShard(Identifier, false, false))
                        .createCompositeState(true));
    }

    public static RenderType WingGlow(Identifier Identifier) {
        return RenderType.create(Avaritia.rl("glow").toString(),
                DefaultVertexFormat.NEW_ENTITY, VertexFormat.Mode.QUADS, 2097152, true, false,
                RenderType.CompositeState.builder().setShaderState(RenderStateShard.POSITION_COLOR_TEX_LIGHTMAP_SHADER)
                        .setDepthTestState(RenderStateShard.LEQUAL_DEPTH_TEST)
                        .setTransparencyState(RenderStateShard.LIGHTNING_TRANSPARENCY)
                        .setCullState(RenderStateShard.NO_CULL)
                        .setLayeringState(RenderStateShard.VIEW_OFFSET_Z_LAYERING)
                        .setTextureState(new RenderStateShard.TextureStateShard(Identifier, false, false))
                        .createCompositeState(true));
    }

    public static RenderType HELL = RenderType.create(
            Avaritia.rl("hell").toString(), DefaultVertexFormat.NEW_ENTITY,
            VertexFormat.Mode.QUADS, 2097152, true, false,
            RenderType.CompositeState.builder()
                    .setShaderState(new RenderStateShard.ShaderStateShard(() -> AvaritiaShaders.HELL_SHADER))
                    .setDepthTestState(RenderStateShard.LEQUAL_DEPTH_TEST)
                    .setLightmapState(RenderStateShard.LIGHTMAP)
                    .setWriteMaskState(RenderStateShard.COLOR_WRITE)
                    .setTransparencyState(RenderStateShard.TRANSLUCENT_TRANSPARENCY)
                    .setTextureState(RenderUtils.COSMIC_TEXTURE_ISOLATED)
                    .setLayeringState(RenderUtils.POLYGON_OFFSET_LAYERING)
                    .createCompositeState(true)
    );

    public static RenderType ETERNAL = RenderType.create(
            Avaritia.rl("eternal").toString(), DefaultVertexFormat.NEW_ENTITY,
            VertexFormat.Mode.QUADS, 2097152, true, false,
            RenderType.CompositeState.builder()
                    .setShaderState(new RenderStateShard.ShaderStateShard(() -> AvaritiaShaders.ETERNAL_SHADER))
                    .setDepthTestState(RenderStateShard.LEQUAL_DEPTH_TEST)
                    .setLightmapState(RenderStateShard.LIGHTMAP)
                    .setWriteMaskState(RenderStateShard.COLOR_WRITE)
                    .setTransparencyState(RenderStateShard.TRANSLUCENT_TRANSPARENCY)
                    .setTextureState(RenderUtils.COSMIC_TEXTURE_ISOLATED)
                    .setLayeringState(RenderUtils.POLYGON_OFFSET_LAYERING)
                    .createCompositeState(true)
    );

    public static RenderType UNSTABLE = RenderType.create(
            Avaritia.rl("unstable").toString(), DefaultVertexFormat.NEW_ENTITY,
            VertexFormat.Mode.QUADS, 2097152, true, false,
            RenderType.CompositeState.builder()
                    .setShaderState(new RenderStateShard.ShaderStateShard(() -> AvaritiaShaders.UNSTABLE_SHADER))
                    .setDepthTestState(RenderStateShard.LEQUAL_DEPTH_TEST)
                    .setLightmapState(RenderStateShard.LIGHTMAP)
                    .setWriteMaskState(RenderStateShard.COLOR_WRITE)
                    .setTransparencyState(RenderStateShard.TRANSLUCENT_TRANSPARENCY)
                    .setTextureState(RenderUtils.COSMIC_TEXTURE_ISOLATED)
                    .setLayeringState(RenderUtils.POLYGON_OFFSET_LAYERING)
                    .createCompositeState(true)
    );

    public static RenderType TRIDENT = RenderType.create(
            Avaritia.rl("infinity_trident").toString(),
            DefaultVertexFormat.NEW_ENTITY,
            VertexFormat.Mode.TRIANGLES,
            256,
            RenderType.CompositeState.builder()
                    .setShaderState(RENDERTYPE_ENTITY_SHADOW_SHADER)
                    .setTextureState(new RenderStateShard.TextureStateShard(Res.TRIDENT_TEX, false, false))
                    .setLightmapState(LIGHTMAP)
                    .setOverlayState(OVERLAY)
                    .setCullState(NO_CULL)
                    .setTransparencyState(NO_TRANSPARENCY)
                    .createCompositeState(true));

}
