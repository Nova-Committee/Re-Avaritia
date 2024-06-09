package committee.nova.mods.avaritia.init.mixins;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.MatrixUtil;
import committee.nova.mods.avaritia.api.client.render.item.IItemRenderer;
import committee.nova.mods.avaritia.client.shader.AvaritiaShaders;
import committee.nova.mods.avaritia.init.registry.ModItems;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
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

    @Shadow public abstract void renderModelLists(BakedModel pModel, ItemStack pStack, int pCombinedLight, int pCombinedOverlay, PoseStack pPoseStack, VertexConsumer pBuffer);

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
        if (modelIn instanceof IItemRenderer) {
            mStack.pushPose();
            IItemRenderer renderer = (IItemRenderer) ForgeHooksClient.handleCameraTransforms(mStack, modelIn, context, leftHand);
            mStack.translate(-0.5D, -0.5D, -0.5D);
            renderer.renderItem(stack, context, mStack, buffers, packedLight, packedOverlay);
            mStack.popPose();
            ci.cancel();
        }

        //        if (stack.is(ModItems.infinity_sword.get())) {
//            mStack.pushPose();
//            mStack.translate(-0.5D, -0.5D, -0.5D);
//            ItemRenderer itemRenderer = minecraft.getItemRenderer();
//            RenderType rType = ItemBlockRenderTypes.getRenderType(stack, true);
//            VertexConsumer builder = ItemRenderer.getFoilBuffer(buffers, rType, true, stack.hasFoil());
//            itemRenderer.renderModelLists(modelIn, stack, packedLight, packedOverlay, mStack, builder);
//            if (buffers instanceof MultiBufferSource.BufferSource bs) {
//                bs.endBatch();
//            }
//
//            float yaw = 0.0f;
//            float pitch = 0.0f;
//            float scale = 1.0f;
//            if (context != ItemDisplayContext.GUI) {
//                yaw = (float)((double)(minecraft.player.getYRot() * 2.0F) * 3.141592653589793D / 360.0D);
//                pitch = -((float)((double)(minecraft.player.getXRot() * 2.0F) * 3.141592653589793D / 360.0D));
//                MatrixUtil.mulComponentWise(mStack.last().pose(), 1F);
//            } else {
//                MatrixUtil.mulComponentWise(mStack.last().pose(), 25F);
//            }
//            if (AvaritiaShaders.cosmicOpacity != null) {
//                AvaritiaShaders.cosmicOpacity.glUniform1f(1.0F);
//            }
//            AvaritiaShaders.cosmicYaw.glUniform1f(yaw);
//            AvaritiaShaders.cosmicPitch.glUniform1f(pitch);
//            AvaritiaShaders.cosmicExternalScale.glUniform1f(scale);
//            final VertexConsumer cons = buffers.getBuffer(AvaritiaShaders.COSMIC_RENDER_TYPE);
//            itemRenderer.renderModelLists(modelIn, stack, packedLight, packedOverlay, mStack, cons);
//
//            mStack.popPose();
//            ci.cancel();
//        }
    }


//    @Inject(
//            method = "render",
//            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/entity/ItemRenderer;renderModelLists(Lnet/minecraft/client/resources/model/BakedModel;Lnet/minecraft/world/item/ItemStack;IILcom/mojang/blaze3d/vertex/PoseStack;Lcom/mojang/blaze3d/vertex/VertexConsumer;)V",
//            shift = At.Shift.BEFORE
//            )
//    )
//    public void onRenderItem0(ItemStack stack, ItemDisplayContext context, boolean leftHand, PoseStack mStack, MultiBufferSource buffers, int packedLight, int packedOverlay, BakedModel modelIn, CallbackInfo ci) {
//        this.avaritia$stack = stack;
//        if (stack.is(ModItems.infinity_sword.get())) {
//            mStack.pushPose();
//            float yaw = 0.0f;
//            float pitch = 0.0f;
//            float scale = 1.0f;
//            if (context != ItemDisplayContext.GUI) {
//                yaw = (float)((double)(minecraft.player.getYRot() * 2.0F) * 3.141592653589793D / 360.0D);
//                pitch = -((float)((double)(minecraft.player.getXRot() * 2.0F) * 3.141592653589793D / 360.0D));
//                MatrixUtil.mulComponentWise(mStack.last().pose(), 1F);
//            } else {
//                MatrixUtil.mulComponentWise(mStack.last().pose(), 25F);
//            }
//            if (AvaritiaShaders.cosmicOpacity != null) {
//                AvaritiaShaders.cosmicOpacity.glUniform1f(1.0F);
//            }
//            AvaritiaShaders.cosmicYaw.glUniform1f(yaw);
//            AvaritiaShaders.cosmicPitch.glUniform1f(pitch);
//            AvaritiaShaders.cosmicExternalScale.glUniform1f(scale);
//            avaritia$vertexConsumer = buffers.getBuffer(AvaritiaShaders.COSMIC_RENDER_TYPE);
//            mStack.popPose();
//            //this.renderModelLists(modelIn, stack, packedLight, packedOverlay, mStack, cons);
//            //ci.cancel();
//        }
//    }

//    @ModifyArg(
//            method = "render",
//            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/entity/ItemRenderer;renderModelLists(Lnet/minecraft/client/resources/model/BakedModel;Lnet/minecraft/world/item/ItemStack;IILcom/mojang/blaze3d/vertex/PoseStack;Lcom/mojang/blaze3d/vertex/VertexConsumer;)V"
//            ),
//            index = 5
//    )
//    public VertexConsumer onRenderItem1(VertexConsumer pBuffer) {
//        if (avaritia$stack.is(ModItems.infinity_sword.get())) {
//            return avaritia$vertexConsumer;
//        }
//        return pBuffer;
//    }

}
