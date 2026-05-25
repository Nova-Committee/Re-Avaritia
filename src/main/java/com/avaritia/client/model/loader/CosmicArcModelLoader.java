package com.avaritia.client.model.loader;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.avaritia.client.model.loader.base.BaseGeometry;
import com.avaritia.client.model.loader.base.BaseModelLoader;
import net.minecraft.client.renderer.block.model.BlockModel;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.Material;
import net.minecraft.client.resources.model.ModelBaker;
import net.minecraft.client.resources.model.ModelState;
import net.minecraft.resources.Identifier;
import net.neoforged.neoforge.client.model.geometry.IGeometryBakingContext;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.function.Function;

/**
 * @author cnlimiter
 */
public class CosmicArcModelLoader extends BaseModelLoader<CosmicArcModelLoader.CosmicArcGeometry> {
    public static final CosmicArcModelLoader INSTANCE = new CosmicArcModelLoader();

    @Override
    public @NotNull CosmicArcGeometry read(@NotNull JsonObject modelContents, @NotNull JsonDeserializationContext deserializationContext) throws JsonParseException {
        List<Identifier> cosmicMaskTexture = getMasks(modelContents, "cosmic");
        final BlockModel baseModel = deserializationContext.deserialize(clear(modelContents, "cosmic"), BlockModel.class);
        return new CosmicArcGeometry(baseModel, cosmicMaskTexture);
    }

    public static class CosmicArcGeometry extends BaseGeometry<CosmicArcGeometry> {
        private final List<Identifier> maskTextures;

        public CosmicArcGeometry(final BlockModel baseModel,
                                 final List<Identifier> maskTextures) {
            super(baseModel);
            this.maskTextures = maskTextures;
        }

        @Override
        public @NotNull BakedModel bake(@NotNull IGeometryBakingContext context, @NotNull ModelBaker baker,
                                        @NotNull Function<Material, TextureAtlasSprite> spriteGetter, @NotNull ModelState modelState, @NotNull ItemOverrides overrides) {
            // 烘焙基础模型
            BakedModel bakedBaseModel = this.baseModel.bake(baker, this.baseModel, spriteGetter, modelState, true);
            // 创建融合了Arc和Cosmic效果的模型
            return new CosmicArcBakeModel(bakedBaseModel,
                    maskTextures);
        }
    }
}
