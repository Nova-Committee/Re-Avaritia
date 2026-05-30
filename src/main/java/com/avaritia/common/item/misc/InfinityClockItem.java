package com.avaritia.common.item.misc;

import com.avaritia.api.iface.item.IInfinityClockSwitchable;
import com.avaritia.common.entity.AcceleratorDisplayEntity;
import com.avaritia.common.item.resources.ResourceItem;
import com.avaritia.common.menu.InfinityClockMenu;
import com.avaritia.init.data.save.AcceleratedBlocksSavedData;
import com.avaritia.init.registry.ModRarities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.saveddata.SavedData;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

public class InfinityClockItem extends ResourceItem implements IInfinityClockSwitchable {
    public static final Map<ResourceKey<Level>, Map<BlockPos, Integer>> acceleratedBlocks = new HashMap<>();
    public static final Map<ResourceKey<Level>, Map<BlockPos, AcceleratorDisplayEntity>> displayEntities = new HashMap<>();

    public InfinityClockItem() {
        super(ModRarities.COSMIC.getValue(), false, new Item.Properties().stacksTo(1));
    }

    @Override
    public boolean isFoil(@NotNull ItemStack pStack) {
        return false;
    }

    @Override
    public boolean isDamageable(@NotNull ItemStack stack) {
        return false;
    }

    @Override
    public @NotNull InteractionResult use(Level level, Player player, @NotNull InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);

        if (level.isClientSide()) return InteractionResult.PASS;

        boolean upMode = isActive(stack, "infinity_clock_up");

        if (player.isShiftKeyDown()) {
            switchClockMode(level, player, hand, "infinity_clock_up");
            return InteractionResult.SUCCESS;
        }

        if (upMode) {
            stack.update(DataComponents.CUSTOM_DATA, CustomData.EMPTY,
                    (customData) ->
                            customData.update(
                                    tag -> {
                                        int current  = tag.contains("SpeedMultiplier") ? tag.getInt("SpeedMultiplier").get() : 1;
                                        int next;

                                        switch (current) {
                                            case 1 -> next = 4;
                                            case 4 -> next = 16;
                                            case 16 -> next = 64;
                                            case 64 -> next = 256;
                                            case 256 -> next = 512;
                                            default -> next = 1;
                                        }
                                        player.sendOverlayMessage(Component.literal(next + "x"));
                                        tag.putInt("SpeedMultiplier", next);
                                    }
                            )
            );
            return InteractionResult.SUCCESS;
        }

        player.openMenu(
                new SimpleMenuProvider(
                        (id, inv, buf) -> new InfinityClockMenu(id, inv),
                        Component.translatable("item.avaritia.infinity_clock")
                )
        );

        return InteractionResult.SUCCESS;
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

        if (level.isClientSide()) return InteractionResult.SUCCESS;

        stack.update(DataComponents.CUSTOM_DATA, CustomData.EMPTY,
                (customData) ->
                        customData.update(
                                tag -> {
                                    int multiplier  = tag.contains("SpeedMultiplier") ? tag.getInt("SpeedMultiplier").get() : 1;

                                    if (multiplier == 1) {
                                        removeAcceleration(level, pos);
                                        return;
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
                                }
                        )
        );
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

    public static AcceleratedBlocksSavedData getSavedData(ServerLevel level) {
        return level.getDataStorage().computeIfAbsent(
                new SavedData(AcceleratedBlocksSavedData::new, AcceleratedBlocksSavedData::new),
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

                // 检查方块是否仍然存在并且有?
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


}
