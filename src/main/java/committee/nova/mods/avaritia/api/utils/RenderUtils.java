package committee.nova.mods.avaritia.api.utils;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderStateShard.TextureStateShard;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.world.inventory.InventoryMenu;
import org.lwjgl.opengl.GL13;

public class RenderUtils {

    public static final TextureStateShard COSMIC_TEXTURE_ISOLATED =
            new TextureStateShard(
                    InventoryMenu.BLOCK_ATLAS,
                    false,
                    false
            );
}
