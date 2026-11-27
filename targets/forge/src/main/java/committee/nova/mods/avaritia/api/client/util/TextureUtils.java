package committee.nova.mods.avaritia.api.client.util;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.platform.NativeImage;
import committee.nova.mods.avaritia.api.client.util.color.Color;
import committee.nova.mods.avaritia.api.client.util.color.ColorARGB;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.texture.MissingTextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.registries.ForgeRegistries;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static committee.nova.mods.avaritia.Const.LOGGER;

public class TextureUtils {
    /**
     * 药水图标文件夹路径
     */
    public static final String DEFAULT_EFFECT_DIR = "textures/mob_effect/";
    /**
     * 自定义贴图缓存
     */
    private static final Map<ResourceLocation, NativeImage> CACHE = new HashMap<>();
    private static final Map<ResourceLocation, BufferedImage> BUFFER_CACHE = new HashMap<>();


    /**
     * @return an array of ARGB pixel data
     */
    public static int[] loadTextureData(ResourceLocation resource) {
        BufferedImage img = getBufferedImage(resource);
        if (img == null) {
            return new int[0];
        }
        int w = img.getWidth();
        int h = img.getHeight();
        int[] data = new int[w * h];
        img.getRGB(0, 0, w, h, data, 0, w);
        return data;
    }

    public static Color[] loadTextureColours(ResourceLocation resource) {
        int[] idata = loadTextureData(resource);
        Color[] data = new Color[idata.length];
        for (int i = 0; i < data.length; i++) {
            data[i] = new ColorARGB(idata[i]);
        }
        return data;
    }

    /**
     * 从资源中加载纹理并转换为 BufferedImage。
     *
     * @param texture 纹理的 ResourceLocation
     * @return 纹理对应的 BufferedImage 或 null
     */
    public static BufferedImage getBufferedImage(ResourceLocation texture) {
        // 优先从缓存中获取
        if (BUFFER_CACHE.containsKey(texture)) {
            return BUFFER_CACHE.get(texture);
        }
        try {
            // 打开资源输入流并加载为 BufferedImage
            try (InputStream inputStream = ResourceUtils.getResourceAsStream(texture)) {
                BufferedImage nativeImage = getBufferedImage(inputStream);
                BUFFER_CACHE.put(texture, nativeImage);
                return nativeImage;
            }
        } catch (Exception e) {
            LOGGER.error("Failed to load texture: {}", texture);
            return null;
        }
    }

    /**
     * 从资源中加载纹理并转换为 NativeImage。
     *
     * @param texture 纹理的 ResourceLocation
     * @return 纹理对应的 NativeImage 或 null
     */
    public static NativeImage getTextureImage(ResourceLocation texture) {
        // 优先从缓存中获取
        if (CACHE.containsKey(texture)) {
            return CACHE.get(texture);
        }
        try {
            // 打开资源输入流并加载为 NativeImage
            try (InputStream inputStream = ResourceUtils.getResourceAsStream(texture)) {
                NativeImage nativeImage = NativeImage.read(inputStream);
                CACHE.put(texture, nativeImage);
                return nativeImage;
            }
        } catch (Exception e) {
            LOGGER.error("Failed to load texture: {}", texture);
            return null;
        }
    }

    public static BufferedImage getBufferedImage(InputStream in) throws IOException {
        BufferedImage img = ImageIO.read(in);
        in.close();
        return img;
    }

