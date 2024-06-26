package committee.nova.mods.avaritia.init.mixins;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import committee.nova.mods.avaritia.api.client.render.item.IItemRenderer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.client.ForgeHooksClient;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
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

    @Shadow @Final private Minecraft minecraft;
    @Unique
    ItemStack avaritia$stack;
    @Unique
    VertexConsumer avaritia$vertexConsumer;

    @Inject(
            method = "render",
            at = @At("HEAD"),
            cancellable = true
    )
    public void onRenderItem(ItemStack stack, ItemDisplayContext context, boolean leftHand, PoseStack mStack, MultiBufferSource buffers, int packedLight, int packedOverlay, BakedModel modelIn, CallbackInfo ci) {
        if (modelIn instanceof IItemRenderer iItemRenderer) {
            mStack.pushPose();
            IItemRenderer renderer = (IItemRenderer) iItemRenderer.applyTransform(context, mStack, leftHand);
            mStack.translate(-0.5D, -0.5D, -0.5D);
            renderer.renderItem(stack, context, mStack, buffers, packedLight, packedOverlay);
            mStack.popPose();
            ci.cancel();
        }
    }

}
