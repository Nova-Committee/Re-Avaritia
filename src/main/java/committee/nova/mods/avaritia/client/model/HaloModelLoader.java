package committee.nova.mods.avaritia.client.model;

import com.google.gson.*;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.blaze3d.vertex.VertexFormatElement;
import committee.nova.mods.avaritia.api.client.model.CachedFormat;
import committee.nova.mods.avaritia.api.client.model.IVertexConsumer;
import committee.nova.mods.avaritia.api.client.model.Quad;
import committee.nova.mods.avaritia.client.model.loader.base.BaseGeometry;
import committee.nova.mods.avaritia.client.model.loader.base.BaseModelLoader;
import committee.nova.mods.avaritia.client.model.loader.base.HaloSetting;
import committee.nova.mods.avaritia.client.model.loader.base.HaloUtils;
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
import net.neoforged.neoforge.client.RenderTypeGroup;
import net.neoforged.neoforge.client.model.geometry.IGeometryBakingContext;
import net.neoforged.neoforge.client.model.geometry.IGeometryLoader;
import net.neoforged.neoforge.client.model.geometry.IUnbakedGeometry;
import org.apache.commons.lang3.tuple.Pair;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
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
