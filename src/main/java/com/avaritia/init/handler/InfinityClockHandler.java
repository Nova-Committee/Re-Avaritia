package com.avaritia.init.handler;

import com.avaritia.common.entity.AcceleratorDisplayEntity;
import com.avaritia.common.item.misc.InfinityClockItem;
import com.avaritia.util.ToolUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.BonemealableBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.level.LevelEvent;
import net.neoforged.neoforge.event.server.ServerStartedEvent;
import net.neoforged.neoforge.event.tick.LevelTickEvent;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * @author cnlimiter
 */
@EventBusSubscriber
public class InfinityClockHandler {
    @SubscribeEvent
    public static void onServerStarted(ServerStartedEvent event) {
        // 鏈嶅姟鍣ㄥ惎鍔ㄥ畬鎴愬悗鍔犺浇鍔犻€熸暟鎹?
        ServerLevel overworld = event.getServer().getLevel(Level.OVERWORLD);
        if (overworld != null) {
            InfinityClockItem.loadAcceleratedBlocksFromSavedData(overworld);
        }
    }

    @SubscribeEvent
    public static void onLevelSave(LevelEvent.Save event) {
        if (event.getLevel() instanceof ServerLevel level && level.dimension() == Level.OVERWORLD) {
            InfinityClockItem.saveAcceleratedBlocksToSavedData(level);
        }
    }

    @SubscribeEvent
    public static void onServerTick(LevelTickEvent.Post event) {
        if (!(event.getLevel() instanceof ServerLevel level)) return;
        Map<BlockPos, Integer> map = InfinityClockItem.acceleratedBlocks.get(level.dimension());
        if (map == null || map.isEmpty()) return;

        Iterator<Map.Entry<BlockPos, Integer>> it = map.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<BlockPos, Integer> entry = it.next();
            BlockPos pos = entry.getKey();
            int times = entry.getValue();


            if (times == 1) {
                it.remove();
                removeDisplayEntity(level, pos);
                continue;
            }

            if (!level.isLoaded(pos)) {
                // 涓嶈绉婚櫎鏈姞杞界殑鏂瑰潡锛屽畠浠彲鑳藉湪鍏朵粬鍖哄潡涓?
                continue;
            }

            BlockState state = level.getBlockState(pos);
            Block block = state.getBlock();


            if (block instanceof BonemealableBlock growable) {
                RandomSource random = level.getRandom();
                for (int i = 0; i < times; i++) {
                    if (!growable.isValidBonemealTarget(level, pos, state)) break;
                    if (growable.isBonemealSuccess(level, random, pos, state)) {
                        try {
                            growable.performBonemeal(level, random, pos, state);
                        } catch (Exception e) {
                            e.printStackTrace();
                            break;
                        }
                        state = level.getBlockState(pos);
                    }
                }

                // 娣诲姞绮掑瓙鏁堟灉
                if (level.getGameTime() % 5 == 0) {
                    addAccelerationParticles(level, pos, times);
                }
                continue;
            }


            BlockEntity be = level.getBlockEntity(pos);
            if (be != null) {
                ToolUtils.accelerateBlockEntity(level, pos, be, times);

                // 娣诲姞绮掑瓙鏁堟灉
                if (level.getGameTime() % 5 == 0) {
                    addAccelerationParticles(level, pos, times);
                }
            } else {
                // 鍙湁褰撴柟鍧楀疄浣撲笉瀛樺湪鏃舵墠绉婚櫎鍔犻€?
                it.remove();
                removeDisplayEntity(level, pos);
            }

            // 鏇存柊瀹炰綋鏄剧ず
            AcceleratorDisplayEntity entity = InfinityClockItem.displayEntities.getOrDefault(level.dimension(), new HashMap<>()).get(pos);
            if (entity != null) {
                if (entity.getSpeedMultiplier() != times) {
                    entity.setSpeedMultiplier(times);
                }
            } else {

                if (times > 1) {

                    AcceleratorDisplayEntity newEntity = new AcceleratorDisplayEntity(level, pos, times, net.minecraft.core.Direction.NORTH);
                    level.addFreshEntity(newEntity);
                    InfinityClockItem.displayEntities.computeIfAbsent(level.dimension(), k -> new HashMap<>()).put(pos.immutable(), newEntity);
                }
            }
        }
    }

    private static void addAccelerationParticles(ServerLevel level, BlockPos pos, int times) {
        long gameTime = level.getGameTime();
        for (int i = 0; i < 10; i++) {
            double hAngle = (gameTime * 0.5 + i * 40) % 360;
            double hRadius = 0.6;
            double hX = pos.getX() + 0.5 + Math.cos(Math.toRadians(hAngle)) * hRadius;
            double hZ = pos.getZ() + 0.5 + Math.sin(Math.toRadians(hAngle)) * hRadius;
            double hY = pos.getY() + 0.5 + (i % 3 - 1) * 0.2;

            level.sendParticles(
                    ParticleTypes.ENCHANT,
                    hX, hY, hZ,
                    1,
                    0, 0, 0,
                    0.0D
            );


            double vAngle = (gameTime * 0.7 + i * 60) % 360;
            double vRadius = 0.6;
            double vX = pos.getX() + 0.5 + Math.cos(Math.toRadians(vAngle)) * vRadius;
            double vY = pos.getY() + 0.5 + Math.sin(Math.toRadians(vAngle)) * vRadius;
            double vZ = pos.getZ() + 0.5 + (i % 2 - 0.5) * 0.2;

            level.sendParticles(
                    ParticleTypes.ENCHANT,
                    vX, vY, vZ,
                    1,
                    0, 0, 0,
                    0.0D
            );
        }
    }

    private static void removeDisplayEntity(Level level, BlockPos pos) {
        ResourceKey<Level> dimension = level.dimension();
        if (InfinityClockItem.displayEntities.containsKey(dimension)) {
            AcceleratorDisplayEntity entity = InfinityClockItem.displayEntities.get(dimension).get(pos);
            if (entity != null && !entity.isRemoved()) {
                entity.remove(AcceleratorDisplayEntity.RemovalReason.DISCARDED);
            }
            InfinityClockItem.displayEntities.get(dimension).remove(pos);
            if (InfinityClockItem.displayEntities.get(dimension).isEmpty()) {
                InfinityClockItem.displayEntities.remove(dimension);
            }
        }
    }
}
