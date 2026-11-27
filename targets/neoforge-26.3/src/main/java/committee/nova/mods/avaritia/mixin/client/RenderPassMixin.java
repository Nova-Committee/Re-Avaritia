package committee.nova.mods.avaritia.mixin.client;

import committee.nova.mods.avaritia.client.render.RenderTypeDrawTracker;
import committee.nova.mods.avaritia.client.shader.AvaritiaShaderUniforms;
import com.mojang.renderpearl.api.commands.RenderPass;
import net.minecraft.client.renderer.StagedVertexBuffer;
import net.minecraft.client.renderer.oit.OitStage;
import net.minecraft.client.renderer.rendertype.PreparedRenderType;
import net.minecraft.client.renderer.rendertype.RenderType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * @author cnlimiter
 * <p>
 * 26.3 适配说明：原注入目标 {@code RenderPass#setPipeline(RenderPipeline)} 已失效
 * （改为抽象方法、入参为 {@code CompiledRenderPipeline}），该职责已迁移到
 * {@link RenderSystemMixin} 的 {@code getCompiledPipeline} 注入。
 * <p>
 * 本类改为注入 {@link PreparedRenderType} 的绘制方法（原 {@code RenderTypeMixin} 的
 * {@code RenderType#draw} 注入目标已在 26.3 移除）：在每段几何绘制前后切换当前
 * {@link RenderType}，供 {@code AvaritiaShaderUniforms.bindIfAvaritia} 选取
 * per-render-type uniform。RenderType 实例经 {@link RenderTypeDrawTracker} 还原。
 */
@Mixin(PreparedRenderType.class)
public abstract class RenderPassMixin {

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
