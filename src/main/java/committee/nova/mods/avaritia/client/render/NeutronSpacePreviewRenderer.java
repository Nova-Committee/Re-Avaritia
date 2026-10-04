package committee.nova.mods.avaritia.client.render;

import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import committee.nova.mods.avaritia.Const;
import committee.nova.mods.avaritia.api.client.screen.component.PortableUi;
import committee.nova.mods.avaritia.common.item.misc.NeutronSpacePreview;
import committee.nova.mods.avaritia.common.item.misc.NeutronSpacePreviewLevel;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.client.gui.render.pip.PictureInPictureRenderer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.client.renderer.block.BlockStateModelSet;
import net.minecraft.client.renderer.block.FluidRenderer;
import net.minecraft.client.renderer.block.ModelBlockRenderer;
import net.minecraft.client.renderer.block.dispatch.BlockStateModel;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.chunk.ChunkSectionLayer;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.gui.pip.PictureInPictureRenderState;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.CardinalLighting;
import net.minecraft.world.level.ColorResolver;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.lighting.LevelLightEngine;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterPictureInPictureRenderersEvent;
import net.neoforged.neoforge.model.data.ModelData;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix3x2f;

import java.util.EnumMap;
import java.util.Map;
import java.util.function.BiConsumer;

/**
 * Snapshot preview submitted through native PIP.
 * Block models tessellate against the captured volume (neighbor cull + ModelData);
 * fluids use FluidRenderer, not MovingBlockRenderState (model-only).
 */
@EventBusSubscriber(modid = Const.MOD_ID, value = Dist.CLIENT)
public final class NeutronSpacePreviewRenderer {
    private @Nullable NeutronSpacePreview preview;
    private @Nullable NeutronSpacePreviewLevel level;
    private @Nullable Level boundWorld;
    private float fitSpan = 1.0F;

    @SubscribeEvent
    public static void registerPip(RegisterPictureInPictureRenderersEvent event) {
        event.register(PreviewState.class, PipRenderer::new);
    }

    public void setPreview(@Nullable NeutronSpacePreview preview) {
        if (this.preview == preview) {
            return;
        }
        this.preview = preview;
        fitSpan = preview == null ? 1.0F : (float) Math.hypot(
                Math.hypot(Math.max(1, preview.sizeX()), Math.max(1, preview.sizeZ())),
                Math.max(1, preview.sizeY()));
        this.level = null;
        this.boundWorld = null;
    }

    public void close() {
        setPreview(null);
    }

    public void draw(GuiGraphicsExtractor graphics, int x, int y, int w, int h, float yaw, float pitch, float zoom,
                     float panX, float panY) {
        graphics.fill(x, y, x + w, y + h, PortableUi.INSET_BG);
        bindSnapshot();
        if (preview == null || level == null || w <= 0 || h <= 0) {
            return;
        }
        ScreenRectangle bounds = new ScreenRectangle(x, y, w, h);
        graphics.submitPictureInPictureRenderState(new PreviewState(
                preview, level, yaw, pitch, zoom, panX, panY, fitSpan,
                x, y, x + w, y + h, 1.0F, new Matrix3x2f(graphics.pose()), bounds, bounds));
    }

    private void bindSnapshot() {
        Minecraft minecraft = Minecraft.getInstance();
        Level world = minecraft.level;
        if (preview == null || world == null) {
            level = null;
            boundWorld = null;
            return;
        }
        if (level != null && boundWorld == world) {
            return;
        }
        BlockPos tintOrigin = minecraft.player != null ? minecraft.player.blockPosition() : BlockPos.ZERO;
        level = new NeutronSpacePreviewLevel(world, tintOrigin, preview);
        boundWorld = world;
    }

    public record PreviewState(
            NeutronSpacePreview preview,
            NeutronSpacePreviewLevel snapshot,
            float yaw,
            float pitch,
            float zoom,
            float panX,
            float panY,
            float fitSpan,
            int x0,
            int y0,
            int x1,
            int y1,
            float scale,
            Matrix3x2f pose,
            @Nullable ScreenRectangle scissorArea,
            @Nullable ScreenRectangle bounds
    ) implements PictureInPictureRenderState {
        public PreviewState(
                NeutronSpacePreview preview,
                NeutronSpacePreviewLevel snapshot,
                float yaw,
                float pitch,
                float zoom,
                float panX,
                float panY,
                float fitSpan,
                int x0,
                int y0,
                int x1,
                int y1,
                float scale,
                Matrix3x2f pose,
                @Nullable ScreenRectangle scissorArea
        ) {
            this(preview, snapshot, yaw, pitch, zoom, panX, panY, fitSpan, x0, y0, x1, y1, scale, pose, scissorArea,
                    PictureInPictureRenderState.getBounds(x0, y0, x1, y1, scissorArea));
        }
    }

    private static final class PipRenderer extends PictureInPictureRenderer<PreviewState> {
        private PipRenderer() {
        }

