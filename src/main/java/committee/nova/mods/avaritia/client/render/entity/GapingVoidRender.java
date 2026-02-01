package committee.nova.mods.avaritia.client.render.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import committee.nova.mods.avaritia.Const;
import committee.nova.mods.avaritia.Res;
import committee.nova.mods.avaritia.api.client.render.CCRenderState;
import committee.nova.mods.avaritia.api.client.render.model.OBJParser;
import committee.nova.mods.avaritia.api.client.util.color.Color;
import committee.nova.mods.avaritia.api.client.util.color.ColorRGBA;
import committee.nova.mods.avaritia.client.shader.AvaritiaRenderTypes;
import committee.nova.mods.avaritia.common.entity.GapingVoidEntity;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.jetbrains.annotations.NotNull;


@OnlyIn(Dist.CLIENT)
public class GapingVoidRender extends EntityRenderer<GapingVoidEntity> {
    public GapingVoidRender(EntityRendererProvider.Context context) {
        super(context);
    }

    public static Color getColour(final double age, final double a) {
        final double l = age / 186.0;
        double f = Math.max(0.0, (l - 0.95) / 0.050000000000000044);
        f = Math.max(f, 1.0 - l * 30.0);
        return new ColorRGBA(f, f, f, a);
    }

    @Override
    public @NotNull ResourceLocation getTextureLocation(@NotNull GapingVoidEntity p_114482_) {
        return Res.VOID;
    }

    @Override
    public void render(@NotNull GapingVoidEntity ent, float entityYaw, float ticks, @NotNull PoseStack stack, @NotNull MultiBufferSource buf, int packedLightIn) {
        final float age = ent.getAge() + ticks;
        final Color color = getColour(age, 1.0);
        final double scale = GapingVoidEntity.getVoidScale(age);
        double halocoord = 0.58 * scale;
        final double haloScaleDist = 2.2 * scale;
        final Vec3 cam = this.entityRenderDispatcher.camera.getPosition();
        final double dx = ent.getX() - cam.x();
        final double dy = ent.getY() - cam.y();
        final double dz = ent.getZ() - cam.z();
        final double len = Math.sqrt(dx * dx + dy * dy + dz * dz);

        if (len <= haloScaleDist) {
            final double close = (haloScaleDist - len) / haloScaleDist;
            halocoord *= 1.0 + close * close * close * close * 1.5;
        }

        stack.pushPose();

        stack.mulPose(Axis.YP.rotationDegrees((float) (Math.atan2(dx, dz) * 57.29577951308232)));
        stack.mulPose(Axis.XP.rotationDegrees((float) (Math.atan2(Math.sqrt(dx * dx + dz * dz), dy) * 57.29577951308232 + 90.0)));

        stack.pushPose();
        stack.mulPose(Axis.XP.rotationDegrees(90.0f));

        var buffer = buf.getBuffer(AvaritiaRenderTypes.VOID_HALO);
        stack.translate(0, 0, 0);

        float r = (float) color.r;
        float g = (float) color.g;
        float b = (float) color.b;
        float a = (float) color.a;

        float negCoord = (float) -halocoord;
        float posCoord = (float) halocoord;

//        buffer.addVertex(stack.last().pose(), negCoord, 0.0f, negCoord)
//                .setColor(r, g, b, a)
//                .setUv(0.0f, 0.0f)
//                .setOverlay(0)
//                .setLight(packedLightIn)
//                .setNormal(stack.last(), 0.0f, 1.0f, 0.0f);
//
//        buffer.addVertex(stack.last().pose(), negCoord, 0.0f, posCoord)
//                .setColor(r, g, b, a)
//                .setUv(0.0f, 1.0f)
//                .setOverlay(0)
//                .setLight(packedLightIn)
//                .setNormal(stack.last(), 0.0f, 1.0f, 0.0f);
//
//        buffer.addVertex(stack.last().pose(), posCoord, 0.0f, posCoord)
//                .setColor(r, g, b, a)
//                .setUv(1.0f, 1.0f)
//                .setOverlay(0)
//                .setLight(packedLightIn)
//                .setNormal(stack.last(), 0.0f, 1.0f, 0.0f);
//
//        buffer.addVertex(stack.last().pose(), posCoord, 0.0f, negCoord)
//                .setColor(r, g, b, a)
//                .setUv(1.0f, 0.0f)
//                .setOverlay(0)
//                .setLight(packedLightIn)
//                .setNormal(stack.last(), 0.0f, 1.0f, 0.0f);

        stack.popPose();

        stack.scale((float) scale, (float) scale, (float) scale);
        final CCRenderState cc = CCRenderState.instance();
        cc.reset();
        cc.bind(AvaritiaRenderTypes.VOID, buf, stack);
        cc.baseColour = color.rgba();
        new OBJParser(Const.rl("models/hemisphere.obj")).parse().get("model").render(cc);

        stack.popPose();
    }

}