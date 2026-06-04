package com.avaritia.client.render.entity;

import com.avaritia.Res;
import com.avaritia.common.entity.arrow.BurningArrowEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class BurningArrowRender extends TexturedArrowRender<BurningArrowEntity> {
    public BurningArrowRender(EntityRendererProvider.Context context) {
        super(context, Res.BURNING_ARROW);
    }
}
