package nova.committee.avaritia.common.block;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Material;

/**
 * Description:
 * Author: cnlimiter
 * Date: 2022/4/2 6:55
 * Version: 1.0
 */
public class ResourceBlock extends Block {
    public ResourceBlock(String registryName) {
        super(BlockBehaviour.Properties.of(Material.METAL)
                .sound(SoundType.METAL)
                .strength(2000f, 50f).requiresCorrectToolForDrops());
        setRegistryName(registryName);
    }


    @Override
    public float getEnchantPowerBonus(BlockState state, LevelReader level, BlockPos pos) {
        return 20.0f;
    }


}
