package committee.nova.mods.avaritia.client.render.shader;

import committee.nova.mods.avaritia.init.handler.RenderHandler;
import net.minecraft.client.Minecraft;
import org.lwjgl.opengl.ARBShaderObjects;

public class CosmicShaderHelper {

    public static final ShaderCallback shaderCallback;

    public static float[] lightlevel = new float[3];

    public static boolean inventoryRender = false;
    public static float cosmicOpacity = 1.0f;

    static {
        shaderCallback = new ShaderCallback() {
            @Override
            public void call(int shader) {
                Minecraft mc = Minecraft.getInstance();

                float yaw = 0;
                float pitch = 0;
                float scale = 1.0f;

                if (!inventoryRender && mc.player != null) {
                    yaw = (float) ((mc.player.getXRot() * 2 * Math.PI) / 360.0);
                    pitch = -(float) ((mc.player.getYRot() * 2 * Math.PI) / 360.0);
                } else {
                    scale = 25.0f;
                }

                int x = ARBShaderObjects.glGetUniformLocationARB(shader, "yaw");
                ARBShaderObjects.glUniform1fARB(x, yaw);

                int z = ARBShaderObjects.glGetUniformLocationARB(shader, "pitch");
                ARBShaderObjects.glUniform1fARB(z, pitch);

                int l = ARBShaderObjects.glGetUniformLocationARB(shader, "lightlevel");
                ARBShaderObjects.glUniform3fARB(l, lightlevel[0], lightlevel[1], lightlevel[2]);

                int lightmix = ARBShaderObjects.glGetUniformLocationARB(shader, "lightmix");
                ARBShaderObjects.glUniform1fARB(lightmix, 0.2f);

                int uvs = ARBShaderObjects.glGetUniformLocationARB(shader, "cosmicuvs");
                ARBShaderObjects.glUniformMatrix2fvARB(uvs, false, RenderHandler.cosmicUVs);

                int s = ARBShaderObjects.glGetUniformLocationARB(shader, "externalScale");
                ARBShaderObjects.glUniform1fARB(s, scale);

                int o = ARBShaderObjects.glGetUniformLocationARB(shader, "opacity");
                ARBShaderObjects.glUniform1fARB(o, cosmicOpacity);
            }
        };
    }

    public static void useShader() {
        ShaderHelper.useShader(ShaderHelper.cosmicShader, shaderCallback);
    }

    public static void releaseShader() {
        ShaderHelper.releaseShader();
    }

//    public static void setLightFromLocation(Level world, BlockPos pos) {
//        if (world == null) {
//            setLightLevel(1.0f);
//            return;
//        }
//
//        int coord = world.getLightEmission(pos);
//
//        int[] map = Minecraft.getInstance().getEntityRenderDispatcher().lightmapColors;
//        if (map == null) {
//            setLightLevel(1.0f);
//            return;
//        }
//
//        int mx = (coord % 65536) / 16;
//        int my = (coord / 65536) / 16;
//
//        int lightcolour = map[my * 16 + mx];
//
//        setLightLevel(((lightcolour >> 16) & 0xFF) / 256.0f, ((lightcolour >> 8) & 0xFF) / 256.0f, ((lightcolour) & 0xFF) / 256.0f);
//    }

    public static void setLightLevel(float level) {
        setLightLevel(level, level, level);
    }

    public static void setLightLevel(float r, float g, float b) {
        lightlevel[0] = Math.max(0.0f, Math.min(1.0f, r));
        lightlevel[1] = Math.max(0.0f, Math.min(1.0f, g));
        lightlevel[2] = Math.max(0.0f, Math.min(1.0f, b));
    }
}
