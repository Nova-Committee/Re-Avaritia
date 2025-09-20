package committee.nova.mods.avaritia.client.model.loader;

import com.google.gson.*;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
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


public class HaloEternalModelLoader implements IGeometryLoader<HaloEternalModelLoader.HaloEternalGeometry> {
    public static final HaloEternalModelLoader INSTANCE = new HaloEternalModelLoader();

    @Override
    public HaloEternalGeometry read(JsonObject modelContents, JsonDeserializationContext deserializationContext) throws JsonParseException {

        final JsonObject halo = modelContents.getAsJsonObject("halo");
        if (halo == null) {
            throw new IllegalStateException("Missing 'halo' object.");
        }


        final JsonObject eternal = modelContents.getAsJsonObject("eternal");
        if (eternal == null) {
            throw new IllegalStateException("Missing 'eternal' object.");
        }


        final IntArrayList layerColors = new IntArrayList();
        final JsonArray layerColorsArr = modelContents.getAsJsonArray("layerColors");
        if (layerColorsArr != null) {
            for (final JsonElement jsonElement : layerColorsArr) {
                layerColors.add(jsonElement.getAsInt());
            }
        }
        final String texture = GsonHelper.getAsString(halo, "texture");
        final int color = GsonHelper.getAsInt(halo, "color");
        final int size = GsonHelper.getAsInt(halo, "size");
        final boolean pulse = GsonHelper.getAsBoolean(halo, "pulse");


        List<String> maskTexture = new ArrayList<>();
        if (eternal.has("mask") && eternal.get("mask").isJsonArray()) {
            JsonArray masks = eternal.getAsJsonArray("mask");
            for (int i = 0; i < masks.size(); i++) {
                maskTexture.add(masks.get(i).getAsString());
            }
        } else {
            maskTexture.add(GsonHelper.getAsString(eternal, "mask"));
        }


        final JsonObject clean = modelContents.deepCopy();
        clean.remove("halo");
        clean.remove("eternal");
        clean.remove("loader");
        final BlockModel baseModel = deserializationContext.deserialize(clean, BlockModel.class);
        return new HaloEternalGeometry(baseModel, layerColors, texture, color, size, pulse, maskTexture);
    }

    public static class HaloEternalGeometry implements IUnbakedGeometry<HaloEternalGeometry> {
        private final BlockModel baseModel;
        private final IntList layerColors;
        private final String texture;
        private final int color;
        private final int size;
        private final boolean pulse;
        private final List<String> maskTextures;

        public HaloEternalGeometry(final BlockModel baseModel, final IntList layerColors, final String texture,
                                   final int color, final int size, final boolean pulse, final List<String> maskTextures) {
            this.baseModel = baseModel;
            this.layerColors = layerColors;
            this.texture = texture;
            this.color = color;
            this.size = size;
            this.pulse = pulse;
            this.maskTextures = maskTextures;
        }

        @Override
        public BakedModel bake(IGeometryBakingContext context, ModelBaker baker, Function<Material, TextureAtlasSprite> spriteGetter, ModelState modelState, ItemOverrides overrides, ResourceLocation modelLocation) {
            // 烘焙基础模型
            BakedModel bakedBaseModel = this.baseModel.bake(baker, this.baseModel, spriteGetter, modelState, modelLocation, true);

            // 获取Halo纹理
            Material particleLocation = this.baseModel.getMaterial(this.texture);
            TextureAtlasSprite particle = spriteGetter.apply(particleLocation);

            // 处理Eternal遮罩纹理
            List<ResourceLocation> textures = new ArrayList<>();
            this.maskTextures.forEach(mask -> textures.add(new ResourceLocation(mask)));

            // 创建融合了Halo和Eternal效果的模型
            return new HaloEternalBakedModel(HaloModelLoader.HaloItemModelGeometry.tintLayers(bakedBaseModel, layerColors),
                    particle, this.color, this.size, this.pulse, textures);
        }

        @Override
        public void resolveParents(Function<ResourceLocation, UnbakedModel> modelGetter, IGeometryBakingContext context) {
            this.baseModel.resolveParents(modelGetter);
        }
    }
}
