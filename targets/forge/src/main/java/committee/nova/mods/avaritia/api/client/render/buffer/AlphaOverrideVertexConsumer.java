package committee.nova.mods.avaritia.api.client.render.buffer;

import com.mojang.blaze3d.vertex.VertexConsumer;
import org.jetbrains.annotations.NotNull;

/**
 * Created by covers1624 on 29/3/22.
 */
public class AlphaOverrideVertexConsumer extends DelegatingVertexConsumer {

    private final int alpha;

    public AlphaOverrideVertexConsumer(VertexConsumer delegate, double alpha) {
        this(delegate, (int) (255 * alpha));
    }

    public AlphaOverrideVertexConsumer(VertexConsumer delegate, int alpha) {
        super(delegate);
        this.alpha = alpha;
    }

    @Override
    public @NotNull VertexConsumer color(int r, int g, int b, int a) {
        return super.color(r, g, b, alpha);
    }
}
