package com.avaritia.client.render.tile;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.avaritia.Const;
import com.avaritia.common.block.chest.InfinityChestBlock;
import com.avaritia.common.tile.InfinityChestTile;
import com.avaritia.init.registry.ModBlocks;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.resources.model.sprite.SpriteGetter;
import net.minecraft.client.resources.model.sprite.SpriteId;
import net.minecraft.core.Direction;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

/**
 * @author cnlimiter
 */
public class InfinityChestBlockRender implements BlockEntityRenderer<InfinityChestTile, InfinityChestBlockRender.State> {

    /** Model layer location — 注册层定义将在 AvaritiaModClient 中关联到此常量。 */
    public static final ModelLayerLocation INFINITY_CHEST = new ModelLayerLocation(Identifier.fromNamespaceAndPath(Const.MOD_ID, "infinity_chest"), "main");
    private static final SpriteId INFINITY_CHEST_SPRITE = Sheets.BLOCKS_MAPPER.apply(Const.rl("chest/infinity_chest"));

    private final ModelPart lid;
    private final ModelPart bottom;
    private final ModelPart lock;
    private final SpriteGetter sprites;

    public InfinityChestBlockRender(BlockEntityRendererProvider.Context pContext) {
        this.sprites = pContext.sprites();
        ModelPart modelpart = pContext.bakeLayer(INFINITY_CHEST);
        this.bottom = modelpart.getChild("bottom");
        this.lid = modelpart.getChild("lid");
        this.lock = modelpart.getChild("lock");
    }

    public static LayerDefinition createLayer() {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();
        partdefinition.addOrReplaceChild("bottom", CubeListBuilder.create().texOffs(0, 19).addBox(1.0F, 0.0F, 1.0F, 14.0F, 10.0F, 14.0F), PartPose.ZERO);
        partdefinition.addOrReplaceChild("lid", CubeListBuilder.create().texOffs(0, 0).addBox(1.0F, 0.0F, 0.0F, 14.0F, 5.0F, 14.0F), PartPose.offset(0.0F, 9.0F, 1.0F));
        partdefinition.addOrReplaceChild("lock", CubeListBuilder.create().texOffs(0, 0).addBox(7.0F, -2.0F, 14.0F, 2.0F, 4.0F, 1.0F), PartPose.offset(0.0F, 9.0F, 1.0F));
        return LayerDefinition.create(meshdefinition, 64, 64);
    }


    @Override
    public State createRenderState() {
        return new State();
    }

    @Override
    public void extractRenderState(InfinityChestTile pBlockEntity, State state, float pPartialTick, Vec3 cameraPos, ModelFeatureRenderer.CrumblingOverlay breakProgress) {
        BlockEntityRenderer.super.extractRenderState(pBlockEntity, state, pPartialTick, cameraPos, breakProgress);
        Level level = pBlockEntity.getLevel();
        boolean flag = level != null;
        BlockState blockstate = flag ? pBlockEntity.getBlockState() : ModBlocks.infinity_chest.get().defaultBlockState().setValue(InfinityChestBlock.FACING, Direction.SOUTH);
        state.facing = blockstate.getValue(InfinityChestBlock.FACING);
        state.open = pBlockEntity.getOpenNess(pPartialTick);
    }

    @Override
    public void submit(State state, @NotNull PoseStack pPoseStack,
                       @NotNull SubmitNodeCollector output, CameraRenderState cameraState) {
        pPoseStack.pushPose();
        float f = state.facing.toYRot();
        pPoseStack.translate(0.5F, 0.5F, 0.5F);
        pPoseStack.mulPose(Axis.YP.rotationDegrees(-f));
        pPoseStack.translate(-0.5F, -0.5F, -0.5F);
        float f1 = state.open;
        f1 = 1.0F - f1;
        f1 = 1.0F - f1 * f1 * f1;
        this.submit(pPoseStack, output, INFINITY_CHEST_SPRITE, this.lid, this.lock, this.bottom, f1, state.lightCoords, state.breakProgress);
        pPoseStack.popPose();
    }

    private void submit(PoseStack pPoseStack, SubmitNodeCollector output, SpriteId sprite, ModelPart pLidPart, ModelPart pLockPart, ModelPart pBottomPart, float pLidAngle, int pPackedLight, ModelFeatureRenderer.CrumblingOverlay breakProgress) {
        pLidPart.xRot = -(pLidAngle * ((float) Math.PI / 2F));
        pLockPart.xRot = pLidPart.xRot;
        var texture = this.sprites.get(sprite);
        var renderType = sprite.renderType(RenderTypes::entityCutout);
        output.submitModelPart(pLidPart, pPoseStack, renderType, pPackedLight, OverlayTexture.NO_OVERLAY, texture, 0, breakProgress);
        output.submitModelPart(pLockPart, pPoseStack, renderType, pPackedLight, OverlayTexture.NO_OVERLAY, texture, 0, breakProgress);
        output.submitModelPart(pBottomPart, pPoseStack, renderType, pPackedLight, OverlayTexture.NO_OVERLAY, texture, 0, breakProgress);
    }

    public static class State extends BlockEntityRenderState {
        public Direction facing = Direction.SOUTH;
        public float open;
    }
}
