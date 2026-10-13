package committee.nova.mods.avaritia.init.mixins;
import committee.nova.mods.avaritia.client.AvaritiaModClient;
import committee.nova.mods.avaritia.client.render.RenderTypeDrawTracker;
import committee.nova.mods.avaritia.client.shader.AvaritiaShaderUniforms;
import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
/** 26.3：仅保留 Minecraft 构造尾部的一次性初始化；tick/setScreen/resizeDisplay/destroy 钩子
 *  已由 Fabric 生命周期事件与自注册组件（UiInspector 等）替代，原 AvaritiaForgeClient/
 *  PortableScreenLayers 逻辑分别并入 AvaritiaClient 与可移植屏幕自管理。 */
@Mixin(Minecraft.class)
public abstract class ClientMinecraftMixin {
    @Inject(method = "<init>", at = @At("TAIL"))
    private void avaritia$setup(CallbackInfo ci) { AvaritiaModClient.finishClientSetup((Minecraft) (Object) this); }

    // 每帧渲染结束后回收 cosmic UBO 动态缓冲与 PreparedRenderType 映射。
    @Inject(method = "renderFrame", at = @At("TAIL"))
    private void avaritia$endFrame(boolean advanceGameTime, CallbackInfo ci) {
        AvaritiaShaderUniforms.endFrame();
        RenderTypeDrawTracker.clear();
    }
}
