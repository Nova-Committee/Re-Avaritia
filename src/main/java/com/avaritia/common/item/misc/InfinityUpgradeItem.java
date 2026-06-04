package com.avaritia.common.item.misc;

import com.avaritia.common.item.resources.ResourceItem;
import com.avaritia.common.tile.NeutronCollectorTile;
import com.avaritia.common.tile.NeutronCompressorTile;
import com.avaritia.init.registry.ModBlocks;
import com.avaritia.init.registry.ModItems;
import com.avaritia.init.registry.ModRarities;
import com.avaritia.init.registry.enums.CollectorTier;
import com.avaritia.init.registry.enums.CompressorTier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import org.jetbrains.annotations.NotNull;
import org.jspecify.annotations.NonNull;


/**
 * @Project: Avaritia
 * @Author: cnlimiter
 * @CreateTime: 2025/3/25 19:21
 * @Description:
 */
public class InfinityUpgradeItem extends ResourceItem {
    public InfinityUpgradeItem() {
        super(ModRarities.LEGEND.getValue(), true,
                ModItems.properties()
                        .durability(16)

                );
    }

    @Override
    public boolean isFoil(@NotNull ItemStack pStack) {
        return false;
    }

    @Override
    public boolean isCombineRepairable(@NonNull ItemStack stack) {
        return stack.is(ModItems.star_fuel.get());
    }


    @Override
    public @NotNull InteractionResult useOn(@NotNull UseOnContext pContext) {
        var level = pContext.getLevel();
        var blockpos = pContext.getClickedPos();
        var blockstate = level.getBlockState(blockpos);
        var tile = pContext.getLevel().getBlockEntity(pContext.getClickedPos());
        var player = pContext.getPlayer();
        var itemInHand = pContext.getItemInHand();
        if (tile instanceof NeutronCompressorTile compressorTile && player instanceof ServerPlayer serverPlayer && serverPlayer.isCrouching()) {
            switch (compressorTile.getTier()) {
                case DEFAULT -> {
                    compressorTile.setTier(CompressorTier.DENSE);
                    level.setBlockAndUpdate(blockpos, ModBlocks.dense_neutron_compressor.get().withPropertiesOf(blockstate));
                    level.playSound(serverPlayer, blockpos, SoundEvents.PLAYER_LEVELUP, SoundSource.PLAYERS);
                    compressorTile.setSideConfiguration(compressorTile.getSideConfiguration());
                    itemInHand.setDamageValue(itemInHand.getDamageValue() + 1);
                    if (itemInHand.getDamageValue() >= itemInHand.getMaxDamage()) {
                        itemInHand.shrink(1);
                    }
                    return InteractionResult.SUCCESS;
                }
                case DENSE -> {
                    compressorTile.setTier(CompressorTier.DENSER);
                    level.setBlockAndUpdate(blockpos, ModBlocks.denser_neutron_compressor.get().withPropertiesOf(blockstate));
                    level.playSound(serverPlayer, blockpos, SoundEvents.PLAYER_LEVELUP, SoundSource.PLAYERS);
                    compressorTile.setSideConfiguration(compressorTile.getSideConfiguration());
                    itemInHand.setDamageValue(itemInHand.getDamageValue() + 1);
                    if (itemInHand.getDamageValue() >= itemInHand.getMaxDamage()) {
                        itemInHand.shrink(1);
                    }
                    return InteractionResult.SUCCESS;
                }
                case DENSER -> {
                    compressorTile.setTier(CompressorTier.DENSEST);
                    level.setBlockAndUpdate(blockpos, ModBlocks.densest_neutron_compressor.get().withPropertiesOf(blockstate));
                    level.playSound(serverPlayer, blockpos, SoundEvents.PLAYER_LEVELUP, SoundSource.PLAYERS);
                    compressorTile.setSideConfiguration(compressorTile.getSideConfiguration());
                    itemInHand.setDamageValue(itemInHand.getDamageValue() + 1);
                    if (itemInHand.getDamageValue() >= itemInHand.getMaxDamage()) {
                        itemInHand.shrink(1);
                    }
                    return InteractionResult.SUCCESS;
                }
                default -> {
                    return InteractionResult.PASS;
                }
            }
        }else if (tile instanceof NeutronCollectorTile collectorTile && player instanceof ServerPlayer serverPlayer && serverPlayer.isCrouching()) {
            switch (collectorTile.getTier()) {
                case DEFAULT -> {
                    collectorTile.setTier(CollectorTier.DENSE);
                    level.setBlockAndUpdate(blockpos, ModBlocks.dense_neutron_collector.get().withPropertiesOf(blockstate));
                    level.playSound(serverPlayer, blockpos, SoundEvents.PLAYER_LEVELUP, SoundSource.PLAYERS);
                    collectorTile.setSideConfiguration(collectorTile.getSideConfiguration());
                    itemInHand.setDamageValue(itemInHand.getDamageValue() + 1);
                    if (itemInHand.getDamageValue() >= itemInHand.getMaxDamage()) {
                        itemInHand.shrink(1);
                    }
                    return InteractionResult.SUCCESS;
                }
                case DENSE -> {
                    collectorTile.setTier(CollectorTier.DENSER);
                    level.setBlockAndUpdate(blockpos, ModBlocks.denser_neutron_collector.get().withPropertiesOf(blockstate));
                    level.playSound(serverPlayer, blockpos, SoundEvents.PLAYER_LEVELUP, SoundSource.PLAYERS);
                    collectorTile.setSideConfiguration(collectorTile.getSideConfiguration());
                    itemInHand.setDamageValue(itemInHand.getDamageValue() + 1);
                    if (itemInHand.getDamageValue() >= itemInHand.getMaxDamage()) {
                        itemInHand.shrink(1);
                    }
                    return InteractionResult.SUCCESS;
                }
                case DENSER -> {
                    collectorTile.setTier(CollectorTier.DENSEST);
                    level.setBlockAndUpdate(blockpos, ModBlocks.densest_neutron_collector.get().withPropertiesOf(blockstate));
                    level.playSound(serverPlayer, blockpos, SoundEvents.PLAYER_LEVELUP, SoundSource.PLAYERS);
                    collectorTile.setSideConfiguration(collectorTile.getSideConfiguration());
                    itemInHand.setDamageValue(itemInHand.getDamageValue() + 1);
                    if (itemInHand.getDamageValue() >= itemInHand.getMaxDamage()) {
                        itemInHand.shrink(1);
                    }
                    return InteractionResult.SUCCESS;
                }
                default -> {
                    return InteractionResult.PASS;
                }
            }
        } else {
            return super.useOn(pContext);
        }
    }

}
