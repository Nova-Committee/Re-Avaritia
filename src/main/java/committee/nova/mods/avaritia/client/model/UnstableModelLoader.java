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


public class UnstableModelLoader implements IGeometryLoader<UnstableModelLoader.UnstableGeometry> {
    public static final UnstableModelLoader INSTANCE = new UnstableModelLoader();

    @Override
    public UnstableGeometry read(JsonObject modelContents, JsonDeserializationContext deserializationContext) throws JsonParseException {
        JsonObject unstableObj = modelContents.getAsJsonObject("unstable");
        if (unstableObj == null) {
            throw new IllegalStateException("Missing 'unstable' object.");
        } else {
            List<String> maskTexture = new ArrayList<>();
            if (unstableObj.has("mask") && unstableObj.get("mask").isJsonArray()) {
                JsonArray masks = unstableObj.getAsJsonArray("mask");
                for (int i = 0; i < masks.size(); i++) {
                    maskTexture.add(masks.get(i).getAsString());
                }
            } else {
                maskTexture.add(GsonHelper.getAsString(unstableObj, "mask"));
            }
            JsonObject clean = modelContents.deepCopy();
            clean.remove("unstable");
            clean.remove("loader");
            BlockModel baseModel = deserializationContext.deserialize(clean, BlockModel.class);
            return new UnstableModelLoader.UnstableGeometry(baseModel, maskTexture);
        }
    }

    public static class UnstableGeometry implements IUnbakedGeometry<UnstableGeometry> {
        private final BlockModel baseModel;
        private final List<String> maskTextures;

        public UnstableGeometry(final BlockModel baseModel, final List<String> maskTextures) {
            this.baseModel = baseModel;
            this.maskTextures = maskTextures;
        }

        @Override
        public BakedModel bake(IGeometryBakingContext context, ModelBaker baker, Function<Material, TextureAtlasSprite> spriteGetter, ModelState modelState, ItemOverrides overrides, ResourceLocation modelLocation) {
            BakedModel baseBakedModel = this.baseModel.bake(baker, this.baseModel, spriteGetter, modelState, modelLocation, true);
            List<ResourceLocation> textures = new ArrayList<>();
            this.maskTextures.forEach(mask -> textures.add(new ResourceLocation(mask)));
            return new UnstableBakeModel(baseBakedModel, textures);
        }

        @Override
        public void resolveParents(Function<ResourceLocation, UnbakedModel> modelGetter, IGeometryBakingContext context) {
            this.baseModel.resolveParents(modelGetter);
        }
    }
}
