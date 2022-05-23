package nova.committee.avaritia.common.item.tools;

import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.AxeItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.level.Level;
import nova.committee.avaritia.common.entity.ImmortalItemEntity;
import nova.committee.avaritia.init.config.ModConfig;
import nova.committee.avaritia.init.handler.InfinityHandler;
import nova.committee.avaritia.init.registry.ModEntities;
import nova.committee.avaritia.init.registry.ModItems;
import nova.committee.avaritia.init.registry.ModTab;
import nova.committee.avaritia.util.ToolHelper;
import org.jetbrains.annotations.Nullable;

import java.util.HashSet;

/**
 * Description:
 * Author: cnlimiter
 * Date: 2022/5/15 17:11
 * Version: 1.0
 */
public class AxeInfinityItem extends AxeItem {

    public AxeInfinityItem() {
        super(Tier.INFINITY_PICKAXE, 10, -3.0f, (new Properties())
                .tab(ModTab.TAB)
                .stacksTo(1)
                .fireResistant());

        setRegistryName("infinity_axe");
    }

    @Override
    public boolean isDamageable(ItemStack stack) {
        return false;
    }

    @Nullable
    @Override
    public Entity createEntity(Level level, Entity location, ItemStack stack) {
        return ImmortalItemEntity.create(ModEntities.IMMORTAL.get(), level, location.getX(), location.getY(), location.getZ(), stack);
    }

    @Override
    public Rarity getRarity(ItemStack p_77613_1_) {
        return ModItems.COSMIC_RARITY;
    }

    @Override
    public int getItemEnchantability(ItemStack stack) {
        return 0;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (player.isCrouching()) {
            player.swing(hand);

            int range = 13;
            var min = new BlockPos(-range, -3, -range);
            var max = new BlockPos(range, range * 2 - 3, range);

            ToolHelper.aoeBlocks(player, stack, level, player.getOnPos(), min, max, null, ToolHelper.materialsAxe, false);
        }
        return new InteractionResultHolder<>(InteractionResult.SUCCESS, stack);
    }

    @Override
    public boolean onBlockStartBreak(ItemStack stack, BlockPos pos, Player player) {
        if (player.isCrouching() && !player.level.isClientSide) {
            breakOtherBlock(player, stack, pos);
        }
        return false;
    }

    public void breakOtherBlock(Player player, ItemStack stack, BlockPos pos) {
        if (player.isCrouching()) {
            return;
        }
        InfinityHandler.startCrawlerTask(player.level, player, stack, pos, ModConfig.SERVER.axeChainCount.get(), false, true, new HashSet<>());
    }
}
