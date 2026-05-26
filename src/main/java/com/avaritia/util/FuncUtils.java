package com.avaritia.util;

import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;

import java.util.Map;

/**
 * 通用方块转换工具。
 */
public class FuncUtils {
    public static void upgradeMachine(Level level, Player player, BlockPos pos, Block to) {
        level.setBlockAndUpdate(pos, to.withPropertiesOf(level.getBlockState(pos)));
        level.playSound(player, pos, SoundEvents.PLAYER_LEVELUP, SoundSource.PLAYERS);
    }

    public static InteractionResult transBlock(Level level, Player player, BlockPos pos, Map<Block, Block> transMap) {
        for (var entry : transMap.keySet()) {
            if (level.getBlockState(pos).is(entry)) {
                level.playSound(player, pos, SoundEvents.PLAYER_LEVELUP, SoundSource.PLAYERS);
                level.setBlockAndUpdate(pos, transMap.get(entry).defaultBlockState());
                return InteractionResult.SUCCESS;
            }
        }
        return InteractionResult.PASS;
    }
}
