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
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.joml.Vector3f;

@OnlyIn(Dist.CLIENT)
public class AcceleratorDisplayRenderer extends EntityRenderer<AcceleratorDisplayEntity> {
    private final Font font;
    private static final float SCALE = 0.02f;
    private static final float OFFSET = 0.51f; // 文字与方块的距离

    public AcceleratorDisplayRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.font = context.getFont();
    }

    @Override
    public void render(AcceleratorDisplayEntity entity, float yaw, float partialTicks,
                       PoseStack poseStack, MultiBufferSource buffer, int packedLight) {

        String text = "x" + entity.getSpeedMultiplier();
        float textWidth = font.width(text) * SCALE / 2;

        // 获取玩家视角（相机）
        Camera camera = Minecraft.getInstance().gameRenderer.getMainCamera();

        // 计算实体到相机的方向向量
        Vector3f entityToCamera = new Vector3f(
                (float)(camera.getPosition().x - entity.getX()),
                (float)(camera.getPosition().y - entity.getY()),
                (float)(camera.getPosition().z - entity.getZ())
        ).normalize();

        // 计算文字应该面对的水平旋转角度（绕Y轴）
        float yRot = (float) Math.toDegrees(Math.atan2(entityToCamera.x, entityToCamera.z));

        // 只渲染一个面向玩家的文字（无需六个面）
        drawFacingText(poseStack, buffer, text,
                new Vector3f(-textWidth, 1, OFFSET), // 基础位置（可调整高度）
                yRot, packedLight);
    }

    private void drawFacingText(PoseStack poseStack, MultiBufferSource buffer, String text,
                                Vector3f pos, float yRotation, int light) {
        poseStack.pushPose();

        // 移动到方块表面位置
        poseStack.translate(pos.x, pos.y + 0.1f, pos.z); // +0.1f 可以稍微抬高文字

        // 关键：让文字绕Y轴旋转，始终面向玩家
        poseStack.mulPose(Axis.YP.rotationDegrees(yRotation));

        // 应用缩放
        poseStack.scale(SCALE, -SCALE, SCALE);

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
    