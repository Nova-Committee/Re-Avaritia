package committee.nova.mods.avaritia.init.mixins;

import committee.nova.mods.avaritia.init.registry.ModTags;
import committee.nova.mods.avaritia.init.registry.ModToolTiers;
import net.minecraft.world.item.DiggerItem;
import net.minecraft.world.item.TieredItem;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(DiggerItem.class)
public abstract class RegistryToolTierMixin {
    @Inject(method = "isCorrectToolForDrops", at = @At("HEAD"), cancellable = true)
    private void avaritia$harvestTier(BlockState state, CallbackInfoReturnable<Boolean> cir) {
        int level = ((TieredItem) (Object) this).getTier().getLevel();
        if (state.is(ModTags.NEEDS_INFINITY_TOOL) && level < ModToolTiers.INFINITY.getLevel()
                || state.is(ModTags.NEEDS_CRYSTAL_TOOL) && level < ModToolTiers.CRYSTAL.getLevel()
                || state.is(ModTags.NEEDS_BLAZE_TOOL) && level < ModToolTiers.BLAZE.getLevel()) {
            cir.setReturnValue(false);
        }
    }
}