        @Override
        public Class<PreviewState> getRenderStateClass() {
            return PreviewState.class;
        }

        @Override
        protected void renderToTexture(PreviewState state, PoseStack poseStack, SubmitNodeCollector submitNodeCollector) {
            Minecraft minecraft = Minecraft.getInstance();
            // 26.3 起 Lighting 访问器由 getLighting() 改名为 lighting()（同原版 PiP 实体渲染）。
            minecraft.gameRenderer.lighting().setupFor(Lighting.Entry.ENTITY_IN_UI);
            NeutronSpacePreview preview = state.preview();
            NeutronSpacePreviewLevel snapshot = state.snapshot();
            int sizeX = Math.max(1, preview.sizeX());
            int sizeY = Math.max(1, preview.sizeY());
            int sizeZ = Math.max(1, preview.sizeZ());
            poseStack.translate(state.panX(), state.panY(), 0.0F);
            float view = Math.min(Math.max(1, state.x1() - state.x0()), Math.max(1, state.y1() - state.y0()));
            float scale = state.zoom() * view / Math.max(1.0F, state.fitSpan());
            poseStack.scale(scale, scale, scale);
            poseStack.rotate(Axis.XP.rotationDegrees(state.pitch()));
            poseStack.rotate(Axis.YP.rotationDegrees(state.yaw()));
            poseStack.translate(-sizeX / 2.0F, -sizeY / 2.0F, -sizeZ / 2.0F);

            SnapshotTintGetter getter = new SnapshotTintGetter(snapshot);
            BlockStateModelSet models = minecraft.getModelManager().getBlockStateModelSet();
            ModelBlockRenderer blockRenderer = new ModelBlockRenderer(true, true, minecraft.getBlockColors());
            FluidRenderer fluidRenderer = new FluidRenderer(minecraft.getModelManager().getFluidStateModelSet());
            int count = Math.min(preview.positions().length, preview.states().length);
            for (int i = 0; i < count; i++) {
                int packed = preview.positions()[i];
                int x = NeutronSpacePreview.unpackX(packed);
                int y = NeutronSpacePreview.unpackY(packed);
                int z = NeutronSpacePreview.unpackZ(packed);
                BlockPos pos = new BlockPos(x, y, z);
                BlockState blockState = snapshot.getBlockState(pos);
                if (blockState.getRenderShape() == RenderShape.MODEL) {
                    tessellateModel(submitNodeCollector, poseStack, blockRenderer, models, getter, pos, blockState);
                }
                FluidState fluid = blockState.getFluidState();
                if (!fluid.isEmpty()) {
                    tessellateFluid(submitNodeCollector, poseStack, fluidRenderer, getter, pos, blockState, fluid);
                }
            }

            CameraRenderState camera = new CameraRenderState();
            for (BlockEntity blockEntity : snapshot.blockEntities()) {
                submitBlockEntity(minecraft, blockEntity, poseStack, submitNodeCollector, camera);
            }
        }

        private void tessellateModel(SubmitNodeCollector collector, PoseStack poseStack, ModelBlockRenderer blockRenderer, BlockStateModelSet models,
                                     SnapshotTintGetter getter, BlockPos pos, BlockState blockState) {
            BlockStateModel model = models.get(blockState);
            poseStack.pushPose();
            try {
                poseStack.translate(pos.getX(), pos.getY(), pos.getZ());
                blockRenderer.tesselateBlock(
                        (ox, oy, oz, quad, instance) -> {
                            poseStack.pushPose();
                            try {
                                poseStack.translate(ox, oy, oz);
                                collector.submitCustomGeometry(poseStack, layerType(quad.materialInfo().layer()),
                                        (pose, buffer) -> buffer.putBakedQuad(pose, quad, instance));
                            } finally {
                                poseStack.popPose();
                            }
                        },
                        0.0F, 0.0F, 0.0F, getter, pos, blockState, model, blockState.getSeed(pos));
            } finally {
                poseStack.popPose();
            }
        }

        private void tessellateFluid(SubmitNodeCollector collector, PoseStack poseStack, FluidRenderer fluidRenderer, SnapshotTintGetter getter,
                                     BlockPos pos, BlockState blockState, FluidState fluid) {
            poseStack.pushPose();
            try {
                poseStack.translate(pos.getX() & ~15, pos.getY() & ~15, pos.getZ() & ~15);
                Map<ChunkSectionLayer, RecordingVertexConsumer> recordings = new EnumMap<>(ChunkSectionLayer.class);
                fluidRenderer.tesselate(getter, pos, layer -> recordings.computeIfAbsent(layer, l -> new RecordingVertexConsumer()),
                        blockState, fluid);
                recordings.forEach((layer, recording) ->
                        collector.submitCustomGeometry(poseStack, layerType(layer),
                                (pose, buffer) -> recording.replay(buffer, pose)));
            } finally {
                poseStack.popPose();
            }
        }

