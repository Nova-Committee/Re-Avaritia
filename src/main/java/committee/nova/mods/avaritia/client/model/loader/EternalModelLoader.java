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


public class EternalModelLoader extends BaseModelLoader<EternalModelLoader.EternalGeometry> {
    public static final EternalModelLoader INSTANCE = new EternalModelLoader();

    @Override
    public EternalGeometry read(JsonObject modelContents, JsonDeserializationContext deserializationContext) throws JsonParseException {
        BlockModel baseModel = deserializationContext.deserialize(clear(modelContents, "eternal"), BlockModel.class);
        List<ResourceLocation> eternalMaskTexture = getMasks(modelContents, "eternal");
        return new EternalModelLoader.EternalGeometry(baseModel, eternalMaskTexture);

    }

    public static class EternalGeometry extends BaseGeometry<EternalGeometry> {
        private final List<ResourceLocation> maskTextures;

        public EternalGeometry(final BlockModel baseModel, final List<ResourceLocation> maskTextures) {
            super(baseModel);
            this.maskTextures = maskTextures;
        }

        @Override
        public BakedModel bake(IGeometryBakingContext context, ModelBaker baker, Function<Material, TextureAtlasSprite> spriteGetter, ModelState modelState, ItemOverrides overrides, ResourceLocation modelLocation) {
            BakedModel baseBakedModel = this.baseModel.bake(baker, this.baseModel, spriteGetter, modelState, modelLocation, true);
            return new EternalBakeModel(baseBakedModel, maskTextures);
        }
    }
}