    public static void copySubImg(int[] fromTex, int fromWidth, int fromX, int fromY, int width, int height, int[] toTex, int toWidth, int toX, int toY) {
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int fp = (y + fromY) * fromWidth + x + fromX;
                int tp = (y + toX) * toWidth + x + toX;

                toTex[tp] = fromTex[fp];
            }
        }
    }

    public static void prepareTexture(int target, int texture, int min_mag_filter, int wrap) {
        GlStateManager._texParameter(target, GL11.GL_TEXTURE_MIN_FILTER, min_mag_filter);
        GlStateManager._texParameter(target, GL11.GL_TEXTURE_MAG_FILTER, min_mag_filter);
        if (target == GL11.GL_TEXTURE_2D) {
            GlStateManager._bindTexture(target);
        } else {
            GL11.glBindTexture(target, texture);
        }

        switch (target) {
            case GL12.GL_TEXTURE_3D:
                GlStateManager._texParameter(target, GL12.GL_TEXTURE_WRAP_R, wrap);
            case GL11.GL_TEXTURE_2D:
                GlStateManager._texParameter(target, GL11.GL_TEXTURE_WRAP_T, wrap);
            case GL11.GL_TEXTURE_1D:
                GlStateManager._texParameter(target, GL11.GL_TEXTURE_WRAP_S, wrap);
        }
    }

    /**
     * 获取贴图管理器
     */
    public static TextureManager getTextureManager() {
        return Minecraft.getInstance().getTextureManager();
    }

    /**
     * 获取贴图集
     */
    public static TextureAtlas getTextureMap() {
        return Minecraft.getInstance().getModelManager().getAtlas(InventoryMenu.BLOCK_ATLAS);
    }

    /**
     * 获取错误贴图
     */
    public static TextureAtlasSprite getMissingSprite() {
        return getTextureMap().getSprite(MissingTextureAtlasSprite.getLocation());
    }

    /**
     * 获取指定命名空间的任意贴图
     */
    public static TextureAtlasSprite getTexture(String location) {
        return getTextureMap().getSprite(new ResourceLocation(location));
    }

    /**
     * 获取指定命名空间的任意贴图
     */
    public static TextureAtlasSprite getTexture(ResourceLocation location) {
        return getTextureMap().getSprite(location);
    }

    /**
     * 获取指定命名空间的方块贴图
     */
    public static TextureAtlasSprite getBlockTexture(String string) {
        return getBlockTexture(new ResourceLocation(string));
    }

    /**
     * 获取指定命名空间的方块贴图
     *
     * @param location 命名空间
     * @return 贴图
     */
    public static TextureAtlasSprite getBlockTexture(ResourceLocation location) {
        return getTexture(new ResourceLocation(location.getNamespace(), "block/" + location.getPath()));
    }

    /**
     * 获取指定命名空间的物品贴图
     */
    public static TextureAtlasSprite getItemTexture(String string) {
        return getItemTexture(new ResourceLocation(string));
    }

    /**
     * 获取指定命名空间的物品贴图
     */
    public static TextureAtlasSprite getItemTexture(ResourceLocation location) {
        return getTexture(new ResourceLocation(location.getNamespace(), "items/" + location.getPath()));
    }

    /**
     * 获取方块所有方向的贴图
     */
    @Deprecated
    public static TextureAtlasSprite[] getSideIconsForBlock(BlockState state) {
        TextureAtlasSprite[] sideSprites = new TextureAtlasSprite[6];
        TextureAtlasSprite missingSprite = getMissingSprite();
        for (int i = 0; i < 6; i++) {
            TextureAtlasSprite[] sprites = getIconsForBlock(state, i);
            TextureAtlasSprite sideSprite = missingSprite;
            if (sprites.length != 0) {
                sideSprite = sprites[0];
            }
            sideSprites[i] = sideSprite;
        }
        return sideSprites;
    }

    /**
     * 获取方块指定方向的贴图
     */
    @Deprecated
    public static TextureAtlasSprite[] getIconsForBlock(BlockState state, int side) {
        return getIconsForBlock(state, Direction.values()[side]);
    }

    /**
     * 获取方块指定方向的贴图
     */
    @Deprecated
    public static TextureAtlasSprite[] getIconsForBlock(BlockState state, Direction side) {
        BakedModel model = Minecraft.getInstance().getBlockRenderer().getBlockModel(state);
        if (model != null) {
            List<BakedQuad> quads = model.getQuads(state, side, RandomSource.create(0));
            if (quads != null && quads.size() > 0) {
                TextureAtlasSprite[] sprites = new TextureAtlasSprite[quads.size()];
                for (int i = 0; i < quads.size(); i++) {
                    sprites[i] = quads.get(i).getSprite();
                }
                return sprites;
            }
        }
        return new TextureAtlasSprite[0];
    }

    /**
     * 获取方块的粒子贴图
     */
    @Deprecated
    public static TextureAtlasSprite getParticleIconForBlock(BlockState state) {
        BakedModel model = Minecraft.getInstance().getBlockRenderer().getBlockModel(state);
        return model.getParticleIcon();
    }

    /**
     * 获取药水效果图标
     */
    public static ResourceLocation getEffectTexture(MobEffectInstance mobEffectInstance) {
        ResourceLocation effectIcon;
        ResourceLocation registryName = ForgeRegistries.MOB_EFFECTS.getKey(mobEffectInstance.getEffect());
        if (registryName != null) {
            effectIcon = new ResourceLocation(registryName.getNamespace(), DEFAULT_EFFECT_DIR + registryName.getPath() + ".png");
        } else {
            effectIcon = null;
        }
        return effectIcon;
    }


}
