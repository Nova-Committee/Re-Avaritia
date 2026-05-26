package com.avaritia.api.iface.item;

import com.avaritia.init.registry.ModTooltips;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

public interface IInfinityClockSwitchable extends ISwitchable {

    default void switchClockMode(@NotNull Level world, Player player, @NotNull InteractionHand hand, String funcName) {
        ItemStack stack = player.getItemInHand(hand);
        stack.update(DataComponents.CUSTOM_DATA, CustomData.EMPTY,
                (customData) ->
                        customData.update(
                                tag -> {
                                    CompoundTag modeTag  = new CompoundTag();
                                    modeTag = tag.contains("mode") ? tag.getCompound("mode") : modeTag;

                                    Component message = Component.empty();

                                    switch (funcName) {
                                        case "infinity_clock_up":
                                            message = modeTag.getBoolean(funcName) ?
                                                    Component.translatable("tooltip.avaritia.tool.infinity_clock.overclock_enabled") :
                                                    Component.translatable("tooltip.avaritia.tool.infinity_clock.overclock_disabled");
                                            break;
                                        default:
                                            Component funcTooltip = Component.translatable("tooltip.avaritia.tool." + funcName);
                                            message = modeTag.getBoolean(funcName) ?
                                                    ModTooltips.ACTIVE.args(funcTooltip).build() :
                                                    ModTooltips.INACTIVE.args(funcTooltip).build();
                                            break;
                                    }

                                    modeTag.putBoolean(funcName, !modeTag.getBoolean(funcName));
                                    if (!world.isClientSide && player instanceof ServerPlayer serverPlayer)
                                        serverPlayer.sendSystemMessage(message, true);
                                    tag.put("mode", modeTag);
                                }
                        )
        );
        player.swing(hand);
    }
}
