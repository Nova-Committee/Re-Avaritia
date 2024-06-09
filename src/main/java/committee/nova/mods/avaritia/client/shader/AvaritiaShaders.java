package committee.nova.mods.avaritia.client.shader;

import com.mojang.blaze3d.shaders.Uniform;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import committee.nova.mods.avaritia.Static;
import committee.nova.mods.avaritia.api.client.shader.CCShaderInstance;
import committee.nova.mods.avaritia.api.client.shader.CCUniform;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.RegisterShadersEvent;
import net.minecraftforge.client.event.ScreenEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.Objects;

/**
 * Name: Avaritia-forge / AvaritiaShaders
 * Author: cnlimiter
 * CreateTime: 2023/9/18 1:37
 * Description:
 */

@Mod.EventBusSubscriber(modid = Static.MOD_ID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class AvaritiaShaders {
    private static final float[] COSMIC_UVS = new float[40];
    public static boolean inventoryRender = false;

    private static int renderTime;
    private static float renderFrame;


    public static CCShaderInstance cosmicShader;
    public static CCShaderInstance cosmicShader2;

    public static CCUniform cosmicTime;
    public static CCUniform cosmicYaw;
    public static CCUniform cosmicPitch;
    public static CCUniform cosmicExternalScale;
    public static CCUniform cosmicOpacity;
    public static CCUniform cosmicUVs;

    public static Uniform cosmicTime2;
    public static Uniform cosmicYaw2;
    public static Uniform cosmicPitch2;
    public static Uniform cosmicExternalScale2;
    public static Uniform cosmicOpacity2;
    public static Uniform cosmicUVs2;


    public static RenderType COSMIC_RENDER_TYPE = RenderType.create("avaritia:cosmic",
            DefaultVertexFormat.BLOCK, VertexFormat.Mode.QUADS, 209715, true, false,
            RenderType.CompositeState.builder().setShaderState(new RenderStateShard.ShaderStateShard(() -> cosmicShader))
                    .setDepthTestState(RenderStateShard.EQUAL_DEPTH_TEST)
                    .setLightmapState(RenderStateShard.LIGHTMAP)
                    .setTransparencyState(RenderStateShard.TRANSLUCENT_TRANSPARENCY)
                    .setTextureState(RenderStateShard.BLOCK_SHEET_MIPPED)
                    .createCompositeState(true)
    );


    public static void onRegisterShaders(RegisterShadersEvent event){
        event.registerShader(CCShaderInstance.create(event.getResourceProvider(), new ResourceLocation(Static.MOD_ID, "cosmic"), DefaultVertexFormat.BLOCK), e -> {
            cosmicShader = (CCShaderInstance)e;
            cosmicTime = Objects.requireNonNull(cosmicShader.getUniform("time"));
            cosmicYaw = Objects.requireNonNull(cosmicShader.getUniform("yaw"));
            cosmicPitch = Objects.requireNonNull(cosmicShader.getUniform("pitch"));
            cosmicExternalScale = Objects.requireNonNull(cosmicShader.getUniform("externalScale"));
            cosmicOpacity = Objects.requireNonNull(cosmicShader.getUniform("opacity"));
            cosmicUVs = Objects.requireNonNull(cosmicShader.getUniform("cosmicuvs"));
            cosmicTime.set((float)renderTime + renderFrame);
            cosmicShader.onApply(() -> {
                cosmicTime.set((float)renderTime + renderFrame);
            });
        });

        event.registerShader(CCShaderInstance.create(event.getResourceProvider(), new ResourceLocation(Static.MOD_ID, "cosmic"), DefaultVertexFormat.BLOCK), e -> {
            cosmicShader2 = (CCShaderInstance)e;
            cosmicTime2 = Objects.requireNonNull(cosmicShader2.getUniform("time"));
            cosmicYaw2 = Objects.requireNonNull(cosmicShader2.getUniform("yaw"));
            cosmicPitch2 = Objects.requireNonNull(cosmicShader2.getUniform("pitch"));
            cosmicExternalScale2 = Objects.requireNonNull(cosmicShader2.getUniform("externalScale"));
            cosmicOpacity2 = Objects.requireNonNull(cosmicShader2.getUniform("opacity"));
            cosmicUVs2 = Objects.requireNonNull(cosmicShader2.getUniform("cosmicuvs"));
            cosmicTime2.set((float)renderTime + renderFrame);
            cosmicShader2.onApply(() -> {
                cosmicTime2.set((float)renderTime + renderFrame);
            });
        });


    }

    @SubscribeEvent
    public static void onRenderTick(TickEvent.RenderTickEvent event) {
        if (event.phase == TickEvent.Phase.START) {
            //Static.LOGGER.info(Minecraft.getInstance().getTextureAtlas(InventoryMenu.BLOCK_ATLAS).apply(new ResourceLocation(Static.MOD_ID, "misc/halo")).toString());
            for (int i = 0; i < 10; ++i) {
                TextureAtlasSprite sprite = Minecraft.getInstance().getTextureAtlas(InventoryMenu.BLOCK_ATLAS).apply(new ResourceLocation(Static.MOD_ID, "misc/cosmic_" + i));
                //SpriteRegistryHandler.sprites.getTextureLocations().forEach(Static.LOGGER::info);
                COSMIC_UVS[i * 4] = sprite.getU0();
                COSMIC_UVS[i * 4 + 1] = sprite.getV0();
                COSMIC_UVS[i * 4 + 2] = sprite.getU1();
                COSMIC_UVS[i * 4 + 3] = sprite.getV1();
            }
            if (cosmicUVs != null) {
                cosmicUVs.set(COSMIC_UVS);
            }
            if (cosmicUVs2 != null) {
                cosmicUVs2.set(COSMIC_UVS);
            }
        }
    }


    @SubscribeEvent
    public static void clientTick(TickEvent.ClientTickEvent event) {
        if (!Minecraft.getInstance().isPaused() && event.phase == TickEvent.Phase.END) {
            ++renderTime;
        }
    }

    @SubscribeEvent
    public static void renderTick(TickEvent.RenderTickEvent event) {
        if (!Minecraft.getInstance().isPaused() && event.phase == TickEvent.Phase.START) {
            renderFrame = event.renderTickTime;
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
