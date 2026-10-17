package committee.nova.mods.avaritia.client.render.entity;

import committee.nova.mods.avaritia.Res;
import committee.nova.mods.avaritia.common.entity.RainProEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;

public class RainProRender extends BillboardProjectileRender<RainProEntity> {
    public RainProRender(EntityRendererProvider.Context context) {
        super(context, Res.RAIN_PRO_TEX, 1.0F);
    }
}
