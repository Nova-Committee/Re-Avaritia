package committee.nova.mods.avaritia.client.model.loader.base;

import com.google.gson.*;
import committee.nova.mods.avaritia.client.model.loader.*;
import net.minecraft.client.renderer.block.model.BlockModel;
import net.minecraft.client.resources.model.*;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.resources.ResourceLocation;
import java.util.*;
import java.util.function.Function;

/** Native JSON extension: deserialization preserves vanilla parent/override/texture resolution. */
public final class NativeModelGeometry {
    public interface GeometryHolder {
        BaseGeometry<?> avaritia$getGeometry();
        void avaritia$setGeometry(BaseGeometry<?> geometry);
    }
    private static final Map<String, BaseModelLoader<?>> LOADERS = Map.of(
        "avaritia:cosmic", CosmicModelLoader.INSTANCE,
        "avaritia:cosmic_arc", CosmicArcModelLoader.INSTANCE,
        "avaritia:hell", HellModelLoader.INSTANCE,
        "avaritia:eternal", EternalModelLoader.INSTANCE,
        "avaritia:unstable", UnstableModelLoader.INSTANCE,
        "avaritia:halo", HaloModelLoader.INSTANCE,
        "avaritia:halo_cosmic", HaloCosmicModelLoader.INSTANCE,
        "avaritia:halo_eternal", HaloEternalModelLoader.INSTANCE,
        "avaritia:obj", NativeObjModelLoader.INSTANCE);

    public static BlockModel deserialize(JsonElement json, JsonDeserializationContext context) {
        if (!json.isJsonObject()) return null;
        JsonObject object = json.getAsJsonObject();
        if (!object.has("loader")) return null;
        BaseModelLoader<?> loader = LOADERS.get(object.get("loader").getAsString());
        if (loader == null) return null;
        BaseGeometry<?> geometry = loader.read(object, context);
        ((GeometryHolder) (Object) geometry.baseModel).avaritia$setGeometry(geometry);
        return geometry.baseModel;
    }

    public static BakedModel decorate(UnbakedModel original, BakedModel baked, ModelBaker baker,
            Function<Material, TextureAtlasSprite> sprites, ModelState state, ResourceLocation location) {
        if (!(original instanceof BlockModel block) || baked == null) return baked;
        BaseGeometry<?> geometry = null;
        for (BlockModel parent = block; parent != null && geometry == null; parent = parent.parent) {
            geometry = ((GeometryHolder) (Object) parent).avaritia$getGeometry();
        }
        return geometry == null ? baked : geometry.bake(baked, baker, sprites, state, baked.getOverrides(), location);
    }
}
