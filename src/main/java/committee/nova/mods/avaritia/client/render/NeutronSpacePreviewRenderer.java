package committee.nova.mods.avaritia.client.render;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.ByteBufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.MeshData;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexBuffer;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.blaze3d.vertex.VertexSorting;
import com.mojang.math.Axis;
import committee.nova.mods.avaritia.api.client.screen.component.PortableUi;
import committee.nova.mods.avaritia.common.item.misc.NeutronSpacePreview;
import committee.nova.mods.avaritia.common.item.misc.NeutronSpacePreviewLevel;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.block.ModelBlockRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import net.neoforged.neoforge.client.model.data.ModelData;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Cached BlockRenderDispatcher mesh of a captured Neutron Ring volume. */
public final class NeutronSpacePreviewRenderer {
    private static int reloadGeneration;

    private @Nullable NeutronSpacePreview preview;
    private @Nullable NeutronSpacePreviewLevel level;
    private @Nullable Level boundWorld;
    private final List<Layer> layers = new ArrayList<>();
    private int bakedGeneration = -1;
    private final Matrix4f modelView = new Matrix4f();
    private @Nullable ByteBufferBuilder sortingBuffer;
    private float sortedYaw = Float.NaN;
    private float sortedPitch = Float.NaN;

    public static void invalidate() {
        reloadGeneration++;
    }

    public void setPreview(@Nullable NeutronSpacePreview preview) {
        if (this.preview == preview) {
            return;
        }
        closeMesh();
        this.preview = preview;
        this.level = null;
        this.boundWorld = null;
        bakedGeneration = -1;
    }

    public void close() {
        setPreview(null);
    }

    public void draw(GuiGraphics graphics, int x, int y, int w, int h, float yaw, float pitch, float zoom) {
        graphics.fill(x, y, x + w, y + h, PortableUi.INSET_BG);
        graphics.flush();
        bindSnapshot();
        if (preview == null || level == null) {
            return;
        }
        ensureBaked(yaw, pitch);
        sortTranslucent(yaw, pitch);
        int sizeX = Math.max(1, preview.sizeX());
        int sizeY = Math.max(1, preview.sizeY());
        int sizeZ = Math.max(1, preview.sizeZ());
        graphics.enableScissor(x, y, x + w, y + h);
        var pose = graphics.pose();
        pose.pushPose();
        pose.translate(x + w / 2.0F, y + h / 2.0F, 150.0F);
        float sinYaw = Math.abs((float) Math.sin(Math.toRadians(yaw)));
        float cosYaw = Math.abs((float) Math.cos(Math.toRadians(yaw)));
        float projectedWidth = cosYaw * sizeX + sinYaw * sizeZ;
        float projectedDepth = sinYaw * sizeX + cosYaw * sizeZ;
        float projectedHeight = Math.abs((float) Math.cos(Math.toRadians(pitch))) * sizeY
                + Math.abs((float) Math.sin(Math.toRadians(pitch))) * projectedDepth;
        float scale = zoom * Math.min(Math.max(1, w - 12) / Math.max(1, projectedWidth),
                Math.max(1, h - 12) / Math.max(1, projectedHeight));
        pose.scale(scale, -scale, scale);
        pose.mulPose(Axis.XP.rotationDegrees(pitch));
        pose.mulPose(Axis.YP.rotationDegrees(yaw));
        pose.translate(-sizeX / 2.0F, -sizeY / 2.0F, -sizeZ / 2.0F);

        float fogStart = RenderSystem.getShaderFogStart();
        RenderSystem.setShaderFogStart(Float.MAX_VALUE);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.enableDepthTest();
        RenderSystem.depthMask(true);
        RenderSystem.disableBlend();
        modelView.set(RenderSystem.getModelViewMatrix()).mul(pose.last().pose());
        Matrix4f projection = RenderSystem.getProjectionMatrix();
        for (Layer layer : layers) {
            if (layer.translucent) {
                continue;
            }
            drawLayer(layer, modelView, projection);
        }
        drawBlockEntities(graphics, pose);
        for (Layer layer : layers) {
            if (layer.translucent) {
                drawLayer(layer, modelView, projection);
            }
        }
        RenderSystem.setShaderFogStart(fogStart);
        RenderSystem.enableCull();
        RenderSystem.enableBlend();
        RenderSystem.disableDepthTest();
        RenderSystem.depthMask(true);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        pose.popPose();
        graphics.disableScissor();
        graphics.flush();
    }

