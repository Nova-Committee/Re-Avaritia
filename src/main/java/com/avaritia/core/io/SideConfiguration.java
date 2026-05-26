package com.avaritia.core.io;

import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.StreamCodec;
import org.jetbrains.annotations.NotNull;

/**
 * 方块配置数据类
 * Description: 管理NeutronCompressor各个面的输入输出配置
 * @author cnlimiter
 * Date: 2025/11/01
 * Version: 1.0
 */
public class  SideConfiguration {
    public static final StreamCodec<FriendlyByteBuf, SideConfiguration> STREAM_CODEC = new StreamCodec<>() {
        @Override
        public @NotNull SideConfiguration decode(@NotNull FriendlyByteBuf buffer) {
            return new SideConfiguration(
                    buffer.readEnum(SideMode.class),
                    buffer.readEnum(SideMode.class),
                    buffer.readEnum(SideMode.class),
                    buffer.readEnum(SideMode.class),
                    buffer.readEnum(SideMode.class),
                    buffer.readEnum(SideMode.class)
            );
        }

        @Override
        public void encode(@NotNull FriendlyByteBuf buffer, @NotNull SideConfiguration sideConfiguration) {
            buffer.writeEnum(sideConfiguration.north);
            buffer.writeEnum(sideConfiguration.south);
            buffer.writeEnum(sideConfiguration.east);
            buffer.writeEnum(sideConfiguration.west);
            buffer.writeEnum(sideConfiguration.up);
            buffer.writeEnum(sideConfiguration.down);
        }
    };

    /**
     * 面配置模式枚举
     */
    public enum SideMode {
        OFF(Component.translatable("tooltip.avaritia.side.mode.off"), "off"),           // 关闭
        PASSIVE_INPUT(Component.translatable("tooltip.avaritia.side.mode.passive_input"), "passive_input"),  // 被动输入
        PASSIVE_OUTPUT(Component.translatable("tooltip.avaritia.side.mode.passive_output"), "passive_output"), // 被动输出
        PASSIVE_MIXIN(Component.translatable("tooltip.avaritia.side.mode.passive_mixin"), "passive_mixin"), // 被动混合
        ACTIVE_INPUT(Component.translatable("tooltip.avaritia.side.mode.active_input"), "active_input"),    // 主动输入
        ACTIVE_OUTPUT(Component.translatable("tooltip.avaritia.side.mode.active_output"), "active_output"),  // 主动输出
        ACTIVE_MIXIN(Component.translatable("tooltip.avaritia.side.mode.active_mixin"), "active_mixin"); // 主动混合

        private final Component displayName;
        private final String name;

        SideMode(Component displayName, String name) {
            this.displayName = displayName;
            this.name = name;
        }

        public Component getDisplayName() {
            return displayName;
        }

        public String getName() {
            return name;
        }

        public boolean canInput() {
            return this == PASSIVE_INPUT || this == ACTIVE_INPUT || this == PASSIVE_MIXIN || this == ACTIVE_MIXIN;
        }

        public boolean canOutput() {
            return this == PASSIVE_OUTPUT || this == ACTIVE_OUTPUT || this == PASSIVE_MIXIN || this == ACTIVE_MIXIN;
        }

        public boolean isActive() {
            return this == ACTIVE_INPUT || this == ACTIVE_OUTPUT || this == ACTIVE_MIXIN;
        }

        public boolean isPassive() {
            return this == PASSIVE_INPUT || this == PASSIVE_OUTPUT || this == PASSIVE_MIXIN;
        }

        /**
         * 检查是否为混合模式（支持输入和输出）
         */
        public boolean isMixed() {
            return this == PASSIVE_MIXIN || this == ACTIVE_MIXIN;
        }

        public static SideMode fromName(String name) {
            for (SideMode mode : values()) {
                if (mode.name.equals(name)) {
                    return mode;
                }
            }
            return OFF;
        }
    }

    // 六个面的配置
    private SideMode north = SideMode.OFF;
    private SideMode south = SideMode.OFF;
    private SideMode east = SideMode.OFF;
    private SideMode west = SideMode.OFF;
    private SideMode up = SideMode.OFF;
    private SideMode down = SideMode.OFF;

