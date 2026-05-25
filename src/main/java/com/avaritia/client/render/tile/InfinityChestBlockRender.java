package com.avaritia.client.render.tile;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import com.avaritia.Avaritia;
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
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.resources.model.Material;
import net.minecraft.core.Direction;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;

/**
 * @author cnlimiter
 */
public class InfinityChestBlockRender implements BlockEntityRenderer<InfinityChestTile> {

    /** Model layer location — 注册层定义将在 AvaritiaModClient 中关联到此常量。 */
    public static final ModelLayerLocation INFINITY_CHEST = new ModelLayerLocation(Identifier.of(Avaritia.MOD_ID, "infinity_chest"), "main");

    private final ModelPart lid;
    private final ModelPart bottom;
    private final ModelPart lock;

    public InfinityChestBlockRender(BlockEntityRendererProvider.Context pContext) {
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
    public void render(InfinityChestTile pBlockEntity, float pPartialTick, @NotNull PoseStack pPoseStack,
                       @NotNull MultiBufferSource pBuffer, int pPackedLight, int pPackedOverlay) {
        Level level = pBlockEntity.getLevel();
        boolean flag = level != null;
        BlockState blockstate = flag ? pBlockEntity.getBlockState() : ModBlocks.infinity_chest.get().defaultBlockState().setValue(InfinityChestBlock.FACING, Direction.SOUTH);
        pPoseStack.pushPose();
        float f = blockstate.getValue(InfinityChestBlock.FACING).toYRot();
        pPoseStack.translate(0.5F, 0.5F, 0.5F);
        pPoseStack.mulPose(Axis.YP.rotationDegrees(-f));
        pPoseStack.translate(-0.5F, -0.5F, -0.5F);
        float f1 = pBlockEntity.getOpenNess(pPartialTick);
        f1 = 1.0F - f1;
        f1 = 1.0F - f1 * f1 * f1;
        // 26.1.2: BrightnessCombiner 已移除，单一方块实体直接使用传入的 packedLight
        int i = pPackedLight;
        VertexConsumer vertexconsumer = new Material(Sheets.CHEST_SHEET, Identifier.of(Avaritia.MOD_ID, "block/chest/infinity_chest")).buffer(pBuffer, RenderType::entityCutout);
        this.render(pPoseStack, vertexconsumer, this.lid, this.lock, this.bottom, f1, i, pPackedOverlay);
        pPoseStack.popPose();
    }

    private void render(PoseStack pPoseStack, VertexConsumer pConsumer, ModelPart pLidPart, ModelPart pLockPart, ModelPart pBottomPart, float pLidAngle, int pPackedLight, int pPackedOverlay) {
        pLidPart.xRot = -(pLidAngle * ((float) Math.PI / 2F));
        pLockPart.xRot = pLidPart.xRot;
        pLidPart.render(pPoseStack, pConsumer, pPackedLight, pPackedOverlay);
        pLockPart.render(pPoseStack, pConsumer, pPackedLight, pPackedOverlay);
        pBottomPart.render(pPoseStack, pConsumer, pPackedLight, pPackedOverlay);
    }
}
