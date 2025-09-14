package committee.nova.mods.avaritia.client.model;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import committee.nova.mods.avaritia.Const;
import committee.nova.mods.avaritia.api.client.model.CachedFormat;
import committee.nova.mods.avaritia.api.client.model.PerspectiveModelState;
import committee.nova.mods.avaritia.api.client.model.Quad;
import committee.nova.mods.avaritia.api.client.model.bakedmodels.WrappedItemModel;
import committee.nova.mods.avaritia.api.client.render.buffer.AlphaOverrideVertexConsumer;
import committee.nova.mods.avaritia.api.client.util.TransformUtils;
import committee.nova.mods.avaritia.api.client.util.colour.ColourARGB;
import committee.nova.mods.avaritia.client.shader.AvaritiaRenderTypes;
import committee.nova.mods.avaritia.client.shader.AvaritiaShaders;
import committee.nova.mods.avaritia.common.item.resources.MatterClusterItem;
import committee.nova.mods.avaritia.init.registry.ModItems;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;


public class HaloEternalBakedModel extends WrappedItemModel {

    public static final float[] ETERNAL_UVS = new float[40];
    private final Random random;
    private final BakedQuad haloQuad;
    private final boolean pulse;
    private final List<ResourceLocation> maskSprite;

    public HaloEternalBakedModel(BakedModel wrapped, TextureAtlasSprite sprite, int color, int size, boolean pulse, List<ResourceLocation> maskSprite) {
        super(wrapped);
        this.random = new Random();
        this.haloQuad = generateHaloQuad(sprite, size, color);
        this.pulse = pulse;
        this.maskSprite = maskSprite;
    }

    static BakedQuad generateHaloQuad(final TextureAtlasSprite sprite, final int size, final int color) {
        final float[] colors = new ColourARGB(color).getRGBA();
        final double spread = size / 16.0;
        final double min = 0.0 - spread;
        final double max = 1.0 + spread;
        final float minU = sprite.getU0();
        final float maxU = sprite.getU1();
        final float minV = sprite.getV0();
        final float maxV = sprite.getV1();
        final Quad quad = new Quad();
        quad.reset(CachedFormat.BLOCK);
        quad.setTexture(sprite);
        putVertex(quad.vertices[0], max, max, 0.0, maxU, minV);
        putVertex(quad.vertices[1], min, max, 0.0, minU, minV);
        putVertex(quad.vertices[2], min, min, 0.0, minU, maxV);
        putVertex(quad.vertices[3], max, min, 0.0, maxU, maxV);
        for (int i = 0; i < 4; ++i) {
            System.arraycopy(colors, 0, quad.vertices[i].color, 0, 4);
        }
        quad.calculateOrientation(true);
        return quad.bake();
    }

    static void putVertex(final Quad.Vertex vx, final double x, final double y, final double z, final double u, final double v) {
        vx.vec[0] = (float) x;
        vx.vec[1] = (float) y;
        vx.vec[2] = (float) z;
        vx.uv[0] = (float) u;
        vx.uv[1] = (float) v;
    }

    @Override
    public void renderItem(ItemStack stack, ItemDisplayContext transformType, PoseStack pStack, MultiBufferSource source, int light, int overlay) {
        if (stack.getItem() == ModItems.infinity_umbrella.get()) {
            this.parentState = TransformUtils.DEFAULT_TOOL;
        } else if (stack.getItem() == ModItems.infinity_sword.get()) {
            this.parentState = TransformUtils.DEFAULT_TOOL;
        } else if (stack.getItem() == ModItems.infinity_bow.get() || stack.getItem() == ModItems.infinity_crossbow.get()) {
            this.parentState = TransformUtils.DEFAULT_BOW;
        } else {
            this.parentState = TransformUtils.DEFAULT_ITEM;
        }

        // 渲染Halo效果
        if (transformType == ItemDisplayContext.GUI) {
            Minecraft.getInstance().getItemRenderer()
                    .renderQuadList(pStack, source.getBuffer(ItemBlockRenderTypes.getRenderType(stack, true)), List.of(this.haloQuad), stack, light, overlay);
            if (this.pulse) {
                pStack.pushPose();
                double scale = random.nextDouble() * 0.15D + 0.95D;
                double trans = (1.0D - scale) / 2.0D;
                pStack.translate(trans, trans, 0.0D);
                pStack.scale((float) scale, (float) scale, 1.0001F);
                this.renderWrapped(stack, pStack, source, light, overlay, true, (e) -> new AlphaOverrideVertexConsumer(e, 0.6000000238418579D));
                pStack.popPose();
            }
        }

        // 渲染基础模型
        this.renderWrapped(stack, pStack, source, light, overlay, true);
        if (source instanceof MultiBufferSource.BufferSource bs) {
            bs.endBatch();
        }

        // 渲染Eternal效果
        final Minecraft mc = Minecraft.getInstance();
        float yaw = 0.0f;
        float pitch = 0.0f;
        float scale = 1f;
        if (AvaritiaShaders.inventoryRender || transformType == ItemDisplayContext.GUI) {
            scale = 100.0F;
        } else {
            yaw = (float) (mc.player.getYRot() * 2.0f * Math.PI / 360.0);
            pitch = -(float) (mc.player.getXRot() * 2.0f * Math.PI / 360.0);
        }

        AvaritiaShaders.eternalTime
                .set((System.currentTimeMillis() - AvaritiaShaders.renderTime) / 2000.0F);
        AvaritiaShaders.eternalYaw.set(yaw);
        AvaritiaShaders.eternalPitch.set(pitch);
        AvaritiaShaders.eternalExternalScale.set(scale);

        if (stack.getItem() == ModItems.matter_cluster.get()) {
            AvaritiaShaders.eternalOpacity.set(MatterClusterItem.getClusterSize(stack) / (float) MatterClusterItem.CAPACITY);
        } else {
            AvaritiaShaders.eternalOpacity.set(1.5F);
        }

        for (int i = 0; i < 10; ++i) {
            TextureAtlasSprite sprite = Minecraft.getInstance().getTextureAtlas(InventoryMenu.BLOCK_ATLAS).apply(Const.rl("misc/cosmic_" + i));
            ETERNAL_UVS[i * 4] = sprite.getU0();
            ETERNAL_UVS[i * 4 + 1] = sprite.getV0();
            ETERNAL_UVS[i * 4 + 2] = sprite.getU1();
            ETERNAL_UVS[i * 4 + 3] = sprite.getV1();
        }
        if (AvaritiaShaders.eternalUVs != null) {
            AvaritiaShaders.eternalUVs.set(ETERNAL_UVS);
        }

        final VertexConsumer cons = source.getBuffer(AvaritiaRenderTypes.ETERNAL);
        List<TextureAtlasSprite> atlasSprite = new ArrayList<>();
        for (ResourceLocation res : maskSprite) {
            atlasSprite.add(Minecraft.getInstance().getTextureAtlas(InventoryMenu.BLOCK_ATLAS).apply(res));
        }
        mc.getItemRenderer().renderQuadList(pStack, cons, bakeItem(atlasSprite), stack, light, overlay);
    }

    @Override
    public @Nullable PerspectiveModelState getModelState() {
        return (PerspectiveModelState) this.parentState;
    }

    @Override
    public boolean isCosmic() {
        return true;
    }
}
