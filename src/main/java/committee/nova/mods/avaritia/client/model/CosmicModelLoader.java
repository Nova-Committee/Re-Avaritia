package committee.nova.mods.avaritia.client.model;

import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import net.minecraft.client.renderer.block.model.BlockModel;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.neoforged.neoforge.client.model.geometry.IGeometryBakingContext;
import net.neoforged.neoforge.client.model.geometry.IGeometryLoader;
import net.neoforged.neoforge.client.model.geometry.IUnbakedGeometry;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

/**
 * Name: Avaritia-forge / HaloItemModelLoader
 * Author: cnlimiter
 * CreateTime: 2023/9/18 23:45
 * Description:
 */

public class CosmicModelLoader implements IGeometryLoader<CosmicModelLoader.CosmicGeometry> {
    public static final CosmicModelLoader INSTANCE = new CosmicModelLoader();

    @Override
    public CosmicGeometry read(JsonObject modelContents, JsonDeserializationContext deserializationContext) throws JsonParseException {
        JsonObject cosmicObj = modelContents.getAsJsonObject("cosmic");
        if (cosmicObj == null) {
            throw new IllegalStateException("Missing 'cosmic' object.");
        } else {
            List<String> maskTexture = new ArrayList<>();
            if (cosmicObj.has("mask") && cosmicObj.get("mask").isJsonArray()) {
                JsonArray masks = cosmicObj.getAsJsonArray("mask");
                for (int i = 0; i < masks.size(); i++) {
                    maskTexture.add(masks.get(i).getAsString());
                }
            } else {
                maskTexture.add(GsonHelper.getAsString(cosmicObj, "mask"));
            }
            JsonObject clean = modelContents.deepCopy();
            clean.remove("cosmic");
            clean.remove("loader");
            BlockModel baseModel = deserializationContext.deserialize(clean, BlockModel.class);
            return new CosmicModelLoader.CosmicGeometry(baseModel, maskTexture);
        }
    }

    public static class CosmicGeometry implements IUnbakedGeometry<CosmicGeometry> {
        private final BlockModel baseModel;
        private final List<String> maskTextures;

        public CosmicGeometry(final BlockModel baseModel, final List<String> maskTextures) {
            this.baseModel = baseModel;
            this.maskTextures = maskTextures;
        }

        @Override
        public BakedModel bake(IGeometryBakingContext context, ModelBaker baker, Function<Material, TextureAtlasSprite> spriteGetter, ModelState modelState, ItemOverrides overrides) {
            BakedModel baseBakedModel = this.baseModel.bake(baker, this.baseModel, spriteGetter, modelState, true);
            List<ResourceLocation> textures = new ArrayList<>();
            this.maskTextures.forEach(mask -> textures.add(ResourceLocation.tryParse(mask)));
            return new CosmicBakeModel(baseBakedModel, textures);
        }

        @Override
        public void resolveParents(Function<ResourceLocation, UnbakedModel> modelGetter, IGeometryBakingContext context) {
            this.baseModel.resolveParents(modelGetter);
        }

    }
}
