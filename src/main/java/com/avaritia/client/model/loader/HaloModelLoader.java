package com.avaritia.client.model.loader;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.avaritia.client.model.loader.base.BaseGeometry;
import com.avaritia.client.model.loader.base.BaseModelLoader;
import com.avaritia.client.model.loader.base.HaloSetting;
import com.avaritia.client.model.loader.base.HaloUtils;
import net.minecraft.client.renderer.block.model.BlockModel;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.Material;
import net.minecraft.client.resources.model.ModelBaker;
import net.minecraft.client.resources.model.ModelState;
import net.neoforged.neoforge.client.model.geometry.IGeometryBakingContext;
import org.jetbrains.annotations.NotNull;

import java.util.function.Function;

/**
 * Name: Avaritia-forge / HaloItemModelLoader
 * Author: cnlimiter
 * CreateTime: 2023/9/18 23:45
 * Description:
 */

public class HaloModelLoader extends BaseModelLoader<HaloModelLoader.HaloItemModelGeometry> {

    public static final HaloModelLoader INSTANCE = new HaloModelLoader();

    @Override
    public @NotNull HaloItemModelGeometry read(@NotNull JsonObject modelContents, JsonDeserializationContext deserializationContext) throws JsonParseException {
        BlockModel baseModel = deserializationContext.deserialize(clear(modelContents, "halo"), BlockModel.class);
        HaloSetting setting = getHalo(modelContents);
        return new HaloItemModelGeometry(baseModel, setting);
    }


    public static class HaloItemModelGeometry extends BaseGeometry<HaloItemModelGeometry> {
        private final HaloSetting setting;

        public HaloItemModelGeometry(final BlockModel baseModel, final HaloSetting setting) {
            super(baseModel);
            this.setting = setting;
        }

        @Override
        public @NotNull BakedModel bake(final @NotNull IGeometryBakingContext owner, final @NotNull ModelBaker bakery,
                                        final @NotNull Function<Material, TextureAtlasSprite> spriteGetter,
                                        final @NotNull ModelState modelTransform, final @NotNull ItemOverrides overrides) {
            final BakedModel bakedBaseModel = this.baseModel.bake(bakery, this.baseModel, spriteGetter, modelTransform, false);
            Material particleLocation = this.baseModel.getMaterial(this.setting.texture());
            TextureAtlasSprite particle = spriteGetter.apply(particleLocation);
            return new HaloBakedModel(HaloUtils.tintLayers(bakedBaseModel, this.setting.layerColors()), particle,
                    this.setting);
        }
    }
}
