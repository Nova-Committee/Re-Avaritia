package committee.nova.mods.avaritia.init.mixins;

import com.mojang.blaze3d.vertex.PoseStack;
import committee.nova.mods.avaritia.api.client.model.PerspectiveModel;
import net.minecraft.client.renderer.ItemModelShaper;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * IItemRendererMixin
 *
 * @author cnlimiter
 * @version 1.0
 * @description
 * @date 2024/4/2 20:37
 */
@Mixin(ItemRenderer.class)
public abstract class ItemRendererMixin {
    @Shadow
    @Final
    private ItemModelShaper itemModelShaper;

    @Shadow
    @Final
    private TextureManager textureManager;

    @Inject(
            method = "render",
            at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/vertex/PoseStack;pushPose()V", ordinal = 0)
    )
    public void avaritia$onRenderItem(ItemStack stack, ItemDisplayContext context, boolean leftHand, PoseStack mStack, MultiBufferSource buffers, int packedLight, int packedOverlay, BakedModel modelIn, CallbackInfo ci) {
        if (modelIn instanceof PerspectiveModel model) {
            mStack.pushPose();
            final PerspectiveModel transformModel = (PerspectiveModel) model.applyTransform(context, mStack, leftHand);
            mStack.translate(-0.5D, -0.5D, -0.5D);
            transformModel.renderItem(stack, context, mStack, buffers, packedLight, packedOverlay, this.itemModelShaper, this.textureManager);
            mStack.popPose();
        }
    }

}
