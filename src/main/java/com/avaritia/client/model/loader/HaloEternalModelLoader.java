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


public class HaloEternalModelLoader extends BaseModelLoader<HaloEternalModelLoader.HaloEternalGeometry> {
    public static final HaloEternalModelLoader INSTANCE = new HaloEternalModelLoader();

    @Override
    public @NotNull HaloEternalGeometry read(@NotNull JsonObject modelContents, JsonDeserializationContext deserializationContext) throws JsonParseException {
        BlockModel baseModel = deserializationContext.deserialize(clear(modelContents, "halo", "eternal"), BlockModel.class);
        HaloSetting setting = getHalo(modelContents);
        List<Identifier> eternalMaskTexture = getMasks(modelContents, "eternal");
        return new HaloEternalGeometry(baseModel, setting, eternalMaskTexture);
    }

    public static class HaloEternalGeometry extends BaseGeometry<HaloEternalGeometry> {
        private final BlockModel baseModel;
        private final HaloSetting setting;
        private final List<Identifier> maskTextures;

        public HaloEternalGeometry(final BlockModel baseModel, final HaloSetting setting, final List<Identifier> maskTextures) {
            super(baseModel);
            this.baseModel = baseModel;
            this.setting = setting;
            this.maskTextures = maskTextures;
        }

        public @NotNull Object bake() {
            return this;
        }
    }
}