    private void drawLayer(Layer layer, Matrix4f modelView, Matrix4f projection) {
        layer.type.setupRenderState();
        ShaderInstance shader = RenderSystem.getShader();
        if (shader != null && !layer.buffer.isInvalid()) {
            if (shader.CHUNK_OFFSET != null) {
                shader.CHUNK_OFFSET.set(0.0F, 0.0F, 0.0F);
            }
            layer.buffer.bind();
            layer.buffer.drawWithShader(modelView, projection, shader);
            VertexBuffer.unbind();
        }
        layer.type.clearRenderState();
    }

    private void drawBlockEntities(GuiGraphics graphics, PoseStack pose) {
        if (level == null || level.blockEntities().isEmpty()) {
            return;
        }
        Minecraft minecraft = Minecraft.getInstance();
        MultiBufferSource.BufferSource buffers = graphics.bufferSource();
        int light = LightTexture.pack(15, 15);
        for (BlockEntity blockEntity : level.blockEntities()) {
            BlockEntityRenderer<?> renderer = minecraft.getBlockEntityRenderDispatcher().getRenderer(blockEntity);
            if (renderer == null) {
                continue;
            }
            BlockPos pos = blockEntity.getBlockPos();
            pose.pushPose();
            pose.translate(pos.getX(), pos.getY(), pos.getZ());
            ((BlockEntityRenderer<BlockEntity>) renderer).render(blockEntity, 0.0F, pose, buffers, light, OverlayTexture.NO_OVERLAY);
            pose.popPose();
        }
        graphics.flush();
    }

    private static VertexSorting sorting(float yaw, float pitch) {
        Vector3f direction = new Vector3f(0, 0, 1)
                .rotateX((float) Math.toRadians(-pitch))
                .rotateY((float) Math.toRadians(-yaw));
        return VertexSorting.byDistance(point -> -point.dot(direction));
    }

    private void sortTranslucent(float yaw, float pitch) {
        if (yaw == sortedYaw && pitch == sortedPitch) {
            return;
        }
        VertexSorting sorting = sorting(yaw, pitch);
        for (Layer layer : layers) {
            if (layer.sortState == null) {
                continue;
            }
            if (sortingBuffer == null) {
                sortingBuffer = new ByteBufferBuilder(65536);
            }
            ByteBufferBuilder.Result indices = layer.sortState.buildSortedIndexBuffer(sortingBuffer, sorting);
            if (indices != null) {
                layer.buffer.bind();
                layer.buffer.uploadIndexBuffer(indices);
                VertexBuffer.unbind();
            }
        }
        sortedYaw = yaw;
        sortedPitch = pitch;
    }

    private void bindSnapshot() {
        Minecraft minecraft = Minecraft.getInstance();
        Level world = minecraft.level;
        if (preview == null || world == null) {
            if (level != null) {
                closeMesh();
                level = null;
                boundWorld = null;
            }
            return;
        }
        if (level != null && boundWorld == world) {
            return;
        }
        closeMesh();
        BlockPos tintOrigin = minecraft.player != null ? minecraft.player.blockPosition() : BlockPos.ZERO;
        level = new NeutronSpacePreviewLevel(world, tintOrigin, preview);
        boundWorld = world;
        bakedGeneration = -1;
    }

    private void ensureBaked(float yaw, float pitch) {
        if (bakedGeneration == reloadGeneration) {
            return;
        }
        closeMesh();
        if (preview == null || level == null) {
            return;
        }
        bake(preview, level, yaw, pitch);
        bakedGeneration = reloadGeneration;
    }

