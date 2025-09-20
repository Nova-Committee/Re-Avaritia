package committee.nova.mods.avaritia.client.shader;

import com.mojang.blaze3d.shaders.Uniform;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import committee.nova.mods.avaritia.Const;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraftforge.client.event.RegisterShadersEvent;

/**
 * Name: Avaritia-forge / AvaritiaShaders
 * Author: cnlimiter
 * CreateTime: 2023/9/18 1:37
 * Description:
 */

public class AvaritiaShaders {
    public static final float[] COSMIC_UVS = new float[40];
    public static TextureAtlasSprite[] COSMIC_SPRITES = new TextureAtlasSprite[10];
    public static final float[] ETERNAL_UVS = new float[40];
    public static TextureAtlasSprite[] ETERNAL_SPRITES = new TextureAtlasSprite[10];

    public static ShaderInstance COSMIC_SHADER;
    public static ShaderInstance COSMIC_ARMOR_SHADER;
    public static ShaderInstance HELL_SHADER;
    public static ShaderInstance ETERNAL_SHADER;
    public static ShaderInstance UNSTABLE_SHADER;

    public static Uniform cosmicTime;
    public static Uniform cosmicYaw;
    public static Uniform cosmicPitch;
    public static Uniform cosmicExternalScale;
    public static Uniform cosmicOpacity;
    public static Uniform cosmicUVs;

    public static Uniform cosmicArmorTime;
    public static Uniform cosmicArmorYaw;
    public static Uniform cosmicArmorPitch;
    public static Uniform cosmicArmorExternalScale;
    public static Uniform cosmicArmorOpacity;
    public static Uniform cosmicArmorUVs;

    public static Uniform hellTime;
    public static Uniform hellYaw;
    public static Uniform hellPitch;
    public static Uniform hellExternalScale;
    public static Uniform hellOpacity;
    public static Uniform hellUVs;

    public static Uniform eternalTime;
    public static Uniform eternalYaw;
    public static Uniform eternalPitch;
    public static Uniform eternalExternalScale;
    public static Uniform eternalOpacity;
    public static Uniform eternalUVs;

    public static Uniform unstableTime;
    public static Uniform unstableYaw;
    public static Uniform unstablePitch;
    public static Uniform unstableExternalScale;
    public static Uniform unstableOpacity;
    public static Uniform unstableUVs;


    public static void onRegisterShaders(RegisterShadersEvent event) {
        try {
            event.registerShader(new ShaderInstance(event.getResourceProvider(), Const.rl("cosmic"), DefaultVertexFormat.BLOCK), shader -> {
                COSMIC_SHADER = shader;
                cosmicTime = COSMIC_SHADER.getUniform("time");
                cosmicYaw = COSMIC_SHADER.getUniform("yaw");
                cosmicPitch = COSMIC_SHADER.getUniform("pitch");
                cosmicExternalScale = COSMIC_SHADER.getUniform("externalScale");
                cosmicOpacity = COSMIC_SHADER.getUniform("opacity");
                cosmicUVs = COSMIC_SHADER.getUniform("cosmicuvs");
                COSMIC_SHADER.apply();
            });
            event.registerShader(new ShaderInstance(event.getResourceProvider(), Const.rl("cosmic"), DefaultVertexFormat.NEW_ENTITY), shader -> {
                COSMIC_ARMOR_SHADER = shader;
                cosmicArmorTime = COSMIC_ARMOR_SHADER.getUniform("time");
                cosmicArmorYaw = COSMIC_ARMOR_SHADER.getUniform("yaw");
                cosmicArmorPitch = COSMIC_ARMOR_SHADER.getUniform("pitch");
                cosmicArmorExternalScale = COSMIC_ARMOR_SHADER.getUniform("externalScale");
                cosmicArmorOpacity = COSMIC_ARMOR_SHADER.getUniform("opacity");
                cosmicArmorUVs = COSMIC_ARMOR_SHADER.getUniform("cosmicuvs");
                COSMIC_ARMOR_SHADER.apply();
            });
            event.registerShader(new ShaderInstance(event.getResourceProvider(), Const.rl("hell"), DefaultVertexFormat.BLOCK), shader -> {
                HELL_SHADER = shader;
                hellTime = HELL_SHADER.getUniform("time");
                hellYaw = HELL_SHADER.getUniform("yaw");
                hellPitch = HELL_SHADER.getUniform("pitch");
                hellExternalScale = HELL_SHADER.getUniform("externalScale");
                hellOpacity = HELL_SHADER.getUniform("opacity");
                hellUVs = HELL_SHADER.getUniform("cosmicuvs");
                HELL_SHADER.apply();
            });
            event.registerShader(new ShaderInstance(event.getResourceProvider(), Const.rl("eternal"), DefaultVertexFormat.BLOCK), shader -> {
                ETERNAL_SHADER = shader;
                eternalTime = ETERNAL_SHADER.getUniform("time");
                eternalYaw = ETERNAL_SHADER.getUniform("yaw");
                eternalPitch = ETERNAL_SHADER.getUniform("pitch");
                eternalExternalScale = ETERNAL_SHADER.getUniform("externalScale");
                eternalOpacity = ETERNAL_SHADER.getUniform("opacity");
                eternalUVs = ETERNAL_SHADER.getUniform("cosmicuvs");
                ETERNAL_SHADER.apply();
            });
            event.registerShader(new ShaderInstance(event.getResourceProvider(), Const.rl("unstable"), DefaultVertexFormat.BLOCK), shader -> {
                UNSTABLE_SHADER = shader;
                unstableTime = UNSTABLE_SHADER.getUniform("time");
                unstableYaw = UNSTABLE_SHADER.getUniform("yaw");
                unstablePitch = UNSTABLE_SHADER.getUniform("pitch");
                unstableExternalScale = UNSTABLE_SHADER.getUniform("externalScale");
                unstableOpacity = UNSTABLE_SHADER.getUniform("opacity");
                unstableUVs = UNSTABLE_SHADER.getUniform("cosmicuvs");
                UNSTABLE_SHADER.apply();
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
