package committee.nova.mods.avaritia.client.render.entity;

import committee.nova.mods.avaritia.Res;
import committee.nova.mods.avaritia.common.entity.arrow.ExplosionsArrowEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;

public class ExplosionsArrowRender extends TexturedArrowRender<ExplosionsArrowEntity> {
    public ExplosionsArrowRender(EntityRendererProvider.Context context) {
        super(context, Res.HEAVEN_ARROW);
    }
}
