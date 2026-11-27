package committee.nova.mods.avaritia.util;

import committee.nova.mods.avaritia.common.item.misc.InfinityElytraItem;
import committee.nova.mods.avaritia.init.registry.ModItems;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ElytraItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import committee.nova.mods.avaritia.init.compat.trinkets.TrinketsIntegration;

/** Shared chest-slot and optional Trinkets query with a loader-only-safe bridge. */
public final class InfinityElytraUtils {
    private InfinityElytraUtils() {}

    public static ItemStack getInfinityElytraStack(LivingEntity entity) {
        ItemStack chest = entity.getItemBySlot(EquipmentSlot.CHEST);
        return chest.is(ModItems.infinity_elytra.get()) ? chest
                : TrinketsIntegration.findBack(entity, stack -> stack.is(ModItems.infinity_elytra.get()));
    }

    /** Infinity elytra has no durability; all other elytra keep vanilla's damage limit. */
    public static boolean isFlyEnabled(ItemStack stack) {
        return stack.getItem() instanceof InfinityElytraItem || ElytraItem.isFlyEnabled(stack);
    }

    public static boolean hasInfinityElytraEquipped(Player player) {
        return !getInfinityElytraStack(player).isEmpty();
    }

    public static boolean isUsingOtherFlightMode(Player player) {
        return player.getAbilities().flying || player.isSpectator();
    }

    public static boolean tryStartFallFlying(ServerPlayer player) {
        if (!hasInfinityElytraEquipped(player) || isUsingOtherFlightMode(player)) return false;
        return player.isFallFlying() || player.tryToStartFallFlying();
    }
}
