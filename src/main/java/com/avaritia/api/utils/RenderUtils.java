package com.avaritia.api.utils;

import net.minecraft.client.renderer.rendertype.LayeringTransform;
import net.minecraft.resources.Identifier;
import net.minecraft.world.inventory.InventoryMenu;

public class RenderUtils {

    public static final Identifier COSMIC_TEXTURE_ISOLATED = InventoryMenu.BLOCK_ATLAS;

    public static final LayeringTransform POLYGON_OFFSET_LAYERING =
            new LayeringTransform(
                    "polygon_offset_layering",
                    matrix -> matrix.translate(0.0F, 0.0F, -0.001F)
            );
}
