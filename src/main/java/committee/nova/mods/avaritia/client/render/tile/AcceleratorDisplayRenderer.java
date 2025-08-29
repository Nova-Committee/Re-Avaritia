// committee/nova/mods/avaritia/client/renderer/AcceleratorDisplayRenderer.java
package committee.nova.mods.avaritia.client.render.tile;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.joml.Quaternionf;
import org.joml.Vector3f;

@OnlyIn(Dist.CLIENT)
public class AcceleratorDisplayRenderer extends EntityRenderer<AcceleratorDisplayEntity> {
    private final Font font; // 用于绘制文字的字体

    public AcceleratorDisplayRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.font = context.getFont(); // 获取游戏字体
    }

    @Override
    public void render(AcceleratorDisplayEntity entity, float yaw, float partialTicks,
                       PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
        // 要显示的文字（加速倍数）
        String text = "x" + entity.getSpeedMultiplier();

        // 计算文字居中偏移（根据文字长度调整）
        float textWidth = font.width(text) * 0.02f / 2; // 0.02是缩放因子

        // 在方块6个面都绘制文字（确保从任何角度都能看到）
        drawText(poseStack, buffer, text, new Vector3f(-textWidth, 0.0f, 0.51f),
                Axis.YP.rotationDegrees(0), packedLight); // 前面
        drawText(poseStack, buffer, text, new Vector3f(-textWidth, 0.0f, -0.51f),
                Axis.YP.rotationDegrees(180), packedLight); // 后面
        drawText(poseStack, buffer, text, new Vector3f(0.51f, 0.0f, -textWidth),
                Axis.YP.rotationDegrees(90), packedLight); // 右面
        drawText(poseStack, buffer, text, new Vector3f(-0.51f, 0.0f, -textWidth),
                Axis.YP.rotationDegrees(-90), packedLight); // 左面
        drawText(poseStack, buffer, text, new Vector3f(-textWidth, 0.51f, 0.0f),
                Axis.XP.rotationDegrees(90), packedLight); // 上面
        drawText(poseStack, buffer, text, new Vector3f(-textWidth, -0.51f, 0.0f),
                Axis.XP.rotationDegrees(-90), packedLight); // 下面
    }

    // 绘制文字的工具方法
    private void drawText(PoseStack poseStack, MultiBufferSource buffer, String text,
                          Vector3f pos, Quaternionf rotation, int light) {
        poseStack.pushPose();
        // 移动到绘制位置
        poseStack.translate(pos.x, pos.y, pos.z);
        // 缩放文字（0.02是合适的大小）
        poseStack.scale(0.02f, -0.02f, 0.02f);
        // 旋转文字使其面向对应方向
        poseStack.mulPose(rotation);
        // 绘制文字（白色，使用游戏默认字体）
        font.drawInBatch(text, 0, 0, 0xFFFFFF, false,
                poseStack.last().pose(), buffer, Font.DisplayMode.NORMAL, 0, light);
        poseStack.popPose();
    }

    @Override
    public ResourceLocation getTextureLocation(AcceleratorDisplayEntity entity) {
        return null;
    }
}