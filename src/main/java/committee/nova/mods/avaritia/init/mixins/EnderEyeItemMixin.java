package committee.nova.mods.avaritia.init.mixins;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import net.minecraft.core.Direction;
import net.minecraft.world.item.EnderEyeItem;
import net.minecraft.world.level.block.state.pattern.BlockPattern;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(EnderEyeItem.class)
public abstract class EnderEyeItemMixin {
    @ModifyExpressionValue(method = "useOn", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/world/level/block/state/pattern/BlockPattern;find(Lnet/minecraft/world/level/LevelReader;Lnet/minecraft/core/BlockPos;)Lnet/minecraft/world/level/block/state/pattern/BlockPattern$BlockPatternMatch;"))
    private BlockPattern.BlockPatternMatch avaritia$requireInwardPortalFrames(BlockPattern.BlockPatternMatch match) {
        // Vanilla's fixed world-space portal offset is valid only for this pattern orientation.
        return match != null && match.getForwards() == Direction.DOWN && match.getUp() == Direction.SOUTH
                ? match : null;
    }
}
