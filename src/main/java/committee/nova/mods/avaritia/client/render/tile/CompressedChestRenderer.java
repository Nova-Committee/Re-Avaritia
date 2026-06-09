package committee.nova.mods.avaritia.client.render.tile;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import committee.nova.mods.avaritia.Const;
import net.minecraft.client.model.geom.ModelLayers;
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
import net.minecraft.client.renderer.blockentity.BrightnessCombiner;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.resources.model.sprite.SpriteGetter;
import net.minecraft.client.resources.model.sprite.SpriteId;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.level.block.entity.LidBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.ChestType;
import org.jetbrains.annotations.NotNull;

/**
 * @Project: Avaritia
 * @Author: cnlimiter
 * @CreateTime: 2024/7/13 下午1:39
 * @Description:
 */
public class CompressedChestRenderer<T extends BlockEntity & LidBlockEntity> implements BlockEntityRenderer<T, CompressedChestRenderer.State> {
    private static final SpriteId COMPRESSED_CHEST_SPRITE = Sheets.BLOCKS_MAPPER.apply(Const.rl("chest/compressed_chest"));

    private final ModelPart lid;
    private final ModelPart bottom;
    private final ModelPart lock;
    private final ModelPart doubleLeftLid;
    private final ModelPart doubleLeftBottom;
    private final ModelPart doubleLeftLock;
    private final ModelPart doubleRightLid;
    private final ModelPart doubleRightBottom;
    private final ModelPart doubleRightLock;
    private final SpriteGetter sprites;

    public CompressedChestRenderer(BlockEntityRendererProvider.Context pContext) {
        this.sprites = pContext.sprites();
        ModelPart modelpart = pContext.bakeLayer(ModelLayers.CHEST);
        this.bottom = modelpart.getChild("bottom");
        this.lid = modelpart.getChild("lid");
        this.lock = modelpart.getChild("lock");
        ModelPart modelpart1 = pContext.bakeLayer(ModelLayers.DOUBLE_CHEST_LEFT);
        this.doubleLeftBottom = modelpart1.getChild("bottom");
        this.doubleLeftLid = modelpart1.getChild("lid");
        this.doubleLeftLock = modelpart1.getChild("lock");
        ModelPart modelpart2 = pContext.bakeLayer(ModelLayers.DOUBLE_CHEST_RIGHT);
        this.doubleRightBottom = modelpart2.getChild("bottom");
        this.doubleRightLid = modelpart2.getChild("lid");
        this.doubleRightLock = modelpart2.getChild("lock");
    }

    public static LayerDefinition createSingleBodyLayer() {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();
        partdefinition.addOrReplaceChild("bottom", CubeListBuilder.create().texOffs(0, 19).addBox(1.0F, 0.0F, 1.0F, 14.0F, 10.0F, 14.0F), PartPose.ZERO);
        partdefinition.addOrReplaceChild("lid", CubeListBuilder.create().texOffs(0, 0).addBox(1.0F, 0.0F, 0.0F, 14.0F, 5.0F, 14.0F), PartPose.offset(0.0F, 9.0F, 1.0F));
        partdefinition.addOrReplaceChild("lock", CubeListBuilder.create().texOffs(0, 0).addBox(7.0F, -2.0F, 14.0F, 2.0F, 4.0F, 1.0F), PartPose.offset(0.0F, 9.0F, 1.0F));
        return LayerDefinition.create(meshdefinition, 64, 64);
    }

    public static LayerDefinition createDoubleBodyRightLayer() {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();
        partdefinition.addOrReplaceChild("bottom", CubeListBuilder.create().texOffs(0, 19).addBox(1.0F, 0.0F, 1.0F, 15.0F, 10.0F, 14.0F), PartPose.ZERO);
        partdefinition.addOrReplaceChild("lid", CubeListBuilder.create().texOffs(0, 0).addBox(1.0F, 0.0F, 0.0F, 15.0F, 5.0F, 14.0F), PartPose.offset(0.0F, 9.0F, 1.0F));
        partdefinition.addOrReplaceChild("lock", CubeListBuilder.create().texOffs(0, 0).addBox(15.0F, -2.0F, 14.0F, 1.0F, 4.0F, 1.0F), PartPose.offset(0.0F, 9.0F, 1.0F));
        return LayerDefinition.create(meshdefinition, 64, 64);
    }

