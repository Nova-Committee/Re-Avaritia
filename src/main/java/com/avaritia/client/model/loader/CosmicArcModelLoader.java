package com.avaritia.client.model.loader;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.avaritia.client.model.loader.base.BaseGeometry;
import com.avaritia.client.model.loader.base.BaseModelLoader;
import net.minecraft.client.renderer.block.model.BlockModel;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.NotNull;

import java.util.List;

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

        public @NotNull Object bake() {
            return this;
        }
    }
}
