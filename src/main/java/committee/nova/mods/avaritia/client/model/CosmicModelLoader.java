package committee.nova.mods.avaritia.client.model;

import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import committee.nova.mods.avaritia.client.model.loader.base.BaseGeometry;
import committee.nova.mods.avaritia.client.model.loader.base.BaseModelLoader;
import net.minecraft.client.renderer.block.model.BlockModel;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.neoforged.neoforge.client.model.geometry.IGeometryBakingContext;
import net.neoforged.neoforge.client.model.geometry.IGeometryLoader;
import net.neoforged.neoforge.client.model.geometry.IUnbakedGeometry;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

/**
 * Name: Avaritia-forge / HaloItemModelLoader
 * Author: cnlimiter
 * CreateTime: 2023/9/18 23:45
 * Description:
 */

public class CosmicModelLoader extends BaseModelLoader<CosmicModelLoader.CosmicGeometry> {
    public static final CosmicModelLoader INSTANCE = new CosmicModelLoader();

    @Override
    public @NotNull CosmicGeometry read(@NotNull JsonObject modelContents, JsonDeserializationContext deserializationContext) throws JsonParseException {

        BlockModel baseModel = deserializationContext.deserialize(clear(modelContents, "cosmic"), BlockModel.class);
        List<ResourceLocation> cosmicMaskTexture = getMasks(modelContents, "cosmic");

        return new CosmicModelLoader.CosmicGeometry(baseModel, cosmicMaskTexture);

    }

    public static class CosmicGeometry extends BaseGeometry<CosmicGeometry> {
        private final List<ResourceLocation> maskTextures;

        public CosmicGeometry(final BlockModel baseModel, final List<ResourceLocation> maskTextures) {
            super(baseModel);
            this.maskTextures = maskTextures;
        }

        @Override
        public @NotNull BakedModel bake(@NotNull IGeometryBakingContext context, @NotNull ModelBaker baker,
                               @NotNull Function<Material, @NotNull TextureAtlasSprite> spriteGetter,
                               @NotNull ModelState modelState, @NotNull ItemOverrides overrides) {
            BakedModel baseBakedModel = this.baseModel.bake(baker, this.baseModel, spriteGetter, modelState, true);
            return new CosmicBakeModel(baseBakedModel, maskTextures);
        }
    }
}
