package committee.nova.mods.avaritia.api.iface;

import committee.nova.mods.avaritia.init.registry.ModTooltips;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

/**
 * @Project: Avaritia
 * @Author: Cu6
 * @CreateTime: 2025/08/23 2:00
 * @Description: 三模式切换
 */
public interface ITriModeSwitchable {


    static int getMode(ItemStack stack, String funcName) {

        return stack.getOrCreateTagElement("tri_mode").getInt(funcName);
    }


    default boolean isMode(ItemStack stack, String funcName, int mode) {

        if (mode < 0 || mode > 2) return false;
        return getMode(stack, funcName) == mode;
    }


    default void cycleMode(@NotNull Level world, Player player, @NotNull InteractionHand hand,
                           String funcName, String[] modeNames) {

        if (modeNames.length != 3) {
            throw new IllegalArgumentException("模式名称数组必须包含3个元素");
        }

        ItemStack stack = player.getItemInHand(hand);
        CompoundTag tags = stack.getOrCreateTagElement("tri_mode");
        int currentMode = tags.getInt(funcName);


        int nextMode = (currentMode + 1) % 3;
        tags.putInt(funcName, nextMode);


        if (!world.isClientSide && player instanceof ServerPlayer serverPlayer) {
            Component modeName = Component.translatable(modeNames[nextMode]);
            serverPlayer.sendSystemMessage(
                    ModTooltips.CHANGED.args(modeName).build(),
                    true
            );
        }

        player.swing(hand);
    }


    default void setMode(ItemStack stack, String funcName, int mode) {
        if (mode < 0 || mode > 2) {
            throw new IllegalArgumentException("模式值必须在0-2之间");
        }
        stack.getOrCreateTagElement("tri_mode").putInt(funcName, mode);
    }
}
