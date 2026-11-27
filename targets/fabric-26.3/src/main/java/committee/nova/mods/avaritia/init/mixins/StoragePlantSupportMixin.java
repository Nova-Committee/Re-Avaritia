package committee.nova.mods.avaritia.init.mixins;
import committee.nova.mods.avaritia.init.registry.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.VegetationBlock;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.StemBlock;
import net.minecraft.world.level.block.AttachedStemBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** DeadBushBlock removed in 26.3 (replaced by DryVegetationBlock); handled separately via NetherWartBlockMixin-like if needed. */
@Mixin({VegetationBlock.class, CropBlock.class, StemBlock.class, AttachedStemBlock.class})
public abstract class StoragePlantSupportMixin {
    @Inject(method = "mayPlaceOn", at = @At("HEAD"), cancellable = true)
    private void avaritia$soulFarmland(BlockState floor, BlockGetter level, BlockPos pos, CallbackInfoReturnable<Boolean> cir) {
        if (floor.is(ModBlocks.soul_farmland.get())) cir.setReturnValue(true);
    }
}
