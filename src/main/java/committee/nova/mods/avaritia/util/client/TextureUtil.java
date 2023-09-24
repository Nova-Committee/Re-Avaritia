package committee.nova.mods.avaritia.util.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.MissingTextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.world.inventory.InventoryMenu;

/**
 * Name: Avaritia-forge / TextureUtil
 * Author: cnlimiter
 * CreateTime: 2023/9/24 5:17
 * Description:
 */

public class TextureUtil {
    public static TextureAtlas getTextureMap() {
        return Minecraft.getInstance().getModelManager().getAtlas(InventoryMenu.BLOCK_ATLAS);
    }

    public static TextureAtlasSprite getMissingSprite() {
        return getTextureMap().getSprite(MissingTextureAtlasSprite.getLocation());
    }
}
