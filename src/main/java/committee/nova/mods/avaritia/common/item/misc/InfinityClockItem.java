package committee.nova.mods.avaritia.common.item.misc;

import committee.nova.mods.avaritia.api.iface.ISwitchable;
import committee.nova.mods.avaritia.common.entity.AcceleratorDisplayEntity;
import committee.nova.mods.avaritia.common.item.resources.ResourceItem;
import committee.nova.mods.avaritia.init.registry.ModRarities;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.BonemealableBlock;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.network.chat.Component;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class InfinityClockItem extends ResourceItem implements ISwitchable {


    public static final Map<ResourceKey<Level>, Map<BlockPos, Integer>> acceleratedBlocks = new HashMap<>();
    private static final Map<ResourceKey<Level>, Map<BlockPos, AcceleratorDisplayEntity>> displayEntities = new HashMap<>();
    private static final int[] SPEED_STEPS = {1, 4, 16, 64, 256, 512};

    public InfinityClockItem() {
        super(ModRarities.COSMIC, "infinity_clock", true, new Properties().stacksTo(1));
    }

    @Override
    public boolean isFoil(@NotNull ItemStack pStack) {
        return false;
    }

    @Override
    public boolean isDamageable(ItemStack stack) {
        return false;
    }


    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);

        if (!level.isClientSide) {
            if (player.isCrouching()) {
                switchMode(level, player, hand, "infinity_clock_up");
                return InteractionResultHolder.success(stack);
            }

            if (isActive(stack, "infinity_clock_up")) {
                int current = stack.getOrCreateTag().getInt("SpeedMultiplier");
                int next;

                switch (current) {
                    case 1 -> next = 4;
                    case 4 -> next = 16;
                    case 16 -> next = 64;
                    case 64 -> next = 256;
                    case 256 -> next = 512;
                    default -> next = 1;
                }
                stack.getOrCreateTag().putInt("SpeedMultiplier", next);
                player.displayClientMessage(Component.literal(next+"x"), true);
                return InteractionResultHolder.success(stack);
            }
        }

        return super.use(level, player, hand);
    }

    @Override
    public InteractionResult useOn(UseOnContext ctx) {
        ItemStack stack = ctx.getItemInHand();
        Level level = ctx.getLevel();
        BlockPos pos = ctx.getClickedPos();

        if (!isActive(stack, "infinity_clock_up")) {
            // 关闭状态时移除加速和实体
            removeAcceleration(level, pos);
            return InteractionResult.CONSUME;
        }

        if (level.isClientSide) return InteractionResult.SUCCESS;

        CompoundTag tag = stack.getOrCreateTag();
        int multiplier = tag.getInt("SpeedMultiplier");
        // 确保有默认值
        if (multiplier == 0) {
            multiplier = 1;
            tag.putInt("SpeedMultiplier", multiplier);
        }

        // 处理1x的特殊情况 - 不加速也不显示实体
        if (multiplier == 1) {
            removeAcceleration(level, pos);
            return InteractionResult.CONSUME;
        }

        // 更新加速倍数
        acceleratedBlocks
                .computeIfAbsent(level.dimension(), k -> new HashMap<>())
                .put(pos.immutable(), multiplier);

        // 处理显示实体
        if (level instanceof ServerLevel serverLevel) {
            // 先移除旧实体（关键修复）
            removeDisplayEntity(level, pos);

            // 创建新实体
            AcceleratorDisplayEntity entity = new AcceleratorDisplayEntity(level, pos, multiplier);
            serverLevel.addFreshEntity(entity);
            displayEntities.computeIfAbsent(level.dimension(), k -> new HashMap<>()).put(pos.immutable(), entity);
        }

        return InteractionResult.CONSUME;
    }

    // 新增：移除方块的加速状态和实体
    private void removeAcceleration(Level level, BlockPos pos) {
        // 移除加速数据
        ResourceKey<Level> dimension = level.dimension();
        if (acceleratedBlocks.containsKey(dimension)) {
            acceleratedBlocks.get(dimension).remove(pos);
            // 如果维度下没有加速方块了，移除整个维度条目
            if (acceleratedBlocks.get(dimension).isEmpty()) {
                acceleratedBlocks.remove(dimension);
            }
        }

        // 移除显示实体
        removeDisplayEntity(level, pos);
    }

    // 新增：单独的移除实体方法
    private void removeDisplayEntity(Level level, BlockPos pos) {
        ResourceKey<Level> dimension = level.dimension();
        if (displayEntities.containsKey(dimension)) {
            AcceleratorDisplayEntity entity = displayEntities.get(dimension).get(pos);
            if (entity != null && !entity.isRemoved()) {
                entity.remove(AcceleratorDisplayEntity.RemovalReason.DISCARDED);
            }
            displayEntities.get(dimension).remove(pos);
            // 清理空映射
            if (displayEntities.get(dimension).isEmpty()) {
                displayEntities.remove(dimension);
            }
        }
    }


    @Mod.EventBusSubscriber
    public static class TickHandler {
        @SubscribeEvent
        public static void onServerTick(TickEvent.LevelTickEvent event) {
            if (event.phase != TickEvent.Phase.END) return;
            if (!(event.level instanceof ServerLevel level)) return;
            Map<BlockPos, Integer> map = acceleratedBlocks.get(level.dimension());
            if (map == null) return;

            Iterator<Map.Entry<BlockPos, Integer>> it = map.entrySet().iterator();
            while (it.hasNext()) {
                Map.Entry<BlockPos, Integer> entry = it.next();
                BlockPos pos = entry.getKey();
                int times = entry.getValue();

                // 1x倍数时不处理加速并移除
                if (times == 1) {
                    it.remove();
                    removeDisplayEntity(level, pos);
                    continue;
                }

                if (!level.isLoaded(pos)) {
                    it.remove();
                    removeDisplayEntity(level, pos);
                    continue;
                }

                BlockState state = level.getBlockState(pos);
                Block block = state.getBlock();


                if (block instanceof BonemealableBlock growable) {
                    RandomSource random = level.getRandom();
                    for (int i = 0; i < times; i++) {
                        if (!growable.isValidBonemealTarget(level, pos, state, false)) break;
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
                    continue;
                }


                BlockEntity be = level.getBlockEntity(pos);
                if (be != null) {
                    InfinityClockItem.accelerateBlockEntity(level, pos, be, times);
                } else {
                    it.remove();
                    removeDisplayEntity(level, pos);
                }

                // 更新实体显示
                AcceleratorDisplayEntity entity = displayEntities.getOrDefault(level.dimension(), new HashMap<>()).get(pos);
                if (entity != null) {
                    if (entity.getSpeedMultiplier() != times) {
                        entity.setSpeedMultiplier(times);
                    }
                } else {
                    // 如果实体不存在但应该存在，创建它
                    if (times > 1) {
                        AcceleratorDisplayEntity newEntity = new AcceleratorDisplayEntity(level, pos, times);
                        level.addFreshEntity(newEntity);
                        displayEntities.computeIfAbsent(level.dimension(), k -> new HashMap<>()).put(pos.immutable(), newEntity);
                    }
                }
            }
        }

        // 静态版本的移除实体方法，供TickHandler使用
        private static void removeDisplayEntity(Level level, BlockPos pos) {
            ResourceKey<Level> dimension = level.dimension();
            if (displayEntities.containsKey(dimension)) {
                AcceleratorDisplayEntity entity = displayEntities.get(dimension).get(pos);
                if (entity != null && !entity.isRemoved()) {
                    entity.remove(AcceleratorDisplayEntity.RemovalReason.DISCARDED);
                }
                displayEntities.get(dimension).remove(pos);
                if (displayEntities.get(dimension).isEmpty()) {
                    displayEntities.remove(dimension);
                }
            }
        }
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private static void accelerateBlockEntity(ServerLevel level, BlockPos pos, BlockEntity be, int times) {
        if (be.isRemoved()) return;
        BlockState state = level.getBlockState(pos);
        Block block = state.getBlock();

        if (block instanceof EntityBlock entityBlock) {
            BlockEntityType type = be.getType();
            BlockEntityTicker ticker = entityBlock.getTicker(level, state, type);

            if (ticker != null) {
                for (int i = 0; i < times; i++) {
                    ticker.tick(level, pos, state, be);
                    if (be.isRemoved()) break;
                }
                be.setChanged();
                level.sendBlockUpdated(pos, state, state, 3);
            }
        }
    }
}
