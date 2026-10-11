package committee.nova.mods.avaritia.mixin.client;

import committee.nova.mods.avaritia.client.shader.AvaritiaShaderUniforms;
import com.mojang.blaze3d.opengl.GlRenderPass;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

// RenderPass is an interface in 1.21.11. Mixing into the interface passes the PREPARE stage but
// then blows up during APPLY with "Unexpected NullPointerException preparing @Inject annotation",
// because Mixin cannot graft a concrete method body onto an interface that way. Targeting the
// real implementation avoids that entirely; NeoForge's ValidationRenderPass wraps GlRenderPass
// and delegates, so hooking GlRenderPass covers both.
@Mixin(GlRenderPass.class)
public abstract class RenderPassMixin {
    @Inject(method = "setPipeline", at = @At("HEAD"))
    private void avaritia$setActivePipeline(RenderPipeline pipeline, CallbackInfo ci) {
        AvaritiaShaderUniforms.setActivePipeline(pipeline);
    }
}
