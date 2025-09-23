package committee.nova.mods.avaritia.client.model.loader.base;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraftforge.client.model.geometry.IGeometryLoader;
import net.minecraftforge.client.model.geometry.IUnbakedGeometry;

import java.util.ArrayList;
import java.util.List;

/**
 * @author: cnlimiter
 */
public abstract class BaseModelLoader<T extends IUnbakedGeometry<T>> implements IGeometryLoader<T> {
    public JsonObject clear(JsonObject modelContents, String... types) {
        final JsonObject clean = modelContents.deepCopy();
        clean.remove("loader");
        for (String type : types) {
            clean.remove(type);
        }
        return clean;
    }

    public List<ResourceLocation> getMasks(JsonObject modelContents, String type) {
        final JsonObject cosmic = modelContents.getAsJsonObject(type);
        if (cosmic == null) {
            throw new IllegalStateException("Missing " + type + " object.");
        }
        List<ResourceLocation> maskTexture = new ArrayList<>();
        if (cosmic.has("mask") && cosmic.get("mask").isJsonArray()) {
            JsonArray masks = cosmic.getAsJsonArray("mask");
            for (int i = 0; i < masks.size(); i++) {
                maskTexture.add(new ResourceLocation(masks.get(i).getAsString()));
            }
        } else {
            maskTexture.add(new ResourceLocation(GsonHelper.getAsString(cosmic, "mask")));
        }
        return maskTexture;
    }

    public HaloSetting getHalo(JsonObject modelContents) {
        final JsonObject halo = modelContents.getAsJsonObject("halo");
        if (halo == null) {
            throw new IllegalStateException("Missing 'halo' object.");
        }

        final IntArrayList layerColors = new IntArrayList();
        final JsonArray layerColorsArr = modelContents.getAsJsonArray("layerColors");
        if (layerColorsArr != null) {
            for (final JsonElement jsonElement : layerColorsArr) {
                layerColors.add(jsonElement.getAsInt());
            }
        }
        final String texture = GsonHelper.getAsString(halo, "texture");
        final int color = GsonHelper.getAsInt(halo, "color");
        final int size = GsonHelper.getAsInt(halo, "size");
        final boolean pulse = GsonHelper.getAsBoolean(halo, "pulse");
        return new HaloSetting(layerColors, texture, color, size, pulse);
    }
}
