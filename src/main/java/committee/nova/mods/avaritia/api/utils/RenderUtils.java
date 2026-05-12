package committee.nova.mods.avaritia.api.utils;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.renderer.RenderStateShard.*;
import net.minecraft.client.renderer.RenderStateShard.TextureStateShard;
import net.minecraft.world.inventory.InventoryMenu;

public class RenderUtils {

    public static final TextureStateShard COSMIC_TEXTURE_ISOLATED =
            new TextureStateShard(
                    InventoryMenu.BLOCK_ATLAS,
                    false,
                    false
            );

    public static final LayeringStateShard POLYGON_OFFSET_LAYERING =
            new LayeringStateShard(
                    "polygon_offset_layering",
                    () -> {
                        RenderSystem.polygonOffset(-1.0F, -10.0F);
                        RenderSystem.enablePolygonOffset();
                    },
                    () -> {
                        RenderSystem.polygonOffset(0.0F, 0.0F);
                        RenderSystem.disablePolygonOffset();
                    }
            );
}
