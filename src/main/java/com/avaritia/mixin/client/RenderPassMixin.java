package com.avaritia.mixin.client;

import com.avaritia.client.shader.AvaritiaShaderUniforms;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.systems.RenderPass;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(RenderPass.class)
public abstract class RenderPassMixin {
    @Inject(method = "setPipeline", at = @At("HEAD"))
    private void avaritia$setActivePipeline(RenderPipeline pipeline, CallbackInfo ci) {
        AvaritiaShaderUniforms.setActivePipeline(pipeline);
    }
}
