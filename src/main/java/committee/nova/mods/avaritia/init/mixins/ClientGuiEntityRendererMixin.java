package committee.nova.mods.avaritia.init.mixins;

import com.mojang.blaze3d.vertex.PoseStack;
import committee.nova.mods.avaritia.client.AvaritiaClient;
import net.minecraft.client.gui.render.pip.GuiEntityRenderer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.state.gui.pip.GuiEntityRenderState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * 26.3 GUI 内实体渲染（背包玩家预览等）统一走 GuiEntityRenderer.renderToTexture，
 * 盔甲渲染层在其中同步提交，因此在此标记 inventoryRender，
 * 使 InfinityArmorModel 使用 inventory 专用的 cosmic scale/yaw/pitch。
 */
@Mixin(GuiEntityRenderer.class)
public abstract class ClientGuiEntityRendererMixin {
    @Inject(method = "renderToTexture(Lnet/minecraft/client/renderer/state/gui/pip/GuiEntityRenderState;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;)V",
            at = @At("HEAD"))
    private void avaritia$enterInventoryRender(GuiEntityRenderState state, PoseStack poseStack, SubmitNodeCollector collector, CallbackInfo ci) {
        AvaritiaClient.inventoryRender = true;
    }

    @Inject(method = "renderToTexture(Lnet/minecraft/client/renderer/state/gui/pip/GuiEntityRenderState;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;)V",
            at = @At("RETURN"))
    private void avaritia$exitInventoryRender(GuiEntityRenderState state, PoseStack poseStack, SubmitNodeCollector collector, CallbackInfo ci) {
        AvaritiaClient.inventoryRender = false;
    }
}
