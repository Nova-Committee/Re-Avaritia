package committee.nova.mods.avaritia.util;

import committee.nova.mods.avaritia.init.compat.curios.CuriosTools;
import committee.nova.mods.avaritia.init.registry.ModItems;
import committee.nova.mods.avaritia.Const;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Seperate logics of infinity elytra checking from single chest to both curios and equipment
 * @author  HowXu <dev@howxu.cn>
 */
public class InfinityElytraUtils {
    private static final String CAELUS_MOD_ID = "caelus";
    private static final int ACTIVE_FALL_FLYING = -1;
    private static final int PENDING_TAKEOFF_TICKS = 8;
    private static final Map<UUID, Integer> CURIO_FALL_FLYERS = new HashMap<>();

    public static boolean hasInfinityElytraEquipped(Player player) {
        return !getInfinityElytraStack(player).isEmpty();
    }

    public static ItemStack getInfinityElytraStack(Player player) {
        ItemStack chestStack = player.getItemBySlot(EquipmentSlot.CHEST);
        if (isInfinityElytra(chestStack)) {
            return chestStack;
        }
        return findInfinityElytraInCurios(player);
    }

    public static boolean hasInfinityElytraInCurios(Player player) {
        return !findInfinityElytraInCurios(player).isEmpty();
    }

    public static boolean canStartCuriosFallbackFallFlying(Player player) {
        return canMaintainCuriosFallbackFallFlying(player) && !player.isFallFlying();
    }

    public static void startCuriosFallbackFallFlying(ServerPlayer player) {
        if (canMaintainCuriosFallbackFallFlying(player)) {
            CURIO_FALL_FLYERS.put(player.getUUID(), ACTIVE_FALL_FLYING);
            keepCuriosFallbackFallFlying(player);
        } else if (canWaitForCuriosFallbackTakeoff(player)) {
            CURIO_FALL_FLYERS.put(player.getUUID(), PENDING_TAKEOFF_TICKS);
        }
    }

    public static void updateCuriosFallbackFallFlying(ServerPlayer player) {
        Integer state = CURIO_FALL_FLYERS.get(player.getUUID());
        if (state == null) {
            return;
        }
        if (canMaintainCuriosFallbackFallFlying(player)) {
            CURIO_FALL_FLYERS.put(player.getUUID(), ACTIVE_FALL_FLYING);
            keepCuriosFallbackFallFlying(player);
        } else if (state > 0 && canWaitForCuriosFallbackTakeoff(player)) {
            CURIO_FALL_FLYERS.put(player.getUUID(), state - 1);
        } else {
            CURIO_FALL_FLYERS.remove(player.getUUID());
        }
    }

    public static void clearCuriosFallbackFallFlying(Player player) {
        CURIO_FALL_FLYERS.remove(player.getUUID());
    }

    private static void keepCuriosFallbackFallFlying(ServerPlayer player) {
        player.startFallFlying();
        player.resetFallDistance();
    }

    private static boolean canMaintainCuriosFallbackFallFlying(Player player) {
        return canWaitForCuriosFallbackTakeoff(player)
                && !player.onGround();
    }

    private static boolean canWaitForCuriosFallbackTakeoff(Player player) {
        return !Const.isLoad(CAELUS_MOD_ID)
                && hasInfinityElytraInCurios(player)
                && !player.isPassenger()
                && !player.isInWater()
                && !player.onClimbable()
                && !player.hasEffect(MobEffects.LEVITATION);
    }

    private static ItemStack findInfinityElytraInCurios(Player player) {
        if (!Const.curios) {
            return ItemStack.EMPTY;
        }
        return CuriosTools.getFirstItemFromCuriosSlot(player, CuriosTools.BACK_SLOT, InfinityElytraUtils::isInfinityElytra);
    }

    private static boolean isInfinityElytra(ItemStack stack) {
        return stack.is(ModItems.infinity_elytra.get());
    }
}
