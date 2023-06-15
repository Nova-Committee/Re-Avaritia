package nova.committee.avaritia.common.block;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;
import nova.committee.avaritia.api.common.block.BaseBlock;

/**
 * Description:
 * Author: cnlimiter
 * Date: 2022/4/2 6:55
 * Version: 1.0
 */
public class ResourceBlock extends BaseBlock {
    public ResourceBlock(SoundType soundType) {
        super(soundType, 25f, 1000f, true);
    }


    @Override
    public float getEnchantPowerBonus(BlockState state, LevelReader level, BlockPos pos) {
        return 20.0f;
    }


}
