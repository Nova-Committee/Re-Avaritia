package com.avaritia.client.model.loader.base;

import net.minecraft.client.color.item.ItemTintSource;
import net.minecraft.resources.Identifier;

import java.util.List;

/**
 * codec 反序列化后创建具体 halo loader record 的函数接口。
 *
 * @author cnlimiter
 */
public interface HaloFactory<T> {
    T create(Identifier model, Identifier texture, int color, int size, boolean pulse, List<ItemTintSource> tints);
}
