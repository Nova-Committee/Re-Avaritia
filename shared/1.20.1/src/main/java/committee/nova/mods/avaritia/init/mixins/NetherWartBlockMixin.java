package committee.nova.mods.avaritia.init.mixins;

import committee.nova.mods.avaritia.init.registry.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.BonemealableBlock;
import net.minecraft.world.level.block.BushBlock;
import net.minecraft.world.level.block.NetherWartBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * NetherWartBlockMixin
 *
 * @author cnlimiter
 * @version 1.0
 * @description
 * @date 2024/11/2 20:37
 */
@Mixin(NetherWartBlock.class)
public abstract class NetherWartBlockMixin extends BushBlock implements BonemealableBlock {

    public NetherWartBlockMixin(Properties pProperties) {
        super(pProperties);
    }

    @Inject(
            method = "mayPlaceOn",
            at = @At("HEAD"),
            cancellable = true
    )
    public void avaritia$mayPlaceOn(BlockState pState, BlockGetter pLevel, BlockPos pPos, CallbackInfoReturnable<Boolean> cir) {
        if (pState.is(ModBlocks.soul_farmland.get())) cir.setReturnValue(true);
    }

    @Override
    public boolean isValidBonemealTarget(@NotNull LevelReader pLevel, @NotNull BlockPos pPos, @NotNull BlockState pState, boolean pIsClient) {
        return !this.avaritia$isMaxAge(pState);
    }

    @Override
    public boolean isBonemealSuccess(@NotNull Level pLevel, @NotNull RandomSource pRandom, @NotNull BlockPos pPos, @NotNull BlockState pState) {
        return true;
    }

    @Override
    public void performBonemeal(@NotNull ServerLevel pLevel, @NotNull RandomSource pRandom, @NotNull BlockPos pPos, @NotNull BlockState pState) {
        this.avaritia$growCrops(pLevel, pPos, pState);
    }

    @Unique
    public void avaritia$growCrops(Level level, BlockPos blockPos, BlockState blockState) {
        int i = this.avaritia$getAge(blockState) + this.avaritia$getBonemealAgeIncrease(level);
        int j = this.avaritia$getMaxAge();
        if (i > j) {
            i = j;
        }

        level.setBlock(blockPos, this.avaritia$getStateForAge(blockState, i), 2);
    }

    @Unique
    public BlockState avaritia$getStateForAge(BlockState blockState, int plantAge) {
        return blockState.getBlock().defaultBlockState().setValue(this.avaritia$getAgeProperty(), plantAge);
    }

    @Unique
    protected int avaritia$getBonemealAgeIncrease(Level level) {
        return Mth.nextInt(level.random, 2, 5) / 3;
    }

    @Unique
    protected int avaritia$getAge(BlockState blockState) {
        return blockState.getValue(this.avaritia$getAgeProperty());
    }

    @Unique
    public IntegerProperty avaritia$getAgeProperty() {
        return NetherWartBlock.AGE;
    }

    @Unique
    public int avaritia$getMaxAge() {
        return 3;
    }

    @Unique
    public boolean avaritia$isMaxAge(BlockState blockState) {
        return blockState.getValue(this.avaritia$getAgeProperty()) >= this.avaritia$getMaxAge();
    }
}
