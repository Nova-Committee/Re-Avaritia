//package committee.nova.mods.avaritia.init.handler;
//
//import committee.nova.mods.avaritia.Static;
//import net.minecraft.client.renderer.texture.TextureAtlas;
//import net.minecraftforge.client.event.TextureStitchEvent;
//import net.minecraftforge.eventbus.api.SubscribeEvent;
//import net.minecraftforge.fml.common.Mod;
//
///**
// * Name: Avaritia-forge / SpriteRegistryHelper
// * Author: cnlimiter
// * CreateTime: 2023/9/18 1:40
// * Description:
// */
//
//@Mod.EventBusSubscriber(modid = Static.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
//public class SpriteRegistryHandler {
//    public static TextureAtlas sprites;
//
//    @SubscribeEvent
//    public static void onTextureStitchPost(TextureStitchEvent.Post event) {
//        sprites = event.getAtlas();
//    }
//}
