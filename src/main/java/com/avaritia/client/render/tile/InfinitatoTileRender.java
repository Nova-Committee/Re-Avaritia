package com.avaritia.client.render.tile;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.avaritia.common.tile.InfinitatoTile;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Style;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nonnull;
import java.util.Locale;

/**
 * Description:
 * Author: cnlimiter
 * Date: 2022/5/22 18:56
 * Version: 1.0
 */
public class InfinitatoTileRender implements BlockEntityRenderer<InfinitatoTile, InfinitatoTileRender.State> {


    public InfinitatoTileRender(BlockEntityRendererProvider.Context ctx) {
    }


    @Override
    public State createRenderState() {
        return new State();
    }

    @Override
    public void extractRenderState(@Nonnull InfinitatoTile potato, State state, float partialTicks, Vec3 cameraPos, ModelFeatureRenderer.CrumblingOverlay breakProgress) {
        BlockEntityRenderer.super.extractRenderState(potato, state, partialTicks, cameraPos, breakProgress);
        Level level = potato.getLevel();
        if (level == null || !level.isLoaded(potato.getBlockPos())) {
            state.shouldRender = false;
            return;
        }
        state.shouldRender = true;
        state.name = potato.name == null ? "" : potato.name;
        state.potatoFacing = potato.getBlockState().hasProperty(BlockStateProperties.HORIZONTAL_FACING)
                ? potato.getBlockState().getValue(BlockStateProperties.HORIZONTAL_FACING)
                : Direction.SOUTH;
        state.jump = InfinitatoTile.jumpTicks > 0 ? InfinitatoTile.jumpTicks - partialTicks : InfinitatoTile.jumpTicks;
        HitResult pos = Minecraft.getInstance().hitResult;
        state.showName = Minecraft.renderNames()
                && !state.name.toLowerCase(Locale.ROOT).trim().isEmpty()
                && pos != null && pos.getType() == HitResult.Type.BLOCK
                && potato.getBlockPos().equals(((BlockHitResult) pos).getBlockPos());
    }

    @Override
    public void submit(State state, PoseStack ms, @Nonnull SubmitNodeCollector output, CameraRenderState cameraState) {
        if (!state.shouldRender) return;

        ms.pushPose();

        String name = state.name.toLowerCase(Locale.ROOT).trim();
        //BakedModel model = getModel(name);

        ms.translate(0.5F, 0F, 0.5F);
        Direction potatoFacing = state.potatoFacing;
        float rotY = 0;
        switch (potatoFacing) {
            default:
            case SOUTH:
                rotY = 180F;
                break;
            case NORTH:
                break;
            case EAST:
                rotY = 90F;
                break;
            case WEST:
                rotY = 270F;
                break;
        }
        ms.mulPose(Axis.YN.rotationDegrees(rotY));

        float jump = state.jump;

        float up = (float) Math.abs(Math.sin(jump / 10 * Math.PI)) * 0.2F;
        float rotZ = (float) Math.sin(jump / 10 * Math.PI) * 2;
        float wiggle = (float) Math.sin(jump / 10 * Math.PI) * 0.05F;

        ms.translate(wiggle, up, 0F);
        ms.mulPose(Axis.ZP.rotationDegrees(rotZ));

        boolean render = !(name.equals("mami") || name.equals("soaryn") || name.equals("eloraam") && jump != 0);
        if (render) {
            ms.pushPose();
            ms.translate(-0.5F, 0, -0.5F);

            //renderModel(ms, buffer, light, overlay, model);
            ms.popPose();
        }

        ms.translate(0F, 1.5F, 0F);


        ms.pushPose();
        ms.mulPose(Axis.ZP.rotationDegrees(180F));
        ms.popPose();

        ms.mulPose(Axis.ZP.rotationDegrees(-rotZ));
        ms.mulPose(Axis.YN.rotationDegrees(-rotY));

        renderName(state, name, ms, output, state.lightCoords);
        ms.popPose();
    }

    private void renderName(State state, String name, PoseStack ms, SubmitNodeCollector output, int light) {
        Minecraft mc = Minecraft.getInstance();
        if (state.showName) {
            ms.pushPose();
            ms.translate(0F, -0.6F, 0F);
            ms.mulPose(mc.getEntityRenderDispatcher().camera.rotation());
            float f1 = 0.016666668F * 1.6F;
            ms.scale(-f1, -f1, f1);
            int halfWidth = mc.font.width(state.name) / 2;

            float opacity = Minecraft.getInstance().options.getBackgroundOpacity(0.25F);
            int opacityRGB = (int) (opacity * 255.0F) << 24;
            output.submitText(ms, -halfWidth, 0, FormattedCharSequence.forward(state.name, Style.EMPTY), false, Font.DisplayMode.SEE_THROUGH, 0x20FFFFFF, opacityRGB, light, 0);
            output.submitText(ms, -halfWidth, 0, FormattedCharSequence.forward(state.name, Style.EMPTY), false, Font.DisplayMode.NORMAL, 0xFFFFFFFF, 0, light, 0);
            if (name.equals("pahimar") || name.equals("soaryn")) {
                ms.translate(0F, 14F, 0F);
                String str = name.equals("pahimar") ? "[WIP]" : "(soon)";
                halfWidth = mc.font.width(str) / 2;

                output.submitText(ms, -halfWidth, 0, FormattedCharSequence.forward(str, Style.EMPTY), false, Font.DisplayMode.SEE_THROUGH, 0x20FFFFFF, opacityRGB, light, 0);
                output.submitText(ms, -halfWidth, 0, FormattedCharSequence.forward(str, Style.EMPTY), false, Font.DisplayMode.SEE_THROUGH, 0xFFFFFFFF, 0, light, 0);
            }

            ms.popPose();
        }
    }

    public static class State extends BlockEntityRenderState {
        public boolean shouldRender;
        public String name = "";
        public Direction potatoFacing = Direction.SOUTH;
        public float jump;
        public boolean showName;
    }
}
