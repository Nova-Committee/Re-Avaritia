package committee.nova.mods.avaritia.init.mixins;

import committee.nova.mods.avaritia.common.block.cake.EndlessCandleCakeBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.CandleCakeBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Map;

/**
 * @Project: Avaritia-forge
 * @Author: cnlimiter
 * @CreateTime: 2023/12/31 14:03
 * @Description:
 */

@Mixin(CandleCakeBlock.class)
public class EndlessCakeBlockMixin {

    @Shadow @Final private static Map<Block, CandleCakeBlock> BY_CANDLE;

    @Inject( method = "<init>", at = @At(value = "TAIL"))
    private void after(Block pCandleBlock, BlockBehaviour.Properties pProperties, CallbackInfo ci) {
        //noinspection ConstantConditions
        if ((Object)this instanceof EndlessCandleCakeBlock block)
            BY_CANDLE.put(pCandleBlock, block);
    }
}
