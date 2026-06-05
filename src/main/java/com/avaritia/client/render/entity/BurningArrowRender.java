package com.avaritia.client.render.entity;

import com.avaritia.Res;
import com.avaritia.common.entity.arrow.BurningArrowEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;

public class BurningArrowRender extends TexturedArrowRender<BurningArrowEntity> {
    public BurningArrowRender(EntityRendererProvider.Context context) {
        super(context, Res.BURNING_ARROW);
    }
}
