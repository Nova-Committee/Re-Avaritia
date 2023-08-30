package committee.nova.mods.avaritia.common.item.tools;

import committee.nova.mods.avaritia.common.entity.ImmortalItemEntity;
import committee.nova.mods.avaritia.init.config.ModConfig;
import committee.nova.mods.avaritia.init.handler.InfinityHandler;
import committee.nova.mods.avaritia.init.registry.ModCreativeModeTabs;
import committee.nova.mods.avaritia.init.registry.ModEntities;
import committee.nova.mods.avaritia.init.registry.ModItems;
import committee.nova.mods.avaritia.util.ToolHelper;
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
                .stacksTo(1)
                .tab(ModCreativeModeTabs.TAB)
                .fireResistant());

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
    public int getEnchantmentValue(ItemStack stack) {
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
        if (player.isCrouching() && !player.getCommandSenderWorld().isClientSide) {
            breakOtherBlock(player, stack, pos);
        }
        return false;
    }

    public void breakOtherBlock(Player player, ItemStack stack, BlockPos pos) {
        if (player.isCrouching()) {
            return;
        }
        InfinityHandler.startCrawlerTask(player.getCommandSenderWorld(), player, stack, pos, ModConfig.axeChainCount.get(), false, true, new HashSet<>());
    }
}
