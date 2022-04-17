package nova.committee.avaritia.client.render.entity;

import codechicken.lib.math.MathHelper;
import codechicken.lib.render.CCModel;
import codechicken.lib.render.CCRenderState;
import codechicken.lib.render.OBJParser;
import codechicken.lib.texture.TextureUtils;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import nova.committee.avaritia.common.entity.GapingVoidEntity;

/**
 * Description:
 * Author: cnlimiter
 * Date: 2022/4/3 10:34
 * Version: 1.0
 */
public class GapingVoidRender extends EntityRenderer<GapingVoidEntity> {
    private ResourceLocation fill = new ResourceLocation("avaritia", "textures/entity/void.png");
    private ResourceLocation halo = new ResourceLocation("avaritia", "textures/entity/voidhalo.png");


    private CCModel model;

    protected GapingVoidRender(EntityRendererProvider.Context p_174008_) {
        super(p_174008_);
        model = OBJParser.parseModels(new ResourceLocation("avaritia", "models/entity/hemisphere.obj")).get("model");
    }

    @Override
    public ResourceLocation getTextureLocation(GapingVoidEntity p_114482_) {
        return fill;
    }

    @Override
    public void render(GapingVoidEntity ent, float entityYaw, float partialTicks, PoseStack poseStack, MultiBufferSource p_114489_, int p_114490_) {
        Minecraft mc = Minecraft.getInstance();
        Tesselator tess = Tesselator.getInstance();
        BufferBuilder buffer = tess.getBuilder();
        CCRenderState ccrs = CCRenderState.instance();
        TextureUtils.changeTexture(halo);

        double age = ent.getAge() + partialTicks;

        setColour(age, 1.0);

        double scale = GapingVoidEntity.getVoidScale(age);

        double fullfadedist = 0.6 * scale;
        double fadedist = fullfadedist + 1.5;

        double halocoord = 0.58 * scale;
        double haloscaledist = 2.2 * scale;

        double dx = ent.getX() - entityRenderDispatcher.crosshairPickEntity.getX();
        double dy = ent.getY() - entityRenderDispatcher.crosshairPickEntity.getY();
        double dz = ent.getZ() - entityRenderDispatcher.crosshairPickEntity.getZ();

        double xzlen = Math.sqrt(dx * dx + dz * dz);
        double len = Math.sqrt(dx * dx + dy * dy + dz * dz);

        if (len <= haloscaledist) {
            double close = (haloscaledist - len) / haloscaledist;
            halocoord *= 1.0 + close * close * close * close * 1.5;
        }

        double yang = Math.atan2(xzlen, dy) * MathHelper.todeg;
        double xang = Math.atan2(dx, dz) * MathHelper.todeg;

        //Lumberjack.info("dx: "+dx+", dy: "+dy+", dz: "+dz+", xang: "+xang);
        //Lumberjack.info("x: "+x+", y: "+y+", z: "+z);

        poseStack.disableLighting();
        mc.getEntityRenderDispatcher().disableLightmap();

        poseStack.pushMatrix();
        {
            poseStack.translate(x, y, z);

            poseStack.rotate((float) xang, 0, 1, 0);
            poseStack.rotate((float) (yang + 90), 1, 0, 0);

            poseStack.pushMatrix();
            {
                poseStack.rotate(90, 1, 0, 0);

                poseStack.disableAlpha();
                poseStack.enableBlend();
                poseStack.depthMask(false);

                buffer.begin(0x07, VertexFormats.POSITION_TEX);
                buffer.pos(-halocoord, 0.0, -halocoord).tex(0.0, 0.0).endVertex();
                buffer.pos(-halocoord, 0.0, halocoord).tex(0.0, 1.0).endVertex();
                buffer.pos(halocoord, 0.0, halocoord).tex(1.0, 1.0).endVertex();
                buffer.pos(halocoord, 0.0, -halocoord).tex(1.0, 0.0).endVertex();
                tess.draw();

                poseStack.depthMask(true);
                poseStack.disableBlend();
                poseStack.enableAlpha();
            }
            poseStack.popMatrix();

            TextureUtils.changeTexture(fill);

            poseStack.scale(scale, scale, scale);

            poseStack.disableCull();
            ccrs.startDrawing(0x07, DefaultVertexFormats.POSITION_TEX_NORMAL);
            model.render(ccrs);
            ccrs.draw();
            poseStack.enableCull();

        }
        poseStack.popMatrix();

        if (len <= fadedist) {
            double alpha = 1.0;
            if (len >= fullfadedist) {
                alpha = 1.0 - ((len - fullfadedist) / (fadedist - fullfadedist));
                alpha = alpha * alpha * (3 - 2 * alpha);
            }
            setColour(age, alpha);
            GlStateManager.pushMatrix();
            {
                GlStateManager.disableAlpha();
                GlStateManager.enableBlend();

                GlStateManager.rotate(180.0F - renderManager.playerViewY, 0.0F, 1.0F, 0.0F);
                GlStateManager.rotate(-renderManager.playerViewX, 1.0F, 0.0F, 0.0F);

                double d = 0;

                buffer.begin(0x07, DefaultVertexFormats.POSITION_TEX);
                buffer.pos(-100, 100, d).tex(0.0, 0.0).endVertex();
                buffer.pos(-100, -100, d).tex(0.0, 1.0).endVertex();
                buffer.pos(100, -100, d).tex(1.0, 1.0).endVertex();
                buffer.pos(100, 100, d).tex(1.0, 0.0).endVertex();
                tess.draw();

                GlStateManager.disableBlend();
                GlStateManager.enableAlpha();
            }
            GlStateManager.popMatrix();
        }

        mc.entityRenderer.enableLightmap();
        GlStateManager.enableLighting();

        GlStateManager.color(1, 1, 1, 1);
    }

    private void setColour(double age, double alpha) {
        double life = (age / GapingVoidEntity.maxLifetime);
        double f = Math.max(0, (life - GapingVoidEntity.collapse) / (1 - GapingVoidEntity.collapse));
        f = Math.max(f, 1 - (life * 30));
        RenderSystem.setShaderColor((float) f, (float) f, (float) f, (float) alpha);
    }
}
