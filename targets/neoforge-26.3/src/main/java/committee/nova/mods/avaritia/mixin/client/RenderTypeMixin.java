package committee.nova.mods.avaritia.mixin.client;

import committee.nova.mods.avaritia.client.render.RenderTypeDrawTracker;
import net.minecraft.client.renderer.rendertype.PreparedRenderType;
import net.minecraft.client.renderer.rendertype.RenderType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * @author cnlimiter
 * <p>
 * 26.3 适配说明：原 {@code RenderType#draw(MeshData)} 注入已失效（方法移除，绘制改由
 * {@link PreparedRenderType#drawFromBuffer} 系列执行），绘制期的 RenderType 切换迁移到
 * {@link RenderPassMixin}。
 * <p>
 * 本类改为在 {@code RenderType#prepare} 返回时登记
 * {@link PreparedRenderType} -> {@link RenderType} 映射（见 {@link RenderTypeDrawTracker}），
 * 因为绘制阶段只持有 PreparedRenderType，无法还原其来源 RenderType 实例。
 */
@Mixin(RenderType.class)
public abstract class RenderTypeMixin {

    @Inject(
            method = "prepare",
            at = @At("RETURN")
    )
    private void avaritia$trackPreparedRenderType(CallbackInfoReturnable<PreparedRenderType> cir) {
        RenderTypeDrawTracker.register(cir.getReturnValue(), (RenderType) (Object) this);
    }
}