    private void bake(NeutronSpacePreview preview, NeutronSpacePreviewLevel level, float yaw, float pitch) {
        Minecraft minecraft = Minecraft.getInstance();
        BlockRenderDispatcher dispatcher = minecraft.getBlockRenderer();
        Map<RenderType, BufferSink> sinks = new LinkedHashMap<>();
        PoseStack pose = new PoseStack();
        RandomSource random = RandomSource.create();
        ModelBlockRenderer.enableCaching();
        try {
            int count = Math.min(preview.positions().length, preview.states().length);
            for (int i = 0; i < count; i++) {
                int packed = preview.positions()[i];
                int x = NeutronSpacePreview.unpackX(packed);
                int y = NeutronSpacePreview.unpackY(packed);
                int z = NeutronSpacePreview.unpackZ(packed);
                BlockPos pos = new BlockPos(x, y, z);
                BlockState state = level.getBlockState(pos);
                if (state.isAir()) {
                    continue;
                }
                if (state.getRenderShape() == RenderShape.MODEL) {
                    BakedModel model = dispatcher.getBlockModel(state);
                    BlockEntity blockEntity = level.getBlockEntity(pos);
                    ModelData modelData = model.getModelData(level, pos, state,
                            blockEntity != null ? blockEntity.getModelData() : ModelData.EMPTY);
                    random.setSeed(state.getSeed(pos));
                    for (RenderType type : model.getRenderTypes(state, random, modelData)) {
                        BufferSink sink = sinks.computeIfAbsent(type, BufferSink::new);
                        pose.pushPose();
                        pose.translate(x, y, z);
                        dispatcher.renderBatched(state, pos, level, pose, sink.buffer, true, random, modelData, type);
                        pose.popPose();
                    }
                }
                FluidState fluid = state.getFluidState();
                if (!fluid.isEmpty()) {
                    RenderType type = ItemBlockRenderTypes.getRenderLayer(fluid);
                    BufferSink sink = sinks.computeIfAbsent(type, BufferSink::new);
                    dispatcher.renderLiquid(pos, level, new OffsetConsumer(sink.buffer, 0, pos.getY() & ~15, 0),
                            state, fluid);
                }
            }
        } finally {
            ModelBlockRenderer.clearCache();
        }
        for (BufferSink sink : sinks.values()) {
            MeshData mesh = sink.buffer.build();
            if (mesh == null) {
                sink.bytes.close();
                continue;
            }
            boolean translucent = sink.type == RenderType.translucent() || sink.type == RenderType.tripwire();
            MeshData.SortState sortState = translucent ? mesh.sortQuads(sink.bytes, sorting(yaw, pitch)) : null;
            VertexBuffer vertexBuffer = new VertexBuffer(VertexBuffer.Usage.STATIC);
            vertexBuffer.bind();
            vertexBuffer.upload(mesh);
            VertexBuffer.unbind();
            sink.bytes.close();
            layers.add(new Layer(sink.type, vertexBuffer, translucent, sortState));
        }
        sortedYaw = yaw;
        sortedPitch = pitch;
    }

    private void closeMesh() {
        for (Layer layer : layers) {
            layer.buffer.close();
        }
        layers.clear();
        if (sortingBuffer != null) {
            sortingBuffer.close();
            sortingBuffer = null;
        }
        sortedYaw = Float.NaN;
        sortedPitch = Float.NaN;
    }

    private record Layer(RenderType type, VertexBuffer buffer, boolean translucent, @Nullable MeshData.SortState sortState) {
    }

    private static final class BufferSink {
        private final RenderType type;
        private final ByteBufferBuilder bytes = new ByteBufferBuilder(262144);
        private final BufferBuilder buffer;

        private BufferSink(RenderType type) {
            this.type = type;
            this.buffer = new BufferBuilder(bytes, VertexFormat.Mode.QUADS, DefaultVertexFormat.BLOCK);
        }
    }

    private static final class OffsetConsumer implements VertexConsumer {
        private final VertexConsumer inner;
        private final float ox;
        private final float oy;
        private final float oz;

        private OffsetConsumer(VertexConsumer inner, float ox, float oy, float oz) {
            this.inner = inner;
            this.ox = ox;
            this.oy = oy;
            this.oz = oz;
        }

        @Override
        public VertexConsumer addVertex(float x, float y, float z) {
            return inner.addVertex(x + ox, y + oy, z + oz);
        }

        @Override
        public VertexConsumer setColor(int red, int green, int blue, int alpha) {
            inner.setColor(red, green, blue, alpha);
            return this;
        }

        @Override
        public VertexConsumer setUv(float u, float v) {
            inner.setUv(u, v);
            return this;
        }

        @Override
        public VertexConsumer setUv1(int u, int v) {
            inner.setUv1(u, v);
            return this;
        }

        @Override
        public VertexConsumer setUv2(int u, int v) {
            inner.setUv2(u, v);
            return this;
        }

        @Override
        public VertexConsumer setNormal(float normalX, float normalY, float normalZ) {
            inner.setNormal(normalX, normalY, normalZ);
            return this;
        }
    }
}
