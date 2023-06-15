package committee.nova.mods.avaritia.init.handler;

import committee.nova.mods.avaritia.Static;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.TextureStitchEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * Description:
 * Author: cnlimiter
 * Date: 2022/6/5 12:41
 * Version: 1.0
 */
@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class TexturesHandler {
    private static final String SHADER_ = "avaritia:shaders/";
    public static TextureAtlasSprite[] COSMIC;
    public static TextureAtlasSprite COSMIC_0;
    public static TextureAtlasSprite COSMIC_1;
    public static TextureAtlasSprite COSMIC_2;
    public static TextureAtlasSprite COSMIC_3;
    public static TextureAtlasSprite COSMIC_4;
    public static TextureAtlasSprite COSMIC_5;
    public static TextureAtlasSprite COSMIC_6;
    public static TextureAtlasSprite COSMIC_7;
    public static TextureAtlasSprite COSMIC_8;
    public static TextureAtlasSprite COSMIC_9;
    private static TextureAtlas map;

    private static TextureAtlasSprite register(String sprite) {

        return map.getSprite(new ResourceLocation(sprite));
    }

    @SubscribeEvent
    public static void textureLoad(TextureStitchEvent event) {
        stitch(event);
        map = event.getAtlas();

        COSMIC_0 = register(SHADER_ + "cosmic_0");
        COSMIC_1 = register(SHADER_ + "cosmic_1");
        COSMIC_2 = register(SHADER_ + "cosmic_2");
        COSMIC_3 = register(SHADER_ + "cosmic_3");
        COSMIC_4 = register(SHADER_ + "cosmic_4");
        COSMIC_5 = register(SHADER_ + "cosmic_5");
        COSMIC_6 = register(SHADER_ + "cosmic_6");
        COSMIC_7 = register(SHADER_ + "cosmic_7");
        COSMIC_8 = register(SHADER_ + "cosmic_8");
        COSMIC_9 = register(SHADER_ + "cosmic_9");

        COSMIC = new TextureAtlasSprite[]{
                COSMIC_0,
                COSMIC_1,
                COSMIC_2,
                COSMIC_3,
                COSMIC_4,
                COSMIC_5,
                COSMIC_6,
                COSMIC_7,
                COSMIC_8,
                COSMIC_9
        };
    }

    private static void stitch(final TextureStitchEvent evt) {

        if (evt.getAtlas().location() == InventoryMenu.BLOCK_ATLAS) {
            for (int i = 0; i < 10; i++) {
                evt.getAtlas().getSprite(Static.rl("shaders/cosmic_" + i));
            }

        }
    }

}
