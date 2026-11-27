package committee.nova.mods.avaritia.mixin.client;

import committee.nova.mods.avaritia.client.render.RenderTypeDrawTracker;
import committee.nova.mods.avaritia.client.shader.AvaritiaShaderUniforms;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.renderpearl.api.commands.RenderPass;
import com.mojang.renderpearl.api.pipeline.CompiledRenderPipeline;
import com.mojang.renderpearl.api.pipeline.RenderPipeline;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * @author cnlimiter
 * <p>
 * 26.3 适配说明：
 * <ul>
 *   <li>原 {@code RenderPass#setPipeline(RenderPipeline)} 注入（RenderPassMixin）失效——
 *       {@code RenderPass.setPipeline} 改为抽象方法且入参为 {@link CompiledRenderPipeline}，
 *       无法再拿到 {@link RenderPipeline} 实例。所有绘制路径均通过
 *       {@code RenderSystem.getCompiledPipeline(RenderPipeline)} 解析后调用 setPipeline，
 *       故改为在该方法上注入，保持"记录当前绑定管线"的原语义。</li>
 *   <li>原 {@code flipFrame} 注入失效——26.3 移除了 {@code RenderSystem.flipFrame}。
 *       帧末清理改挂在 {@code ensurePipelineModifiersEmpty}（由
 *       {@code ClientHooks.fireRenderFramePost} 在 gameRenderer.render() 之后每帧调用一次）。</li>
 * </ul>
 */
@Mixin(RenderSystem.class)
public abstract class RenderSystemMixin {
    @Inject(method = "getCompiledPipeline", at = @At("RETURN"))
    private static void avaritia$setActivePipeline(RenderPipeline pipeline, CallbackInfoReturnable<CompiledRenderPipeline> cir) {
        AvaritiaShaderUniforms.setActivePipeline(pipeline);
    }

    @Inject(method = "bindDefaultUniforms", at = @At("TAIL"))
    private static void avaritia$bindUniforms(RenderPass renderPass, CallbackInfo ci) {
        AvaritiaShaderUniforms.bindIfAvaritia(renderPass);
    }

    @Inject(method = "ensurePipelineModifiersEmpty", at = @At("TAIL"))
    private static void avaritia$endFrame(CallbackInfo ci) {
        AvaritiaShaderUniforms.endFrame();
        RenderTypeDrawTracker.clear();
    }
}
