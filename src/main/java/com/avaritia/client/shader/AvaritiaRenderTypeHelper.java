package com.avaritia.client.shader;

import com.avaritia.Avaritia;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.rendertype.LayeringTransform;
import net.minecraft.client.renderer.rendertype.RenderSetup;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.resources.model.geometry.BakedQuad;
import net.minecraft.client.resources.model.sprite.Material;
import net.minecraft.client.resources.model.sprite.Material.Baked;
import net.minecraft.client.resources.model.sprite.SpriteId;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * 26.1.2 渲染管线兼容工具。
 */
public class AvaritiaRenderTypeHelper {
    public static RenderType textured(String name, RenderPipeline pipeline, Identifier texture, boolean lightmap, boolean overlay, boolean sortOnUpload, boolean viewOffset) {
        RenderSetup.RenderSetupBuilder builder = RenderSetup.builder(pipeline).withTexture("Sampler0", texture);
        if (lightmap) {
            builder.useLightmap();
        }
        if (overlay) {
            builder.useOverlay();
        }
        if (sortOnUpload) {
            builder.sortOnUpload();
        }
        if (viewOffset) {
            builder.setLayeringTransform(LayeringTransform.VIEW_OFFSET_Z_LAYERING);
        }
        return RenderType.create(name, builder.createRenderSetup());
    }

    public static RenderType entityTranslucent(String name, Identifier texture) {
        return textured(name, RenderPipelines.ENTITY_TRANSLUCENT, texture, true, true, true, false);
    }

    public static RenderType entityTranslucentNoCull(String name, Identifier texture) {
        return textured(name, RenderPipelines.ARMOR_TRANSLUCENT, texture, true, true, true, false);
    }

    public static RenderType entityTranslucentNoCullViewOffset(String name, Identifier texture) {
        return textured(name, RenderPipelines.ARMOR_TRANSLUCENT, texture, true, true, true, true);
    }

    public static RenderType entityCutoutNoCull(String name, Identifier texture) {
        return textured(name, RenderPipelines.ARMOR_CUTOUT_NO_CULL, texture, true, true, false, false);
    }

    public static RenderType textSeeThrough(String name, Identifier texture) {
        return textured(name, RenderPipelines.TEXT_SEE_THROUGH, texture, true, false, false, false);
    }

    public static RenderType lightning(String name, Identifier texture) {
        return textured(name, RenderPipelines.LIGHTNING, texture, false, false, true, true);
    }

    public static RenderType itemTranslucent() {
        return RenderTypes.itemTranslucent(Const.rl("item/halo"));
    }

    public static @NotNull BakedQuad withFallbackMaterial(@NotNull BakedQuad quad) {
        Baked material = quad.getMaterial();
        if (material != null) {
            return quad;
        }
        return new Material(quad.sprite().atlasLocation()).bakeForTopLevel(List.of());
    }
}
