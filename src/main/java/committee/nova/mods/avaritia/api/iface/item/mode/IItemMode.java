package committee.nova.mods.avaritia.api.iface.item.mode;

import committee.nova.mods.avaritia.api.utils.text.ILangEntry;
import committee.nova.mods.avaritia.init.registry.enums.ModLang;
import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

/**
 * @Project: Avaritia
 * @Author: cnlimiter
 * @CreateTime: 2025/5/24 19:39
 * @Description:
 */
public interface IItemMode<MODE extends Enum<MODE> & IModeEnum<MODE>> extends IModeChanger<MODE> {

    DataComponentType<MODE> getDataComponentType();

    MODE getDefaultMode();

    @Override
    default MODE getMode(@NotNull ItemStack stack) {
        return stack.getOrDefault(getDataComponentType(), getDefaultMode());
    }

    @Override
    default boolean changeMode(@NotNull Player player, @NotNull ItemStack stack, InteractionHand hand) {
        //Update the mode
        MODE mode = getMode(stack);
        MODE newMode = mode.next(stack);
        if (mode != newMode) {
            stack.set(getDataComponentType(), newMode);
            player.displayClientMessage(getModeSwitchEntry().translate(newMode), true);
            return true;
        }
        //If we have no modes, or we are set to the only mode fail
        return false;
    }

    @Override
    default boolean changeMode(@NotNull Player player, @NotNull ItemStack stack, InteractionHand hand, MODE newMode) {
        //Update the mode
        MODE mode = getMode(stack);
        if (mode != newMode) {
            stack.set(getDataComponentType(), newMode);
            player.displayClientMessage(getModeSwitchEntry().translate(newMode), true);
            return true;
        }
        //If we have no modes, or we are set to the only mode fail
        return false;
    }

    default ILangEntry getModeSwitchEntry() {
        return ModLang.MODE_SWITCH;
    }

    default Component getToolTip(ItemStack stack) {
        return ModLang.CURRENT_MODE.translate(ChatFormatting.AQUA, getMode(stack));
    }
}
