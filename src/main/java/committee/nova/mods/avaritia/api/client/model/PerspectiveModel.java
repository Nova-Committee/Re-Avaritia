package committee.nova.mods.avaritia.api.client.model;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.mojang.math.Transformation;
import committee.nova.mods.avaritia.util.client.TransformUtils;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.world.item.ItemDisplayContext;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;

/**
 * A simple {@link BakedModel} implementation, with automatic handling of
 * {@link PerspectiveModelState}s.
 * <p>
 * Created by covers1624 on 9/7/22.
 *
 * @see TransformUtils
 */
public interface PerspectiveModel extends BakedModel {

    /**
     * The {@link PerspectiveModelState} for this model.
     *
     * @return The state or {@code null} for vanilla behaviour.
     */
    @Nullable
    PerspectiveModelState getModelState();

    @Override
    default @NotNull BakedModel applyTransform(@NotNull ItemDisplayContext context, @NotNull PoseStack pStack, boolean leftFlip) {
        PerspectiveModelState modelState = getModelState();
        Transformation transform = getModelState().getTransform(context);
        Vector3f trans = transform.getTranslation();
        Vector3f scale = transform.getScale();
        if (modelState != null) {
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
