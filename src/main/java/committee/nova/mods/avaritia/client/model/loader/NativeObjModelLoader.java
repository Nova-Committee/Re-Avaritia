package committee.nova.mods.avaritia.client.model.loader;

import com.google.gson.*;
import com.mojang.blaze3d.vertex.VertexFormat;
import committee.nova.mods.avaritia.api.client.render.CCModel;
import committee.nova.mods.avaritia.api.client.render.model.OBJParser;
import committee.nova.mods.avaritia.api.util.vec.Vector3;
import committee.nova.mods.avaritia.client.model.loader.base.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.*;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.*;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.InventoryMenu;
import java.util.*;
import java.util.function.Function;

/** Bakes supplied OBJ faces/materials into ordinary vanilla quads, including normals and atlas UVs. */
public final class NativeObjModelLoader extends BaseModelLoader<NativeObjModelLoader.Geometry> {
    public static final NativeObjModelLoader INSTANCE = new NativeObjModelLoader();
    @Override public Geometry read(JsonObject object, JsonDeserializationContext context) {
        return new Geometry(context.deserialize(clear(object, "model", "flip_v", "automatic_culling", "emissive_ambient"), BlockModel.class),
                new ResourceLocation(object.get("model").getAsString()),
                !object.has("flip_v") || object.get("flip_v").getAsBoolean());
    }
    public static final class Geometry extends BaseGeometry<Geometry> {
        private final ResourceLocation object;
        private final boolean flipV;
        Geometry(BlockModel base, ResourceLocation object, boolean flipV) { super(base); this.object = object; this.flipV = flipV; }
        @Override public BakedModel bake(BakedModel base, ModelBaker baker, Function<Material, TextureAtlasSprite> sprites,
                ModelState state, ItemOverrides overrides, ResourceLocation location) {
            List<BakedQuad> quads = new ArrayList<>();
            for (CCModel part : OBJParser.parse(Minecraft.getInstance().getResourceManager(), object, VertexFormat.Mode.QUADS, null, false).values()) {
                if (part.normals() == null) part.computeNormals();
                var material = part.material();
                TextureAtlasSprite sprite = material != null && material.diffuseColourMap != null
                        ? sprites.apply(new Material(InventoryMenu.BLOCK_ATLAS, new ResourceLocation(material.diffuseColourMap)))
                        : base.getParticleIcon();
                for (int face = 0; face < part.verts.length; face += 4) {
                    int[] vertices = new int[32];
                    Vector3 normal = part.normals()[face];
                    for (int vertex = 0; vertex < 4; vertex++) {
                        var value = part.verts[face + vertex];
                        int offset = vertex * 8;
                        vertices[offset] = Float.floatToRawIntBits((float) value.vec.x);
                        vertices[offset + 1] = Float.floatToRawIntBits((float) value.vec.y);
                        vertices[offset + 2] = Float.floatToRawIntBits((float) value.vec.z);
                        vertices[offset + 3] = -1;
                        vertices[offset + 4] = Float.floatToRawIntBits(sprite.getU(value.uv.u * 16));
                        vertices[offset + 5] = Float.floatToRawIntBits(sprite.getV((flipV ? value.uv.v : 1 - value.uv.v) * 16));
                        Vector3 n = part.normals()[face + vertex];
                        vertices[offset + 7] = ((int) (n.x * 127) & 255) | (((int) (n.y * 127) & 255) << 8) | (((int) (n.z * 127) & 255) << 16);
                    }
                    quads.add(new BakedQuad(vertices, -1, Direction.getNearest((float) normal.x, (float) normal.y, (float) normal.z), sprite, true));
                }
            }
            Map<Direction, List<BakedQuad>> faces = new EnumMap<>(Direction.class);
            for (Direction face : Direction.values()) faces.put(face, List.of());
            return new SimpleBakedModel(List.copyOf(quads), faces, base.useAmbientOcclusion(), base.usesBlockLight(), true,
                    base.getParticleIcon(), base.getTransforms(), overrides);
        }
    }
}
