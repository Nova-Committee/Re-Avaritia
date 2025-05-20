package committee.nova.mods.avaritia.client.shader;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import committee.nova.mods.avaritia.Static;
import committee.nova.mods.avaritia.api.client.shader.CCUniform;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.ShaderInstance;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.*;

import java.io.IOException;

/**
 * Name: Avaritia-forge / AvaritiaShaders
 * Author: cnlimiter
 * CreateTime: 2023/9/18 1:37
 * Description:
 */

@EventBusSubscriber(modid = Static.MOD_ID, value = Dist.CLIENT, bus = EventBusSubscriber.Bus.GAME)
public class AvaritiaShaders {
    public static final float[] COSMIC_UVS = new float[40];
    public static boolean inventoryRender = false;

    public static int renderTime;
    public static float renderFrame;


    public static ShaderInstance cosmicShader;
    public static ShaderInstance cosmicArmorShader;

    public static CCUniform cosmicTime;
    public static CCUniform cosmicYaw;
    public static CCUniform cosmicPitch;
    public static CCUniform cosmicExternalScale;
    public static CCUniform cosmicOpacity;
    public static CCUniform cosmicUVs;


    public static RenderType COSMIC_RENDER_TYPE = RenderType.create(
            Static.rl("cosmic").toString(),
            DefaultVertexFormat.BLOCK,
            VertexFormat.Mode.QUADS, 2097152, true, false,
            RenderType.CompositeState.builder()
                    .setShaderState(new RenderStateShard.ShaderStateShard(() -> cosmicShader))
                    .setDepthTestState(RenderStateShard.EQUAL_DEPTH_TEST)
                    .setLightmapState(RenderStateShard.LIGHTMAP)
                    .setTransparencyState(RenderStateShard.TRANSLUCENT_TRANSPARENCY)
                    .setTextureState(RenderStateShard.BLOCK_SHEET_MIPPED)
                    .createCompositeState(true)
    );


    public static void onRegisterShaders(RegisterShadersEvent event) {
        try {
            cosmicShader = new ShaderInstance(event.getResourceProvider(), Static.rl("cosmic"), DefaultVertexFormat.BLOCK);
            cosmicArmorShader = new ShaderInstance(event.getResourceProvider(), Static.rl("cosmic"), DefaultVertexFormat.NEW_ENTITY);
            event.registerShader(cosmicShader, shaderInstance -> {});
            event.registerShader(cosmicArmorShader, shaderInstance -> {});
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
//        event.registerShader(CCShaderInstance.create(event.getResourceProvider(), Static.rl("cosmic"), DefaultVertexFormat.BLOCK), e -> {
//            cosmicShader = (CCShaderInstance) e;
//            cosmicTime = Objects.requireNonNull(cosmicShader.getUniform("time"));
//            cosmicYaw = Objects.requireNonNull(cosmicShader.getUniform("yaw"));
//            cosmicPitch = Objects.requireNonNull(cosmicShader.getUniform("pitch"));
//            cosmicExternalScale = Objects.requireNonNull(cosmicShader.getUniform("externalScale"));
//            cosmicOpacity = Objects.requireNonNull(cosmicShader.getUniform("opacity"));
//            cosmicUVs = Objects.requireNonNull(cosmicShader.getUniform("cosmicuvs"));
//            cosmicTime.set((float) renderTime + renderFrame);
//            cosmicShader.onApply(() -> {
//                cosmicTime.set((float) renderTime + renderFrame);
//            });
//        });
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
