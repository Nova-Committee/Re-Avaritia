package com.avaritia.client.render.entity;

import com.avaritia.Const;

import com.avaritia.Avaritia;
import com.avaritia.common.entity.arrow.HeavenSubArrowEntity;
import net.minecraft.client.renderer.entity.ArrowRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.ArrowRenderState;
import net.minecraft.resources.Identifier;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.jetbrains.annotations.NotNull;

/**
 * Description:
 * Author: cnlimiter
 * Date: 2022/4/20 18:54
 * Version: 1.0
 */
@OnlyIn(Dist.CLIENT)
public class HeavenSubArrowRender extends ArrowRenderer<HeavenSubArrowEntity, ArrowRenderState> {
    private static final Identifier HEAVEN_ARROW_TEXTURE = Identifier.fromNamespaceAndPath(Const.MOD_ID, "textures/entity/heaven_arrow.png");

    public HeavenSubArrowRender(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    protected @NotNull Identifier getTextureLocation(@NotNull ArrowRenderState state) {
        return HEAVEN_ARROW_TEXTURE;
    }

    @Override
    public @NotNull ArrowRenderState createRenderState() {
        return new ArrowRenderState();
    }
}