    public static LayerDefinition createDoubleBodyLeftLayer() {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();
        partdefinition.addOrReplaceChild("bottom", CubeListBuilder.create().texOffs(0, 19).addBox(0.0F, 0.0F, 1.0F, 15.0F, 10.0F, 14.0F), PartPose.ZERO);
        partdefinition.addOrReplaceChild("lid", CubeListBuilder.create().texOffs(0, 0).addBox(0.0F, 0.0F, 0.0F, 15.0F, 5.0F, 14.0F), PartPose.offset(0.0F, 9.0F, 1.0F));
        partdefinition.addOrReplaceChild("lock", CubeListBuilder.create().texOffs(0, 0).addBox(0.0F, -2.0F, 14.0F, 1.0F, 4.0F, 1.0F), PartPose.offset(0.0F, 9.0F, 1.0F));
        return LayerDefinition.create(meshdefinition, 64, 64);
    }

    @Override
    public State createRenderState() {
        return new State();
    }

    @Override
    public void extractRenderState(T pBlockEntity, State state, float pPartialTick, Vec3 cameraPos, ModelFeatureRenderer.CrumblingOverlay breakProgress) {
        BlockEntityRenderer.super.extractRenderState(pBlockEntity, state, pPartialTick, cameraPos, breakProgress);
        Level level = pBlockEntity.getLevel();
        boolean flag = level != null;
        BlockState blockstate = flag ? pBlockEntity.getBlockState() : Blocks.CHEST.defaultBlockState().setValue(ChestBlock.FACING, Direction.SOUTH);
        state.chestType = blockstate.hasProperty(ChestBlock.TYPE) ? blockstate.getValue(ChestBlock.TYPE) : ChestType.SINGLE;
        state.facing = blockstate.getValue(ChestBlock.FACING);
        state.sprite = getSprite(pBlockEntity, state.chestType);
        Block block = blockstate.getBlock();
        if (block instanceof AbstractChestBlock<?> abstractchestblock) {
            DoubleBlockCombiner.NeighborCombineResult<? extends ChestBlockEntity> neighborcombineresult;
            if (flag) {
                neighborcombineresult = abstractchestblock.combine(blockstate, level, pBlockEntity.getBlockPos(), true);
            } else {
                neighborcombineresult = DoubleBlockCombiner.Combiner::acceptNone;
            }

            state.open = neighborcombineresult.apply(ChestBlock.opennessCombiner(pBlockEntity)).get(pPartialTick);
            if (state.chestType != ChestType.SINGLE) {
                state.lightCoords = neighborcombineresult.apply(new BrightnessCombiner<>()).applyAsInt(state.lightCoords);
            }
        }
    }

    @Override
    public void submit(State state, @NotNull PoseStack pPoseStack, SubmitNodeCollector output, CameraRenderState cameraState) {
        pPoseStack.pushPose();
        float f = state.facing.toYRot();
        pPoseStack.translate(0.5F, 0.5F, 0.5F);
        pPoseStack.mulPose(Axis.YP.rotationDegrees(-f));
        pPoseStack.translate(-0.5F, -0.5F, -0.5F);
        float f1 = state.open;
        f1 = 1.0F - f1;
        f1 = 1.0F - f1 * f1 * f1;
        if (state.chestType != ChestType.SINGLE) {
            if (state.chestType == ChestType.LEFT) {
                this.submit(pPoseStack, output, state.sprite, this.doubleLeftLid, this.doubleLeftLock, this.doubleLeftBottom, f1, state.lightCoords, state.breakProgress);
            } else {
                this.submit(pPoseStack, output, state.sprite, this.doubleRightLid, this.doubleRightLock, this.doubleRightBottom, f1, state.lightCoords, state.breakProgress);
            }
        } else {
            this.submit(pPoseStack, output, state.sprite, this.lid, this.lock, this.bottom, f1, state.lightCoords, state.breakProgress);
        }

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

    protected SpriteId getSprite(T blockEntity, ChestType chestType) {
        return COMPRESSED_CHEST_SPRITE;
    }

    public static class State extends BlockEntityRenderState {
        public ChestType chestType = ChestType.SINGLE;
        public float open;
        public Direction facing = Direction.SOUTH;
        public SpriteId sprite = COMPRESSED_CHEST_SPRITE;
    }
}
