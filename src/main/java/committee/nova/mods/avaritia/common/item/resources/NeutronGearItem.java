package committee.nova.mods.avaritia.common.item.resources;

import committee.nova.mods.avaritia.common.tile.collector.BaseNeutronCollectorTile;
import committee.nova.mods.avaritia.common.tile.collector.CollectorTier;
import committee.nova.mods.avaritia.init.registry.ModBlocks;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.context.UseOnContext;
import org.jetbrains.annotations.NotNull;

/**
 * NeutronGearItem
 *
 * @author cnlimiter
 * @version 1.0
 * @description
 * @date 2024/6/14 下午12:15
 */
public class NeutronGearItem extends ResourceItem{
    public NeutronGearItem() {
        super(Rarity.RARE, "neutron_gear", true);
    }

    @Override
    public @NotNull InteractionResult useOn(@NotNull UseOnContext pContext) {
        var level = pContext.getLevel();
        var blockpos = pContext.getClickedPos();
        var blockstate = level.getBlockState(blockpos);
        var tile = pContext.getLevel().getBlockEntity(pContext.getClickedPos());
        var player = pContext.getPlayer();
        var itemInHand = pContext.getItemInHand();

        if (tile instanceof BaseNeutronCollectorTile collectorTile && player != null && player.isCrouching()){
            switch (collectorTile.getTier()) {
                case DEFAULT -> {
                    collectorTile.setTier(CollectorTier.DENSE);
                    level.setBlockAndUpdate(blockpos, ModBlocks.dense_neutron_collector.get().withPropertiesOf(blockstate));
                    level.playSound(player, blockpos, SoundEvents.PLAYER_LEVELUP, SoundSource.PLAYERS);
                    itemInHand.shrink(1);
                    player.getCooldowns().addCooldown(this, 20);
                    return InteractionResult.CONSUME;
                }
                case DENSE -> {
                    collectorTile.setTier(CollectorTier.DENSER);
                    level.setBlockAndUpdate(blockpos, ModBlocks.denser_neutron_collector.get().withPropertiesOf(blockstate));
                    level.playSound(player, blockpos, SoundEvents.PLAYER_LEVELUP, SoundSource.PLAYERS);
                    itemInHand.shrink(1);
                    player.getCooldowns().addCooldown(this, 20);
                    return InteractionResult.CONSUME;
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
