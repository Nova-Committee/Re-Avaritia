package committee.nova.mods.avaritia.api.iface;

import committee.nova.mods.avaritia.init.registry.ModTooltips;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.component.SwingAnimation;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.function.Consumer;

/**
 * @Project: Avaritia
 * @author cnlimiter
 * @CreateTime: 2024/11/4 00:16
 * @Description: 切换状态接口 - 支持多模式切换
 */
public interface ISwitchable {

    static CompoundTag getModeTag(ItemStack stack) {
        return stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag().getCompoundOrEmpty("mode");
    }

    static void updateModeTag(ItemStack stack, Consumer<CompoundTag> consumer) {
        stack.update(DataComponents.CUSTOM_DATA, CustomData.EMPTY,
                data -> data.update(tag -> {
                    CompoundTag modeTag = tag.getCompoundOrEmpty("mode");
                    consumer.accept(modeTag);
                    tag.put("mode", modeTag);
                }));
    }

    /**
     * 检查指定功能是否处于激活状态
     */
    static boolean isMode(ItemStack stack, String funcName) {
        CompoundTag modeTag = getModeTag(stack);
        return modeTag.contains(funcName) && modeTag.getBooleanOr(funcName, false);
    }

    /**
     * 检查指定功能是否处于激活状态（实例方法）
     */
    default boolean isActive(ItemStack stack, String funcName) {
        return isMode(stack, funcName);
    }

    /**
     * 获取当前激活的模式
     */
    static int getCurrentMode(ItemStack stack, List<String> modeList) {
        CompoundTag modeTag = getModeTag(stack);
        for (int i = 0; i < modeList.size(); i++) {
            String mode = modeList.get(i);
            if (modeTag.contains(mode) && modeTag.getBooleanOr(mode, false)) {
                return i;
            }
        }
        // 如果没有找到激活的模式且模式列表不为空，默认激活第一个模式
        if (!modeList.isEmpty()) {
            updateModeTag(stack, tag -> tag.putBoolean(modeList.get(0), true));
            return 0;
        }
        return -1; // 无激活模式
    }

    /**
     * 获取当前激活的模式（实例方法）
     */
    default int getCurrentModeIndex(ItemStack stack, List<String> modeList) {
        return getCurrentMode(stack, modeList);
    }

    /**
     * 设置指定模式为激活状态
     */
    default void setMode(ItemStack stack, List<String> modeList, int modeIndex) {
        updateModeTag(stack, modeTag -> {
            // 先关闭所有模式
            for (String mode : modeList) {
                modeTag.putBoolean(mode, false);
            }

            // 激活指定模式
            if (modeIndex >= 0 && modeIndex < modeList.size()) {
                modeTag.putBoolean(modeList.get(modeIndex), true);
            }
        });
    }

    /**
     * 循环切换模式
     */
    default void cycleMode(@NotNull Level world, Player player, @NotNull InteractionHand hand, List<String> modeList) {
        if (modeList.isEmpty()) return;

        ItemStack stack = player.getItemInHand(hand);
        int currentIndex = getCurrentModeIndex(stack, modeList);
        int nextIndex = (currentIndex + 1) % modeList.size();

        setMode(stack, modeList, nextIndex);

        if (!world.isClientSide() && player instanceof ServerPlayer serverPlayer) {
            String nextMode = modeList.get(nextIndex);
            Component modeTooltip = Component.translatable("tooltip.avaritia.tool." + nextMode);
            serverPlayer.sendSystemMessage(ModTooltips.ACTIVE.args(modeTooltip).build());
        }
        player.swing(hand, SwingAnimation.DEFAULT, false);
    }

    /**
     * 切换到指定功能模式
     */
    default void switchToMode(@NotNull Level world, Player player, @NotNull InteractionHand hand, List<String> modeList, String modeName) {
        if (!modeList.contains(modeName)) return;

        ItemStack stack = player.getItemInHand(hand);
        updateModeTag(stack, modeTag -> {
            // 关闭所有模式
            for (String mode : modeList) {
                modeTag.putBoolean(mode, false);
            }

            // 激活指定模式
            modeTag.putBoolean(modeName, true);
        });

        if (!world.isClientSide() && player instanceof ServerPlayer serverPlayer) {
            Component modeTooltip = Component.translatable("tooltip.avaritia.tool." + modeName);
            serverPlayer.sendSystemMessage(ModTooltips.ACTIVE.args(modeTooltip).build());
        }
        player.swing(hand, SwingAnimation.DEFAULT, false);
    }

    /**
     * 保持原有的单功能切换方法（向后兼容）
     */
    default void switchMode(@NotNull Level world, Player player, @NotNull InteractionHand hand, String funcName) {
        ItemStack stack = player.getItemInHand(hand);
        Component funcTooltip = Component.translatable("tooltip.avaritia.tool." + funcName);
        updateModeTag(stack, modeTag -> modeTag.putBoolean(funcName, !modeTag.getBooleanOr(funcName, false)));
        if (!world.isClientSide() && player instanceof ServerPlayer serverPlayer) {
            CompoundTag modeTag = getModeTag(stack);
            serverPlayer.sendSystemMessage(
                    modeTag.getBooleanOr(funcName, false) ? ModTooltips.ACTIVE.args(funcTooltip).build() : ModTooltips.INACTIVE.args(funcTooltip).build());
        }
        player.swing(hand, SwingAnimation.DEFAULT, false);
    }
}