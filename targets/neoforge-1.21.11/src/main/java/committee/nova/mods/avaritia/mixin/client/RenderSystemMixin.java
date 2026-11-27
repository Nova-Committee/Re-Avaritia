package committee.nova.mods.avaritia.mixin.client;

import committee.nova.mods.avaritia.client.shader.AvaritiaShaderUniforms;
import com.mojang.blaze3d.TracyFrameCapture;
import com.mojang.blaze3d.platform.Window;
import com.mojang.blaze3d.systems.RenderPass;
import com.mojang.blaze3d.systems.RenderSystem;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(RenderSystem.class)
public abstract class RenderSystemMixin {
    @Inject(method = "bindDefaultUniforms", at = @At("TAIL"))
    private static void avaritia$bindUniforms(RenderPass renderPass, CallbackInfo ci) {
        AvaritiaShaderUniforms.bindIfAvaritia(renderPass);
    }

    // 1.21.11 declares flipFrame(Window, @Nullable TracyFrameCapture), so the handler has to
    // declare the Window too; in 26.1 the TracyFrameCapture was the only parameter.
    // A signature mismatch here fails injection, and the config is required, so it would
    // abort client startup - which a dedicated-server run can never surface.
    @Inject(method = "flipFrame", at = @At("TAIL"))
    private static void avaritia$endFrame(Window window, @Nullable TracyFrameCapture tracyFrameCapture, CallbackInfo ci) {
        AvaritiaShaderUniforms.endFrame();
    }
}
