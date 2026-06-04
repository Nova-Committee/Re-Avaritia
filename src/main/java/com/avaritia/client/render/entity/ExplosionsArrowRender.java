package com.avaritia.client.render.entity;

import com.avaritia.Res;
import com.avaritia.common.entity.arrow.ExplosionsArrowEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class ExplosionsArrowRender extends TexturedArrowRender<ExplosionsArrowEntity> {
    public ExplosionsArrowRender(EntityRendererProvider.Context context) {
        super(context, Res.HEAVEN_ARROW);
    }
}
