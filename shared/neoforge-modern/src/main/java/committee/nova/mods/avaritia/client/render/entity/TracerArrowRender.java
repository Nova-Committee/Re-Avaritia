package committee.nova.mods.avaritia.client.render.entity;

import committee.nova.mods.avaritia.Const;

import committee.nova.mods.avaritia.Avaritia;
import committee.nova.mods.avaritia.common.entity.arrow.TraceArrowEntity;
import net.minecraft.client.renderer.entity.ArrowRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.ArrowRenderState;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.NotNull;

/**
 * Description:
 * Author: cnlimiter
 * Date: 2022/4/20 18:54
 * Version: 1.0
 */
public class TracerArrowRender extends ArrowRenderer<TraceArrowEntity, ArrowRenderState> {
    private static final Identifier HEAVEN_ARROW_TEXTURE = Identifier.fromNamespaceAndPath(Const.MOD_ID, "textures/entity/heaven_arrow.png");

    public TracerArrowRender(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    protected @NotNull Identifier getTextureLocation(@NotNull ArrowRenderState state) {
        return HEAVEN_ARROW_TEXTURE;
    }

    @Override
    public @NotNull ArrowRenderState createRenderState() {
        return new ArrowRenderState();
    }
}
