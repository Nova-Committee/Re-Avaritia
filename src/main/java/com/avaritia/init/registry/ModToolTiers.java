package com.avaritia.init.registry;

import net.minecraft.world.item.ToolMaterial;

/**
 * 模组工具材质定义。
 *
 * <p>MC 26.1.2 移除了旧版 {@code Tier}/{@code SimpleTier}，工具材质改为
 * {@link ToolMaterial} 记录。构造参数顺序为：错误挖掘方块标签、耐久、挖掘速度、攻击伤害加成、附魔值、修复物品标签。</p>
 */
public class ModToolTiers {
    public static final ToolMaterial BLAZE = new ToolMaterial(ModTags.INCORRECT_FOR_BLAZE_TOOL, 7777, 25f, 25f, 77, ModTags.REPAIRS_BLAZE_TOOLS);
    public static final ToolMaterial CRYSTAL = new ToolMaterial(ModTags.INCORRECT_FOR_CRYSTAL_TOOL, 8888, 50f, 50f, 888, ModTags.REPAIRS_CRYSTAL_TOOLS);
    public static final ToolMaterial INFINITY = new ToolMaterial(ModTags.INCORRECT_FOR_INFINITY_TOOL, 9999, 100f, 100f, 9999, ModTags.REPAIRS_INFINITY_TOOLS);
}
