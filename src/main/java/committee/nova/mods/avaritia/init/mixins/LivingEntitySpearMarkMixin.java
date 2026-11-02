package committee.nova.mods.avaritia.init.mixins;

import committee.nova.mods.avaritia.api.iface.SpearMarkEntityData;
import committee.nova.mods.avaritia.common.component.SpearMark;
import committee.nova.mods.avaritia.init.handler.SpearMarkHandler;
import net.minecraft.world.entity.LivingEntity;
import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * 给 LivingEntity 挂载长矛标记字段（Fabric 无 DataAttachment API），
 * 并在 tick 尾部执行标记维护。
 */
@Mixin(LivingEntity.class)
public abstract class LivingEntitySpearMarkMixin implements SpearMarkEntityData {
    @Unique
    private SpearMark avaritia$spearMark;

    @Override
    public @Nullable SpearMark avaritia$getSpearMark() {
        return this.avaritia$spearMark;
    }

    @Override
    public void avaritia$setSpearMark(@Nullable SpearMark mark) {
        this.avaritia$spearMark = mark;
    }

    @Inject(method = "tick", at = @At("TAIL"))
    private void avaritia$tickSpearMark(CallbackInfo ci) {
        SpearMarkHandler.tick((LivingEntity) (Object) this);
    }
}
