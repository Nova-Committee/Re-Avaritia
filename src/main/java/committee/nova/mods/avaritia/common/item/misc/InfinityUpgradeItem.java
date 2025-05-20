package committee.nova.mods.avaritia.common.item.misc;

import committee.nova.mods.avaritia.common.item.resources.ResourceItem;
import committee.nova.mods.avaritia.common.tile.collector.BaseNeutronCollectorTile;
import committee.nova.mods.avaritia.common.tile.collector.CollectorTier;
import committee.nova.mods.avaritia.init.registry.ModBlocks;
import committee.nova.mods.avaritia.init.registry.ModItems;
import committee.nova.mods.avaritia.init.registry.ModRarities;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import org.jetbrains.annotations.NotNull;

/**
 * @Project: Avaritia
 * @Author: cnlimiter
 * @CreateTime: 2025/3/25 19:21
 * @Description:
 */
public class InfinityUpgradeItem extends ResourceItem {
    public InfinityUpgradeItem(String registryName) {
        super(ModRarities.LEGEND.getValue(), registryName, true,
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
        var tile = pContext.getLevel().getBlockEntity(pContext.getClickedPos());
        var player = pContext.getPlayer();
        var itemInHand = pContext.getItemInHand();

        if (tile instanceof BaseNeutronCollectorTile collectorTile && player instanceof ServerPlayer serverPlayer && serverPlayer.isCrouching()) {
            switch (collectorTile.getTier()) {
                case DEFAULT -> {
                    collectorTile.setTier(CollectorTier.DENSE);
                    level.setBlockAndUpdate(blockpos, ModBlocks.dense_neutron_collector.get().withPropertiesOf(blockstate));
                    level.playSound(serverPlayer, blockpos, SoundEvents.PLAYER_LEVELUP, SoundSource.PLAYERS);
                    itemInHand.consume(1, serverPlayer);
                    return InteractionResult.SUCCESS;
                }
                case DENSE -> {
                    collectorTile.setTier(CollectorTier.DENSER);
                    level.setBlockAndUpdate(blockpos, ModBlocks.denser_neutron_collector.get().withPropertiesOf(blockstate));
                    level.playSound(serverPlayer, blockpos, SoundEvents.PLAYER_LEVELUP, SoundSource.PLAYERS);
                    itemInHand.consume(1, serverPlayer);
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
