package committee.nova.mods.avaritia.api.iface.item;

import committee.nova.mods.avaritia.api.utils.ItemUtils;
import committee.nova.mods.avaritia.init.registry.ModTooltips;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

public interface IInfinityClockSwitchable extends ISwitchable {

    default void switchClockMode(@NotNull Level world, Player player, @NotNull InteractionHand hand, String funcName) {
        ItemStack stack = player.getItemInHand(hand);
        CompoundTag tags = ItemUtils.getOrCreateChildTag(stack, "mode");
        Component message;

        switch (funcName) {
            case "infinity_clock_up":
                message = tags.getBoolean(funcName) ?
                        Component.translatable("tooltip.avaritia.tool.infinity_clock.overclock_enabled") :
                        Component.translatable("tooltip.avaritia.tool.infinity_clock.overclock_disabled");
                break;
            default:
                Component funcTooltip = Component.translatable("tooltip.avaritia.tool." + funcName);
                message = tags.getBoolean(funcName) ?
                        ModTooltips.ACTIVE.args(funcTooltip).build() :
                        ModTooltips.INACTIVE.args(funcTooltip).build();
                break;
        }

        tags.putBoolean(funcName, !tags.getBoolean(funcName));
        if (!world.isClientSide && player instanceof ServerPlayer serverPlayer)
            serverPlayer.sendSystemMessage(message, true);
        player.swing(hand);
    }
}
