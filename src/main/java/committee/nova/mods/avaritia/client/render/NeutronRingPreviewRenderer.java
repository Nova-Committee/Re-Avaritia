package committee.nova.mods.avaritia.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import committee.nova.mods.avaritia.Const;
import committee.nova.mods.avaritia.common.item.misc.NeutronRingContents;
import committee.nova.mods.avaritia.common.item.misc.NeutronRingSpaces;
import committee.nova.mods.avaritia.init.registry.ModItems;
import net.fabricmc.fabric.api.client.rendering.v1.level.LevelExtractionContext;
import net.fabricmc.fabric.api.client.rendering.v1.level.LevelExtractionEvents;
import net.fabricmc.fabric.api.client.rendering.v1.level.LevelRenderContext;
import net.fabricmc.fabric.api.client.rendering.v1.level.LevelRenderEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

/** Wireframe preview of the Neutron Ring capture/place volume using extract-then-submit. */
public final class NeutronRingPreviewRenderer {
    // Fabric 的 LevelRenderContext 没有 Neo 的 LevelRenderState/RenderData，
    // 用静态字段在本帧 extract -> submit 之间传递（同线程同帧，安全）。
    private static @Nullable Boxes pending;

    private NeutronRingPreviewRenderer() {
    }

    public static void register() {
        LevelExtractionEvents.END_EXTRACTION.register(NeutronRingPreviewRenderer::extract);
        LevelRenderEvents.COLLECT_SUBMITS.register(NeutronRingPreviewRenderer::submit);
    }

    private static void extract(LevelExtractionContext ctx) {
        pending = null;
        Minecraft minecraft = Minecraft.getInstance();
        Player player = minecraft.player;
        if (player == null || minecraft.level == null) {
            return;
        }
        ItemStack stack = ring(player);
        if (stack.isEmpty()) {
            return;
        }
        NeutronRingContents data = NeutronRingContents.of(stack);
        if (data == null) {
            return;
        }
        Vec3 camera = ctx.camera().position();
        AABB capture = data.captureBase()
                .filter(pos -> pos.dimension().equals(minecraft.level.dimension()) && data.selectedId().isEmpty())
                .map(pos -> NeutronRingSpaces.previewBox(minecraft.level, pos.pos(), NeutronRingContents.Size.DEFAULT)
                        .move(-camera.x, -camera.y, -camera.z))
                .orElse(null);
        AABB place = data.placeBase()
                .filter(pos -> pos.dimension().equals(minecraft.level.dimension()) && data.selectedId().isPresent())
                .map(pos -> NeutronRingSpaces.previewBox(minecraft.level, pos.pos(), data.size())
                        .move(-camera.x, -camera.y, -camera.z))
                .orElse(null);
        if (capture != null || place != null) {
            pending = new Boxes(capture, place);
        }
    }

    private static void submit(LevelRenderContext ctx) {
        Boxes boxes = pending;
        if (boxes == null) {
            return;
        }
        SubmitNodeCollector collector = ctx.submitNodeCollector();
        PoseStack pose = ctx.poseStack();
        // 26.3 的 RenderTypes.lines() 使用 POSITION_COLOR_NORMAL_LINE_WIDTH 格式，缺少 LineWidth
        // 元素会抛 IllegalStateException("Missing elements in vertex")（Neo #388 崩溃根因）。
        float width = Minecraft.getInstance().gameRenderer.gameRenderState().windowRenderState.appropriateLineWidth;
        if (boxes.capture() != null) {
            collector.submitCustomGeometry(pose, RenderTypes.lines(),
                    (entry, buffer) -> draw(entry, buffer, boxes.capture(), 0.2F, 0.8F, 1.0F, 1.0F, width));
        }
        if (boxes.place() != null) {
            collector.submitCustomGeometry(pose, RenderTypes.lines(),
                    (entry, buffer) -> draw(entry, buffer, boxes.place(), 1.0F, 0.85F, 0.2F, 1.0F, width));
        }
    }

    private static void draw(PoseStack.Pose pose, VertexConsumer lines, AABB box, float r, float g, float b, float a,
                             float width) {
        float x0 = (float) box.minX;
        float y0 = (float) box.minY;
        float z0 = (float) box.minZ;
        float x1 = (float) box.maxX;
        float y1 = (float) box.maxY;
        float z1 = (float) box.maxZ;
        line(pose, lines, x0, y0, z0, x1, y0, z0, r, g, b, a, width);
        line(pose, lines, x0, y0, z1, x1, y0, z1, r, g, b, a, width);
        line(pose, lines, x0, y1, z0, x1, y1, z0, r, g, b, a, width);
        line(pose, lines, x0, y1, z1, x1, y1, z1, r, g, b, a, width);
        line(pose, lines, x0, y0, z0, x0, y0, z1, r, g, b, a, width);
        line(pose, lines, x1, y0, z0, x1, y0, z1, r, g, b, a, width);
        line(pose, lines, x0, y1, z0, x0, y1, z1, r, g, b, a, width);
        line(pose, lines, x1, y1, z0, x1, y1, z1, r, g, b, a, width);
        line(pose, lines, x0, y0, z0, x0, y1, z0, r, g, b, a, width);
        line(pose, lines, x1, y0, z0, x1, y1, z0, r, g, b, a, width);
        line(pose, lines, x0, y0, z1, x0, y1, z1, r, g, b, a, width);
        line(pose, lines, x1, y0, z1, x1, y1, z1, r, g, b, a, width);
    }

    private static void line(PoseStack.Pose pose, VertexConsumer lines, float x0, float y0, float z0,
                             float x1, float y1, float z1, float r, float g, float b, float a, float width) {
        float dx = x1 - x0;
        float dy = y1 - y0;
        float dz = z1 - z0;
        float len = (float) Math.sqrt(dx * dx + dy * dy + dz * dz);
        if (len < 1.0E-4F) {
            return;
        }
        dx /= len;
        dy /= len;
        dz /= len;
        lines.addVertex(pose, x0, y0, z0).setColor(r, g, b, a).setNormal(pose, dx, dy, dz).setLineWidth(width);
        lines.addVertex(pose, x1, y1, z1).setColor(r, g, b, a).setNormal(pose, dx, dy, dz).setLineWidth(width);
    }

    private static ItemStack ring(Player player) {
        if (player.getMainHandItem().is(ModItems.neutron_ring.get())) {
            return player.getMainHandItem();
        }
        if (player.getOffhandItem().is(ModItems.neutron_ring.get())) {
            return player.getOffhandItem();
        }
        return ItemStack.EMPTY;
    }

    private record Boxes(@Nullable AABB capture,
                         @Nullable AABB place) {
    }
}
