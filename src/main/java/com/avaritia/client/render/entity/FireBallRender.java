package com.avaritia.client.render.entity;

import com.avaritia.Res;
import com.avaritia.common.entity.ball.FireBallEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class FireBallRender extends BillboardProjectileRender<FireBallEntity> {
    public FireBallRender(EntityRendererProvider.Context context) {
        super(context, Res.DRAGON_FIREBALL, 2.0F);
    }
}
