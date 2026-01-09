package committee.nova.mods.avaritia.api.client.render.model;

import committee.nova.mods.avaritia.Const;
import committee.nova.mods.avaritia.api.Lib;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;

import mekanism.client.render.lib.Quad;
import mekanism.client.render.lib.QuadUtils;
import mekanism.client.render.lib.Vertex;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.BlockModel;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.resources.model.*;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.client.event.ModelEvent;
import net.minecraftforge.client.model.geometry.IGeometryBakingContext;
import net.minecraftforge.client.model.geometry.IUnbakedGeometry;
import net.minecraftforge.client.model.obj.ObjLoader;
import net.minecraftforge.client.model.obj.ObjModel;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;
import java.util.function.Function;

/**
 * @author cnlimiter
 */
public class BaseModelCache {
    private final Map<ResourceLocation, BaseModelCache.ModelData> modelMap = new Object2ObjectOpenHashMap<>();

    private final String modid;

    protected BaseModelCache(String modid) {
        this.modid = modid;
    }

    private ResourceLocation rl(String path) {
        return new ResourceLocation(modid, path);
    }

    public void onBake(ModelEvent.BakingCompleted evt) {
        modelMap.values().forEach(m -> m.reload(evt));
    }

    public void setup(ModelEvent.RegisterAdditional event) {
        modelMap.values().forEach(mekanismModelData -> mekanismModelData.setup(event));
    }

    protected BaseModelCache.OBJModelData registerOBJ(String path) {
        return registerOBJ(rl(path));
    }

    protected BaseModelCache.OBJModelData registerOBJ(ResourceLocation rl) {
        return register(rl, BaseModelCache.OBJModelData::new);
    }

    protected BaseModelCache.JSONModelData registerJSON(String path) {
        return registerJSON(rl(path));
    }

    protected BaseModelCache.JSONModelData registerJSON(ResourceLocation rl) {
        return register(rl, BaseModelCache.JSONModelData::new);
    }

    protected BaseModelCache.JSONModelData registerJSONAndBake(ResourceLocation rl) {
        ModelManager modelManager = Minecraft.getInstance().getModelManager();
        ModelBakery modelBakery = modelManager.getModelBakery();
        ModelBaker baker = modelBakery.new ModelBakerImpl(
                (modelLoc, material) -> material.sprite(),
                rl
        );
        //Register the model
        BaseModelCache.JSONModelData data = registerJSON(rl);
        //Manually run the JsonModelData#reload logic
        data.bakedModel = baker.bake(rl, BlockModelRotation.X0_Y0, Material::sprite);
        if (modelBakery.getModel(rl) instanceof BlockModel blockModel) {
            data.model = blockModel.customData.getCustomGeometry();
        }
        return data;
    }

    protected <DATA extends BaseModelCache.ModelData> DATA register(ResourceLocation rl, Function<ResourceLocation, DATA> creator) {
        DATA data = creator.apply(rl);
        modelMap.put(rl, data);
        return data;
    }

    public static BakedModel getBakedModel(ModelEvent.BakingCompleted evt, ResourceLocation rl) {
        BakedModel bakedModel = evt.getModels().get(rl);
        if (bakedModel == null) {
            Lib.LOGGER.error("Baked model doesn't exist: {}", rl.toString());
            return evt.getModelManager().getMissingModel();
        }
        return bakedModel;
    }

    public static class ModelData {

        protected IUnbakedGeometry<?> model;

        protected final ResourceLocation rl;
        private final Map<IGeometryBakingContext, BakedModel> bakedMap = new Object2ObjectOpenHashMap<>();

        protected ModelData(ResourceLocation rl) {
            this.rl = rl;
        }

        protected void reload(ModelEvent.BakingCompleted evt) {
            bakedMap.clear();
        }

        protected void setup(ModelEvent.RegisterAdditional event) {
        }

        public BakedModel bake(IGeometryBakingContext config) {
            return bakedMap.computeIfAbsent(config, c -> {
                ModelBaker baker = Minecraft.getInstance().getModelManager().getModelBakery().new ModelBakerImpl(
                        (modelLoc, material) -> material.sprite(),
                        rl
                );
                return model.bake(c, baker, Material::sprite, BlockModelRotation.X0_Y0, ItemOverrides.EMPTY, rl);
            });
        }

        public IUnbakedGeometry<?> getModel() {
            return model;
        }
    }

    public static class OBJModelData extends ModelData {

        protected OBJModelData(ResourceLocation rl) {
            super(rl);
        }

        @Override
        protected void reload(ModelEvent.BakingCompleted evt) {
            super.reload(evt);
            model = ObjLoader.INSTANCE.loadModel(new ObjModel.ModelSettings(rl, true, useDiffuseLighting(), true, true, null));
        }

        @Override
        public ObjModel getModel() {
            return (ObjModel) super.getModel();
        }

        protected boolean useDiffuseLighting() {
            return true;
        }
    }

    public static class JSONModelData extends ModelData {

        private BakedModel bakedModel;

        private JSONModelData(ResourceLocation rl) {
            super(rl);
        }

        @Override
        protected void reload(ModelEvent.BakingCompleted evt) {
            super.reload(evt);
            bakedModel = BaseModelCache.getBakedModel(evt, rl);
            UnbakedModel unbaked = evt.getModelBakery().getModel(rl);
            if (unbaked instanceof BlockModel blockModel) {
                model = blockModel.customData.getCustomGeometry();
            }
        }

        @Override
        protected void setup(ModelEvent.RegisterAdditional event) {
            event.register(rl);
        }

        public void collectQuadVertices(List<Vertex[]> vertices, RandomSource random) {
            for (Quad quad : QuadUtils.unpack(getQuads(random))) {
                vertices.add(quad.getVertices());
            }
        }

        public List<BakedQuad> getQuads(RandomSource random) {
            //TODO: Decide if this should just redirect to the other get quads method (some impls might be different depending on if it gets data and render type vs not)
            return getBakedModel().getQuads(null, null, random);
        }

        public List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction side, RandomSource rand, net.minecraftforge.client.model.data.ModelData data, @Nullable RenderType renderType) {
            return getBakedModel().getQuads(state, side, rand, data, renderType);
        }

        public BakedModel getBakedModel() {
            return bakedModel;
        }
    }
}
