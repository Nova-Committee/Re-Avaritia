package committee.nova.mods.avaritia.client.render.entity;

import committee.nova.mods.avaritia.Res;
import committee.nova.mods.avaritia.common.entity.arrow.BurningArrowEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;

public class BurningArrowRender extends TexturedArrowRender<BurningArrowEntity> {
    public BurningArrowRender(EntityRendererProvider.Context context) {
        super(context, Res.BURNING_ARROW);
    }
}
