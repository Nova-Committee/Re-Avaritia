package committee.nova.mods.avaritia.init.registry;

import com.mojang.serialization.Codec;
import committee.nova.mods.avaritia.Const;
import committee.nova.mods.avaritia.common.component.CrystalSpearCooldown;
import committee.nova.mods.avaritia.common.component.CrystalSpearTarget;
import committee.nova.mods.avaritia.common.component.SpearTargetReference;
import net.minecraft.core.Registry;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.resources.Identifier;

/**
 * 模组自定义数据组件（长矛目标/冷却等），对应 Neo 侧 ModDataComponents。
 */
public final class ModDataComponents {
    public static final DataComponentType<SpearTargetReference> INFINITY_SPEAR_TARGET = register("infinity_spear_target",
            DataComponentType.<SpearTargetReference>builder()
                    .persistent(SpearTargetReference.CODEC)
                    .networkSynchronized(SpearTargetReference.STREAM_CODEC)
                    .build());
    public static final DataComponentType<CrystalSpearTarget> CRYSTAL_SPEAR_TARGET = register("crystal_spear_target",
            DataComponentType.<CrystalSpearTarget>builder()
                    .persistent(CrystalSpearTarget.CODEC)
                    .networkSynchronized(CrystalSpearTarget.STREAM_CODEC)
                    .build());
    public static final DataComponentType<Integer> CRYSTAL_SPEAR_REMAINING_THRUSTS = register("crystal_spear_remaining_thrusts",
            DataComponentType.<Integer>builder()
                    .persistent(Codec.INT)
                    .networkSynchronized(ByteBufCodecs.VAR_INT)
                    .build());
    public static final DataComponentType<CrystalSpearCooldown> CRYSTAL_SPEAR_COOLDOWN = register("crystal_spear_cooldown",
            DataComponentType.<CrystalSpearCooldown>builder()
                    .persistent(CrystalSpearCooldown.CODEC)
                    .networkSynchronized(CrystalSpearCooldown.STREAM_CODEC)
                    .build());

    private static <T> DataComponentType<T> register(String name, DataComponentType<T> type) {
        return Registry.register(BuiltInRegistries.DATA_COMPONENT_TYPE,
                Identifier.fromNamespaceAndPath(Const.MOD_ID, name), type);
    }

    /** 触发静态注册；须在物品注册之前调用。 */
    public static void initialize() {
        // 静态字段加载即完成注册
    }

    private ModDataComponents() {
    }
}
