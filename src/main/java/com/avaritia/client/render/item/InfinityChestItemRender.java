package com.avaritia.client.render.item;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.serialization.MapCodec;
import com.avaritia.Const;
import com.avaritia.api.client.util.TextureUtils;
import com.avaritia.client.render.tile.InfinityChestBlockRender;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.special.NoDataSpecialModelRenderer;
import net.minecraft.client.renderer.special.SpecialModelRenderer;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import org.joml.Vector3fc;
import org.jetbrains.annotations.NotNull;

import java.util.function.Consumer;

/**
 * @author cnlimiter
 */
public class InfinityChestItemRender implements NoDataSpecialModelRenderer {
    private final ModelPart lid;
    private final ModelPart lock;
    private final ModelPart bottom;

    public InfinityChestItemRender(EntityModelSet entityModelSet) {
        ModelPart root = entityModelSet.bakeLayer(InfinityChestBlockRender.INFINITY_CHEST);
        this.lid = root.getChild("lid");
        this.lock = root.getChild("lock");
        this.bottom = root.getChild("bottom");
    }

    @Override
    public void submit(@NotNull PoseStack poseStack, @NotNull SubmitNodeCollector output, int packedLight, int packedOverlay, boolean hasFoilType, int outlineColor) {
        this.lid.xRot = 0.0F;
        this.lock.xRot = 0.0F;
        TextureAtlasSprite sprite = TextureUtils.getTexture(Const.rl("block/chest/infinity_chest"));
        var renderType = RenderTypes.entityCutout(TextureAtlas.LOCATION_BLOCKS);
        output.submitModelPart(this.lid, poseStack, renderType, packedLight, packedOverlay, sprite, outlineColor, null);
        output.submitModelPart(this.lock, poseStack, renderType, packedLight, packedOverlay, sprite, outlineColor, null);
        output.submitModelPart(this.bottom, poseStack, renderType, packedLight, packedOverlay, sprite, outlineColor, null);
    }

    @Override
    public void getExtents(@NotNull Consumer<Vector3fc> output) {
    }

    public static record Unbaked() implements SpecialModelRenderer.Unbaked<Void> {
        public static final MapCodec<InfinityChestItemRender.Unbaked> MAP_CODEC = MapCodec.unit(new InfinityChestItemRender.Unbaked());

        @Override
        public MapCodec<? extends SpecialModelRenderer.Unbaked<Void>> type() {
            return MAP_CODEC;
        }

        @Override
        public SpecialModelRenderer<Void> bake(SpecialModelRenderer.BakingContext context) {
            return new InfinityChestItemRender(context.entityModelSet());
        }
    }
}
