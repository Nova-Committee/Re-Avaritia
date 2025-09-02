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
import net.minecraftforge.client.model.geometry.IGeometryBakingContext;
import net.minecraftforge.client.model.geometry.IGeometryLoader;
import net.minecraftforge.client.model.geometry.IUnbakedGeometry;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;


public class EternalModelLoader implements IGeometryLoader<EternalModelLoader.EternalGeometry> {
    public static final EternalModelLoader INSTANCE = new EternalModelLoader();

    @Override
    public EternalGeometry read(JsonObject modelContents, JsonDeserializationContext deserializationContext) throws JsonParseException {
        JsonObject eternalObj = modelContents.getAsJsonObject("eternal");
        if (eternalObj == null) {
            throw new IllegalStateException("Missing 'eternal' object.");
        } else {
            List<String> maskTexture = new ArrayList<>();
            if (eternalObj.has("mask") && eternalObj.get("mask").isJsonArray()) {
                JsonArray masks = eternalObj.getAsJsonArray("mask");
                for (int i = 0; i < masks.size(); i++) {
                    maskTexture.add(masks.get(i).getAsString());
                }
            } else {
                maskTexture.add(GsonHelper.getAsString(eternalObj, "mask"));
            }
            JsonObject clean = modelContents.deepCopy();
            clean.remove("eternal");
            clean.remove("loader");
            BlockModel baseModel = deserializationContext.deserialize(clean, BlockModel.class);
            return new EternalModelLoader.EternalGeometry(baseModel, maskTexture);
        }
    }

    public static class EternalGeometry implements IUnbakedGeometry<EternalGeometry> {
        private final BlockModel baseModel;
        private final List<String> maskTextures;

        public EternalGeometry(final BlockModel baseModel, final List<String> maskTextures) {
            this.baseModel = baseModel;
            this.maskTextures = maskTextures;
        }

        @Override
        public BakedModel bake(IGeometryBakingContext context, ModelBaker baker, Function<Material, TextureAtlasSprite> spriteGetter, ModelState modelState, ItemOverrides overrides, ResourceLocation modelLocation) {
            BakedModel baseBakedModel = this.baseModel.bake(baker, this.baseModel, spriteGetter, modelState, modelLocation, true);
            List<ResourceLocation> textures = new ArrayList<>();
            this.maskTextures.forEach(mask -> textures.add(new ResourceLocation(mask)));
            return new EternalBakeModel(baseBakedModel, textures);
        }

        @Override
        public void resolveParents(Function<ResourceLocation, UnbakedModel> modelGetter, IGeometryBakingContext context) {
            this.baseModel.resolveParents(modelGetter);
        }
    }
}
