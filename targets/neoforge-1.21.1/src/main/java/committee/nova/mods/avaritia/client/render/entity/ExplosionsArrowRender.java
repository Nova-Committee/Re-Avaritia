package committee.nova.mods.avaritia.client.render.entity;

import committee.nova.mods.avaritia.Res;
import committee.nova.mods.avaritia.common.entity.arrow.ExplosionsArrowEntity;
import net.minecraft.client.renderer.entity.ArrowRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.jetbrains.annotations.NotNull;

/**
 * Description:
 * @author cnlimiter
 * Date: 2022/4/20 18:54
 * Version: 1.0
 */
@OnlyIn(Dist.CLIENT)
public class ExplosionsArrowRender extends ArrowRenderer<ExplosionsArrowEntity> {
    public ExplosionsArrowRender(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public @NotNull ResourceLocation getTextureLocation(@NotNull ExplosionsArrowEntity entity) {
        return Res.HEAVEN_ARROW;
    }
}
