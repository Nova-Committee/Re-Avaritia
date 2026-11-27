package committee.nova.mods.avaritia.client.model.loader;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import committee.nova.mods.avaritia.client.model.loader.base.BaseGeometry;
import committee.nova.mods.avaritia.client.model.loader.base.BaseModelLoader;
import net.minecraft.client.renderer.block.model.BlockModel;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.Material;
import net.minecraft.client.resources.model.ModelBaker;
import net.minecraft.client.resources.model.ModelState;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.client.model.geometry.IGeometryBakingContext;

import java.util.List;
import java.util.function.Function;

/**
 * @author cnlimiter
 */
public class CosmicArcModelLoader extends BaseModelLoader<CosmicArcModelLoader.CosmicArcGeometry> {
    public static final CosmicArcModelLoader INSTANCE = new CosmicArcModelLoader();

    @Override
    public CosmicArcGeometry read(JsonObject modelContents, JsonDeserializationContext deserializationContext) throws JsonParseException {
        List<ResourceLocation> cosmicMaskTexture = getMasks(modelContents, "cosmic");
        final BlockModel baseModel = deserializationContext.deserialize(clear(modelContents, "cosmic"), BlockModel.class);
        return new CosmicArcGeometry(baseModel, cosmicMaskTexture);
    }

    public static class CosmicArcGeometry extends BaseGeometry<CosmicArcGeometry> {
        private final List<ResourceLocation> maskTextures;

        public CosmicArcGeometry(final BlockModel baseModel,
                                 final List<ResourceLocation> maskTextures) {
            super(baseModel);
            this.maskTextures = maskTextures;
        }

        @Override
        public BakedModel bake(IGeometryBakingContext context, ModelBaker baker, Function<Material, TextureAtlasSprite> spriteGetter, ModelState modelState, ItemOverrides overrides, ResourceLocation modelLocation) {
            // 烘焙基础模型
            BakedModel bakedBaseModel = this.baseModel.bake(baker, this.baseModel, spriteGetter, modelState, modelLocation, true);
            // 创建融合了Arc和Cosmic效果的模型
            return new CosmicArcBakeModel(bakedBaseModel,
                    maskTextures);
        }
    }
}
