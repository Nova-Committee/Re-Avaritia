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

import static committee.nova.mods.avaritia.init.registry.ModDataComponents.INFINITY_MODE;

/**
 * @Project: Avaritia
 * @Author: cnlimiter
 * @CreateTime: 2024/11/4 00:16
 * @Description: 切换状态
 */
public interface ISwitchable {
    default boolean isActive(ItemStack stack, String funcName) {
        if (!stack.getOrDefault(INFINITY_MODE, new CompoundTag()).contains(funcName)) return false;
        return stack.getOrDefault(INFINITY_MODE, new CompoundTag()).getBoolean(funcName);
    }

    default void switchMode(@NotNull Level world, Player player, @NotNull InteractionHand hand, String funcName) {
        ItemStack stack = player.getItemInHand(hand);
        CompoundTag tags = stack.getOrDefault(INFINITY_MODE, new CompoundTag());
        Component funcTooltip = Component.translatable("tooltip.avaritia.tool." + funcName);
        tags.putBoolean(funcName, !tags.getBoolean(funcName));
        if (!world.isClientSide && player instanceof ServerPlayer serverPlayer)
            serverPlayer.sendSystemMessage(
                tags.getBoolean(funcName) ? ModTooltips.ACTIVE.args(funcTooltip).build() : ModTooltips.INACTIVE.args(funcTooltip).build()
                , true);
        player.swing(hand);
    }
}
