package committee.nova.mods.avaritia.util;

import net.minecraft.client.renderer.RenderStateShard;

public final class AccessUtils {
    public static final RenderStateShard.DepthTestStateShard EQUAL_DEPTH_TEST = RenderStateShardAccess.EQUAL_DEPTH_TEST;
    public static final RenderStateShard.TransparencyStateShard TRANSLUCENT_TRANSPARENCY = RenderStateShardAccess.TRANSLUCENT_TRANSPARENCY;
    public static final RenderStateShard.TextureStateShard BLOCK_SHEET_MIPPED = RenderStateShardAccess.BLOCK_SHEET_MIPPED;
    public static final RenderStateShard.LightmapStateShard LIGHT_MAP = RenderStateShardAccess.LIGHT_MAP;
    public static final RenderStateShard.ShaderStateShard RENDERTYPE_ENTITY_SHADOW_SHADER = RenderStateShardAccess.RENDERTYPE_ENTITY_SHADOW_SHADER;
    public static final RenderStateShard.ShaderStateShard POSITION_COLOR_TEX_SHADER = RenderStateShardAccess.POSITION_COLOR_TEX_SHADER;
    public static final RenderStateShard.CullStateShard NO_CULL = RenderStateShardAccess.NO_CULL;
    public static final RenderStateShard.WriteMaskStateShard COLOR_WRITE = RenderStateShardAccess.COLOR_WRITE;
    public static final RenderStateShard.LayeringStateShard VIEW_OFFSET_Z_LAYERING = RenderStateShardAccess.VIEW_OFFSET_Z_LAYERING;
    public static final RenderStateShard.ShaderStateShard POSITION_COLOR_TEX_LIGHTMAP_SHADER = RenderStateShardAccess.POSITION_COLOR_TEX_LIGHTMAP_SHADER;
    public static final RenderStateShard.TransparencyStateShard LIGHTNING_TRANSPARENCY = RenderStateShardAccess.LIGHTNING_TRANSPARENCY;
    public static final RenderStateShard.OverlayStateShard OVERLAY = RenderStateShardAccess.OVERLAY;

    private static final class RenderStateShardAccess extends RenderStateShard {
        private static final LightmapStateShard LIGHT_MAP = RenderStateShard.LIGHTMAP;
        private static final DepthTestStateShard EQUAL_DEPTH_TEST = RenderStateShard.EQUAL_DEPTH_TEST;
        private static final TransparencyStateShard TRANSLUCENT_TRANSPARENCY = RenderStateShard.TRANSLUCENT_TRANSPARENCY;
        private static final TextureStateShard BLOCK_SHEET_MIPPED = RenderStateShard.BLOCK_SHEET_MIPPED;
        private static final ShaderStateShard RENDERTYPE_ENTITY_SHADOW_SHADER = RenderStateShard.RENDERTYPE_ENTITY_SHADOW_SHADER;
        private static final ShaderStateShard POSITION_COLOR_TEX_SHADER = RenderStateShard.POSITION_COLOR_TEX_SHADER;
        private static final CullStateShard NO_CULL = RenderStateShard.NO_CULL;
        private static final WriteMaskStateShard COLOR_WRITE = RenderStateShard.COLOR_WRITE;
        private static final RenderStateShard.LayeringStateShard VIEW_OFFSET_Z_LAYERING = RenderStateShard.VIEW_OFFSET_Z_LAYERING;
        private static final RenderStateShard.ShaderStateShard POSITION_COLOR_TEX_LIGHTMAP_SHADER = RenderStateShard.POSITION_COLOR_TEX_LIGHTMAP_SHADER;
        private static final RenderStateShard.TransparencyStateShard LIGHTNING_TRANSPARENCY = RenderStateShard.LIGHTNING_TRANSPARENCY;
        private static final RenderStateShard.OverlayStateShard OVERLAY = RenderStateShard.OVERLAY;

        private RenderStateShardAccess(String pName, Runnable pSetupState, Runnable pClearState) {
            super(pName, pSetupState, pClearState);
            throw new AssertionError();
        }
    }
}
