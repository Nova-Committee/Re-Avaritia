package committee.nova.mods.avaritia.client.render.entity;

import net.minecraft.client.renderer.entity.ArrowRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.ArrowRenderState;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.projectile.arrow.AbstractArrow;
import org.jetbrains.annotations.NotNull;

public class TexturedArrowRender<T extends AbstractArrow> extends ArrowRenderer<T, ArrowRenderState> {
    private final Identifier texture;

    public TexturedArrowRender(EntityRendererProvider.Context context, Identifier texture) {
        super(context);
        this.texture = texture;
    }

    @Override
    protected @NotNull Identifier getTextureLocation(@NotNull ArrowRenderState state) {
        return this.texture;
    }

    @Override
    public @NotNull ArrowRenderState createRenderState() {
        return new ArrowRenderState();
    }
}
