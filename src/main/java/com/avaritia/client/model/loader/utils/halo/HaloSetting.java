package com.avaritia.client.model.loader.utils.halo;

import it.unimi.dsi.fastutil.ints.IntArrayList;

/**
 * halo 渲染参数。
 * <p>
 * {@code size} 控制圆形 halo 半径，{@code pulse} 控制是否追加基于原物品轮廓的脉冲层。
 *
 * @author cnlimiter
 */
public record HaloSetting(IntArrayList layerColors, String texture,
                          int color, int size, boolean pulse
) {
}
