package committee.nova.mods.avaritia.client.shader;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import committee.nova.mods.avaritia.Const;
import committee.nova.mods.avaritia.Res;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;

import static net.minecraft.client.renderer.RenderStateShard.*;

/**
 * @Project: Avaritia
 * @Author: cnlimiter
 * @CreateTime: 2025/5/21 00:31
 * @Description:
 */
public class AvaritiaRenderTypes {
    public static RenderType VOID = RenderType.create(
            Const.rl("void_hemisphere").toString(),
            DefaultVertexFormat.NEW_ENTITY, VertexFormat.Mode.TRIANGLES, 256,
            RenderType.CompositeState.builder()
                    .setShaderState(RENDERTYPE_ENTITY_SHADOW_SHADER)
                    .setTextureState(new RenderStateShard.TextureStateShard(Res.VOID, false, false))
                    .setCullState(RenderType.NO_CULL)
                    .createCompositeState(false));

    public static RenderType VOID_HALO = RenderType.create(
            Const.rl("void_halo").toString(),
            DefaultVertexFormat.POSITION_TEX_COLOR, VertexFormat.Mode.QUADS, 256,
            RenderType.CompositeState.builder()
                    .setShaderState(RenderType.POSITION_COLOR_TEX_SHADER)
                    .setTextureState(new RenderStateShard.TextureStateShard(Res.VOID_HALO, false, false))
                    .setTransparencyState(RenderType.TRANSLUCENT_TRANSPARENCY)
                    .setWriteMaskState(RenderType.COLOR_WRITE)
                    .createCompositeState(false));

    public static RenderType COSMIC = RenderType.create(
            Const.rl("cosmic").toString(), DefaultVertexFormat.BLOCK,
            VertexFormat.Mode.QUADS, 2097152, true, false,
            RenderType.CompositeState.builder()
                    .setShaderState(new RenderStateShard.ShaderStateShard(() -> AvaritiaShaders.COSMIC_SHADER))
                    .setDepthTestState(RenderStateShard.EQUAL_DEPTH_TEST)
                    .setLightmapState(RenderStateShard.LIGHTMAP)
                    .setTransparencyState(RenderStateShard.TRANSLUCENT_TRANSPARENCY)
                    .setTextureState(RenderStateShard.BLOCK_SHEET_MIPPED)
                    .createCompositeState(true)
    );
    public static RenderType ETERNAL = RenderType.create(
            Const.rl("eternal").toString(), DefaultVertexFormat.BLOCK,
            VertexFormat.Mode.QUADS, 2097152, true, false,
            RenderType.CompositeState.builder()
                    .setShaderState(new RenderStateShard.ShaderStateShard(() -> AvaritiaShaders.ETERNAL_SHADER))
                    .setDepthTestState(RenderStateShard.EQUAL_DEPTH_TEST)
                    .setLightmapState(RenderStateShard.LIGHTMAP)
                    .setTransparencyState(RenderStateShard.TRANSLUCENT_TRANSPARENCY)
                    .setTextureState(RenderStateShard.BLOCK_SHEET_MIPPED)
                    .createCompositeState(true)
    );
    public static final RenderType COSMIC_ARMOR = RenderType.create(
            Const.rl("cosmic").toString(), DefaultVertexFormat.NEW_ENTITY,
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

    public static RenderType glow(ResourceLocation tex) {
        return RenderType.create(Const.rl("glow").toString(),
                DefaultVertexFormat.NEW_ENTITY, VertexFormat.Mode.QUADS, 2097152, true, false,
                RenderType.CompositeState.builder()
                        .setShaderState(RenderStateShard.POSITION_COLOR_TEX_LIGHTMAP_SHADER)
                        .setTransparencyState(RenderStateShard.TRANSLUCENT_TRANSPARENCY)
                        .setCullState(RenderStateShard.NO_CULL)
                        .setLayeringState(RenderStateShard.VIEW_OFFSET_Z_LAYERING)
                        .setTextureState(new RenderStateShard.TextureStateShard(tex, false, false))
                        .createCompositeState(true));
    }

    public static RenderType armorMask(final ResourceLocation tex) {
        return RenderType.create(Const.rl("armor_mask").toString(),
                DefaultVertexFormat.NEW_ENTITY, VertexFormat.Mode.QUADS, 2097152, true, false,
                RenderType.CompositeState.builder()
                        .setShaderState(new RenderStateShard.ShaderStateShard(() -> AvaritiaShaders.COSMIC_ARMOR_SHADER))
                        .setTextureState(new RenderStateShard.TextureStateShard(tex, false, false))
                        .setTransparencyState(RenderType.TRANSLUCENT_TRANSPARENCY)
                        .setLightmapState(RenderType.LIGHTMAP)
                        .setWriteMaskState(RenderStateShard.COLOR_WRITE)
                        .setCullState(RenderType.NO_CULL)
                        .setLayeringState(RenderType.VIEW_OFFSET_Z_LAYERING)
                        .createCompositeState(true));
    }

    public static RenderType armorMask2(final ResourceLocation tex) {
        return RenderType.create(Const.rl("armor_mask2").toString(),
                DefaultVertexFormat.NEW_ENTITY, VertexFormat.Mode.QUADS, 2097152, true, false,
                RenderType.CompositeState.builder()
                        .setShaderState(new RenderStateShard.ShaderStateShard(() -> AvaritiaShaders.COSMIC_ARMOR_SHADER))
                        .setTextureState(new RenderStateShard.TextureStateShard(tex, false, false))
                        .setTransparencyState(RenderType.TRANSLUCENT_TRANSPARENCY)
                        .setLightmapState(RenderType.LIGHTMAP)
                        .setWriteMaskState(RenderStateShard.COLOR_WRITE)
                        .setCullState(RenderType.NO_CULL)
                        .createCompositeState(true));
    }

}
