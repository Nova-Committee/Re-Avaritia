package committee.nova.mods.avaritia.init.handler;

import committee.nova.mods.avaritia.client.render.shader.CosmicShaderHelper;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ScreenEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.lwjgl.BufferUtils;

import java.nio.FloatBuffer;

/**
 * Description:
 * Author: cnlimiter
 * Date: 2022/6/5 13:44
 * Version: 1.0
 */
@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public class RenderHandler {
    public static FloatBuffer cosmicUVs = BufferUtils.createFloatBuffer(4 * 10);


    @SubscribeEvent
    public static void onDrawScreenPre(ScreenEvent.Render.Pre event) {
        CosmicShaderHelper.inventoryRender = true;
    }

    @SubscribeEvent
    public static void drawScreenPost(ScreenEvent.Render.Post event) {
        CosmicShaderHelper.inventoryRender = false;
    }

    @SubscribeEvent
    public static void onRenderTick(TickEvent.RenderTickEvent event) {
        if (event.phase == TickEvent.Phase.START) {
            cosmicUVs = BufferUtils.createFloatBuffer(4 * TexturesHandler.COSMIC.length);
            TextureAtlasSprite icon;
            for (TextureAtlasSprite cosmicIcon : TexturesHandler.COSMIC) {
                icon = cosmicIcon;


                if (icon != null) {
                    cosmicUVs.put(icon.getU0());
                    cosmicUVs.put(icon.getV0());
                    cosmicUVs.put(icon.getU1());
                    cosmicUVs.put(icon.getV1());
                }

            }
            cosmicUVs.flip();
        }
    }
}
