package committee.nova.mods.avaritia.util.client;

import net.minecraft.client.renderer.texture.TextureAtlas;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.client.event.TextureAtlasStitchedEvent;

/**
 * Name: Avaritia-forge / SpriteRegistryHelper
 * Author: cnlimiter
 * CreateTime: 2023/9/18 1:40
 * Description:
 */

public class SpriteRegistryHelper {
    public TextureAtlas sprites;

    public SpriteRegistryHelper(IEventBus eventBus) {
        eventBus.addListener(this::onTextureStitchPost);
    }

    private void onTextureStitchPost(TextureAtlasStitchedEvent event) {
        this.sprites = event.getAtlas();
    }
}