    public SideConfiguration() {
    }

    public SideConfiguration(SideMode north,
                             SideMode south,
                             SideMode east,
                             SideMode west,
                             SideMode up,
                             SideMode down
                             ) {
        this.north = north;
        this.south = south;
        this.east = east;
        this.west = west;
        this.up = up;
        this.down = down;
    }

    public SideConfiguration(SideConfiguration other) {
        this.north = other.north;
        this.south = other.south;
        this.east = other.east;
        this.west = other.west;
        this.up = other.up;
        this.down = other.down;
    }

    /**
     * 获取指定面的配置
     */
    public SideMode getSideMode(Direction side) {
        return switch (side) {
            case NORTH -> north;
            case SOUTH -> south;
            case EAST -> east;
            case WEST -> west;
            case UP -> up;
            case DOWN -> down;
        };
    }

    /**
     * 设置指定面的配置
     */
    public void setSideMode(Direction side, SideMode mode) {
        switch (side) {
            case NORTH -> north = mode;
            case SOUTH -> south = mode;
            case EAST -> east = mode;
            case WEST -> west = mode;
            case UP -> up = mode;
            case DOWN -> down = mode;
        }
    }

    /**
     * 切换指定面到下一个模式
     */
    public void cycleSideMode(Direction side) {
        SideMode current = getSideMode(side);
        SideMode[] modes = SideMode.values();
        int nextIndex = (current.ordinal() + 1) % modes.length;
        setSideMode(side, modes[nextIndex]);
    }

    /**
     * 检查指定面是否可以输入
     */
    public boolean canInput(Direction side) {
        return getSideMode(side).canInput();
    }

    /**
     * 检查指定面是否可以输出
     */
    public boolean canOutput(Direction side) {
        return getSideMode(side).canOutput();
    }

    /**
     * 检查指定面是否为主动模式
     */
    public boolean isActive(Direction side) {
        return getSideMode(side).isActive();
    }

    /**
     * 检查指定面是否为被动模式
     */
    public boolean isPassive(Direction side) {
        return getSideMode(side).isPassive();
    }

    /**
     * 获取所有主动输入面
     */
    public Direction[] getActiveInputSides() {
        return Direction.stream()
                .filter(this::isActive)
                .filter(this::canInput)
                .toArray(Direction[]::new);
    }

    /**
     * 获取所有主动输出面
     */
    public Direction[] getActiveOutputSides() {
        return Direction.stream()
                .filter(this::isActive)
                .filter(this::canOutput)
                .toArray(Direction[]::new);
    }

    /**
     * 获取所有被动输入面
     */
    public Direction[] getPassiveInputSides() {
        return Direction.stream()
                .filter(this::isPassive)
                .filter(this::canInput)
                .toArray(Direction[]::new);
    }

    /**
     * 获取所有被动输出面
     */
    public Direction[] getPassiveOutputSides() {
        return Direction.stream()
                .filter(this::isPassive)
                .filter(this::canOutput)
                .toArray(Direction[]::new);
    }

    /**
     * 序列化到NBT
     */
    public CompoundTag toNBT() {
        CompoundTag tag = new CompoundTag();
        tag.putString("north", north.getName());
        tag.putString("south", south.getName());
        tag.putString("east", east.getName());
        tag.putString("west", west.getName());
        tag.putString("up", up.getName());
        tag.putString("down", down.getName());
        return tag;
    }

    /**
     * 从NBT反序列化
     */
    public static SideConfiguration fromNBT(CompoundTag tag) {
        SideConfiguration config = new SideConfiguration();
        config.north = SideMode.fromName(tag.getString("north"));
        config.south = SideMode.fromName(tag.getString("south"));
        config.east = SideMode.fromName(tag.getString("east"));
        config.west = SideMode.fromName(tag.getString("west"));
        config.up = SideMode.fromName(tag.getString("up"));
        config.down = SideMode.fromName(tag.getString("down"));
        return config;
    }

    @Override
    public String toString() {
        return String.format("SideConfiguration{北=%s, 南=%s, 东=%s, 西=%s, 上=%s, 下=%s}",
                north, south, east, west, up, down);
    }
}
