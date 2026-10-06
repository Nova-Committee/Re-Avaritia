package committee.nova.mods.avaritia.init.mixins;
import committee.nova.mods.avaritia.init.registry.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.SugarCaneBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
@Mixin(SugarCaneBlock.class)
public abstract class StorageSugarCaneMixin {
    @Inject(method = "canSurvive", at = @At("HEAD"), cancellable = true)
    private void avaritia$soulFarmland(BlockState state, LevelReader level, BlockPos pos, CallbackInfoReturnable<Boolean> cir) {
        if (level.getBlockState(pos.below()).is(ModBlocks.soul_farmland.get())) cir.setReturnValue(true);
    }
}
