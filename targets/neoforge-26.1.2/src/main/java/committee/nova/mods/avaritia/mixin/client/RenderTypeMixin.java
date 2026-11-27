package committee.nova.mods.avaritia.mixin.client;

import committee.nova.mods.avaritia.client.shader.AvaritiaShaderUniforms;
import com.mojang.blaze3d.vertex.MeshData;
import net.minecraft.client.renderer.rendertype.RenderType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(RenderType.class)
public abstract class RenderTypeMixin {
    @Inject(method = "draw", at = @At("HEAD"))
    private void avaritia$setActiveRenderType(MeshData mesh, CallbackInfo ci) {
        AvaritiaShaderUniforms.setActiveRenderType((RenderType) (Object) this);
    }

    @Inject(method = "draw", at = @At("TAIL"))
    private void avaritia$clearActiveRenderType(MeshData mesh, CallbackInfo ci) {
        AvaritiaShaderUniforms.clearActiveRenderType((RenderType) (Object) this);
    }
}
