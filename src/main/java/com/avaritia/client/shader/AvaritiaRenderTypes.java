package com.avaritia.client.shader;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.avaritia.Avaritia;
import com.avaritia.Res;
import com.avaritia.api.utils.RenderUtils;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.resources.Identifier;

/**
 * @Project: Avaritia
 * @Author: cnlimiter
 * @CreateTime: 2025/5/21 00:31
 * @Description:
 */
public class AvaritiaRenderTypes {
    public static RenderType VOID = AvaritiaRenderTypeHelper.entityCutoutNoCull("void_hemisphere", Res.VOID);

    public static RenderType VOID_HALO = AvaritiaRenderTypeHelper.entityTranslucent("void_halo", Res.VOID_HALO);

    public static RenderType COSMIC = AvaritiaRenderTypeHelper.entityTranslucentNoCullViewOffset("cosmic", RenderUtils.COSMIC_TEXTURE_ISOLATED);

    public static final RenderType COSMIC_ARMOR = AvaritiaRenderTypeHelper.entityTranslucentNoCullViewOffset("cosmic_armor", RenderUtils.COSMIC_TEXTURE_ISOLATED);

    public static final RenderType BLADE_SLASH = AvaritiaRenderTypeHelper.textSeeThrough("blade_slash", Res.BLADE_SLASH);

    public static RenderType Glow(Identifier Identifier) {
        return AvaritiaRenderTypeHelper.entityTranslucentNoCullViewOffset("glow", Identifier);
    }

    public static RenderType WingGlow(Identifier Identifier) {
        return AvaritiaRenderTypeHelper.lightning("wing_glow", Identifier);
    }

    public static RenderType HELL = AvaritiaRenderTypeHelper.entityTranslucentNoCullViewOffset("hell", RenderUtils.COSMIC_TEXTURE_ISOLATED);

    public static RenderType ETERNAL = AvaritiaRenderTypeHelper.entityTranslucentNoCullViewOffset("eternal", RenderUtils.COSMIC_TEXTURE_ISOLATED);

    public static RenderType UNSTABLE = AvaritiaRenderTypeHelper.entityTranslucentNoCullViewOffset("unstable", RenderUtils.COSMIC_TEXTURE_ISOLATED);

    public static RenderType TRIDENT = AvaritiaRenderTypeHelper.entityCutoutNoCull("infinity_trident", Res.TRIDENT_TEX);

}
