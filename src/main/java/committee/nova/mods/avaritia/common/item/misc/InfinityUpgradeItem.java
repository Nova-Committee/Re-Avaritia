package committee.nova.mods.avaritia.common.item.misc;

import committee.nova.mods.avaritia.common.item.resources.ResourceItem;
import committee.nova.mods.avaritia.common.tile.NeutronCollectorTile;
import committee.nova.mods.avaritia.common.tile.NeutronCompressorTile;
import committee.nova.mods.avaritia.init.registry.ModBlocks;
import committee.nova.mods.avaritia.init.registry.ModItems;
import committee.nova.mods.avaritia.init.registry.ModRarities;
import committee.nova.mods.avaritia.init.registry.enums.CollectorTier;
import committee.nova.mods.avaritia.init.registry.enums.CompressorTier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import org.jetbrains.annotations.NotNull;

/**
 * @Project: Avaritia
 * @author cnlimiter
 * @CreateTime: 2025/3/25 19:21
 * @Description:
 */
public class InfinityUpgradeItem extends ResourceItem {
    public InfinityUpgradeItem() {
        super(ModRarities.LEGEND, true,
                new Properties()
                        .durability(16)

        );
    }

    @Override
    public boolean isFoil(@NotNull ItemStack pStack) {
        return false;
    }

    @Override
    public boolean isValidRepairItem(@NotNull ItemStack pStack, @NotNull ItemStack pRepairCandidate) {
        return pRepairCandidate.is(ModItems.star_fuel.get());
    }

