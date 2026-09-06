package committee.nova.mods.avaritia.client.render;

import committee.nova.mods.avaritia.Const;
import committee.nova.mods.avaritia.common.component.NeutronRingContents;
import committee.nova.mods.avaritia.common.item.misc.NeutronRingSpaces;
import committee.nova.mods.avaritia.init.registry.ModDataComponents;
import committee.nova.mods.avaritia.init.registry.ModItems;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.GlobalPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;

/** Wireframe preview of the Neutron Ring capture/place volume. */
@EventBusSubscriber(modid = Const.MOD_ID, value = Dist.CLIENT)
public final class NeutronRingPreviewRenderer {
    private NeutronRingPreviewRenderer() {
    }

    @SubscribeEvent
    public static void onRender(RenderLevelStageEvent event) {
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_TRANSLUCENT_BLOCKS) {
            return;
        }
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
        Vec3 camera = event.getCamera().getPosition();
        var buffers = minecraft.renderBuffers().bufferSource();
        var pose = event.getPoseStack();
        pose.pushPose();
        pose.translate(-camera.x, -camera.y, -camera.z);
        var lines = buffers.getBuffer(RenderType.lines());
        data.captureBase().filter(pos -> pos.dimension().equals(minecraft.level.dimension()) && data.selectedId().isEmpty())
                .ifPresent(pos -> draw(pose, lines, NeutronRingSpaces.previewBox(minecraft.level, pos.pos(), NeutronRingContents.Size.DEFAULT), 0.2F, 0.8F, 1.0F, 1.0F));
        data.placeBase().filter(pos -> pos.dimension().equals(minecraft.level.dimension()) && data.selectedId().isPresent())
                .ifPresent(pos -> draw(pose, lines, NeutronRingSpaces.previewBox(minecraft.level, pos.pos(), data.size()), 1.0F, 0.85F, 0.2F, 1.0F));
        pose.popPose();
        buffers.endBatch(RenderType.lines());
    }

    private static void draw(com.mojang.blaze3d.vertex.PoseStack pose, com.mojang.blaze3d.vertex.VertexConsumer lines,
                             AABB box, float r, float g, float b, float a) {
        LevelRenderer.renderLineBox(pose, lines, box, r, g, b, a);
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
}
