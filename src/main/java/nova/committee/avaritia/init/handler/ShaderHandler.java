package nova.committee.avaritia.init.handler;

import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.server.packs.resources.ResourceProvider;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterShadersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.io.IOException;

/**
 * Description:
 * Author: cnlimiter
 * Date: 2022/6/5 11:24
 * Version: 1.0
 */
@Mod.EventBusSubscriber(value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
public class ShaderHandler {

    public static ShaderInstance cosmicShader;

    @SubscribeEvent
    public static void onRegisterShaders(RegisterShadersEvent event) throws IOException {
        ResourceProvider resourceManager = event.getResourceProvider();
        //event.registerShader(new ShaderInstance(resourceManager, Static.rl("cosmic_shader"), DefaultVertexFormat.POSITION_TEX), shader -> cosmicShader = shader);
    }


}
