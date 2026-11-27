//package committee.nova.mods.avaritia.client.render.tile;
//
//import com.mojang.blaze3d.vertex.PoseStack;
//import com.mojang.blaze3d.vertex.VertexConsumer;
//import com.mojang.math.Axis;
//import committee.nova.mods.avaritia.common.tile.InfinitatoTile;
//import committee.nova.mods.avaritia.init.registry.ModBlocks;
//import net.minecraft.client.Minecraft;
//import net.minecraft.client.gui.Font;
//import net.minecraft.client.renderer.MultiBufferSource;
//import net.minecraft.client.renderer.RenderType;
//import net.minecraft.client.renderer.Sheets;
//import net.minecraft.client.renderer.block.BlockRenderDispatcher;
//import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
//import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
//import net.minecraft.client.renderer.entity.ItemRenderer;
//import net.minecraft.client.resources.model.BakedModel;
//import net.minecraft.core.Direction;
//import net.minecraft.world.item.ItemDisplayContext;
//import net.minecraft.world.item.ItemStack;
//import net.minecraft.world.level.block.Block;
//import net.minecraft.world.level.block.state.properties.BlockStateProperties;
//import net.minecraft.world.phys.BlockHitResult;
//import net.minecraft.world.phys.HitResult;
//
//import javax.annotation.Nonnull;
//import java.util.Locale;
//import java.util.Objects;
//
/// **
// * Description:
// * Author: cnlimiter
// * Date: 2022/5/22 18:56
// * Version: 1.0
// */
//public class InfinitatoTileRender implements BlockEntityRenderer<InfinitatoTile> {
//
//
//    private final BlockRenderDispatcher blockRenderDispatcher;
//
//    public InfinitatoTileRender(BlockEntityRendererProvider.Context ctx) {
//        this.blockRenderDispatcher = ctx.getBlockRenderDispatcher();
//    }
//
//
//    @Override
//    public void render(@Nonnull InfinitatoTile potato, float partialTicks, PoseStack ms, @Nonnull MultiBufferSource buffers, int light, int overlay) {
//        if (!Objects.requireNonNull(potato.getLevel()).isLoaded(potato.getBlockPos())
//                || potato.getLevel().getBlockState(potato.getBlockPos()).getBlock() != ModBlocks.infinitato.get())
//            return;
//
//        ms.pushPose();
//
//        String name = potato.name.toLowerCase(Locale.ROOT).trim();
//        RenderType layer = Sheets.translucentCullBlockSheet();
//        //BakedModel model = getModel(name);
//
//        ms.translate(0.5F, 0F, 0.5F);
//        Direction potatoFacing = potato.getBlockState().getValue(BlockStateProperties.HORIZONTAL_FACING);
//        float rotY = 0;
//        switch (potatoFacing) {
//            default:
//            case SOUTH:
//                rotY = 180F;
//                break;
//            case NORTH:
//                break;
//            case EAST:
//                rotY = 90F;
//                break;
//            case WEST:
//                rotY = 270F;
//                break;
//        }
//        ms.mulPose(Axis.YN.rotationDegrees(rotY));
//
//        float jump = InfinitatoTile.jumpTicks;
//        if (jump > 0) {
//            jump -= partialTicks;
//        }
//
//        float up = (float) Math.abs(Math.sin(jump / 10 * Math.PI)) * 0.2F;
//        float rotZ = (float) Math.sin(jump / 10 * Math.PI) * 2;
//        float wiggle = (float) Math.sin(jump / 10 * Math.PI) * 0.05F;
//
//        ms.translate(wiggle, up, 0F);
//        ms.mulPose(Axis.ZP.rotationDegrees(rotZ));
//
//        boolean render = !(name.equals("mami") || name.equals("soaryn") || name.equals("eloraam") && jump != 0);
//        if (render) {
//            ms.pushPose();
//            ms.translate(-0.5F, 0, -0.5F);
//            VertexConsumer buffer = ItemRenderer.getFoilBuffer(buffers, layer, true, false);
//
//            //renderModel(ms, buffer, light, overlay, model);
//            ms.popPose();
//        }
//
//        ms.translate(0F, 1.5F, 0F);
//
//
//        ms.pushPose();
//        ms.mulPose(Axis.ZP.rotationDegrees(180F));
//        ms.popPose();
//
//        ms.mulPose(Axis.ZP.rotationDegrees(-rotZ));
//        ms.mulPose(Axis.YN.rotationDegrees(-rotY));
//
//        renderName(potato, name, ms, buffers, light);
//        ms.popPose();
//    }
//
//    private void renderName(InfinitatoTile potato, String name, PoseStack ms, MultiBufferSource buffers, int light) {
//        Minecraft mc = Minecraft.getInstance();
//        HitResult pos = mc.hitResult;
//        if (Minecraft.renderNames()
//                && !name.isEmpty() && pos != null && pos.getType() == HitResult.Type.BLOCK
//                && potato.getBlockPos().equals(((BlockHitResult) pos).getBlockPos())) {
//            ms.pushPose();
//            ms.translate(0F, -0.6F, 0F);
//            ms.mulPose(mc.getEntityRenderDispatcher().cameraOrientation());
//            float f1 = 0.016666668F * 1.6F;
//            ms.scale(-f1, -f1, f1);
//            int halfWidth = mc.font.width(potato.name) / 2;
//
//            float opacity = Minecraft.getInstance().options.getBackgroundOpacity(0.25F);
//            int opacityRGB = (int) (opacity * 255.0F) << 24;
//            mc.font.drawInBatch(potato.name, -halfWidth, 0, 0x20FFFFFF, false, ms.last().pose(), buffers, Font.DisplayMode.SEE_THROUGH, opacityRGB, light);
//            mc.font.drawInBatch(potato.name, -halfWidth, 0, 0xFFFFFFFF, false, ms.last().pose(), buffers, Font.DisplayMode.NORMAL, 0, light);
//            if (name.equals("pahimar") || name.equals("soaryn")) {
//                ms.translate(0F, 14F, 0F);
//                String str = name.equals("pahimar") ? "[WIP]" : "(soon)";
//                halfWidth = mc.font.width(str) / 2;
//
//                mc.font.drawInBatch(str, -halfWidth, 0, 0x20FFFFFF, false, ms.last().pose(), buffers, Font.DisplayMode.SEE_THROUGH, opacityRGB, light);
//                mc.font.drawInBatch(str, -halfWidth, 0, 0xFFFFFFFF, false, ms.last().pose(), buffers, Font.DisplayMode.SEE_THROUGH, 0, light);
//            }
//
//            ms.popPose();
//        }
//    }
//
//    private void renderModel(PoseStack ms, MultiBufferSource buffers, int light, int overlay, BakedModel model) {
//        renderModel(ms, buffers.getBuffer(Sheets.translucentCullBlockSheet()), light, overlay, model);
//    }
//
//    private void renderModel(PoseStack ms, VertexConsumer buffer, int light, int overlay, BakedModel model) {
//        blockRenderDispatcher.getModelRenderer().renderModel(ms.last(), buffer, null, model, 1, 1, 1, light, overlay);
//    }
//
//    private void renderItem(PoseStack ms, MultiBufferSource buffers, int light, int overlay, ItemStack stack) {
//        Minecraft.getInstance().getItemRenderer().renderStatic(stack, ItemDisplayContext.HEAD,
//                light, overlay, ms, buffers, null, 0);
//    }
//
//    private void renderBlock(PoseStack ms, MultiBufferSource buffers, int light, int overlay, Block block) {
//        blockRenderDispatcher.renderSingleBlock(block.defaultBlockState(), ms, buffers, light, overlay);
//    }
//}
