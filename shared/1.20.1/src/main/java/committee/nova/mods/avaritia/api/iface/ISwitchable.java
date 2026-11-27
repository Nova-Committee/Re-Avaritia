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

import java.util.List;

/**
 * @Project: Avaritia
 * @author cnlimiter
 * @CreateTime: 2024/11/4 00:16
 * @Description: 切换状态接口 - 支持多模式切换
 */
public interface ISwitchable {

    /**
     * 检查指定功能是否处于激活状态
     *
     * @param stack    物品堆
     * @param funcName 功能名称
     * @return 是否激活
     */
    static boolean isMode(ItemStack stack, String funcName) {
        if (!stack.getOrCreateTagElement("mode").contains(funcName)) return false;
        return stack.getOrCreateTagElement("mode").getBoolean(funcName);
    }

    /**
     * 检查指定功能是否处于激活状态（实例方法）
     *
     * @param stack    物品堆
     * @param funcName 功能名称
     * @return 是否激活
     */
    default boolean isActive(ItemStack stack, String funcName) {
        return isMode(stack, funcName);
    }

    /**
     * 获取当前激活的模式
     *
     * @param stack    物品堆
     * @param modeList 模式列表
     * @return 当前模式索引，-1表示无激活模式
     */
    static int getCurrentMode(ItemStack stack, List<String> modeList) {
        CompoundTag modeTag = stack.getOrCreateTagElement("mode");
        for (int i = 0; i < modeList.size(); i++) {
            String mode = modeList.get(i);
            if (modeTag.contains(mode) && modeTag.getBoolean(mode)) {
                return i;
            }
        }
        // 如果没有找到激活的模式且模式列表不为空，默认激活第一个模式
        if (!modeList.isEmpty()) {
            modeTag.putBoolean(modeList.get(0), true);
            return 0;
        }
        return -1; // 无激活模式
    }

    /**
     * 获取当前激活的模式（实例方法）
     *
     * @param stack    物品堆
     * @param modeList 模式列表
     * @return 当前模式索引，-1表示无激活模式
     */
    default int getCurrentModeIndex(ItemStack stack, List<String> modeList) {
        return getCurrentMode(stack, modeList);
    }

    /**
     * 设置指定模式为激活状态
     *
     * @param stack     物品堆
     * @param modeList  模式列表
     * @param modeIndex 要激活的模式索引
     */
    default void setMode(ItemStack stack, List<String> modeList, int modeIndex) {
        CompoundTag modeTag = stack.getOrCreateTagElement("mode");

        // 先关闭所有模式
        for (String mode : modeList) {
            modeTag.putBoolean(mode, false);
        }

        // 激活指定模式
        if (modeIndex >= 0 && modeIndex < modeList.size()) {
            modeTag.putBoolean(modeList.get(modeIndex), true);
        }
    }

    /**
     * 循环切换模式
     *
     * @param world    世界
     * @param player   玩家
     * @param hand     手部
     * @param modeList 模式列表
     */
    default void cycleMode(@NotNull Level world, Player player, @NotNull InteractionHand hand, List<String> modeList) {
        if (modeList.isEmpty()) return;

        ItemStack stack = player.getItemInHand(hand);
        int currentIndex = getCurrentModeIndex(stack, modeList);
        int nextIndex = (currentIndex + 1) % modeList.size();

        setMode(stack, modeList, nextIndex);

        if (!world.isClientSide && player instanceof ServerPlayer serverPlayer) {
            String nextMode = modeList.get(nextIndex);
            Component modeTooltip = Component.translatable("tooltip.avaritia.tool." + nextMode);
            serverPlayer.sendSystemMessage(
                    ModTooltips.ACTIVE.args(modeTooltip).build(),
                    true
            );
        }
        player.swing(hand);
    }


    /**
     * 切换到指定功能模式
     *
     * @param world    世界
     * @param player   玩家
     * @param hand     手部
     * @param modeList 模式列表
     * @param modeName 要切换到的模式名称
     */
    default void switchToMode(@NotNull Level world, Player player, @NotNull InteractionHand hand, List<String> modeList, String modeName) {
        if (!modeList.contains(modeName)) return;

        ItemStack stack = player.getItemInHand(hand);
        CompoundTag modeTag = stack.getOrCreateTagElement("mode");

        // 关闭所有模式
        for (String mode : modeList) {
            modeTag.putBoolean(mode, false);
        }

        // 激活指定模式
        modeTag.putBoolean(modeName, true);

        if (!world.isClientSide && player instanceof ServerPlayer serverPlayer) {
            Component modeTooltip = Component.translatable("tooltip.avaritia.tool." + modeName);
            serverPlayer.sendSystemMessage(
                    ModTooltips.ACTIVE.args(modeTooltip).build(),
                    true
            );
        }
        player.swing(hand);
    }


    /**
     * 保持原有的单功能切换方法（向后兼容）
     *
     * @param world    世界
     * @param player   玩家
     * @param hand     手部
     * @param funcName 功能名称
     */
    default void switchMode(@NotNull Level world, Player player, @NotNull InteractionHand hand, String funcName) {
        ItemStack stack = player.getItemInHand(hand);
        CompoundTag tags = stack.getOrCreateTagElement("mode");
        Component funcTooltip = Component.translatable("tooltip.avaritia.tool." + funcName);
        tags.putBoolean(funcName, !tags.getBoolean(funcName));
        if (!world.isClientSide && player instanceof ServerPlayer serverPlayer)
            serverPlayer.sendSystemMessage(
                    tags.getBoolean(funcName) ? ModTooltips.ACTIVE.args(funcTooltip).build() : ModTooltips.INACTIVE.args(funcTooltip).build()
                    , true);
        player.swing(hand);
    }
}
