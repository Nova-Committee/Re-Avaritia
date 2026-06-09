package com.avaritia.client.model.loader.base;

import net.minecraft.client.color.item.ItemTintSource;
import net.minecraft.resources.Identifier;

import java.util.List;

/**
 * effect loader record 需要暴露的公共字段，供通用 codec 读取。
 *
 * @author cnlimiter
 */
public interface EffectFields {
    Identifier model();

    List<Identifier> mask();

    List<ItemTintSource> tints();
}
