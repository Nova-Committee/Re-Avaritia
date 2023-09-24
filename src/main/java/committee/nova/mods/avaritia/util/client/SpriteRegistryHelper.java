package committee.nova.mods.avaritia.util.client;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import committee.nova.mods.avaritia.Static;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.MissingTextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraftforge.client.event.TextureStitchEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

import java.util.*;
import java.util.function.Consumer;

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

    private void onTextureStitchPost(TextureStitchEvent.Post event) {
        this.sprites = event.getAtlas();
    }
}
