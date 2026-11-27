package committee.nova.mods.avaritia.client.render.entity;

import committee.nova.mods.avaritia.Res;
import committee.nova.mods.avaritia.common.entity.arrow.NeutronArrowEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;

public class NeutronArrowRender extends TexturedArrowRender<NeutronArrowEntity> {
    public NeutronArrowRender(EntityRendererProvider.Context context) {
        super(context, Res.NEUTRON_ARROW);
    }
}
