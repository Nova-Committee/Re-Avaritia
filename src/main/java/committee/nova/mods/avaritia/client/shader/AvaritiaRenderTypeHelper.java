package committee.nova.mods.avaritia.client.shader;

import committee.nova.mods.avaritia.Const;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.rendertype.LayeringTransform;
import net.minecraft.client.renderer.rendertype.RenderSetup;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.resources.Identifier;
import org.jspecify.annotations.Nullable;

/**
 * 26.1.2 渲染管线兼容工具。
 */
public class AvaritiaRenderTypeHelper {
    static final RenderPipeline ARMOR_GLOW_PIPELINE = RenderPipelines.ARMOR_TRANSLUCENT;

    public static RenderType textured(String name, RenderPipeline pipeline, Identifier texture, boolean lightmap, boolean overlay, boolean sortOnUpload, boolean viewOffset) {
        return textured(name, pipeline, texture, lightmap, overlay, sortOnUpload,
                viewOffset ? LayeringTransform.VIEW_OFFSET_Z_LAYERING : null);
    }

    private static RenderType textured(String name, RenderPipeline pipeline, Identifier texture, boolean lightmap, boolean overlay,
                                       boolean sortOnUpload, @Nullable LayeringTransform layeringTransform) {
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
        if (layeringTransform != null) {
            builder.setLayeringTransform(layeringTransform);
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

    public static RenderType armorGlow(String name, Identifier texture) {
        return textured(name, ARMOR_GLOW_PIPELINE, texture, true, true, true, true);
    }

    public static RenderType entityCutoutNoCull(String name, Identifier texture) {
        return textured(name, RenderPipelines.ARMOR_CUTOUT_NO_CULL, texture, true, true, false, false);
    }

    public static RenderType textSeeThrough(String name, Identifier texture) {
        return textured(name, RenderPipelines.TEXT_SEE_THROUGH, texture, true, false, false, false);
    }

    public static RenderType itemTranslucent() {
        return RenderTypes.itemTranslucent(Const.rl("item/halo"));
    }
}
