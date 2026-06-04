package com.avaritia.api.utils;

import net.minecraft.client.renderer.rendertype.LayeringTransform;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.resources.Identifier;

public class RenderUtils {

    public static final Identifier COSMIC_TEXTURE_ISOLATED = TextureAtlas.LOCATION_BLOCKS;

    public static final LayeringTransform POLYGON_OFFSET_LAYERING =
            new LayeringTransform(
                    "polygon_offset_layering",
                    matrix -> matrix.translate(0.0F, 0.0F, -0.001F)
            );
}
