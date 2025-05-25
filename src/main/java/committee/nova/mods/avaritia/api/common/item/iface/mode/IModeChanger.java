package committee.nova.mods.avaritia.api.common.item.iface.mode;

/**
 * @Project: Avaritia
 * @Author: cnlimiter
 * @CreateTime: 2025/5/24 19:41
 * @Description: from projecte
 * This interface specifies items that switch between modes when the mode switch keybind is activated (default G)
 * <p>
 * This is exposed through the Capability system.
 * <p>
 * Acquire an instance of this using {@link ItemStack#getCapability(ItemCapability)}.
 */

import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.capabilities.ItemCapability;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface IModeChanger<MODE> {

    /**
     * Gets the mode from this ItemStack
     *
     * @param stack The stack we want the mode of
     *
     * @return The mode of this ItemStack
     */
    MODE getMode(@NotNull ItemStack stack);

    /**
     * Called serverside when the player presses change mode
     *
     * @param player The player pressing the change mode key
     * @param stack  The stack whose mode we are changing
     * @param hand   The hand this stack was in, or null if the call was not from the player's hands
     *
     * @return Whether the operation succeeded
     */
    boolean changeMode(@NotNull Player player, @NotNull ItemStack stack, @Nullable InteractionHand hand);

    /**
     * Called serverside when the player presses change mode
     *
     * @param player    The player pressing the change mode key
     * @param stack     The stack whose mode we are changing
     * @param hand      The hand this stack was in, or null if the call was not from the player's hands
     * @param newMode   newMode
     *
     * @return Whether the operation succeeded
     */
    default boolean changeMode(@NotNull Player player, @NotNull ItemStack stack, @Nullable InteractionHand hand, MODE newMode){
        return changeMode(player, stack, hand);
    }
}
