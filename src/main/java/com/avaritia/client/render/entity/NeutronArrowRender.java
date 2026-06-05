package com.avaritia.client.render.entity;

import com.avaritia.Res;
import com.avaritia.common.entity.arrow.NeutronArrowEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;

public class NeutronArrowRender extends TexturedArrowRender<NeutronArrowEntity> {
    public NeutronArrowRender(EntityRendererProvider.Context context) {
        super(context, Res.NEUTRON_ARROW);
    }
}
