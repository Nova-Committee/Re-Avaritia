package committee.nova.mods.avaritia.init.mixins;
import committee.nova.mods.avaritia.init.registry.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.CactusBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
@Mixin(CactusBlock.class)
public abstract class StorageCactusMixin {
    @Inject(method = "canSurvive", at = @At("HEAD"), cancellable = true)
    private void avaritia$soulFarmland(BlockState state, LevelReader level, BlockPos pos, CallbackInfoReturnable<Boolean> cir) {
        if (!level.getBlockState(pos.below()).is(ModBlocks.soul_farmland.get())) return;
        for (Direction side : Direction.Plane.HORIZONTAL) {
            BlockState neighbor = level.getBlockState(pos.relative(side));
            if (neighbor.isSolid() || level.getFluidState(pos.relative(side)).is(FluidTags.LAVA)) { cir.setReturnValue(false); return; }
        }
        cir.setReturnValue(!level.getBlockState(pos.above()).liquid());
    }
}
