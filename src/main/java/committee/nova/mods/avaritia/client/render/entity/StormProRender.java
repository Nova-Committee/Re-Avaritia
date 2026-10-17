package committee.nova.mods.avaritia.client.render.entity;

import committee.nova.mods.avaritia.Res;
import committee.nova.mods.avaritia.common.entity.StormProEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;

public class StormProRender extends BillboardProjectileRender<StormProEntity> {
    public StormProRender(EntityRendererProvider.Context context) {
        super(context, Res.STORM_PRO_TEX, 1.0F);
    }
}
