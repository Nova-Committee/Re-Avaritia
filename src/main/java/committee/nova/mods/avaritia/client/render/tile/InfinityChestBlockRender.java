package committee.nova.mods.avaritia.client.render.tile;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.mojang.math.Transformation;
import committee.nova.mods.avaritia.Const;
import committee.nova.mods.avaritia.common.block.chest.InfinityChestBlock;
import committee.nova.mods.avaritia.common.tile.InfinityChestTile;
import committee.nova.mods.avaritia.init.registry.ModBlocks;
import net.minecraft.client.model.object.chest.ChestModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.SubmitNodeCollector;
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
import net.minecraft.util.Util;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.joml.Matrix4f;

import java.util.Map;

/**
 * @author cnlimiter
 */
public class InfinityChestBlockRender implements BlockEntityRenderer<InfinityChestTile, InfinityChestBlockRender.State> {
    private static final Map<Direction, Transformation> TRANSFORMATIONS = Util.makeEnumMap(Direction.class, InfinityChestBlockRender::createModelTransformation);

    /** Model layer location — 注册层定义将在 AvaritiaModClient 中关联到此常量。 */
    public static final ModelLayerLocation INFINITY_CHEST = new ModelLayerLocation(Identifier.fromNamespaceAndPath(Const.MOD_ID, "infinity_chest"), "main");
    private static final SpriteId INFINITY_CHEST_SPRITE = Sheets.CHEST_MAPPER.apply(Const.rl("infinity_chest"));

    private final ChestModel model;
    private final SpriteGetter sprites;

    public InfinityChestBlockRender(BlockEntityRendererProvider.Context pContext) {
        this.sprites = pContext.sprites();
        this.model = new ChestModel(pContext.bakeLayer(INFINITY_CHEST));
    }

    public static LayerDefinition createLayer() {
        return ChestModel.createSingleBodyLayer();
    }

    private static Transformation createModelTransformation(Direction facing) {
        return new Transformation(new Matrix4f().rotationAround(Axis.YP.rotationDegrees(-facing.toYRot()), 0.5F, 0.0F, 0.5F));
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
        pPoseStack.mulPose(TRANSFORMATIONS.get(state.facing));
        float f1 = state.open;
        f1 = 1.0F - f1;
        f1 = 1.0F - f1 * f1 * f1;
        // 26.x 的箱子模型通过 ChestModel 统一驱动开合动画和部件提交，不能再拆成三个 ModelPart 手动提交。
        output.submitModel(this.model, f1, pPoseStack, state.lightCoords, OverlayTexture.NO_OVERLAY, -1, INFINITY_CHEST_SPRITE, this.sprites, 0, state.breakProgress);
        pPoseStack.popPose();
    }

    @Override
    public net.minecraft.world.phys.AABB getRenderBoundingBox(InfinityChestTile blockEntity) {
        net.minecraft.core.BlockPos pos = blockEntity.getBlockPos();
        return net.minecraft.world.phys.AABB.encapsulatingFullBlocks(pos.offset(-1, 0, -1), pos.offset(1, 1, 1));
    }

    public static class State extends BlockEntityRenderState {
        public Direction facing = Direction.SOUTH;
        public float open;
    }
}
