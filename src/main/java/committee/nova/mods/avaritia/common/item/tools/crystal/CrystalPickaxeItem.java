package committee.nova.mods.avaritia.common.item.tools.crystal;

import committee.nova.mods.avaritia.api.iface.ITooltip;
import committee.nova.mods.avaritia.api.utils.ItemUtils;
import committee.nova.mods.avaritia.init.registry.ModRarities;
import committee.nova.mods.avaritia.init.registry.ModToolTiers;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.PickaxeItem;
import net.minecraft.world.item.enchantment.Enchantment;
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
public class CrystalPickaxeItem extends PickaxeItem implements ITooltip {

    private final String name;

    public CrystalPickaxeItem(String name) {
        super(ModToolTiers.CRYSTAL,
                new Properties()
                        .rarity(ModRarities.EPIC)
                        .stacksTo(1)
                        .fireResistant()
                        .attributes(createAttributes(ModToolTiers.CRYSTAL, 0, ModToolTiers.BLAZE.getSpeed()))
        );
        this.name = name;
    }

    @Override
    public boolean hasDescTooltip() {
        return true;
    }

    @Override
    public boolean isFoil(@NotNull ItemStack pStack) {
        return false;
    }

    @Override
    public int getEnchantmentValue(@NotNull ItemStack stack) {
        return 0;
    }

    @Override
    public float getDestroySpeed(@NotNull ItemStack stack, @NotNull BlockState state) {
        return 100F;
    }

    @Override
    public @NotNull InteractionResultHolder<ItemStack> use(@NotNull Level world, Player player, @NotNull InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        Holder<Enchantment> SILK_TOUCH =
                player.level().registryAccess().lookupOrThrow(Registries.ENCHANTMENT)
                        .getOrThrow(Enchantments.SILK_TOUCH);
        Holder<Enchantment> FORTUNE =
                player.level().registryAccess().lookupOrThrow(Registries.ENCHANTMENT)
                        .getOrThrow(Enchantments.FORTUNE);
        if (player.isShiftKeyDown()) {
            if (EnchantmentHelper.getTagEnchantmentLevel(SILK_TOUCH, stack) > 0) {
                ItemUtils.clearEnchants(stack);
                stack.enchant(FORTUNE, 3);
                if (!world.isClientSide && player instanceof ServerPlayer serverPlayer)
                    serverPlayer.sendSystemMessage(Component.translatable("tooltip.crystal_pickaxe.enchant_1"), true);
            } else {
                ItemUtils.clearEnchants(stack);
                stack.enchant(SILK_TOUCH, 1);
                if (!world.isClientSide && player instanceof ServerPlayer serverPlayer)
                    serverPlayer.sendSystemMessage(Component.translatable("tooltip.crystal_pickaxe.enchant_2"), true);
            }
            player.swing(hand);
            return InteractionResultHolder.success(stack);
        }
        return super.use(world, player, hand);
    }
}
