package committee.nova.mods.avaritia.client.model.loader.utils;

import committee.nova.mods.avaritia.Const;
import committee.nova.mods.avaritia.api.client.model.ItemQuadBakery;
import committee.nova.mods.avaritia.client.model.item.ItemEffect;
import committee.nova.mods.avaritia.client.model.item.LayeredEffectItemModel;
import committee.nova.mods.avaritia.client.model.loader.base.HaloFields;
import committee.nova.mods.avaritia.client.model.loader.utils.halo.HaloLayer;
import committee.nova.mods.avaritia.client.model.loader.utils.halo.HaloSetting;
import committee.nova.mods.avaritia.client.render.mesh.SimpleMesh;
import committee.nova.mods.avaritia.client.render.mesh.SimpleObjMeshLoader;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import net.minecraft.client.color.item.ItemTintSource;
import net.minecraft.client.renderer.item.CuboidItemModelWrapper;
import net.minecraft.client.renderer.item.ItemModel;
import net.minecraft.client.renderer.item.ModelRenderProperties;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.ModelBaker;
import net.minecraft.client.resources.model.ModelDebugName;
import net.minecraft.client.resources.model.ResolvedModel;
import net.minecraft.client.resources.model.geometry.BakedQuad;
import net.minecraft.client.resources.model.sprite.Material;
import net.minecraft.client.resources.model.sprite.MaterialBaker;
import net.minecraft.client.resources.model.sprite.TextureSlots;
import net.minecraft.resources.Identifier;
import org.joml.Matrix4fc;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * 把自定义 item model 的声明数据烘焙成运行时 {@link LayeredEffectItemModel}。
 * <p>
 * 这里故意同时准备 wrapped 原模型、baseQuads 和 effectQuads：
 * wrapped 负责正常物品模型渲染，effectQuads 只负责 mask 限定的动态覆盖层，二者不能互相替代。
 */
public final class EffectItemModelBaker {
    private static final ModelDebugName DEBUG_NAME = () -> "AvaritiaItemModels";

    private EffectItemModelBaker() {
    }

    public static ItemModel bake(ItemModel.BakingContext context, Matrix4fc transformation, Identifier model,
                                 List<ItemTintSource> tints, List<Identifier> masks, @Nullable ItemEffect effect,
                                 Optional<HaloLayer> haloLayer) {
        return bake(context, transformation, model, tints, masks, effect, haloLayer, false);
    }

    public static ItemModel bake(ItemModel.BakingContext context, Matrix4fc transformation, Identifier model,
                                 List<ItemTintSource> tints, List<Identifier> masks, @Nullable ItemEffect effect,
                                 Optional<HaloLayer> haloLayer, boolean cosmicArc) {
        ModelBaker baker = context.blockModelBaker();
        ResolvedModel resolvedModel = baker.getModel(model);
        TextureSlots textureSlots = resolvedModel.getTopTextureSlots();
        // wrapped 是最终必须先提交的基础模型；星空/永恒等效果只作为后续特殊层叠加。
        ItemModel wrapped = new CuboidItemModelWrapper.Unbaked(model, Optional.empty(), tints).bake(context, transformation);
        ModelRenderProperties properties = ModelRenderProperties.fromResolvedModel(baker, resolvedModel, textureSlots);
        // baseQuads 给 halo 脉冲层使用，effectQuads 则来自 mask 贴图，两者来源不同。
        List<BakedQuad> baseQuads = bakeBaseQuads(baker, textureSlots);
        List<BakedQuad> effectQuads = effect == null ? List.of() : bakeEffectQuads(baker, effect, masks);
        Map<String, SimpleMesh> tridentModels = cosmicArc ? loadTridentModels() : Map.of();
        return new LayeredEffectItemModel(wrapped, properties, transformation, effect, effectQuads, baseQuads, haloLayer, cosmicArc, tridentModels);
    }

    /**
     * 将 JSON/datagen 中的 halo 参数转换为运行时层数据，并预先计算 GUI 裁剪所需的 extents。
     */
    public static HaloLayer toHaloLayer(ItemModel.BakingContext context, HaloFields halo) {
        HaloSetting setting = new HaloSetting(new IntArrayList(), halo.texture().toString(), halo.color(), halo.size(), halo.pulse());
        return new HaloLayer(halo.texture(), setting);
    }

    /**
     * mask 贴图只烘焙覆盖层几何，不参与基础物品模型本身的烘焙。
     */
    private static List<BakedQuad> bakeEffectQuads(ModelBaker baker, ItemEffect effect, List<Identifier> masks) {
        MaterialBaker materials = baker.materials();
        List<TextureAtlasSprite> sprites = new ArrayList<>(masks.size());
        for (Identifier mask : masks) {
            sprites.add(materials.get(new Material(mask), DEBUG_NAME).sprite());
        }
        // Custom geometry writes packed UVs directly to the effect buffer. Use
        // the direct bakery so positions and atlas UVs are explicit and remain
        // in the wrapped item's 0..1 model space / mask sprite bounds.
        return ItemQuadBakery.bakeItemOverlay(effect.renderType(), sprites.toArray(TextureAtlasSprite[]::new));
    }

    /**
     * 从基础模型的 layer0/layer1 取出普通物品贴图，用于 pulse 层复用原图轮廓。
     */
    private static List<BakedQuad> bakeBaseQuads(ModelBaker baker, TextureSlots textureSlots) {
        MaterialBaker materials = baker.materials();
        List<TextureAtlasSprite> sprites = new ArrayList<>(2);
        addLayerSprite(materials, textureSlots, "layer0", sprites);
        addLayerSprite(materials, textureSlots, "layer1", sprites);
        return sprites.isEmpty() ? List.of() : ItemQuadBakery.bakeItem(sprites.toArray(TextureAtlasSprite[]::new));
    }

    private static void addLayerSprite(MaterialBaker materials, TextureSlots textureSlots, String layer, List<TextureAtlasSprite> sprites) {
        Material material = textureSlots.getMaterial(layer);
        if (material != null) {
            sprites.add(materials.get(material, DEBUG_NAME).sprite());
        }
    }

    private static Map<String, SimpleMesh> loadTridentModels() {
        try {
            return SimpleObjMeshLoader.load(Const.rl("models/infinity_trident.obj"), true);
        } catch (Exception exception) {
            Const.LOGGER.warn("Failed to load infinity trident item model OBJ; falling back to flat item model.", exception);
            return Map.of();
        }
    }
}
