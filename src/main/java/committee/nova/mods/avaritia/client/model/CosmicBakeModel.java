package committee.nova.mods.avaritia.client.model;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.math.Transformation;
import committee.nova.mods.avaritia.api.client.model.IItemRenderer;
import committee.nova.mods.avaritia.api.client.model.PerspectiveModelState;
import committee.nova.mods.avaritia.api.client.model.WrappedItemModel;
import committee.nova.mods.avaritia.client.shader.AvaritiaShaders;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.*;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.client.model.SimpleModelState;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Name: Avaritia-forge / CosmicbakeModel
 * Author: cnlimiter
 * CreateTime: 2023/9/24 4:41
 * Description:
 */

public class CosmicBakeModel extends WrappedItemModel implements IItemRenderer {
    private final List<BakedQuad> maskQuads;
    public static final SimpleModelState IDENTITY = new SimpleModelState(Transformation.identity());
    private final ItemOverrides overrideList = new ItemOverrides() {
        public BakedModel resolve(@NotNull BakedModel originalModel, @NotNull ItemStack stack, @javax.annotation.Nullable ClientLevel world, @javax.annotation.Nullable LivingEntity entity, int seed) {
            CosmicBakeModel.this.entity = entity;
            CosmicBakeModel.this.world = world == null ? (entity == null ? null : (ClientLevel)entity.level()) : null;
            return CosmicBakeModel.this.wrapped.getOverrides().resolve(originalModel, stack, world, entity, seed);
        }
    };
    private static final ItemModelGenerator ITEM_MODEL_GENERATOR = new ItemModelGenerator();
    private static final FaceBakery FACE_BAKERY = new FaceBakery();

    public CosmicBakeModel(BakedModel wrapped, TextureAtlasSprite maskSprite) {
        super(wrapped);
        this.maskQuads = bakeItem(maskSprite);
    }

    @Override
    public void renderItem(ItemStack stack, ItemDisplayContext transformType, PoseStack pStack, MultiBufferSource source, int packedLight, int packedOverlay) {
        this.renderWrapped(stack, pStack, source, packedLight, packedOverlay, true);
        if (source instanceof MultiBufferSource.BufferSource) {
            MultiBufferSource.BufferSource bs = (MultiBufferSource.BufferSource)source;
            bs.endBatch();
        }

        RenderType cosmicRenderType = RenderType.create("avaritia:cosmic", DefaultVertexFormat.BLOCK, VertexFormat.Mode.QUADS, 2097152, true, false, RenderType.CompositeState.builder().setShaderState(new RenderStateShard.ShaderStateShard(() -> AvaritiaShaders.cosmicShader)).setDepthTestState(RenderStateShard.EQUAL_DEPTH_TEST).setLightmapState(RenderStateShard.LIGHTMAP).setTransparencyState(RenderStateShard.TRANSLUCENT_TRANSPARENCY).setTextureState(RenderStateShard.BLOCK_SHEET_MIPPED).createCompositeState(true));
        float yaw = 0.0F;
        float pitch = 0.0F;
        float scale = 1.0F;
        if (transformType != ItemDisplayContext.GUI) {
            Minecraft mc = Minecraft.getInstance();
            yaw = (float)((double)(mc.player.getYRot() * 2.0F) * 3.141592653589793D / 360.0D);
            pitch = -((float)((double)(mc.player.getXRot() * 2.0F) * 3.141592653589793D / 360.0D));
        } else {
            scale = 25.0F;
        }

        if (AvaritiaShaders.cosmicOpacity != null) {
            AvaritiaShaders.cosmicOpacity.set(1.0F);
        }

        AvaritiaShaders.cosmicYaw.set(yaw);
        AvaritiaShaders.cosmicPitch.set(pitch);
        AvaritiaShaders.cosmicExternalScale.set(scale);
        ItemRenderer itemRenderer = Minecraft.getInstance().getItemRenderer();
        VertexConsumer cons = source.getBuffer(cosmicRenderType);
        itemRenderer.renderQuadList(pStack, cons, this.maskQuads, stack, packedLight, packedOverlay);
    }


    @Override
    public @Nullable PerspectiveModelState getModelState() {
        return (PerspectiveModelState) this.parentState;
    }

    @Override
    public boolean useAmbientOcclusion() {
        return this.wrapped.useAmbientOcclusion();
    }

    @Override
    public boolean isGui3d() {
        return this.wrapped.isGui3d();
    }

    @Override
    public boolean usesBlockLight() {
        return this.wrapped.usesBlockLight();
    }

    @Override
    public @NotNull ItemOverrides getOverrides() {
        return this.overrideList;
    }

    private static List<BakedQuad> bakeItem(TextureAtlasSprite... sprites) {
        return bakeItem(Transformation.identity(), sprites);
    }

    private static List<BakedQuad> bakeItem(Transformation state, TextureAtlasSprite... sprites) {
        List<BakedQuad> quads = new LinkedList<>();

        for(int i = 0; i < sprites.length; ++i) {
            TextureAtlasSprite sprite = sprites[i];
            List<BlockElement> unbaked = ITEM_MODEL_GENERATOR.processFrames(i, "layer" + i, sprite.contents());

            for (BlockElement element : unbaked) {

                for (Map.Entry<Direction, BlockElementFace> directionBlockElementFaceEntry : element.faces.entrySet()) {
                    quads.add(FACE_BAKERY.bakeQuad(element.from, element.to, directionBlockElementFaceEntry.getValue(), sprite, directionBlockElementFaceEntry.getKey(), IDENTITY, element.rotation, element.shade, new ResourceLocation("avaritia", "dynamic")));
                }
            }
        }

        return quads;
    }
}
