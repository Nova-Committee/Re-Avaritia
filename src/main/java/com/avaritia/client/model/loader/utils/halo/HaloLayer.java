package com.avaritia.client.model.loader.utils.halo;

import net.minecraft.client.renderer.item.CuboidItemModelWrapper;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import org.joml.Vector3fc;

/**
 * halo 图层的烘焙结果：贴图、参数，以及 GUI 裁剪/布局需要的边界。
 */
public record HaloLayer(TextureAtlasSprite sprite, HaloSetting setting, Vector3fc[] extents) {
    public HaloLayer(TextureAtlasSprite sprite, HaloSetting setting) {
        this(sprite, setting, CuboidItemModelWrapper.computeExtents(
                HaloUtils.generateHaloQuads(sprite, setting.size(), setting.color())));
    }
}
