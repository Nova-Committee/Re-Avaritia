package committee.nova.mods.avaritia.client.render.util;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexFormat;
import committee.nova.mods.avaritia.Res;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;

import java.util.Random;

import static net.minecraft.client.renderer.RenderStateShard.*;

/**
 * @author cnlimiter
 */
public class ArcRender {
    public static final RenderType ARC_RENDER_TYPE = new RenderType.CompositeRenderType(
            "arc_render_type",
            DefaultVertexFormat.POSITION_TEX,
            VertexFormat.Mode.QUADS,
            256,
            false,
            true,
            RenderType.CompositeState.builder()
                    .setTextureState(new TextureStateShard(
                            Res.ARC_TEX,
                            false,
                            false
                    ))
                    .setShaderState(POSITION_TEX_SHADER)
                    .setCullState(NO_CULL)
                    .setOutputState(ITEM_ENTITY_TARGET)
                    .setTransparencyState(LIGHTNING_TRANSPARENCY)
                    .createCompositeState(false)
    );

    private static final float THICKNESS_VARIATION = 0.4f;
    private static final float MIN_THICKNESS_FACTOR = 0.1f;
    private static final double EPSILON = 1e-6;

    public static void renderArc(PoseStack ps, MultiBufferSource mbs, long seed,
                                 float sx, float sy, float sz, float ex, float ey, float ez,
                                 float thickness, int segments) {
        var vc = mbs.getBuffer(ARC_RENDER_TYPE);
        var matrix = ps.last().pose();
        var rnd = new Random(seed);

        var start = new Vec3(sx, sy, sz);
        var end = new Vec3(ex, ey, ez);
        var delta = end.subtract(start);

        if (delta.lengthSqr() < EPSILON * EPSILON) {
            return;
        }

        var direction = delta.normalize();

        var up = new Vec3(0, 1, 0);
        if (Math.abs(direction.y()) > 1.0 - EPSILON) {
            up = new Vec3(1, 0, 0);
        }

        var side = direction.cross(up);
        if (side.lengthSqr() < EPSILON * EPSILON) {
            up = new Vec3(0, 0, 1);
            side = direction.cross(up);

            if (side.lengthSqr() < EPSILON * EPSILON) {
                if (direction.lengthSqr() > EPSILON * EPSILON) {
                    var arbitraryNonParallel = Math.abs(direction.x()) < 0.9 ? new Vec3(1, 0, 0) : new Vec3(0, 1, 0);
                    side = direction.cross(arbitraryNonParallel);
                    if (side.lengthSqr() < EPSILON * EPSILON) return;
                } else {
                    return;
                }
            }
        }
        side = side.normalize();
        var renderUp = side.cross(direction).normalize();

        var prevL = start;
        var prevR = start;
        var baseHalfThickness = thickness * 0.5f;

        for (var i = 1; i <= segments; ++i) {
            var t = (float) i / segments;
            var currentMidpoint = start.add(delta.scale(t));

            var displacementMagnitude = baseHalfThickness * Mth.TWO_PI;
            var falloff = 1.0f - (float) Math.pow(2.0 * t - 1.0, 2);
            displacementMagnitude *= falloff;
            displacementMagnitude *= (rnd.nextFloat() * 2.0f - 1.0f);

            var angle = rnd.nextDouble() * Mth.TWO_PI;
            var displacementDir = side.scale(Math.cos(angle)).add(renderUp.scale(Math.sin(angle)));

            var currentPos = currentMidpoint.add(displacementDir.scale(displacementMagnitude));

            var currentHalfThickness = baseHalfThickness;
            currentHalfThickness *= (1.0f + THICKNESS_VARIATION * (rnd.nextFloat() * 2.0f - 1.0f));
            currentHalfThickness = Math.max(baseHalfThickness * MIN_THICKNESS_FACTOR, currentHalfThickness);

            var currentL = currentPos.subtract(side.scale(currentHalfThickness));
            var currentR = currentPos.add(side.scale(currentHalfThickness));

            var u0 = (float) (i - 1) / segments;
            var u1 = (float) i / segments;

            vc.addVertex(matrix, (float) prevL.x(), (float) prevL.y(), (float) prevL.z()).setUv(u0, 0);
            vc.addVertex(matrix, (float) prevR.x(), (float) prevR.y(), (float) prevR.z()).setUv(u0, 1);
            vc.addVertex(matrix, (float) currentR.x(), (float) currentR.y(), (float) currentR.z()).setUv(u1, 1);
            vc.addVertex(matrix, (float) currentL.x(), (float) currentL.y(), (float) currentL.z()).setUv(u1, 0);

            prevL = currentL;
            prevR = currentR;
        }
    }
}
