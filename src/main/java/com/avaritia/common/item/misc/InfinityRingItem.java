package com.avaritia.common.item.misc;

import com.avaritia.init.registry.ModItems;

import com.avaritia.common.item.resources.ResourceItem;
import com.avaritia.init.registry.ModRarities;

/**
 * @Project: Avaritia
 * @Author: cnlimiter
 * @CreateTime: 2024/8/2 上午12:32
 * @Description:
 */
public class InfinityRingItem extends ResourceItem {
    public InfinityRingItem() {
        super(ModRarities.LEGEND.getValue(), true, ModItems.properties().stacksTo(1));
    }
}
