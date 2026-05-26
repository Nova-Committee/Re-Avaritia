package com.avaritia.init.registry;

import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ToolMaterial;
import net.minecraft.world.level.block.Block;

/**
 * 模组工具材质定义。
 *
 * <p>MC 26.1.2 移除了旧版 {@code Tier}/{@code SimpleTier}，工具材质改为
 * {@link ToolMaterial} 记录。构造参数顺序为：错误挖掘方块标签、耐久、挖掘速度、攻击伤害加成、附魔值、修复物品标签。</p>
 */
public class ModToolTiers {
    private static final TagKey<Block> NO_INCORRECT_BLOCKS = null;
    private static final TagKey<Item> NO_REPAIR_ITEMS = null;

    public static final ToolMaterial BLAZE = new ToolMaterial(NO_INCORRECT_BLOCKS, 7777, 25f, 25f, 77, NO_REPAIR_ITEMS);
    public static final ToolMaterial CRYSTAL = new ToolMaterial(NO_INCORRECT_BLOCKS, 8888, 50f, 50f, 888, NO_REPAIR_ITEMS);
    public static final ToolMaterial INFINITY = new ToolMaterial(NO_INCORRECT_BLOCKS, 9999, 100f, 100f, 9999, NO_REPAIR_ITEMS);
}
