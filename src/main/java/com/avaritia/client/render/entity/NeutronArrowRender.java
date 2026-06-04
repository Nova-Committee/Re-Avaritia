package com.avaritia.client.render.entity;

import com.avaritia.Res;
import com.avaritia.common.entity.arrow.NeutronArrowEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class NeutronArrowRender extends TexturedArrowRender<NeutronArrowEntity> {
    public NeutronArrowRender(EntityRendererProvider.Context context) {
        super(context, Res.NEUTRON_ARROW);
    }
}
