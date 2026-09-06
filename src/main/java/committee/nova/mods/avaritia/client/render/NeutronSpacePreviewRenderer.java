package committee.nova.mods.avaritia.client.render;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.BufferUploader;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.math.Axis;
import committee.nova.mods.avaritia.api.client.screen.component.PortableUi;
import committee.nova.mods.avaritia.common.item.misc.NeutronSpacePreview;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.GameRenderer;
import org.joml.Matrix4f;

/** Rotatable column mesh of a captured Neutron Ring volume. */
public final class NeutronSpacePreviewRenderer {
    private NeutronSpacePreviewRenderer() {
    }

    public static void draw(GuiGraphics graphics, NeutronSpacePreview preview, int x, int y, int w, int h,
                            float yaw, float pitch, float zoom) {
        graphics.fill(x, y, x + w, y + h, PortableUi.INSET_BG);
        graphics.flush();
        int sizeX = Math.max(1, preview.sizeX());
        int sizeZ = Math.max(1, preview.sizeZ());
        int maxH = 1;
        for (int i = 0; i < sizeX * sizeZ; i++) {
            maxH = Math.max(maxH, preview.columnHeight(i));
        }
        graphics.enableScissor(x, y, x + w, y + h);
        var pose = graphics.pose();
        pose.pushPose();
        pose.translate(x + w / 2.0F, y + h / 2.0F, 150.0F);
        float sinYaw = Math.abs((float) Math.sin(Math.toRadians(yaw)));
        float cosYaw = Math.abs((float) Math.cos(Math.toRadians(yaw)));
        float projectedWidth = cosYaw * sizeX + sinYaw * sizeZ;
        float projectedHeight = Math.abs((float) Math.cos(Math.toRadians(pitch))) * maxH
                + Math.abs((float) Math.sin(Math.toRadians(pitch))) * (sinYaw * sizeX + cosYaw * sizeZ);
        float scale = zoom * Math.min(Math.max(1, w - 12) / Math.max(1, projectedWidth),
                Math.max(1, h - 12) / Math.max(1, projectedHeight));
        pose.mulPose(Axis.ZP.rotationDegrees(180.0F));
        pose.scale(scale, scale, scale);
        pose.mulPose(Axis.XP.rotationDegrees(pitch));
        pose.mulPose(Axis.YP.rotationDegrees(yaw));
        pose.translate(-sizeX / 2.0F, -maxH / 2.0F, -sizeZ / 2.0F);

        RenderSystem.setShader(GameRenderer::getPositionColorShader);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.enableDepthTest();
        RenderSystem.depthMask(true);
        RenderSystem.disableBlend();
        RenderSystem.disableCull();
        BufferBuilder buffer = Tesselator.getInstance().begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);
        Matrix4f matrix = pose.last().pose();
        for (int z = 0; z < sizeZ; z++) {
            for (int bx = 0; bx < sizeX; bx++) {
                int index = bx + z * sizeX;
                int color = index < preview.top().length ? preview.top()[index] : 0;
                int column = preview.columnHeight(index);
                if (color == 0 || column <= 0) {
                    continue;
                }
                column(buffer, matrix, bx, z, column, color);
            }
        }
        var mesh = buffer.build();
        if (mesh != null) {
            BufferUploader.drawWithShader(mesh);
        }
        RenderSystem.enableCull();
        RenderSystem.enableBlend();
        RenderSystem.disableDepthTest();
        pose.popPose();
        graphics.disableScissor();
        graphics.flush();
    }

    private static void column(BufferBuilder buffer, Matrix4f matrix, int x, int z, int height, int color) {
        int r = (color >> 16) & 0xFF;
        int g = (color >> 8) & 0xFF;
        int b = color & 0xFF;
        float x0 = x;
        float x1 = x + 1;
        float y0 = 0;
        float y1 = height;
        float z0 = z;
        float z1 = z + 1;
        quad(buffer, matrix, x0, y1, z0, x1, y1, z0, x1, y1, z1, x0, y1, z1, r, g, b);
        quad(buffer, matrix, x0, y0, z1, x1, y0, z1, x1, y0, z0, x0, y0, z0, shade(r), shade(g), shade(b));
        quad(buffer, matrix, x0, y0, z1, x0, y1, z1, x1, y1, z1, x1, y0, z1, dim(r), dim(g), dim(b));
        quad(buffer, matrix, x1, y0, z0, x1, y1, z0, x0, y1, z0, x0, y0, z0, dim(r), dim(g), dim(b));
        quad(buffer, matrix, x0, y0, z0, x0, y1, z0, x0, y1, z1, x0, y0, z1, dark(r), dark(g), dark(b));
        quad(buffer, matrix, x1, y0, z1, x1, y1, z1, x1, y1, z0, x1, y0, z0, dark(r), dark(g), dark(b));
    }

    private static void quad(BufferBuilder buffer, Matrix4f matrix,
                             float x1, float y1, float z1, float x2, float y2, float z2,
                             float x3, float y3, float z3, float x4, float y4, float z4,
                             int r, int g, int b) {
        buffer.addVertex(matrix, x1, y1, z1).setColor(r, g, b, 255);
        buffer.addVertex(matrix, x2, y2, z2).setColor(r, g, b, 255);
        buffer.addVertex(matrix, x3, y3, z3).setColor(r, g, b, 255);
        buffer.addVertex(matrix, x4, y4, z4).setColor(r, g, b, 255);
    }

    private static int shade(int channel) {
        return channel * 55 / 100;
    }

    private static int dim(int channel) {
        return channel * 75 / 100;
    }

    private static int dark(int channel) {
        return channel * 45 / 100;
    }
}
