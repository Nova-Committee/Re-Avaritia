package committee.nova.mods.avaritia.init.mixins;

import committee.nova.mods.avaritia.client.render.RenderTypeDrawTracker;
import net.minecraft.client.renderer.rendertype.PreparedRenderType;
import net.minecraft.client.renderer.rendertype.RenderType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Fabric 26.3：26.3 的绘制阶段只持有 {@link PreparedRenderType}，
 * 在 {@code RenderType#prepare} 返回时登记 PreparedRenderType -> RenderType 映射，
 * 供 PreparedRenderTypeMixin 在绘制时还原来源 RenderType。
 */
@Mixin(RenderType.class)
public abstract class RenderTypeMixin {

    @Inject(method = "prepare", at = @At("RETURN"))
    private void avaritia$trackPreparedRenderType(CallbackInfoReturnable<PreparedRenderType> cir) {
        RenderTypeDrawTracker.register(cir.getReturnValue(), (RenderType) (Object) this);
    }
}