        private static net.minecraft.client.renderer.rendertype.RenderType layerType(ChunkSectionLayer layer) {
            return switch (layer) {
                case SOLID -> RenderTypes.solidMovingBlock();
                case CUTOUT -> RenderTypes.cutoutMovingBlock();
                case TRANSLUCENT -> RenderTypes.translucentMovingBlock();
            };
        }

        @SuppressWarnings({"rawtypes", "unchecked"})
        private static void submitBlockEntity(Minecraft minecraft, BlockEntity blockEntity, PoseStack poseStack,
                                              SubmitNodeCollector collector, CameraRenderState camera) {
            BlockEntityRenderer renderer = minecraft.getBlockEntityRenderDispatcher().getRenderer(blockEntity);
            if (renderer == null) {
                return;
            }
            BlockEntityRenderState renderState = renderer.createRenderState();
            renderer.extractRenderState(blockEntity, renderState, 0.0F, Vec3.ZERO, null);
            BlockPos pos = blockEntity.getBlockPos();
            poseStack.pushPose();
            poseStack.translate(pos.getX(), pos.getY(), pos.getZ());
            renderer.submit(renderState, poseStack, collector, camera);
            poseStack.popPose();
        }

        @Override
        protected float getTranslateY(int height, int guiScale) {
            return height / 2.0F;
        }

        @Override
        protected String getTextureLabel() {
            return "neutron_space_preview";
        }
    }

    /** Client tint/light view of the captured volume so neighbors, fluids, and ModelData stay on the snapshot. */
    private static final class SnapshotTintGetter implements BlockAndTintGetter {
        private final NeutronSpacePreviewLevel snapshot;

        private SnapshotTintGetter(NeutronSpacePreviewLevel snapshot) {
            this.snapshot = snapshot;
        }

        @Override
        public CardinalLighting cardinalLighting() {
            return CardinalLighting.DEFAULT;
        }

        @Override
        public LevelLightEngine getLightEngine() {
            return LevelLightEngine.EMPTY;
        }

        @Override
        public int getBrightness(net.minecraft.world.level.LightLayer layer, BlockPos pos) {
            return 15;
        }

        @Override
        public int getRawBrightness(BlockPos pos, int darkening) {
            return 15;
        }

        @Override
        public int getBlockTint(BlockPos pos, ColorResolver color) {
            return snapshot.foliageTint(color);
        }

        @Override
        public @Nullable BlockEntity getBlockEntity(BlockPos pos) {
            return snapshot.getBlockEntity(pos);
        }

        @Override
        public BlockState getBlockState(BlockPos pos) {
            return snapshot.getBlockState(pos);
        }

        @Override
        public FluidState getFluidState(BlockPos pos) {
            return snapshot.getFluidState(pos);
        }

        @Override
        public ModelData getModelData(BlockPos pos) {
            return snapshot.getModelData(pos);
        }

        @Override
        public int getHeight() {
            return snapshot.getHeight();
        }

        @Override
        public int getMinY() {
            return snapshot.getMinY();
        }
    }

    /** 26.3 已无逐层 BufferSource：先录制流体顶点写入操作，再按层回放到提交节点。 */
    private static final class RecordingVertexConsumer implements VertexConsumer {
        private final java.util.List<BiConsumer<VertexConsumer, PoseStack.Pose>> ops = new java.util.ArrayList<>();

        void replay(VertexConsumer out, PoseStack.Pose pose) {
            for (BiConsumer<VertexConsumer, PoseStack.Pose> op : ops) {
                op.accept(out, pose);
            }
        }

        @Override
        public VertexConsumer addVertex(float x, float y, float z) {
            ops.add((out, pose) -> out.addVertex(pose, x, y, z));
            return this;
        }

        @Override
        public VertexConsumer setColor(int r, int g, int b, int a) {
            ops.add((out, pose) -> out.setColor(r, g, b, a));
            return this;
        }

        @Override
        public VertexConsumer setColor(int color) {
            ops.add((out, pose) -> out.setColor(color));
            return this;
        }

        @Override
        public VertexConsumer setUv(float u, float v) {
            ops.add((out, pose) -> out.setUv(u, v));
            return this;
        }

        @Override
        public VertexConsumer setUv1(int u, int v) {
            ops.add((out, pose) -> out.setUv1(u, v));
            return this;
        }

        @Override
        public VertexConsumer setUv2(int u, int v) {
            ops.add((out, pose) -> out.setUv2(u, v));
            return this;
        }

        @Override
        public VertexConsumer setUv3(float u, float v) {
            ops.add((out, pose) -> out.setUv3(u, v));
            return this;
        }

        @Override
        public VertexConsumer setNormal(float x, float y, float z) {
            ops.add((out, pose) -> out.setNormal(pose, x, y, z));
            return this;
        }

        @Override
        public VertexConsumer setLineWidth(float width) {
            ops.add((out, pose) -> out.setLineWidth(width));
            return this;
        }
    }
}
