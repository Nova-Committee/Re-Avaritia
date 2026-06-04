package com.avaritia.client.render.entity;

import com.avaritia.Res;
import com.avaritia.common.entity.StormProEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class StormProRender extends BillboardProjectileRender<StormProEntity> {
    public StormProRender(EntityRendererProvider.Context context) {
        super(context, Res.STORM_PRO_TEX, 1.0F);
    }
}
