package committee.nova.mods.avaritia.util;

import committee.nova.mods.avaritia.Const;
import committee.nova.mods.avaritia.init.compat.curios.CuriosTools;
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
 * 无尽鞘翅装备查询与 Curios/Caelus 兼容入口。
 *
 * @author HowXu <dev@howxu.cn>
 * @author cnlimiter
 */
public class InfinityElytraUtils {
    private static final String CAELUS_MOD_ID = "caelus";
    private static final int FALL_FLYING_ACTIVE = -1;
    private static final int PENDING_TAKEOFF_TICKS = 8;
    private static final Map<UUID, Integer> CURIO_FALL_FLYERS = new HashMap<>();

    /**
     * cnlimiter：统一无尽鞘翅的胸甲位和 Curios 背饰槽查询，避免各处自行判断导致兼容逻辑分叉。
     */
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

    public static boolean tryStartFallFlying(ServerPlayer player) {
        if (player.isFallFlying()) {
            return true;
        }
        if (player.tryToStartFallFlying()) {
            return true;
        }
        if (!canUseCuriosFallback(player) || player.onGround()) {
            return false;
        }

        activateCuriosFallbackFallFlying(player);
        return true;
    }

    public static void prepareCuriosFallbackTakeoff(ServerPlayer player) {
        if (canUseCuriosFallback(player)) {
            CURIO_FALL_FLYERS.put(player.getUUID(), PENDING_TAKEOFF_TICKS);
        }
    }

    public static void updateCuriosFallbackFallFlying(ServerPlayer player) {
        Integer state = CURIO_FALL_FLYERS.get(player.getUUID());
        if (state == null) {
            return;
        }

        if (canUseCuriosFallback(player) && !player.onGround()) {
            activateCuriosFallbackFallFlying(player);
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
        CURIO_FALL_FLYERS.put(player.getUUID(), FALL_FLYING_ACTIVE);
        player.startFallFlying();
        player.resetFallDistance();
    }

    private static boolean canUseCuriosFallback(Player player) {
        return needsCuriosFallback(player)
                && hasInfinityElytraInCurios(player)
                && !player.isPassenger()
                && !player.isInWater()
                && !player.onClimbable()
                && !player.hasEffect(MobEffects.LEVITATION);
    }

    private static boolean needsCuriosFallback(Player player) {
        return Const.curios
                && !Const.isLoad(CAELUS_MOD_ID)
                && !isInfinityElytra(player.getItemBySlot(EquipmentSlot.CHEST));
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
