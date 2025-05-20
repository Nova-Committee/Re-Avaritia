package committee.nova.mods.avaritia.client.shader;

import com.mojang.blaze3d.shaders.Uniform;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import committee.nova.mods.avaritia.Const;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ShaderInstance;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.*;

import java.io.IOException;
import java.util.Objects;

/**
 * Name: Avaritia-forge / AvaritiaShaders
 * Author: cnlimiter
 * CreateTime: 2023/9/18 1:37
 * Description:
 */

@EventBusSubscriber(modid = Const.MOD_ID, value = Dist.CLIENT, bus = EventBusSubscriber.Bus.GAME)
public class AvaritiaShaders {
    public static boolean inventoryRender = false;
    public static int renderTime;
    public static float renderFrame;


    public static ShaderInstance COSMIC_SHADER;
    public static ShaderInstance COSMIC_ARMOR_SHADER;

    public static Uniform cosmicTime;
    public static Uniform cosmicYaw;
    public static Uniform cosmicPitch;
    public static Uniform cosmicExternalScale;
    public static Uniform cosmicOpacity;
    public static Uniform cosmicUVs;


    public static void onRegisterShaders(RegisterShadersEvent event) {
        try {
            COSMIC_SHADER = new ShaderInstance(event.getResourceProvider(), Const.rl("cosmic"), DefaultVertexFormat.BLOCK);
            COSMIC_ARMOR_SHADER = new ShaderInstance(event.getResourceProvider(), Const.rl("cosmic"), DefaultVertexFormat.NEW_ENTITY);
            event.registerShader(COSMIC_SHADER, shaderInstance -> {
                cosmicTime = Objects.requireNonNull(shaderInstance.getUniform("time"));
                cosmicYaw = Objects.requireNonNull(shaderInstance.getUniform("yaw"));
                cosmicPitch = Objects.requireNonNull(shaderInstance.getUniform("pitch"));
                cosmicExternalScale = Objects.requireNonNull(shaderInstance.getUniform("externalScale"));
                cosmicOpacity = Objects.requireNonNull(COSMIC_SHADER.getUniform("opacity"));
                cosmicUVs = Objects.requireNonNull(COSMIC_SHADER.getUniform("cosmicuvs"));
                cosmicTime.set((float) renderTime + renderFrame);
                COSMIC_SHADER.apply();
            });
            event.registerShader(COSMIC_ARMOR_SHADER, shaderInstance -> {
                cosmicTime = Objects.requireNonNull(shaderInstance.getUniform("time"));
                cosmicYaw = Objects.requireNonNull(shaderInstance.getUniform("yaw"));
                cosmicPitch = Objects.requireNonNull(shaderInstance.getUniform("pitch"));
                cosmicExternalScale = Objects.requireNonNull(shaderInstance.getUniform("externalScale"));
                cosmicOpacity = Objects.requireNonNull(COSMIC_SHADER.getUniform("opacity"));
                cosmicUVs = Objects.requireNonNull(COSMIC_SHADER.getUniform("cosmicuvs"));
                cosmicTime.set((float) renderTime + renderFrame);
                COSMIC_SHADER.apply();
            });
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


    @SubscribeEvent
    public static void clientTick(ClientTickEvent.Post event) {
        if (!Minecraft.getInstance().isPaused() ) {
            ++renderTime;
        }
    }

    @SubscribeEvent
    public static void renderTick(RenderFrameEvent.Pre event) {
        if (!Minecraft.getInstance().isPaused()) {
            renderFrame = event.getPartialTick().getGameTimeDeltaTicks();
        }
    }

    @OnlyIn(Dist.CLIENT)
    @SubscribeEvent
    public static void drawScreenPre(final ScreenEvent.Render.Pre e) {
        AvaritiaShaders.inventoryRender = true;
    }

    @OnlyIn(Dist.CLIENT)
    @SubscribeEvent
    public static void drawScreenPost(final ScreenEvent.Render.Post e) {
        AvaritiaShaders.inventoryRender = false;
    }
}
