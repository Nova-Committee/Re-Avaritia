package com.avaritia.client.render.entity;

import com.avaritia.Avaritia;
import com.avaritia.common.entity.arrow.HeavenSubArrowEntity;
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
public class HeavenSubArrowRender extends ArrowRenderer<HeavenSubArrowEntity> {
    private static final Identifier HEAVEN_ARROW_TEXTURE = Identifier.of(Avaritia.MOD_ID, "textures/entity/heaven_arrow.png");

    public HeavenSubArrowRender(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public @NotNull Identifier getTextureLocation(@NotNull HeavenSubArrowEntity entity) {
        return HEAVEN_ARROW_TEXTURE;
    }
}
