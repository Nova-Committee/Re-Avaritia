package com.avaritia.client.render.entity;

import com.avaritia.Res;
import com.avaritia.common.entity.RainProEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class RainProRender extends BillboardProjectileRender<RainProEntity> {
    public RainProRender(EntityRendererProvider.Context context) {
        super(context, Res.RAIN_PRO_TEX, 1.0F);
    }
}
