package committee.nova.mods.avaritia.common.item.tools;

import committee.nova.mods.avaritia.init.registry.ModTiers;
import committee.nova.mods.avaritia.util.ItemUtils;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.PickaxeItem;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;

/**
 * Description:
 * Author: cnlimiter
 * Date: 2022/3/31 10:25
 * Version: 1.0
 */
public class CrystalPickaxeItem extends PickaxeItem {

    public CrystalPickaxeItem() {
        super(ModTiers.CRYSTAL_PICKAXE, 1, -1.5F, (new Properties())
                .stacksTo(1)
                .fireResistant());
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
    public @NotNull Rarity getRarity(@NotNull ItemStack pStack) {
        return Rarity.EPIC;
    }

    @Override
    public int getEnchantmentValue(ItemStack stack) {
        return 0;
    }

    @Override
    public float getDestroySpeed(@NotNull ItemStack stack, @NotNull BlockState state) {
        return 100F;
    }

    @Override
    public @NotNull InteractionResultHolder<ItemStack> use(@NotNull Level world, Player player, @NotNull InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (player.isCrouching()) {
            if (EnchantmentHelper.getTagEnchantmentLevel(Enchantments.SILK_TOUCH, stack) > 0) {
                ItemUtils.clearEnchants(stack);
                stack.enchant(Enchantments.BLOCK_FORTUNE, 3);
                if(!world.isClientSide && player instanceof ServerPlayer serverPlayer) serverPlayer.sendSystemMessage(Component.translatable("tooltip.crystal_pickaxe.enchant_1"), true);
            } else {
                ItemUtils.clearEnchants(stack);
                stack.enchant(Enchantments.SILK_TOUCH, 1);
                if(!world.isClientSide && player instanceof ServerPlayer serverPlayer) serverPlayer.sendSystemMessage(Component.translatable("tooltip.crystal_pickaxe.enchant_2"), true);
            }
            player.swing(hand);
            return InteractionResultHolder.success(stack);
        }
        return super.use(world, player, hand);
    }



}
