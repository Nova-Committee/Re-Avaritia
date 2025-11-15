package committee.nova.mods.avaritia.api.iface.item;

import committee.nova.mods.avaritia.init.registry.ModTooltips;
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

import java.util.List;

/**
 * @Project: Avaritia
 * @Author: cnlimiter
 * @CreateTime: 2024/11/4 00:16
 * @Description: 切换状态接口 - 支持多模式切换
 */
public interface ISwitchable {

    /**
     * 获取或创建 "mode" 子标签
     */
    static CompoundTag getOrCreateModeTag(ItemStack stack) {
        CustomData data = stack.get(DataComponents.CUSTOM_DATA);
        CompoundTag root;

        if (data == null) {
            root = new CompoundTag();
        } else {
            root = data.copyTag();
        }

        CompoundTag modeTag;
        if (!root.contains("mode", CompoundTag.TAG_COMPOUND)) {
            modeTag = new CompoundTag();
            root.put("mode", modeTag);
        } else {
            modeTag = root.getCompound("mode");
        }

        // 重要：将修改后的数据保存回 ItemStack
        stack.set(DataComponents.CUSTOM_DATA, CustomData.of(root));

        return modeTag;
    }

    /**
     * 更新模式标签并确保数据保存
     */
    static void updateModeTag(ItemStack stack, CompoundTag modeTag) {
        CustomData data = stack.get(DataComponents.CUSTOM_DATA);
        CompoundTag root;

        if (data == null) {
            root = new CompoundTag();
        } else {
            root = data.copyTag();
        }

        root.put("mode", modeTag);
        stack.set(DataComponents.CUSTOM_DATA, CustomData.of(root));
    }

    /**
     * 检查指定功能是否处于激活状态
     */
    static boolean isMode(ItemStack stack, String funcName) {
        CompoundTag modeTag = getOrCreateModeTag(stack);
        return modeTag.contains(funcName) && modeTag.getBoolean(funcName);
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
        CompoundTag modeTag = getOrCreateModeTag(stack);
        for (int i = 0; i < modeList.size(); i++) {
            String mode = modeList.get(i);
            if (modeTag.contains(mode) && modeTag.getBoolean(mode)) {
                return i;
            }
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
        CustomData data = stack.get(DataComponents.CUSTOM_DATA);
        CompoundTag root = data != null ? data.copyTag() : new CompoundTag();

        if (!root.contains("mode", CompoundTag.TAG_COMPOUND)) {
            root.put("mode", new CompoundTag());
        }

        CompoundTag modeTag = root.getCompound("mode");

        // 先关闭所有模式
        for (String mode : modeList) {
            modeTag.putBoolean(mode, false);
        }

        // 激活指定模式
        if (modeIndex >= 0 && modeIndex < modeList.size()) {
            modeTag.putBoolean(modeList.get(modeIndex), true);
        }

        // 保存更新后的数据
        root.put("mode", modeTag);
        stack.set(DataComponents.CUSTOM_DATA, CustomData.of(root));
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
     */
    default void switchToMode(@NotNull Level world, Player player, @NotNull InteractionHand hand, List<String> modeList, String modeName) {
        if (!modeList.contains(modeName)) return;

        ItemStack stack = player.getItemInHand(hand);
        CompoundTag modeTag = getOrCreateModeTag(stack);

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
     */
    default void switchMode(@NotNull Level world, Player player, @NotNull InteractionHand hand, String funcName) {
        ItemStack stack = player.getItemInHand(hand);

        CustomData data = stack.get(DataComponents.CUSTOM_DATA);
        CompoundTag root = data != null ? data.copyTag() : new CompoundTag();

        if (!root.contains("mode", CompoundTag.TAG_COMPOUND)) {
            root.put("mode", new CompoundTag());
        }

        CompoundTag modeTag = root.getCompound("mode");
        Component funcTooltip = Component.translatable("tooltip.avaritia.tool." + funcName);

        modeTag.putBoolean(funcName, !modeTag.getBoolean(funcName));

        // 保存更新后的数据
        root.put("mode", modeTag);
        stack.set(DataComponents.CUSTOM_DATA, CustomData.of(root));

        if (!world.isClientSide && player instanceof ServerPlayer serverPlayer) {
            serverPlayer.sendSystemMessage(
                    modeTag.getBoolean(funcName)
                            ? ModTooltips.ACTIVE.args(funcTooltip).build()
                            : ModTooltips.INACTIVE.args(funcTooltip).build(),
                    true
            );
        }
        player.swing(hand);
    }
}