    @Override
    public @NotNull InteractionResult useOn(@NotNull UseOnContext pContext) {
        var level = pContext.getLevel();
        var blockpos = pContext.getClickedPos();
        var blockstate = level.getBlockState(blockpos);
        var tile = level.getBlockEntity(blockpos);
        var player = pContext.getPlayer();
        var itemInHand = pContext.getItemInHand();

        if (player instanceof ServerPlayer serverPlayer && serverPlayer.isShiftKeyDown()) {
            if (tile instanceof NeutronCollectorTile collectorTile) {
                switch (collectorTile.getTier()) {
                    case DEFAULT -> {
                        collectorTile.setTier(CollectorTier.DENSE);
                        level.setBlockAndUpdate(blockpos, ModBlocks.dense_neutron_collector.get().withPropertiesOf(blockstate));
                        level.playSound(serverPlayer, blockpos, SoundEvents.PLAYER_LEVELUP, SoundSource.PLAYERS);
                        collectorTile.setSideConfiguration(collectorTile.getSideConfiguration());
                        itemInHand.hurt(1, serverPlayer.getRandom(), serverPlayer);
                        return InteractionResult.SUCCESS;
                    }
                    case DENSE -> {
                        collectorTile.setTier(CollectorTier.DENSER);
                        level.setBlockAndUpdate(blockpos, ModBlocks.denser_neutron_collector.get().withPropertiesOf(blockstate));
                        level.playSound(serverPlayer, blockpos, SoundEvents.PLAYER_LEVELUP, SoundSource.PLAYERS);
                        collectorTile.setSideConfiguration(collectorTile.getSideConfiguration());
                        itemInHand.hurt(1, serverPlayer.getRandom(), serverPlayer);
                        return InteractionResult.SUCCESS;
                    }
                    case DENSER -> {
                        collectorTile.setTier(CollectorTier.DENSEST);
                        level.setBlockAndUpdate(blockpos, ModBlocks.densest_neutron_collector.get().withPropertiesOf(blockstate));
                        level.playSound(serverPlayer, blockpos, SoundEvents.PLAYER_LEVELUP, SoundSource.PLAYERS);
                        collectorTile.setSideConfiguration(collectorTile.getSideConfiguration());
                        itemInHand.hurt(4, serverPlayer.getRandom(), serverPlayer);
                        return InteractionResult.SUCCESS;
                    }
                    default -> {
                        return InteractionResult.PASS;
                    }
                }
            } else if (tile instanceof NeutronCompressorTile compressorTile) {
                switch (compressorTile.getTier()) {
                    case DEFAULT -> {
                        compressorTile.setTier(CompressorTier.DENSE);
                        level.setBlockAndUpdate(blockpos, ModBlocks.dense_neutron_compressor.get().withPropertiesOf(blockstate));
                        level.playSound(serverPlayer, blockpos, SoundEvents.PLAYER_LEVELUP, SoundSource.PLAYERS);
                        compressorTile.setSideConfiguration(compressorTile.getSideConfiguration());
                        itemInHand.hurt(1, serverPlayer.getRandom(), serverPlayer);
                        return InteractionResult.SUCCESS;
                    }
                    case DENSE -> {
                        compressorTile.setTier(CompressorTier.DENSER);
                        level.setBlockAndUpdate(blockpos, ModBlocks.denser_neutron_compressor.get().withPropertiesOf(blockstate));
                        level.playSound(serverPlayer, blockpos, SoundEvents.PLAYER_LEVELUP, SoundSource.PLAYERS);
                        compressorTile.setSideConfiguration(compressorTile.getSideConfiguration());
                        itemInHand.hurt(1, serverPlayer.getRandom(), serverPlayer);
                        return InteractionResult.SUCCESS;
                    }
                    case DENSER -> {
                        compressorTile.setTier(CompressorTier.DENSEST);
                        level.setBlockAndUpdate(blockpos, ModBlocks.densest_neutron_compressor.get().withPropertiesOf(blockstate));
                        level.playSound(serverPlayer, blockpos, SoundEvents.PLAYER_LEVELUP, SoundSource.PLAYERS);
                        compressorTile.setSideConfiguration(compressorTile.getSideConfiguration());
                        itemInHand.hurt(4, serverPlayer.getRandom(), serverPlayer);
                        return InteractionResult.SUCCESS;
                    }
                    default -> {
                        return InteractionResult.PASS;
                    }
                }
            }
            //else if (Const.isLoad("mekanism")){
//            if (tile instanceof TileEntityFactory tileEntityFactory && player instanceof ServerPlayer serverPlayer && serverPlayer.isShiftKeyDown()) {
//                switch (tileEntityFactory.tier) {
//                    case BASIC -> {
//                        tileEntityFactory.tier = FactoryTier.ADVANCED;
//                        level.setBlockAndUpdate(blockpos, tileEntityFactory.getBlockType().withPropertiesOf(blockstate));
//                        level.playSound(serverPlayer, blockpos, SoundEvents.PLAYER_LEVELUP, SoundSource.PLAYERS);
//                        itemInHand.hurt(1, serverPlayer.getRandom(), serverPlayer);
//                        return InteractionResult.SUCCESS;
//                    }
//                    case ADVANCED -> {
//                        tileEntityFactory.tier = FactoryTier.ELITE;
//                        level.setBlockAndUpdate(blockpos, tileEntityFactory.getBlockType().withPropertiesOf(blockstate));
//                        level.playSound(serverPlayer, blockpos, SoundEvents.PLAYER_LEVELUP, SoundSource.PLAYERS);
//                        itemInHand.hurt(1, serverPlayer.getRandom(), serverPlayer);
//                        return InteractionResult.SUCCESS;
//                    }
//                    case ELITE -> {
//                        tileEntityFactory.tier = FactoryTier.ULTIMATE;
//                        level.setBlockAndUpdate(blockpos, tileEntityFactory.getBlockType().withPropertiesOf(blockstate));
//                        level.playSound(serverPlayer, blockpos, SoundEvents.PLAYER_LEVELUP, SoundSource.PLAYERS);
//                        itemInHand.hurt(1, serverPlayer.getRandom(), serverPlayer);
//                        return InteractionResult.SUCCESS;
//                    }
//                    default -> {
//                        return InteractionResult.PASS;
//                    }
//                }
//            }
//            else {
//                return super.useOn(pContext);
//            }
//        }
            else {
                return super.useOn(pContext);
            }
        } else {
            return super.useOn(pContext);
        }
    }
}
