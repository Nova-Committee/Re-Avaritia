package committee.nova.mods.avaritia.client.model.loader;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import committee.nova.mods.avaritia.client.model.loader.base.BaseGeometry;
import committee.nova.mods.avaritia.client.model.loader.base.BaseModelLoader;
import committee.nova.mods.avaritia.client.model.loader.base.HaloSetting;
import committee.nova.mods.avaritia.client.model.loader.base.HaloUtils;
import net.minecraft.client.renderer.block.model.BlockModel;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.Material;
import net.minecraft.client.resources.model.ModelBaker;
import net.minecraft.client.resources.model.ModelState;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.client.model.geometry.IGeometryBakingContext;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.function.Function;


public class HaloCosmicModelLoader extends BaseModelLoader<HaloCosmicModelLoader.HaloCosmicGeometry> {
    public static final HaloCosmicModelLoader INSTANCE = new HaloCosmicModelLoader();

    @Override
    public @NotNull HaloCosmicGeometry read(@NotNull JsonObject modelContents, JsonDeserializationContext deserializationContext) throws JsonParseException {
        BlockModel baseModel = deserializationContext.deserialize(clear(modelContents, "halo", "cosmic"), BlockModel.class);
        HaloSetting setting = getHalo(modelContents);
        List<ResourceLocation> cosmicMaskTexture = getMasks(modelContents, "cosmic");

        return new HaloCosmicGeometry(baseModel, setting, cosmicMaskTexture);
    }

    public static class HaloCosmicGeometry extends BaseGeometry<HaloCosmicGeometry> {
        private final HaloSetting setting;
        private final List<ResourceLocation> maskTextures;

        public HaloCosmicGeometry(final BlockModel baseModel, final HaloSetting setting, final List<ResourceLocation> maskTextures) {
            super(baseModel);
            this.setting = setting;
            this.maskTextures = maskTextures;
        }

        @Override
        public @NotNull BakedModel bake(@NotNull IGeometryBakingContext context, @NotNull ModelBaker baker, @NotNull Function<Material, TextureAtlasSprite> spriteGetter, @NotNull ModelState modelState, @NotNull ItemOverrides overrides)  {
            // 烘焙基础模型
            BakedModel bakedBaseModel = this.baseModel.bake(baker, this.baseModel, spriteGetter, modelState, true);

            // 获取Halo纹理
            Material particleLocation = this.baseModel.getMaterial(this.setting.texture());
            TextureAtlasSprite particle = spriteGetter.apply(particleLocation);

            // 创建融合了Halo和Cosmic效果的模型
            return new HaloCosmicBakedModel(HaloUtils.tintLayers(bakedBaseModel, this.setting.layerColors()),
                    particle, this.setting, maskTextures);
        }
    }
}
