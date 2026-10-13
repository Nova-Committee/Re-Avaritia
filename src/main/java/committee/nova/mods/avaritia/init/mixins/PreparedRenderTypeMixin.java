package committee.nova.mods.avaritia.init.mixins;

import committee.nova.mods.avaritia.client.render.RenderTypeDrawTracker;
import committee.nova.mods.avaritia.client.shader.AvaritiaShaderUniforms;
import net.minecraft.client.renderer.StagedVertexBuffer;
import net.minecraft.client.renderer.oit.OitStage;
import net.minecraft.client.renderer.rendertype.PreparedRenderType;
import net.minecraft.client.renderer.rendertype.RenderType;
import com.mojang.renderpearl.api.commands.RenderPass;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * 在每段几何绘制前后切换当前 {@link RenderType}，供
 * {@code AvaritiaShaderUniforms.bindIfAvaritia} 选取 per-render-type uniform。
 * 不绑定该 UBO 时，自定义管线在 RenderPearl 校验阶段因 bufferView 为 null 崩溃。
 */
@Mixin(PreparedRenderType.class)
public abstract class PreparedRenderTypeMixin {

    @Inject(method = "drawFromBuffer", at = @At("HEAD"))
    private void avaritia$setActiveRenderType(StagedVertexBuffer.ExecuteInfo info, RenderPass renderPass, CallbackInfo ci) {
        RenderType renderType = RenderTypeDrawTracker.get((PreparedRenderType) (Object) this);
        if (renderType != null) {
            AvaritiaShaderUniforms.setActiveRenderType(renderType);
        }
    }

    @Inject(method = "drawFromBuffer", at = @At("TAIL"))
    private void avaritia$clearActiveRenderType(StagedVertexBuffer.ExecuteInfo info, RenderPass renderPass, CallbackInfo ci) {
        RenderType renderType = RenderTypeDrawTracker.get((PreparedRenderType) (Object) this);
        if (renderType != null) {
            AvaritiaShaderUniforms.clearActiveRenderType(renderType);
        }
    }

    @Inject(method = "drawFromBufferOit", at = @At("HEAD"))
    private void avaritia$setActiveRenderTypeOit(StagedVertexBuffer.ExecuteInfo info, OitStage stage, RenderPass renderPass, CallbackInfo ci) {
        RenderType renderType = RenderTypeDrawTracker.get((PreparedRenderType) (Object) this);
        if (renderType != null) {
            AvaritiaShaderUniforms.setActiveRenderType(renderType);
        }
    }

    @Inject(method = "drawFromBufferOit", at = @At("TAIL"))
    private void avaritia$clearActiveRenderTypeOit(StagedVertexBuffer.ExecuteInfo info, OitStage stage, RenderPass renderPass, CallbackInfo ci) {
        RenderType renderType = RenderTypeDrawTracker.get((PreparedRenderType) (Object) this);
        if (renderType != null) {
            AvaritiaShaderUniforms.clearActiveRenderType(renderType);
        }
    }
}
