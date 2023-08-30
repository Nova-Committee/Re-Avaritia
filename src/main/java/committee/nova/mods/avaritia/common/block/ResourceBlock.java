package committee.nova.mods.avaritia.common.block;

import committee.nova.mods.avaritia.api.common.block.BaseBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Material;

/**
 * Name: Avaritia-forge / ResourceBlock
 * Author: cnlimiter
 * CreateTime: 2023/8/13 13:20
 * Description:
 */

public class ResourceBlock extends BaseBlock {
    public ResourceBlock() {
        super(Material.METAL, SoundType.METAL, 25f, 1000f);
    }

    @Override
    public float getEnchantPowerBonus(BlockState state, LevelReader level, BlockPos pos) {
        return 20.0f;
    }


}
