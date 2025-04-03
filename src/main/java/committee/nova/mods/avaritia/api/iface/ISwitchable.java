package committee.nova.mods.avaritia.api.iface;

import committee.nova.mods.avaritia.init.registry.ModTooltips;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

/**
 * @Project: Avaritia
 * @Author: cnlimiter
 * @CreateTime: 2024/11/4 00:16
 * @Description: 切换状态
 */
public interface ISwitchable {
    default boolean isActive(ItemStack stack) {
        if (!stack.getOrCreateTag().contains("active")) return false;
        return stack.getOrCreateTag().getBoolean("active");
    }

    default void switchMode(@NotNull Level world, Player player, @NotNull InteractionHand hand, Component funcName) {
        ItemStack stack = player.getItemInHand(hand);
        CompoundTag tags = stack.getOrCreateTag();
        tags.putBoolean("active", !tags.getBoolean("active"));
        if (!world.isClientSide && player instanceof ServerPlayer serverPlayer)
            serverPlayer.sendSystemMessage(
                tags.getBoolean("active") ? ModTooltips.ACTIVE.args(funcName).build() : ModTooltips.INACTIVE.args(funcName).build()
                , true);
        player.swing(hand);
    }
}
