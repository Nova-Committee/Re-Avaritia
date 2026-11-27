package committee.nova.mods.avaritia.api.client.model;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.mojang.math.Transformation;
import committee.nova.mods.avaritia.api.client.util.TextureUtils;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;

import java.util.Collections;
import java.util.List;

/**
 * @Project: Avaritia
 * @Author: cnlimiter
 * @CreateTime: 2024/11/14 22:58
 * @Description:
 */
public interface PerspectiveModel extends BakedModel {

    /**
     * The {@link PerspectiveModelState} for this model.
     *
     * @return The state or {@code null} for vanilla behaviour.
     */
    @Nullable
    PerspectiveModelState getModelState();

    void renderItem(ItemStack stack, ItemDisplayContext ctx, PoseStack mStack, MultiBufferSource source, int packedLight, int packedOverlay);

    //Useless methods for IItemRenderer.
    //@formatter:off
    @Override default @NotNull List<BakedQuad> getQuads(BlockState state, Direction side, @NotNull RandomSource rand) { return Collections.emptyList(); }
    @Override default boolean isCustomRenderer() { return true; }
    @Override default @NotNull TextureAtlasSprite getParticleIcon() { return TextureUtils.getMissingSprite(); }
    @Override default@NotNull ItemOverrides getOverrides() { return ItemOverrides.EMPTY; }
    //@formatter:on

    @Override
    default @NotNull BakedModel applyTransform(@NotNull ItemDisplayContext context, @NotNull PoseStack pStack, boolean leftFlip) {
        PerspectiveModelState modelState = getModelState();
        if (modelState != null) {
            Transformation transform = getModelState().getTransform(context);
            Vector3f trans = transform.getTranslation();
            Vector3f scale = transform.getScale();
            pStack.translate(trans.x(), trans.y(), trans.z());
            pStack.mulPose(transform.getLeftRotation());
            pStack.scale(scale.x(), scale.y(), scale.z());
            pStack.mulPose(transform.getRightRotation());

            if (leftFlip) {
                pStack.mulPose(Axis.YN.rotationDegrees(180.0f));
                //TransformUtils.applyLeftyFlip(pStack);
            }
            return this;
        }
        return BakedModel.super.applyTransform(context, pStack, leftFlip);
    }
}
