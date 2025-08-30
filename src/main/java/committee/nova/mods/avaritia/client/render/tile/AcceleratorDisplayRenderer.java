package committee.nova.mods.avaritia.client.render.tile;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import committee.nova.mods.avaritia.common.entity.AcceleratorDisplayEntity;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.joml.Vector3f;

@OnlyIn(Dist.CLIENT)
public class AcceleratorDisplayRenderer extends EntityRenderer<AcceleratorDisplayEntity> {
    private final Font font;
    private static final float SCALE = 0.02f;

    public AcceleratorDisplayRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.font = context.getFont();
    }

    @Override
    public void render(AcceleratorDisplayEntity entity, float yaw, float partialTicks,
                       PoseStack poseStack, MultiBufferSource buffer, int packedLight) {

        String text = "x" + entity.getSpeedMultiplier();
        float textWidth = font.width(text) * SCALE / 2;

        // 获取点击的面
        Direction face = entity.getFace();

        // 根据面确定文字的位置和旋转
        drawTextOnFace(poseStack, buffer, text, face, textWidth, packedLight);
    }

    private void drawTextOnFace(PoseStack poseStack, MultiBufferSource buffer, String text,
                                Direction face, float textWidth, int light) {
        poseStack.pushPose();

        // 根据面应用变换，确保文字始终面向玩家
        switch (face) {
            case UP -> {
                // 在顶部面显示，文字面向上方的玩家
                poseStack.translate(0, 0.51, 0);
                poseStack.mulPose(Axis.XP.rotationDegrees(-90));
            }
            case DOWN -> {
                // 在底部面显示，文字面向下方的玩家
                poseStack.translate(0, -0.51, 0);
                poseStack.mulPose(Axis.XP.rotationDegrees(90));
            }
            case NORTH -> {
                // 在北面显示，文字面向北方的玩家
                poseStack.translate(0, 0, -0.51);
                poseStack.mulPose(Axis.YP.rotationDegrees(180));
            }
            case SOUTH -> {
                // 在南面显示，文字面向南方的玩家
                poseStack.translate(0, 0, 0.51);
            }
            case WEST -> {
                // 在西面显示，文字面向西方的玩家
                poseStack.translate(-0.51, 0, 0);
                poseStack.mulPose(Axis.YP.rotationDegrees(-90));
            }
            case EAST -> {
                // 在东面显示，文字面向东方的玩家
                poseStack.translate(0.51, 0, 0);
                poseStack.mulPose(Axis.YP.rotationDegrees(90));
            }
        }

        // 应用缩放
        poseStack.scale(SCALE, -SCALE, SCALE);

        // 居中文字
        poseStack.translate(-textWidth, 0, 0);

        // 绘制文字
        font.drawInBatch(text, 0, 0, 0xFFFFFF, false,
                poseStack.last().pose(), buffer, Font.DisplayMode.NORMAL, 0, light);

        poseStack.popPose();
    }

    @Override
    public ResourceLocation getTextureLocation(AcceleratorDisplayEntity entity) {
        return null;
    }
}
