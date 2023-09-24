package committee.nova.mods.avaritia.api.client.model;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Transformation;
import committee.nova.mods.avaritia.util.client.TransformUtil;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.world.item.ItemDisplayContext;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;

/**
 * Name: Avaritia-forge / PerspectiveModel
 * Author: cnlimiter
 * CreateTime: 2023/9/24 5:08
 * Description:
 */

public interface PerspectiveModel extends BakedModel{
    @Nullable
    PerspectiveModelState getModelState();

    @Override
    default @NotNull BakedModel applyTransform(@NotNull ItemDisplayContext transformType, @NotNull PoseStack pStack, boolean leftFlip) {
        PerspectiveModelState modelState = getModelState();
        if (modelState != null) {
            Transformation transform = getModelState().getTransform(transformType);

            Vector3f trans = transform.getTranslation();
            pStack.translate(trans.x(), trans.y(), trans.z());

            pStack.mulPose(transform.getLeftRotation());

            Vector3f scale = transform.getScale();
            pStack.scale(scale.x(), scale.y(), scale.z());

            pStack.mulPose(transform.getRightRotation());

            if (leftFlip) {
                TransformUtil.applyLeftyFlip(pStack);
            }
            return this;
        }
        return BakedModel.super.applyTransform(transformType, pStack, leftFlip);
    }
}
