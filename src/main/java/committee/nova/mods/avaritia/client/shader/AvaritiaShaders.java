package committee.nova.mods.avaritia.client.shader;

import com.mojang.blaze3d.shaders.Uniform;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import committee.nova.mods.avaritia.Static;
import committee.nova.mods.avaritia.init.handler.SpriteRegistryHandler;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.client.event.RegisterShadersEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

import java.io.IOException;
import java.util.Objects;

/**
 * Name: Avaritia-forge / AvaritiaShaders
 * Author: cnlimiter
 * CreateTime: 2023/9/18 1:37
 * Description:
 */

public class AvaritiaShaders {
    private static final float[] COSMIC_UVS = new float[40];
    private static int renderTime;
    private static float renderFrame;
    public static ShaderInstance cosmicShader;
    public static Uniform cosmicTime;
    public static Uniform cosmicYaw;
    public static Uniform cosmicPitch;
    public static Uniform cosmicExternalScale;
    public static Uniform cosmicOpacity;
    public static Uniform cosmicUVs;
    public static RenderType COSMIC_RENDER_TYPE = RenderType.create(new ResourceLocation(Static.MOD_ID , "cosmic").toString(),
            DefaultVertexFormat.BLOCK, VertexFormat.Mode.QUADS, 2097152, true, false,
            RenderType.CompositeState.builder().setShaderState(new RenderStateShard.ShaderStateShard(() -> cosmicShader))
                    .setDepthTestState(RenderStateShard.EQUAL_DEPTH_TEST)
                    .setLightmapState(RenderStateShard.LIGHTMAP)
                    .setTransparencyState(RenderStateShard.TRANSLUCENT_TRANSPARENCY)
                    .setTextureState(RenderStateShard.BLOCK_SHEET_MIPPED)
                    .createCompositeState(true)
    );

    public static void init() {
        var eventbus = FMLJavaModLoadingContext.get().getModEventBus();
        eventbus.addListener(AvaritiaShaders::onRegisterShaders);
        MinecraftForge.EVENT_BUS.addListener(AvaritiaShaders::onRenderTick);
        MinecraftForge.EVENT_BUS.addListener(AvaritiaShaders::clientTick);
        MinecraftForge.EVENT_BUS.addListener(AvaritiaShaders::renderTick);
    }

    public static void onRegisterShaders(RegisterShadersEvent event){
        try {
            event.registerShader(new ShaderInstance(event.getResourceProvider(), new ResourceLocation(Static.MOD_ID, "cosmic"), DefaultVertexFormat.BLOCK), e -> {
                cosmicShader = e;
                cosmicTime = Objects.requireNonNull(cosmicShader.getUniform("time"));
                cosmicYaw = Objects.requireNonNull(cosmicShader.getUniform("yaw"));
                cosmicPitch = Objects.requireNonNull(cosmicShader.getUniform("pitch"));
                cosmicExternalScale = Objects.requireNonNull(cosmicShader.getUniform("externalScale"));
                cosmicOpacity = Objects.requireNonNull(cosmicShader.getUniform("opacity"));
                cosmicUVs = Objects.requireNonNull(cosmicShader.getUniform("cosmicuvs"));
                cosmicTime.set((float)renderTime + renderFrame);
                cosmicShader.apply();
            });
        }catch (IOException ignore){

        }

    }

    public static void onRenderTick(TickEvent.RenderTickEvent event) {
        if (event.phase == TickEvent.Phase.START) {
            for (int i = 0; i < 10; ++i) {
                TextureAtlasSprite sprite = SpriteRegistryHandler.sprites.getSprite(new ResourceLocation(Static.MOD_ID, "cosmic_" + i));
                AvaritiaShaders.COSMIC_UVS[i * 4] = sprite.getU0();
                AvaritiaShaders.COSMIC_UVS[i * 4 + 1] = sprite.getV0();
                AvaritiaShaders.COSMIC_UVS[i * 4 + 2] = sprite.getU1();
                AvaritiaShaders.COSMIC_UVS[i * 4 + 3] = sprite.getV1();
            }
            if (cosmicUVs != null) cosmicUVs.set(COSMIC_UVS);
        }
    }


    public static void clientTick(TickEvent.ClientTickEvent event) {
        if (event.phase == TickEvent.Phase.END) {
            ++renderTime;
        }
    }

    public static void renderTick(TickEvent.RenderTickEvent event) {
        if (event.phase == TickEvent.Phase.START) {
            renderFrame = event.renderTickTime;
        }
    }
}
