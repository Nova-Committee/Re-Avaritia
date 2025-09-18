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

    public static ShaderInstance COSMIC_SHADER;
    public static ShaderInstance COSMIC_ARMOR_SHADER;

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
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
