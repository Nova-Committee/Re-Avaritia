package committee.nova.mods.avaritia.client.model;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import it.unimi.dsi.fastutil.Pair;
import it.unimi.dsi.fastutil.ints.IntList;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.BlockModel;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.renderer.texture.MissingTextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.client.model.geometry.IGeometryBakingContext;
import net.minecraftforge.client.model.geometry.IGeometryLoader;
import net.minecraftforge.client.model.geometry.IUnbakedGeometry;

import java.util.HashSet;
import java.util.Objects;
import java.util.function.Function;

/**
 * Name: Avaritia-forge / HaloItemModelLoader
 * Author: cnlimiter
 * CreateTime: 2023/9/18 23:45
 * Description:
 */

public class HaloItemModelLoader implements IGeometryLoader<HaloItemModelLoader.HaloItemModel> {



    @Override
    public HaloItemModel read(JsonObject jsonObject, JsonDeserializationContext deserializationContext) throws JsonParseException {
        return null;
    }

    public static class HaloItemModel implements IUnbakedGeometry<HaloItemModel>{
        private final BlockModel baseModel;
        private final IntList layerColors;
        private final String texture;
        private final int color;
        private final int size;
        private final boolean pulse;
        private Material haloMaterial;

        public HaloItemModel(BlockModel baseModel, IntList layerColors, String texture, int color, int size, boolean pulse) {
            this.baseModel = baseModel;
            this.layerColors = layerColors;
            this.texture = texture;
            this.color = color;
            this.size = size;
            this.pulse = pulse;
        }

        @Override
        public BakedModel bake(IGeometryBakingContext context, ModelBaker baker, Function<Material, TextureAtlasSprite> spriteGetter, ModelState modelState, ItemOverrides overrides, ResourceLocation modelLocation) {
            if (haloMaterial == null){
                HashSet<Material> materials = new HashSet<Material>();
                this.haloMaterial = context.getMaterial(this.texture);
                materials.add(this.haloMaterial);
//                this.baseModel.resolveParents()
//                materials.addAll();
            }


            return null;
        }



    }
}
