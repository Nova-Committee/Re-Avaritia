package committee.nova.mods.avaritia.client.model;

import com.google.gson.*;
import net.minecraft.client.renderer.block.model.BlockModel;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraftforge.client.model.geometry.IGeometryBakingContext;
import net.minecraftforge.client.model.geometry.IGeometryLoader;
import net.minecraftforge.client.model.geometry.IUnbakedGeometry;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

/**
 * @author: cnlimiter
 */
public class CosmicArcModelLoader implements IGeometryLoader<CosmicArcModelLoader.CosmicArcGeometry> {
    public static final CosmicArcModelLoader INSTANCE = new CosmicArcModelLoader();

    @Override
    public CosmicArcGeometry read(JsonObject modelContents, JsonDeserializationContext deserializationContext) throws JsonParseException {

        final JsonObject cosmic = modelContents.getAsJsonObject("cosmic");
        if (cosmic == null) {
            throw new IllegalStateException("Missing 'cosmic' object.");
        }

        List<String> maskTexture = new ArrayList<>();
        if (cosmic.has("mask") && cosmic.get("mask").isJsonArray()) {
            JsonArray masks = cosmic.getAsJsonArray("mask");
            for (int i = 0; i < masks.size(); i++) {
                maskTexture.add(masks.get(i).getAsString());
            }
        } else {
            maskTexture.add(GsonHelper.getAsString(cosmic, "mask"));
        }

        final JsonObject clean = modelContents.deepCopy();
        clean.remove("cosmic");
        clean.remove("loader");
        final BlockModel baseModel = deserializationContext.deserialize(clean, BlockModel.class);
        return new CosmicArcGeometry(baseModel, maskTexture);
    }

    public static class CosmicArcGeometry implements IUnbakedGeometry<CosmicArcGeometry> {
        private final BlockModel baseModel;
        private final List<String> maskTextures;

        public CosmicArcGeometry(final BlockModel baseModel,
                                 final List<String> maskTextures) {
            this.baseModel = baseModel;
            this.maskTextures = maskTextures;
        }

        @Override
        public BakedModel bake(IGeometryBakingContext context, ModelBaker baker, Function<Material, TextureAtlasSprite> spriteGetter, ModelState modelState, ItemOverrides overrides, ResourceLocation modelLocation) {
            // 烘焙基础模型
            BakedModel bakedBaseModel = this.baseModel.bake(baker, this.baseModel, spriteGetter, modelState, modelLocation, true);

            // 处理Cosmic遮罩纹理
            List<ResourceLocation> textures = new ArrayList<>();
            this.maskTextures.forEach(mask -> textures.add(new ResourceLocation(mask)));

            // 创建融合了Arc和Cosmic效果的模型
            return new CosmicArcBakeModel(bakedBaseModel,
                    textures);
        }

        @Override
        public void resolveParents(Function<ResourceLocation, UnbakedModel> modelGetter, IGeometryBakingContext context) {
            this.baseModel.resolveParents(modelGetter);
        }
    }
}
