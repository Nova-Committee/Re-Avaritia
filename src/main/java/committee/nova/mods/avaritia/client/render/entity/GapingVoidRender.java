package committee.nova.mods.avaritia.client.render.entity;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.math.Axis;
import committee.nova.mods.avaritia.Static;
import committee.nova.mods.avaritia.api.client.render.CCRenderState;
import committee.nova.mods.avaritia.api.client.render.buffer.TransformingVertexConsumer;
import committee.nova.mods.avaritia.api.client.render.model.OBJParser;
import committee.nova.mods.avaritia.common.entity.GapingVoidEntity;
import committee.nova.mods.avaritia.util.client.colour.Colour;
import committee.nova.mods.avaritia.util.client.colour.ColourRGBA;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.jetbrains.annotations.NotNull;

import static net.minecraft.client.renderer.RenderStateShard.RENDERTYPE_ENTITY_SHADOW_SHADER;

/**
 * Description:
 * Author: cnlimiter
 * Date: 2022/4/3 10:34
 * Version: 1.0
 */
@OnlyIn(Dist.CLIENT)
public final class GapingVoidRender extends EntityRenderer<GapingVoidEntity> {
    private static final ResourceLocation VOID = new ResourceLocation(Static.MOD_ID, "textures/entity/void.png");

    public GapingVoidRender(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public @NotNull ResourceLocation getTextureLocation(@NotNull GapingVoidEntity p_114482_) {
        return VOID;
    }

    @Override
    public void render(@NotNull GapingVoidEntity ent, float entityYaw, float ticks, @NotNull PoseStack stack, @NotNull MultiBufferSource buf, int packedLightIn) {
        final float age = ent.getAge() + ticks;
        final Colour colour = getColour(age, 1.0);
        final double scale = GapingVoidEntity.getVoidScale(age);
        double halocoord = 0.58 * scale;
        final double haloscaledist = 2.2 * scale;
        final Vec3 cam = this.entityRenderDispatcher.camera.getPosition();
        final double dx = ent.getX() - cam.x();
        final double dy = ent.getY() - cam.y();
        final double dz = ent.getZ() - cam.z();
        final double len = Math.sqrt(dx * dx + dy * dy + dz * dz);
        if (len <= haloscaledist) {
            final double close = (haloscaledist - len) / haloscaledist;
            halocoord *= 1.0 + close * close * close * close * 1.5;
        }
        stack.pushPose();
        stack.mulPose(Axis.YP.rotationDegrees((float)(Math.atan2(dx, dz) * 57.29577951308232)));
        stack.mulPose(Axis.XP.rotationDegrees((float)(Math.atan2(Math.sqrt(dx * dx + dz * dz), dy) * 57.29577951308232 + 90.0)));
        stack.pushPose();
        stack.mulPose(Axis.XP.rotationDegrees( 90.0f));
        final TransformingVertexConsumer cons = new TransformingVertexConsumer(buf.getBuffer(RenderType.create("avaritia:void_halo", DefaultVertexFormat.POSITION_COLOR_TEX, VertexFormat.Mode.QUADS, 256, RenderType.CompositeState.builder().setShaderState(RenderType.POSITION_COLOR_TEX_SHADER).setTextureState(new RenderStateShard.TextureStateShard(Static.rl("textures/entity/void_halo.png"), false, false)).setTransparencyState(RenderType.TRANSLUCENT_TRANSPARENCY).setWriteMaskState(RenderType.COLOR_WRITE).createCompositeState(false))), stack);
        cons.vertex(-halocoord, 0.0, -halocoord).color(colour.r, colour.g, colour.b, colour.a).uv(0.0f, 0.0f).endVertex();
        cons.vertex(-halocoord, 0.0, halocoord).color(colour.r, colour.g, colour.b, colour.a).uv(0.0f, 1.0f).endVertex();
        cons.vertex(halocoord, 0.0, halocoord).color(colour.r, colour.g, colour.b, colour.a).uv(1.0f, 1.0f).endVertex();
        cons.vertex(halocoord, 0.0, -halocoord).color(colour.r, colour.g, colour.b, colour.a).uv(1.0f, 0.0f).endVertex();
        stack.popPose();
        stack.scale((float)scale, (float)scale, (float)scale);
        final CCRenderState cc = CCRenderState.instance();
        cc.reset();
        cc.bind(RenderType.create("avaritia:void_hemisphere", DefaultVertexFormat.NEW_ENTITY, VertexFormat.Mode.TRIANGLES, 256, RenderType.CompositeState.builder().setShaderState(RENDERTYPE_ENTITY_SHADOW_SHADER).setTextureState(new RenderStateShard.TextureStateShard(GapingVoidRender.VOID, false, false)).setCullState(RenderType.NO_CULL).createCompositeState(false)), buf, stack);
        cc.baseColour = colour.rgba();
        new OBJParser(Static.rl("models/hemisphere.obj")).parse().get("model").render(cc);
        stack.popPose();

    }

    public static Colour getColour(final double age, final double a) {
        final double l = age / 186.0;
        double f = Math.max(0.0, (l - 0.95) / 0.050000000000000044);
        f = Math.max(f, 1.0 - l * 30.0);
        return new ColourRGBA(f, f, f, a);
    }
}
