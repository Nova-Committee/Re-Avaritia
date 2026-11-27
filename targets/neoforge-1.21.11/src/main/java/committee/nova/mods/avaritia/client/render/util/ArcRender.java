package committee.nova.mods.avaritia.client.render.util;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import committee.nova.mods.avaritia.Res;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.rendertype.RenderSetup;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4fc;

import java.util.Random;

/**
 * @author cnlimiter
 */
public class ArcRender {
    public static final RenderType ARC_RENDER_TYPE = RenderType.create(
            "arc_render_type",
            RenderSetup.builder(RenderPipelines.LIGHTNING)
                    .withTexture("Sampler0", Res.ARC_TEX)
                    .createRenderSetup()
    );

    private static final float THICKNESS_VARIATION = 0.4f;
    private static final float MIN_THICKNESS_FACTOR = 0.1f;
    private static final double EPSILON = 1e-6;
    private static final int ARC_COLOR = 0xFFFFFFFF;

    public static void renderArc(PoseStack ps, MultiBufferSource mbs, long seed,
                                 float sx, float sy, float sz, float ex, float ey, float ez,
                                 float thickness, int segments) {
        var vc = mbs.getBuffer(ARC_RENDER_TYPE);
        renderArc(ps.last(), vc, seed, sx, sy, sz, ex, ey, ez, thickness, segments);
    }

    public static void renderArc(PoseStack.Pose pose, VertexConsumer vc, long seed,
                                 float sx, float sy, float sz, float ex, float ey, float ez,
                                 float thickness, int segments) {
        var matrix = pose.pose();
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

            vertex(matrix, vc, prevL, u0, 0);
            vertex(matrix, vc, prevR, u0, 1);
            vertex(matrix, vc, currentR, u1, 1);
            vertex(matrix, vc, currentL, u1, 0);

            prevL = currentL;
            prevR = currentR;
        }
    }

    private static void vertex(Matrix4fc matrix, VertexConsumer vc, Vec3 position, float u, float v) {
        vc.addVertex(matrix, (float) position.x(), (float) position.y(), (float) position.z())
                .setUv(u, v)
                .setColor(ARC_COLOR);
    }
}
