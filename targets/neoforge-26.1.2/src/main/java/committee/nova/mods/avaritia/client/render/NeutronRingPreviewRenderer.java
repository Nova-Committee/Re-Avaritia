package committee.nova.mods.avaritia.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import committee.nova.mods.avaritia.Const;
import committee.nova.mods.avaritia.common.component.NeutronRingContents;
import committee.nova.mods.avaritia.common.item.misc.NeutronRingSpaces;
import committee.nova.mods.avaritia.init.registry.ModDataComponents;
import committee.nova.mods.avaritia.init.registry.ModItems;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.util.context.ContextKey;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ExtractLevelRenderStateEvent;
import net.neoforged.neoforge.client.event.SubmitCustomGeometryEvent;

/** Wireframe preview of the Neutron Ring capture/place volume using extract-then-submit. */
@EventBusSubscriber(modid = Const.MOD_ID, value = Dist.CLIENT)
public final class NeutronRingPreviewRenderer {
    private static final ContextKey<Boxes> BOXES = new ContextKey<>(Const.rl("neutron_ring_preview"));

    private NeutronRingPreviewRenderer() {
    }

    @SubscribeEvent
    public static void extract(ExtractLevelRenderStateEvent event) {
        Minecraft minecraft = Minecraft.getInstance();
        Player player = minecraft.player;
        if (player == null || minecraft.level == null) {
            return;
        }
        ItemStack stack = ring(player);
        if (stack.isEmpty()) {
            return;
        }
        NeutronRingContents data = stack.get(ModDataComponents.NEUTRON_RING.get());
        if (data == null) {
            return;
        }
        Vec3 camera = event.getCamera().position();
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
            event.getRenderState().setRenderData(BOXES, new Boxes(capture, place));
        }
    }

    @SubscribeEvent
    public static void submit(SubmitCustomGeometryEvent event) {
        Boxes boxes = event.getLevelRenderState().getRenderData(BOXES);
        if (boxes == null) {
            return;
        }
        SubmitNodeCollector collector = event.getSubmitNodeCollector();
        PoseStack pose = event.getPoseStack();
        if (boxes.capture() != null) {
            collector.submitCustomGeometry(pose, RenderTypes.lines(),
                    (entry, buffer) -> draw(entry, buffer, boxes.capture(), 0.2F, 0.8F, 1.0F, 1.0F));
        }
        if (boxes.place() != null) {
            collector.submitCustomGeometry(pose, RenderTypes.lines(),
                    (entry, buffer) -> draw(entry, buffer, boxes.place(), 1.0F, 0.85F, 0.2F, 1.0F));
        }
    }

    private static void draw(PoseStack.Pose pose, VertexConsumer lines, AABB box, float r, float g, float b, float a) {
        float x0 = (float) box.minX;
        float y0 = (float) box.minY;
        float z0 = (float) box.minZ;
        float x1 = (float) box.maxX;
        float y1 = (float) box.maxY;
        float z1 = (float) box.maxZ;
        line(pose, lines, x0, y0, z0, x1, y0, z0, r, g, b, a);
        line(pose, lines, x0, y0, z1, x1, y0, z1, r, g, b, a);
        line(pose, lines, x0, y1, z0, x1, y1, z0, r, g, b, a);
        line(pose, lines, x0, y1, z1, x1, y1, z1, r, g, b, a);
        line(pose, lines, x0, y0, z0, x0, y0, z1, r, g, b, a);
        line(pose, lines, x1, y0, z0, x1, y0, z1, r, g, b, a);
        line(pose, lines, x0, y1, z0, x0, y1, z1, r, g, b, a);
        line(pose, lines, x1, y1, z0, x1, y1, z1, r, g, b, a);
        line(pose, lines, x0, y0, z0, x0, y1, z0, r, g, b, a);
        line(pose, lines, x1, y0, z0, x1, y1, z0, r, g, b, a);
        line(pose, lines, x0, y0, z1, x0, y1, z1, r, g, b, a);
        line(pose, lines, x1, y0, z1, x1, y1, z1, r, g, b, a);
    }

    private static void line(PoseStack.Pose pose, VertexConsumer lines, float x0, float y0, float z0,
                             float x1, float y1, float z1, float r, float g, float b, float a) {
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
        lines.addVertex(pose, x0, y0, z0).setColor(r, g, b, a).setNormal(pose, dx, dy, dz);
        lines.addVertex(pose, x1, y1, z1).setColor(r, g, b, a).setNormal(pose, dx, dy, dz);
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

    private record Boxes(@org.jetbrains.annotations.Nullable AABB capture,
                         @org.jetbrains.annotations.Nullable AABB place) {
    }
}
