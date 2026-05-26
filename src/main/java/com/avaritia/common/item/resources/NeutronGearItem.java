package com.avaritia.common.item.resources;

import com.avaritia.init.registry.ModRarities;

/**
 * NeutronGearItem
 *
 * @author cnlimiter
 * @version 1.0
 * @description
 * @date 2024/6/14 涓嬪�?2:15
 */
public class NeutronGearItem extends ResourceItem {
    public NeutronGearItem() {
        super(ModRarities.RARE, true);
    }

//    @Override
//    public @NotNull InteractionResult useOn(@NotNull UseOnContext pContext) {
//        var level = pContext.getLevel();
//        var blockpos = pContext.getClickedPos();
//        var blockstate = level.getBlockState(blockpos);
//        var tile = pContext.getLevel().getBlockEntity(pContext.getClickedPos());
//        var player = pContext.getPlayer();
//        var itemInHand = pContext.getItemInHand();
//
//        if (tile instanceof BaseNeutronCollectorTile collectorTile && player != null && player.isCrouching()) {
//            switch (collectorTile.getTier()) {
//                case DEFAULT -> {
//                    collectorTile.setTier(CollectorTier.DENSE);
//                    level.setBlockAndUpdate(blockpos, ModBlocks.dense_neutron_collector.get().withPropertiesOf(blockstate));
//                    level.playSound(player, blockpos, SoundEvents.PLAYER_LEVELUP, SoundSource.PLAYERS);
//                    itemInHand.shrink(1);
//                    player.getCooldowns().addCooldown(this, 20);
//                    return InteractionResult.CONSUME;
//                }
//                case DENSE -> {
//                    collectorTile.setTier(CollectorTier.DENSER);
//                    level.setBlockAndUpdate(blockpos, ModBlocks.denser_neutron_collector.get().withPropertiesOf(blockstate));
//                    level.playSound(player, blockpos, SoundEvents.PLAYER_LEVELUP, SoundSource.PLAYERS);
//                    itemInHand.shrink(1);
//                    player.getCooldowns().addCooldown(this, 20);
//                    return InteractionResult.CONSUME;
//                }
//                default -> {
//                    return InteractionResult.PASS;
//                }
//            }
//        } else {
//            return super.useOn(pContext);
//        }
//    }
}
