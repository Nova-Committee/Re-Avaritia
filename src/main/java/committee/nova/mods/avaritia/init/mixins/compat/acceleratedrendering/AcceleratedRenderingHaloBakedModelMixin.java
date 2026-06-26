package committee.nova.mods.avaritia.init.mixins.compat.acceleratedrendering;

import committee.nova.mods.avaritia.Const;
import committee.nova.mods.avaritia.client.model.loader.HaloBakedModel;
import committee.nova.mods.avaritia.client.model.loader.HaloCosmicBakedModel;
import committee.nova.mods.avaritia.client.model.loader.HaloEternalBakedModel;
import committee.nova.mods.avaritia.init.compat.acceleratedrendering.AcceleratedRenderingCompat;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.ItemModelShaper;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = {HaloBakedModel.class, HaloCosmicBakedModel.class, HaloEternalBakedModel.class}, remap = false)
public abstract class AcceleratedRenderingHaloBakedModelMixin {
    @Unique
    private int acceleratedRenderingHaloDepth;

    @Inject(method = "renderItem", at = @At("HEAD"))
    private void beginAcceleratedRenderingHaloCompat(ItemStack stack, ItemDisplayContext itemDisplayContext, PoseStack poseStack, MultiBufferSource bufferSource,
                                                        int packedLight, int packedOverlay, ItemModelShaper itemModelShaper, TextureManager textureManager,
                                                        CallbackInfo ci) {
        if (Const.acceleratedrendering && AcceleratedRenderingCompat.beginHaloRender()) {
            this.acceleratedRenderingHaloDepth++;
        }
    }

    @Inject(method = "renderItem", at = @At("RETURN"))
    private void endAcceleratedRenderingHaloCompat(ItemStack stack, ItemDisplayContext itemDisplayContext, PoseStack poseStack, MultiBufferSource bufferSource,
                                                        int packedLight, int packedOverlay, ItemModelShaper itemModelShaper, TextureManager textureManager,
                                                        CallbackInfo ci) {
        if (Const.acceleratedrendering && this.acceleratedRenderingHaloDepth > 0) {
            AcceleratedRenderingCompat.endHaloRender();
            this.acceleratedRenderingHaloDepth--;
        }
    }
}