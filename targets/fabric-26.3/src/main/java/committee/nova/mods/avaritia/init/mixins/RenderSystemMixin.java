package committee.nova.mods.avaritia.init.mixins;

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
 * 记录当前绑定的自定义管线，并在默认 uniform 绑定时追加 Avaritia 的 cosmic UBO。
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
}
