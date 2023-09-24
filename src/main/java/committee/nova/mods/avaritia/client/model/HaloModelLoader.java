package committee.nova.mods.avaritia.client.model;

import com.google.gson.*;
import committee.nova.mods.avaritia.api.client.render.CachedFormat;
import committee.nova.mods.avaritia.api.client.render.Quad;
import committee.nova.mods.avaritia.util.client.render.VertexUtil;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.BlockModel;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.*;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.client.model.data.ModelData;
import net.minecraftforge.client.model.geometry.IGeometryBakingContext;
import net.minecraftforge.client.model.geometry.IGeometryLoader;
import net.minecraftforge.client.model.geometry.IUnbakedGeometry;

import java.util.*;
import java.util.function.Function;

/**
 * Name: Avaritia-forge / HaloItemModelLoader
 * Author: cnlimiter
 * CreateTime: 2023/9/18 23:45
 * Description:
 */

public class HaloModelLoader implements IGeometryLoader<HaloModelLoader.HaloModelGeometry> {

    public static final HaloModelLoader INSTANCE = new HaloModelLoader();

    @Override
    public HaloModelGeometry read(JsonObject modelContents, JsonDeserializationContext deserializationContext) throws JsonParseException {
        JsonObject haloObj = modelContents.getAsJsonObject("halo");
        if (haloObj == null) {
            throw new IllegalStateException("Missing 'halo' object.");
        } else {
            IntList layerColors = new IntArrayList();
            JsonArray layerColorsArr = modelContents.getAsJsonArray("layerColors");
            if (layerColorsArr != null) {

                for (JsonElement jsonElement : layerColorsArr) {
                    layerColors.add(jsonElement.getAsInt());
                }
            }

            String texture = GsonHelper.getAsString(haloObj, "texture");
            int color = GsonHelper.getAsInt(haloObj, "color");
            int size = GsonHelper.getAsInt(haloObj, "size");
            boolean pulse = GsonHelper.getAsBoolean(haloObj, "pulse");
            JsonObject clean = modelContents.deepCopy();
            clean.remove("halo");
            clean.remove("loader");
            BlockModel baseModel = deserializationContext.deserialize(clean, BlockModel.class);
            return new HaloModelLoader.HaloModelGeometry(baseModel, layerColors, texture, color, size, pulse);
        }
    }

    public static class HaloModelGeometry implements IUnbakedGeometry<HaloModelGeometry>{
        private static final RandomSource RANDOM = RandomSource.create();
        private final BlockModel baseModel;
        private final IntList layerColors;
        private final String texture;
        private final int color;
        private final int size;
        private final boolean pulse;

        public HaloModelGeometry(BlockModel baseModel, IntList layerColors, String texture, int color, int size, boolean pulse) {
            this.baseModel = baseModel;
            this.layerColors = layerColors;
            this.texture = texture;
            this.color = color;
            this.size = size;
            this.pulse = pulse;
        }

        @Override
        public BakedModel bake(IGeometryBakingContext context, ModelBaker baker, Function<Material, TextureAtlasSprite> spriteGetter, ModelState modelState, ItemOverrides overrides, ResourceLocation modelLocation) {
            BakedModel bakedBaseModel = this.baseModel.bake(baker, this.baseModel, spriteGetter, modelState, modelLocation, false);
            Material haloMaterial = context.getMaterial(texture);
            return new HaloBakedModel(tintLayers(bakedBaseModel, this.layerColors), spriteGetter.apply(haloMaterial), this.color, this.size, this.pulse);
        }
        private static BakedModel tintLayers(BakedModel model, IntList layerColors) {
            if (layerColors.isEmpty()) {
                return model;
            } else {
                Map<Direction, List<BakedQuad>> faceQuads = new HashMap<>();
                Direction[] var3 = Direction.values();

                for (Direction face : var3) {
                    faceQuads.put(face, transformQuads(model.getQuads(null, face, RANDOM), layerColors));
                }

                List<BakedQuad> unculled = transformQuads(model.getQuads(null, null, RANDOM), layerColors);
                return new SimpleBakedModel(unculled, faceQuads, model.useAmbientOcclusion(), model.usesBlockLight(), model.isGui3d(), model.getParticleIcon(), model.getTransforms(), ItemOverrides.EMPTY);
            }
        }

        private static List<BakedQuad> transformQuads(List<BakedQuad> quads, IntList layerColors) {
            List<BakedQuad> newQuads = new ArrayList<>(quads.size());

            for (BakedQuad quad : quads) {
                newQuads.add(transformQuad(quad, layerColors));
            }

            return newQuads;
        }

        private static BakedQuad transformQuad(BakedQuad quad, IntList layerColors) {
            int tintIndex = quad.getTintIndex();
            if (tintIndex != -1 && tintIndex < layerColors.size()) {
                int tint = layerColors.getInt(tintIndex);
                if (tint == -1) {
                    return quad;
                } else {
                    Quad newQuad = new Quad();
                    newQuad.reset(CachedFormat.BLOCK);
                    VertexUtil.putBakedQuad(newQuad, quad);
                    float r = (float)(tint >> 16 & 255) / 255.0F;
                    float g = (float)(tint >> 8 & 255) / 255.0F;
                    float b = (float)(tint & 255) / 255.0F;
                    Quad.Vertex[] var8 = newQuad.vertices;
                    int var9 = var8.length;

                    for (Quad.Vertex v : var8) {
                        float[] var10000 = v.color;
                        var10000[0] *= r;
                        var10000 = v.color;
                        var10000[1] *= g;
                        var10000 = v.color;
                        var10000[2] *= b;
                    }

                    newQuad.tintIndex = -1;
                    return newQuad.bake();
                }
            } else {
                return quad;
            }
        }


    }
}
