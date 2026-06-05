package com.avaritia.client.render.entity;

import com.avaritia.Res;
import com.avaritia.common.entity.SunProEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;

public class SunProRender extends BillboardProjectileRender<SunProEntity> {
    public SunProRender(EntityRendererProvider.Context context) {
        super(context, Res.SUN_PRO_TEX, 1.0F);
    }
}
