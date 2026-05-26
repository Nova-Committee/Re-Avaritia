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

public class HellModelLoader extends BaseModelLoader<HellModelLoader.HellGeometry> {
    public static final HellModelLoader INSTANCE = new HellModelLoader();

    @Override
    public @NotNull HellGeometry read(@NotNull JsonObject modelContents, JsonDeserializationContext deserializationContext) throws JsonParseException {
        BlockModel baseModel = deserializationContext.deserialize(clear(modelContents, "hell"), BlockModel.class);
        List<Identifier> hellMaskTexture = getMasks(modelContents, "hell");

        return new HellGeometry(baseModel, hellMaskTexture);
    }

    public static class HellGeometry extends BaseGeometry<HellGeometry> {
        private final List<Identifier> maskTextures;

        public HellGeometry(final BlockModel baseModel, final List<Identifier> maskTextures) {
            super(baseModel);
            this.maskTextures = maskTextures;
        }

        public @NotNull Object bake() {
            return this;
        }
    }
}
