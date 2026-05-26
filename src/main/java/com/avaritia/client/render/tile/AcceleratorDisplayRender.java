package com.avaritia.client.render.tile;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.avaritia.common.entity.AcceleratorDisplayEntity;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.Identifier;
import net.minecraft.util.FormattedCharSequence;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class AcceleratorDisplayRender extends EntityRenderer<AcceleratorDisplayEntity, AcceleratorDisplayRender.State> {
    private final Font font;
    private static final float SCALE = 0.02f;

    public AcceleratorDisplayRender(EntityRendererProvider.Context context) {
        super(context);
        this.font = context.getFont();
    }

    @Override
    public void submit(State state, PoseStack poseStack, SubmitNodeCollector output, CameraRenderState cameraState) {

        String text = "x" + state.speedMultiplier;
        float textWidth = font.width(text) * SCALE / 2;

        // 获取点击的面
        Direction face = state.face;

        // 根据面确定文字的位置和旋转
        drawTextOnFace(poseStack, output, text, face, textWidth, state.lightCoords);
        super.submit(state, poseStack, output, cameraState);
    }

    private void drawTextOnFace(PoseStack poseStack, SubmitNodeCollector output, String text,
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

        // 修正绘制坐标，以居中显示
        float x = -font.width(text) / 2.0f;
        float y = -font.lineHeight / 2.0f;

        output.submitText(poseStack, x, y, FormattedCharSequence.forward(text, Style.EMPTY), false,
                Font.DisplayMode.NORMAL, 0xFFFFFF, 0, light, 0);

        poseStack.popPose();
    }

    @Override
    public State createRenderState() {
        return new State();
    }

    @Override
    public void extractRenderState(AcceleratorDisplayEntity entity, State state, float partialTicks) {
        super.extractRenderState(entity, state, partialTicks);
        state.speedMultiplier = entity.getSpeedMultiplier();
        state.face = entity.getFace();
    }

    public Identifier getTextureLocation(AcceleratorDisplayEntity entity) {
        return null;
    }

    public static class State extends EntityRenderState {
        public int speedMultiplier;
        public Direction face = Direction.NORTH;
    }
}
