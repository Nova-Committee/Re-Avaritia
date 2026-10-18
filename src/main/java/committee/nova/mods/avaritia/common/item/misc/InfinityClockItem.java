package committee.nova.mods.avaritia.common.item.misc;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import committee.nova.mods.avaritia.api.iface.IInfinityClockSwitchable;
import committee.nova.mods.avaritia.api.util.ItemUtils;
import committee.nova.mods.avaritia.common.entity.AcceleratorDisplayEntity;
import committee.nova.mods.avaritia.common.item.resources.ResourceItem;
import committee.nova.mods.avaritia.common.menu.InfinityClockMenu;
import committee.nova.mods.avaritia.init.registry.ModRarities;
import committee.nova.mods.avaritia.util.ToolUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.util.datafix.DataFixTypes;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.BonemealSource;
import net.minecraft.world.level.block.BonemealableBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.saveddata.SavedDataType;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class InfinityClockItem extends ResourceItem implements IInfinityClockSwitchable {
    public static final Map<ResourceKey<Level>, Map<BlockPos, Integer>> acceleratedBlocks = new HashMap<>();
    private static final Map<ResourceKey<Level>, Map<BlockPos, AcceleratorDisplayEntity>> displayEntities = new HashMap<>();

    private static final String SPEED_MULTIPLIER_TAG = "SpeedMultiplier";

    public InfinityClockItem() {
        super(ModRarities.COSMIC, true, new Properties().stacksTo(1));
    }

    @Override
    public boolean isFoil(@NotNull ItemStack pStack) {
        return false;
    }

    private static int getSpeedMultiplier(ItemStack stack) {
        return stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag().getIntOr(SPEED_MULTIPLIER_TAG, 0);
    }

    private static void setSpeedMultiplier(ItemStack stack, int multiplier) {
        stack.update(DataComponents.CUSTOM_DATA, CustomData.EMPTY,
                data -> data.update(tag -> tag.putInt(SPEED_MULTIPLIER_TAG, multiplier)));
    }

    @Override
    public @NotNull InteractionResult use(@NotNull Level level, @NotNull Player player, @NotNull InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);

        if (level.isClientSide()) return InteractionResult.PASS;

        boolean upMode = isActive(stack, "infinity_clock_up");

        if (player.isShiftKeyDown()) {
            switchClockMode(level, player, hand, "infinity_clock_up");
            return InteractionResult.SUCCESS;
        }

        if (upMode) {
            int current = getSpeedMultiplier(stack);
            int next;

            switch (current) {
                case 1 -> next = 4;
                case 4 -> next = 16;
                case 16 -> next = 64;
                case 64 -> next = 256;
                case 256 -> next = 512;
                default -> next = 1;
            }

            setSpeedMultiplier(stack, next);
            ItemUtils.sendActionbar(player, Component.literal(next + "x"));
            return InteractionResult.SUCCESS;
        }


        ((ServerPlayer) player).openMenu(
                new SimpleMenuProvider(
                        (id, inv, buf) -> new InfinityClockMenu(id, inv),
                        Component.translatable("item.avaritia.infinity_clock")
                )
        );

        return InteractionResult.SUCCESS;
    }

    @Override
    public @NotNull InteractionResult useOn(@NotNull UseOnContext ctx) {
        ItemStack stack = ctx.getItemInHand();
        Level level = ctx.getLevel();
        BlockPos pos = ctx.getClickedPos();
        Direction face = ctx.getClickedFace(); // 获取点击的面
        if (level.isClientSide()) return InteractionResult.SUCCESS;

        if (!isActive(stack, "infinity_clock_up")) {

            removeAcceleration(level, pos);
            return InteractionResult.CONSUME;
        }


        int multiplier = getSpeedMultiplier(stack);

        if (multiplier == 0) {
            multiplier = 1;
            setSpeedMultiplier(stack, multiplier);
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
                entity.remove(Entity.RemovalReason.DISCARDED);
            }
            displayEntities.get(dimension).remove(pos);

            if (displayEntities.get(dimension).isEmpty()) {
                displayEntities.remove(dimension);
            }
        }
    }

    public static class AcceleratedBlocksSavedData extends SavedData {
        public static final String NAME = "avaritia_accelerated_blocks";
        private static final Codec<BlockEntry> BLOCK_ENTRY_CODEC = RecordCodecBuilder.create(instance -> instance.group(
                BlockPos.CODEC.fieldOf("pos").forGetter(BlockEntry::pos),
                Codec.INT.fieldOf("multiplier").forGetter(BlockEntry::multiplier)
        ).apply(instance, BlockEntry::new));
        private static final Codec<DimensionEntry> DIMENSION_ENTRY_CODEC = RecordCodecBuilder.create(instance -> instance.group(
                ResourceKey.codec(Registries.DIMENSION).fieldOf("dimension").forGetter(DimensionEntry::dimension),
                BLOCK_ENTRY_CODEC.listOf().fieldOf("blocks").forGetter(DimensionEntry::blocks)
        ).apply(instance, DimensionEntry::new));
        public static final Codec<AcceleratedBlocksSavedData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                DIMENSION_ENTRY_CODEC.listOf().optionalFieldOf("dimensions", List.of()).forGetter(AcceleratedBlocksSavedData::toEntries)
        ).apply(instance, AcceleratedBlocksSavedData::fromEntries));
        //26.3 SavedDataType 需要 4 参（含 DataFixTypes）；自定义数据没有对应 fixer，任意选择一个（无匹配规则即恒等）
        public static final SavedDataType<AcceleratedBlocksSavedData> TYPE = new SavedDataType<>(
                Identifier.fromNamespaceAndPath("avaritia", NAME), AcceleratedBlocksSavedData::new, CODEC,
                DataFixTypes.SAVED_DATA_COMMAND_STORAGE);

        private final Map<ResourceKey<Level>, Map<BlockPos, Integer>> acceleratedBlocks = new HashMap<>();

        public AcceleratedBlocksSavedData() {
        }

        private static AcceleratedBlocksSavedData fromEntries(List<DimensionEntry> entries) {
            AcceleratedBlocksSavedData savedData = new AcceleratedBlocksSavedData();
            for (DimensionEntry dimensionEntry : entries) {
                Map<BlockPos, Integer> blocks = new HashMap<>();
                for (BlockEntry blockEntry : dimensionEntry.blocks()) {
                    blocks.put(blockEntry.pos().immutable(), blockEntry.multiplier());
                }
                savedData.acceleratedBlocks.put(dimensionEntry.dimension(), blocks);
            }
            return savedData;
        }

        private List<DimensionEntry> toEntries() {
            List<DimensionEntry> entries = new ArrayList<>();
            for (Map.Entry<ResourceKey<Level>, Map<BlockPos, Integer>> dimensionEntry : acceleratedBlocks.entrySet()) {
                List<BlockEntry> blocks = new ArrayList<>();
                for (Map.Entry<BlockPos, Integer> blockEntry : dimensionEntry.getValue().entrySet()) {
                    blocks.add(new BlockEntry(blockEntry.getKey(), blockEntry.getValue()));
                }
                entries.add(new DimensionEntry(dimensionEntry.getKey(), blocks));
            }
            return entries;
        }

        public Map<ResourceKey<Level>, Map<BlockPos, Integer>> getAcceleratedBlocks() {
            return acceleratedBlocks;
        }

        public void setAcceleratedBlocks(Map<ResourceKey<Level>, Map<BlockPos, Integer>> blocks) {
            this.acceleratedBlocks.clear();
            this.acceleratedBlocks.putAll(blocks);
            setDirty();
        }

        private record DimensionEntry(ResourceKey<Level> dimension, List<BlockEntry> blocks) {
        }

        private record BlockEntry(BlockPos pos, int multiplier) {
        }
    }

    public static AcceleratedBlocksSavedData getSavedData(ServerLevel level) {
        return level.getDataStorage().computeIfAbsent(AcceleratedBlocksSavedData.TYPE);
    }

    public static void loadAcceleratedBlocksFromSavedData(ServerLevel level) {
        acceleratedBlocks.clear();
        displayEntities.clear();
        acceleratedBlocks.putAll(getSavedData(level).getAcceleratedBlocks());
    }

    public static void saveAcceleratedBlocksToSavedData(ServerLevel level) {
        AcceleratedBlocksSavedData savedData = getSavedData(level);
        savedData.setAcceleratedBlocks(acceleratedBlocks);
    }

    public static void clearServerState() {
        acceleratedBlocks.clear();
        displayEntities.clear();
    }


    public static class TickHandler {
        public static void onServerTick(ServerLevel level) {
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


                // 更新实体显示
                Map<BlockPos, AcceleratorDisplayEntity> displays = displayEntities.get(level.dimension());
                AcceleratorDisplayEntity entity = displays == null ? null : displays.get(pos);
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
                if (block instanceof BonemealableBlock growable) {
                    RandomSource random = level.getRandom();
                    for (int i = 0; i < times; i++) {
                        if (!growable.isValidBonemealTarget(level, pos, state, BonemealSource.MOB)) break;
                        if (growable.isBonemealSuccess(level, random, pos, state, BonemealSource.MOB)) {
                            try {
                                growable.performBonemeal(level, random, pos, state, BonemealSource.MOB);
                            } catch (Exception e) {
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
                    entity.remove(Entity.RemovalReason.DISCARDED);
                }
                displayEntities.get(dimension).remove(pos);
                if (displayEntities.get(dimension).isEmpty()) {
                    displayEntities.remove(dimension);
                }
            }
        }
    }
}
