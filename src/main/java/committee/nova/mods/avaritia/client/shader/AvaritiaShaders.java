package committee.nova.mods.avaritia.client.shader;

import com.mojang.blaze3d.shaders.AbstractUniform;
import com.mojang.blaze3d.shaders.Uniform;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import committee.nova.mods.avaritia.Const;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.neoforged.neoforge.client.event.RegisterShadersEvent;

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

    public static AbstractUniform cosmicTime;
    public static AbstractUniform cosmicYaw;
    public static AbstractUniform cosmicPitch;
    public static AbstractUniform cosmicExternalScale;
    public static AbstractUniform cosmicOpacity;
    public static AbstractUniform cosmicUVs;

    public static AbstractUniform cosmicArmorTime;
    public static AbstractUniform cosmicArmorYaw;
    public static AbstractUniform cosmicArmorPitch;
    public static AbstractUniform cosmicArmorExternalScale;
    public static AbstractUniform cosmicArmorOpacity;
    public static AbstractUniform cosmicArmorUVs;

    public static AbstractUniform hellTime;
    public static AbstractUniform hellYaw;
    public static AbstractUniform hellPitch;
    public static AbstractUniform hellExternalScale;
    public static AbstractUniform hellOpacity;
    public static AbstractUniform hellUVs;

    public static AbstractUniform eternalTime;
    public static AbstractUniform eternalYaw;
    public static AbstractUniform eternalPitch;
    public static AbstractUniform eternalExternalScale;
    public static AbstractUniform eternalOpacity;
    public static AbstractUniform eternalUVs;

    public static AbstractUniform unstableTime;
    public static AbstractUniform unstableYaw;
    public static AbstractUniform unstablePitch;
    public static AbstractUniform unstableExternalScale;
    public static AbstractUniform unstableOpacity;
    public static AbstractUniform unstableUVs;

    public static void onRegisterShaders(RegisterShadersEvent event) {
        try {
            event.registerShader(new ShaderInstance(event.getResourceProvider(), Const.rl("cosmic"), DefaultVertexFormat.BLOCK), shader -> {
                COSMIC_SHADER = shader;
                cosmicTime = COSMIC_SHADER.safeGetUniform("time");
                cosmicYaw = COSMIC_SHADER.safeGetUniform("yaw");
                cosmicPitch = COSMIC_SHADER.safeGetUniform("pitch");
                cosmicExternalScale = COSMIC_SHADER.safeGetUniform("externalScale");
                cosmicOpacity = COSMIC_SHADER.safeGetUniform("opacity");
                cosmicUVs = COSMIC_SHADER.safeGetUniform("cosmicuvs");
                COSMIC_SHADER.apply();
            });
            event.registerShader(new ShaderInstance(event.getResourceProvider(), Const.rl("cosmic"), DefaultVertexFormat.NEW_ENTITY), shader -> {
                COSMIC_ARMOR_SHADER = shader;
                cosmicArmorTime = COSMIC_ARMOR_SHADER.safeGetUniform("time");
                cosmicArmorYaw = COSMIC_ARMOR_SHADER.safeGetUniform("yaw");
                cosmicArmorPitch = COSMIC_ARMOR_SHADER.safeGetUniform("pitch");
                cosmicArmorExternalScale = COSMIC_ARMOR_SHADER.safeGetUniform("externalScale");
                cosmicArmorOpacity = COSMIC_ARMOR_SHADER.safeGetUniform("opacity");
                cosmicArmorUVs = COSMIC_ARMOR_SHADER.safeGetUniform("cosmicuvs");
                COSMIC_ARMOR_SHADER.apply();
            });
            event.registerShader(new ShaderInstance(event.getResourceProvider(), Const.rl("hell"), DefaultVertexFormat.BLOCK), shader -> {
                HELL_SHADER = shader;
                hellTime = HELL_SHADER.safeGetUniform("time");
                hellYaw = HELL_SHADER.safeGetUniform("yaw");
                hellPitch = HELL_SHADER.safeGetUniform("pitch");
                hellExternalScale = HELL_SHADER.safeGetUniform("externalScale");
                hellOpacity = HELL_SHADER.safeGetUniform("opacity");
                hellUVs = HELL_SHADER.safeGetUniform("cosmicuvs");
                HELL_SHADER.apply();
            });
            event.registerShader(new ShaderInstance(event.getResourceProvider(), Const.rl("eternal"), DefaultVertexFormat.BLOCK), shader -> {
                ETERNAL_SHADER = shader;
                eternalTime = ETERNAL_SHADER.safeGetUniform("time");
                eternalYaw = ETERNAL_SHADER.safeGetUniform("yaw");
                eternalPitch = ETERNAL_SHADER.safeGetUniform("pitch");
                eternalExternalScale = ETERNAL_SHADER.safeGetUniform("externalScale");
                eternalOpacity = ETERNAL_SHADER.safeGetUniform("opacity");
                eternalUVs = ETERNAL_SHADER.safeGetUniform("cosmicuvs");
                ETERNAL_SHADER.apply();
            });
            event.registerShader(new ShaderInstance(event.getResourceProvider(), Const.rl("unstable"), DefaultVertexFormat.BLOCK), shader -> {
                UNSTABLE_SHADER = shader;
                unstableTime = UNSTABLE_SHADER.safeGetUniform("time");
                unstableYaw = UNSTABLE_SHADER.safeGetUniform("yaw");
                unstablePitch = UNSTABLE_SHADER.safeGetUniform("pitch");
                unstableExternalScale = UNSTABLE_SHADER.safeGetUniform("externalScale");
                unstableOpacity = UNSTABLE_SHADER.safeGetUniform("opacity");
                unstableUVs = UNSTABLE_SHADER.safeGetUniform("cosmicuvs");
                UNSTABLE_SHADER.apply();
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
