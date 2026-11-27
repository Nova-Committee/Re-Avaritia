package committee.nova.mods.avaritia.client.render.entity;

import committee.nova.mods.avaritia.Res;
import committee.nova.mods.avaritia.common.entity.ball.FireBallEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;

public class FireBallRender extends BillboardProjectileRender<FireBallEntity> {
    public FireBallRender(EntityRendererProvider.Context context) {
        super(context, Res.DRAGON_FIREBALL, 2.0F);
    }
}
