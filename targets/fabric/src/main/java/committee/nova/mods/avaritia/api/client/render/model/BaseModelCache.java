package committee.nova.mods.avaritia.api.client.render.model;

import committee.nova.mods.avaritia.api.client.render.CCModel;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceProvider;
import java.util.*;

/** Project-native OBJ cache. Reloads parse the original OBJ/MTL artwork, not a substitute mesh. */
public class BaseModelCache {
    private final String namespace;
    private final Map<ResourceLocation, OBJModelData> objects = new HashMap<>();
    protected BaseModelCache(String namespace) { this.namespace = namespace; }
    protected OBJModelData registerOBJ(String path) { return registerOBJ(new ResourceLocation(namespace, path)); }
    protected OBJModelData registerOBJ(ResourceLocation location) {
        return objects.computeIfAbsent(location, OBJModelData::new);
    }
    public void reload(ResourceProvider resources) { objects.values().forEach(model -> model.reload(resources)); }
    public static final class OBJModelData {
        private final ResourceLocation location;
        private Map<String, CCModel> parts;
        private OBJModelData(ResourceLocation location) { this.location = location; }
        public Map<String, CCModel> getModel() {
            if (parts == null) reload(net.minecraft.client.Minecraft.getInstance().getResourceManager());
            return parts;
        }
        private void reload(ResourceProvider resources) {
            parts = Collections.unmodifiableMap(OBJParser.parse(resources, location, VertexFormat.Mode.QUADS, null, false));
        }
    }
}
