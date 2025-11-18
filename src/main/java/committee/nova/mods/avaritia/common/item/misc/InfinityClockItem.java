package committee.nova.mods.avaritia.common.item.misc;

import committee.nova.mods.avaritia.common.entity.AcceleratorDisplayEntity;
import committee.nova.mods.avaritia.common.item.resources.ResourceItem;
import committee.nova.mods.avaritia.init.registry.ModRarities;
import committee.nova.mods.avaritia.util.ToolUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.BonemealableBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.saveddata.SavedData;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.LevelTickEvent;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class InfinityClockItem extends ResourceItem {
    public static final Map<ResourceKey<Level>, Map<BlockPos, Integer>> acceleratedBlocks = new HashMap<>();
    private static final Map<ResourceKey<Level>, Map<BlockPos, AcceleratorDisplayEntity>> displayEntities = new HashMap<>();

    public InfinityClockItem() {
        super(ModRarities.COSMIC.getValue(), false, new Item.Properties().stacksTo(1));
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
    public @NotNull InteractionResultHolder<ItemStack> use(Level level, Player player, @NotNull InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);

        if (level.isClientSide) return InteractionResultHolder.pass(stack);

        boolean upMode = isActive(stack, "infinity_clock_up");

        if (player.isShiftKeyDown()) {
            switchClockMode(level, player, hand, "infinity_clock_up");
            return InteractionResultHolder.success(stack);
        }

        if (upMode) {
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
            player.displayClientMessage(Component.literal(next + "x"), true);
            return InteractionResultHolder.success(stack);
        }


        player.openMenu(
                new SimpleMenuProvider(
                        (id, inv, buf) -> new InfinityClockMenu(id, inv),
                        Component.translatable("item.avaritia.infinity_clock")
                )
        );

        return InteractionResultHolder.success(stack);
    }

    @Override
    public @NotNull InteractionResult useOn(UseOnContext ctx) {
        ItemStack stack = ctx.getItemInHand();
        Level level = ctx.getLevel();
        BlockPos pos = ctx.getClickedPos();
        Direction face = ctx.getClickedFace(); // 获取点击的面

        if (!isActive(stack, "infinity_clock_up")) {

            removeAcceleration(level, pos);
            return InteractionResult.CONSUME;
        }

        if (level.isClientSide) return InteractionResult.SUCCESS;

        CompoundTag tag = stack.getOrCreateTag();
        int multiplier = tag.getInt("SpeedMultiplier");

        if (multiplier == 0) {
            multiplier = 1;
            tag.putInt("SpeedMultiplier", multiplier);
        }


        if (multiplier == 1) {
            removeAcceleration(level, pos);
            return InteractionResult.CONSUME;
        }


        acceleratedBlocks
                .computeIfAbsent(level.dimension(), k -> new HashMap<>())
                .put(pos.immutable(), multiplier);


        if (level instanceof ServerLevel serverLevel) {

            removeDisplayEntity(level, pos);


            AcceleratorDisplayEntity entity = new AcceleratorDisplayEntity(level, pos, multiplier, face); // 传递面信息
            serverLevel.addFreshEntity(entity);
            displayEntities.computeIfAbsent(level.dimension(), k -> new HashMap<>()).put(pos.immutable(), entity);
        }

        return InteractionResult.CONSUME;
    }


    private void removeAcceleration(Level level, BlockPos pos) {

        ResourceKey<Level> dimension = level.dimension();
        if (acceleratedBlocks.containsKey(dimension)) {
            acceleratedBlocks.get(dimension).remove(pos);

            if (acceleratedBlocks.get(dimension).isEmpty()) {
                acceleratedBlocks.remove(dimension);
            }
        }


        removeDisplayEntity(level, pos);
    }


    private void removeDisplayEntity(Level level, BlockPos pos) {
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

    public static class AcceleratedBlocksSavedData extends SavedData {
        public static final String NAME = "avaritia_accelerated_blocks";
        private final Map<ResourceKey<Level>, Map<BlockPos, Integer>> acceleratedBlocks = new HashMap<>();

        public AcceleratedBlocksSavedData() {
        }

        public AcceleratedBlocksSavedData(@NotNull CompoundTag nbt, HolderLookup.@NotNull Provider registries) {
            ListTag dimensionsList = nbt.getList("Dimensions", Tag.TAG_COMPOUND);
            for (int i = 0; i < dimensionsList.size(); i++) {
                CompoundTag dimensionTag = dimensionsList.getCompound(i);
                ResourceLocation dimensionLocation = ResourceLocation.tryParse(dimensionTag.getString("Dimension"));
                ResourceKey<Level> dimensionKey = ResourceKey.create(net.minecraft.core.registries.Registries.DIMENSION, dimensionLocation);

                Map<BlockPos, Integer> blocksMap = new HashMap<>();
                ListTag blocksList = dimensionTag.getList("Blocks", Tag.TAG_COMPOUND);

                for (int j = 0; j < blocksList.size(); j++) {
                    CompoundTag blockTag = blocksList.getCompound(j);
                    BlockPos pos = BlockPos.of(blockTag.getLong("Pos"));
                    int multiplier = blockTag.getInt("Multiplier");
                    blocksMap.put(pos, multiplier);
                }

                this.acceleratedBlocks.put(dimensionKey, blocksMap);
            }
        }

        @Override
        public @NotNull CompoundTag save(@NotNull CompoundTag compound, HolderLookup.@NotNull Provider registries) {
            ListTag dimensionsList = new ListTag();

            for (Map.Entry<ResourceKey<Level>, Map<BlockPos, Integer>> dimensionEntry : acceleratedBlocks.entrySet()) {
                CompoundTag dimensionTag = new CompoundTag();
                dimensionTag.putString("Dimension", dimensionEntry.getKey().location().toString());

                ListTag blocksList = new ListTag();
                for (Map.Entry<BlockPos, Integer> blockEntry : dimensionEntry.getValue().entrySet()) {
                    CompoundTag blockTag = new CompoundTag();
                    blockTag.putLong("Pos", blockEntry.getKey().asLong());
                    blockTag.putInt("Multiplier", blockEntry.getValue());
                    blocksList.add(blockTag);
                }

                dimensionTag.put("Blocks", blocksList);
                dimensionsList.add(dimensionTag);
            }

            compound.put("Dimensions", dimensionsList);
            return compound;
        }

        public Map<ResourceKey<Level>, Map<BlockPos, Integer>> getAcceleratedBlocks() {
            return acceleratedBlocks;
        }

        public void setAcceleratedBlocks(Map<ResourceKey<Level>, Map<BlockPos, Integer>> blocks) {
            this.acceleratedBlocks.clear();
            this.acceleratedBlocks.putAll(blocks);
            setDirty();
        }
    }

    public static AcceleratedBlocksSavedData getSavedData(ServerLevel level) {
        return level.getDataStorage().computeIfAbsent(
                new SavedData.Factory<>(AcceleratedBlocksSavedData::new, AcceleratedBlocksSavedData::new),
                AcceleratedBlocksSavedData.NAME
        );
    }

    public static void loadAcceleratedBlocksFromSavedData(ServerLevel level) {
        AcceleratedBlocksSavedData savedData = getSavedData(level);
        acceleratedBlocks.clear();
        acceleratedBlocks.putAll(savedData.getAcceleratedBlocks());

        // 为每个加载的加速方块创建显示实体
        for (Map.Entry<ResourceKey<Level>, Map<BlockPos, Integer>> dimensionEntry : acceleratedBlocks.entrySet()) {
            ResourceKey<Level> dimension = dimensionEntry.getKey();
            Map<BlockPos, Integer> blocks = dimensionEntry.getValue();

            for (Map.Entry<BlockPos, Integer> blockEntry : blocks.entrySet()) {
                BlockPos pos = blockEntry.getKey();
                int multiplier = blockEntry.getValue();

                // 检查方块是否仍然存在并且有效
                if (level.isLoaded(pos) && multiplier > 1) {
                    // 创建显示实体
                    AcceleratorDisplayEntity entity = new AcceleratorDisplayEntity(level, pos, multiplier, Direction.NORTH);
                    level.addFreshEntity(entity);
                    displayEntities.computeIfAbsent(dimension, k -> new HashMap<>()).put(pos.immutable(), entity);
                }
            }
        }
    }

    public static void saveAcceleratedBlocksToSavedData(ServerLevel level) {
        AcceleratedBlocksSavedData savedData = getSavedData(level);
        savedData.setAcceleratedBlocks(acceleratedBlocks);
    }


    @EventBusSubscriber
    public static class TickHandler {
        @SubscribeEvent
        public static void onServerTick(LevelTickEvent.Post event) {
            if (!(event.getLevel() instanceof ServerLevel level)) return;
            Map<BlockPos, Integer> map = acceleratedBlocks.get(level.dimension());
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
                    // 不要移除未加载的方块，它们可能在其他区块中
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

                    // 添加粒子效果
                    if (level.getGameTime() % 5 == 0) {
                        addAccelerationParticles(level, pos, times);
                    }
                    continue;
                }


                BlockEntity be = level.getBlockEntity(pos);
                if (be != null) {
                    ToolUtils.accelerateBlockEntity(level, pos, be, times);

                    // 添加粒子效果
                    if (level.getGameTime() % 5 == 0) {
                        addAccelerationParticles(level, pos, times);
                    }
                } else {
                    // 只有当方块实体不存在时才移除加速
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

                    if (times > 1) {

                        AcceleratorDisplayEntity newEntity = new AcceleratorDisplayEntity(level, pos, times, net.minecraft.core.Direction.NORTH);
                        level.addFreshEntity(newEntity);
                        displayEntities.computeIfAbsent(level.dimension(), k -> new HashMap<>()).put(pos.immutable(), newEntity);
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
}
