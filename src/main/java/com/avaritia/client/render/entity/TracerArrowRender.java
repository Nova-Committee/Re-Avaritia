package com.avaritia.client.render.entity;

import com.avaritia.Avaritia;
import com.avaritia.common.entity.arrow.TraceArrowEntity;
import net.minecraft.client.renderer.entity.ArrowRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
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
public class TracerArrowRender extends ArrowRenderer<TraceArrowEntity> {
    private static final Identifier HEAVEN_ARROW_TEXTURE = Identifier.of(Avaritia.MOD_ID, "textures/entity/heaven_arrow.png");

    public TracerArrowRender(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public @NotNull Identifier getTextureLocation(@NotNull TraceArrowEntity entity) {
        return HEAVEN_ARROW_TEXTURE;
    }
}
