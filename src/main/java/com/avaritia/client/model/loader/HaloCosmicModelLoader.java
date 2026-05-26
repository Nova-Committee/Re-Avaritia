package com.avaritia.client.model.loader;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.avaritia.client.model.loader.base.BaseGeometry;
import com.avaritia.client.model.loader.base.BaseModelLoader;
import com.avaritia.client.model.loader.base.HaloSetting;
import net.minecraft.client.renderer.block.model.BlockModel;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.NotNull;

import java.util.List;


public class HaloCosmicModelLoader extends BaseModelLoader<HaloCosmicModelLoader.HaloCosmicGeometry> {
    public static final HaloCosmicModelLoader INSTANCE = new HaloCosmicModelLoader();

    @Override
    public @NotNull HaloCosmicGeometry read(@NotNull JsonObject modelContents, JsonDeserializationContext deserializationContext) throws JsonParseException {
        BlockModel baseModel = deserializationContext.deserialize(clear(modelContents, "halo", "cosmic"), BlockModel.class);
        HaloSetting setting = getHalo(modelContents);
        List<Identifier> cosmicMaskTexture = getMasks(modelContents, "cosmic");

        return new HaloCosmicGeometry(baseModel, setting, cosmicMaskTexture);
    }

    public static class HaloCosmicGeometry extends BaseGeometry<HaloCosmicGeometry> {
        private final HaloSetting setting;
        private final List<Identifier> maskTextures;

        public HaloCosmicGeometry(final BlockModel baseModel, final HaloSetting setting, final List<Identifier> maskTextures) {
            super(baseModel);
            this.setting = setting;
            this.maskTextures = maskTextures;
        }

        public @NotNull Object bake() {
            return this;
        }
    }
}
