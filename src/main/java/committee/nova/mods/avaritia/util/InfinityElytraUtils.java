package committee.nova.mods.avaritia.util;

import committee.nova.mods.avaritia.Const;
import committee.nova.mods.avaritia.compat.curios.CuriosTools;
import committee.nova.mods.avaritia.init.registry.ModItems;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * 统一查询原版胸甲位与 Curios 背部槽中的无尽鞘翅。
 */
public final class InfinityElytraUtils {
    private static final String BACK_SLOT = "back";
    private static final int FALL_FLYING_ACTIVE = -1;
    private static final int PENDING_TAKEOFF_TICKS = 8;
    private static final Map<UUID, Integer> CURIO_FALL_FLYERS = new HashMap<>();

    private InfinityElytraUtils() {
    }

    public static boolean hasInfinityElytraEquipped(Player player) {
        return !getInfinityElytraStack(player).isEmpty();
    }

    public static ItemStack getInfinityElytraStack(Player player) {
        ItemStack chestStack = player.getItemBySlot(EquipmentSlot.CHEST);
        if (isInfinityElytra(chestStack)) {
            return chestStack;
        }
        return getInfinityElytraFromCuriosBackSlot(player);
    }

    public static ItemStack getInfinityElytraFromCuriosBackSlot(Player player) {
        if (!Const.curios) {
            return ItemStack.EMPTY;
        }
        return CuriosTools.getFirstItemFromCuriosSlot(player, BACK_SLOT, InfinityElytraUtils::isInfinityElytra);
    }

    public static boolean isUsingOtherFlightMode(Player player) {
        return player.isSpectator() || player.getAbilities().flying;
    }

    public static boolean tryStartFallFlying(ServerPlayer player) {
        if (isUsingOtherFlightMode(player)) {
            clearCuriosFallbackFallFlying(player);
            return false;
        }
        if (player.isFallFlying() || player.tryToStartFallFlying()) {
            return true;
        }
        if (player.onGround() || !canUseCuriosFallback(player)) {
            return false;
        }

        return startCuriosFallbackFallFlying(player);
    }

    public static boolean startCuriosFallbackFallFlying(ServerPlayer player) {
        if (!canUseCuriosFallback(player)) {
            return false;
        }
        activateCuriosFallbackFallFlying(player);
        return player.isFallFlying();
    }

    public static void prepareCuriosFallbackTakeoff(ServerPlayer player) {
        if (isUsingOtherFlightMode(player)) {
            clearCuriosFallbackFallFlying(player);
        } else if (canUseCuriosFallback(player)) {
            CURIO_FALL_FLYERS.put(player.getUUID(), PENDING_TAKEOFF_TICKS);
        }
    }

    public static void updateCuriosFallbackFallFlying(ServerPlayer player) {
        Integer state = CURIO_FALL_FLYERS.get(player.getUUID());
        if (state == null) {
            return;
        }
        if (isUsingOtherFlightMode(player)) {
            CURIO_FALL_FLYERS.remove(player.getUUID());
            return;
        }
        if (canUseCuriosFallback(player) && !player.onGround()) {
            if (!player.isFallFlying()) {
                activateCuriosFallbackFallFlying(player);
            }
            return;
        }
        if (state > 0 && canUseCuriosFallback(player)) {
            CURIO_FALL_FLYERS.put(player.getUUID(), state - 1);
        } else {
            CURIO_FALL_FLYERS.remove(player.getUUID());
        }
    }

    public static void clearCuriosFallbackFallFlying(Player player) {
        CURIO_FALL_FLYERS.remove(player.getUUID());
    }

    private static void activateCuriosFallbackFallFlying(ServerPlayer player) {
        CURIO_FALL_FLYERS.compute(player.getUUID(), (uuid, state) ->
                player.onGround() && state != null && state > 0 ? state : FALL_FLYING_ACTIVE);
        if (!player.isFallFlying()) {
            player.startFallFlying();
        }
        player.resetFallDistance();
    }

    private static boolean canUseCuriosFallback(Player player) {
        return Const.curios
                && !getInfinityElytraFromCuriosBackSlot(player).isEmpty()
                && !isInfinityElytra(player.getItemBySlot(EquipmentSlot.CHEST))
                && !isUsingOtherFlightMode(player)
                && !player.isPassenger()
                && !player.isInWater()
                && !player.onClimbable()
                && !player.hasEffect(MobEffects.LEVITATION);
    }

    private static boolean isInfinityElytra(ItemStack stack) {
        return stack.is(ModItems.infinity_elytra.get());
    }
}
