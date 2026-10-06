package committee.nova.mods.avaritia.client.model.item;

import committee.nova.mods.avaritia.client.model.loader.AvaritiaItemModelRenderers;
import committee.nova.mods.avaritia.client.model.loader.utils.halo.HaloLayer;
import committee.nova.mods.avaritia.client.render.mesh.SimpleMesh;
import committee.nova.mods.avaritia.common.item.singularity.SingularityItem;
import committee.nova.mods.avaritia.core.singularity.Singularity;
import committee.nova.mods.avaritia.util.SingularityUtils;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.item.CuboidItemModelWrapper;
import net.minecraft.client.renderer.item.ItemModel;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.item.ModelRenderProperties;
import net.minecraft.client.resources.model.geometry.BakedQuad;
import net.minecraft.core.Holder;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.ItemOwner;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.joml.Matrix4f;
import org.joml.Matrix4fc;
import org.joml.Vector3fc;
import org.jspecify.annotations.Nullable;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * 运行时的分层物品模型。
 * <p>
 * 这个类的核心职责是“先渲染基础物品，再追加 halo/星空/弧光/三叉戟层”。因此 {@link #update} 中的
 * {@code wrapped.update(...)} 是星空遮罩和原模型能同时存在的关键顺序，不能被效果层替换掉。
 */
public final class LayeredEffectItemModel implements ItemModel {
    private final ItemModel wrapped;
    private final ModelRenderProperties properties;
    private final Matrix4fc transformation;
    private final @Nullable ItemEffect effect;
    private final List<BakedQuad> effectQuads;
    private final List<BakedQuad> baseQuads;
    private final Optional<HaloLayer> haloLayer;
    private final Vector3fc[] effectExtents;
    private final boolean cosmicArc;
    private final Map<String, SimpleMesh> tridentModels;

    public LayeredEffectItemModel(ItemModel wrapped, ModelRenderProperties properties, Matrix4fc transformation,
                                  @Nullable ItemEffect effect, List<BakedQuad> effectQuads, List<BakedQuad> baseQuads,
                                  Optional<HaloLayer> haloLayer, boolean cosmicArc, Map<String, SimpleMesh> tridentModels) {
        this.wrapped = wrapped;
        this.properties = properties;
        this.transformation = transformation;
        this.effect = effect;
        this.effectQuads = effectQuads;
        this.baseQuads = baseQuads;
        this.haloLayer = haloLayer;
        this.effectExtents = CuboidItemModelWrapper.computeExtents(effectQuads);
        this.cosmicArc = cosmicArc;
        this.tridentModels = tridentModels;
    }

    @Override
    public void update(ItemStackRenderState renderState, ItemStack stack, ItemModelResolver resolver,
                       ItemDisplayContext displayContext, @Nullable ClientLevel level, @Nullable ItemOwner owner, int seed) {
        // 奇点在 GUI 中按住 Shift 时显示材料预览，这是唯一会替换基础模型的分支。
        if (displayContext == ItemDisplayContext.GUI && stack.getItem() instanceof SingularityItem && hasShiftDown()) {
            ItemStack ingredientPreview = singularityIngredientPreview(stack);
            if (!ingredientPreview.isEmpty()) {
                resolver.updateForTopItem(renderState, ingredientPreview, displayContext, level, owner, seed);
                return;
            }
        }

        boolean tridentGeometry = shouldRenderTridentGeometry(displayContext);
        // 三叉戟手持态使用 OBJ 实体层；GUI/地面/展示架仍使用原始 2D 图标。
        if (!tridentGeometry) {
            this.wrapped.update(renderState, stack, resolver, displayContext, level, owner, seed);
        }

        if (tridentGeometry) {
            appendTridentLayer(renderState, displayContext);
        }

        // halo 是 GUI 图标特效，世界/手持中的实体外观仍交给基础模型和其他特殊层。
        if (displayContext == ItemDisplayContext.GUI) {
            this.haloLayer.ifPresent(halo -> {
                appendHaloLayer(renderState, displayContext, halo);
                if (halo.setting().pulse()) {
                    appendPulseLayer(renderState, displayContext, level, seed);
                }
            });
        }

        // effectQuads 由 mask 烘焙而来，只在 mask 区域绘制动态星空/永恒等效果。
        if (shouldRenderEffectLayer(tridentGeometry)) {
            appendEffectLayer(renderState, displayContext,
                    this.effect.createArgument(this.effectQuads, this.baseQuads, level, owner, displayContext, stack),
                    this.effectExtents);
        }

        if (shouldRenderArc(displayContext)) {
            appendArcLayer(renderState, displayContext, level, seed);
        }
    }

    private ItemStack singularityIngredientPreview(ItemStack stack) {
        Singularity singularity = SingularityUtils.getSingularity(stack);
        if (singularity == null || !singularity.hasIngredient()) {
            return ItemStack.EMPTY;
        }

        return singularity.getIngredient().items()
                .findFirst()
                .map(LayeredEffectItemModel::stackFromHolder)
                .orElse(ItemStack.EMPTY);
    }

    private static ItemStack stackFromHolder(Holder<Item> holder) {
        return new ItemStack(holder);
    }

    private static boolean hasShiftDown() {
        var window = Minecraft.getInstance().getWindow();
        return InputConstants.isKeyDown(window, InputConstants.KEY_LSHIFT)
                || InputConstants.isKeyDown(window, InputConstants.KEY_RSHIFT);
    }

    private boolean shouldRenderTridentGeometry(ItemDisplayContext displayContext) {
        return this.cosmicArc
                && !this.tridentModels.isEmpty()
                && displayContext != ItemDisplayContext.GUI
                && displayContext != ItemDisplayContext.GROUND
                && displayContext != ItemDisplayContext.FIXED;
    }

    private boolean shouldRenderArc(ItemDisplayContext displayContext) {
        return this.cosmicArc
                && displayContext != ItemDisplayContext.GUI
                && displayContext != ItemDisplayContext.GROUND;
    }

    private boolean shouldRenderEffectLayer(boolean tridentGeometry) {
        return this.effect != null
                && !this.effectQuads.isEmpty()
                && !tridentGeometry;
    }

    private void appendHaloLayer(ItemStackRenderState renderState, ItemDisplayContext displayContext, HaloLayer halo) {
        ItemStackRenderState.LayerRenderState layer = renderState.newLayer();
        this.properties.applyToLayer(layer, displayContext);
        layer.setLocalTransform(this.transformation);
        layer.setExtents(halo::extents);
        layer.setupSpecialModel(AvaritiaItemModelRenderers.HALO, new AvaritiaItemModelRenderers.HaloLayerArgument(halo.texture(), halo.setting()));
        renderState.setAnimated();
    }

    private void appendPulseLayer(ItemStackRenderState renderState, ItemDisplayContext displayContext,
                                  @Nullable ClientLevel level, int seed) {
        if (this.baseQuads.isEmpty()) {
            return;
        }

        ItemStackRenderState.LayerRenderState layer = renderState.newLayer();
        this.properties.applyToLayer(layer, displayContext);
        layer.setLocalTransform(pulseTransform(level, seed));
        // 脉冲始终位于固定 halo 外包络内，不能让动画缩放参与 oversized GUI 的离屏纹理尺寸计算。
        // 同一模型在创造栏和 JEI 中会使用不同 seed；动态 extents 会让共享渲染器在同帧改尺寸并关闭待绘制纹理。
        layer.setupSpecialModel(AvaritiaItemModelRenderers.PULSE, new AvaritiaItemModelRenderers.PulseLayerArgument(this.baseQuads));
        renderState.setAnimated();
    }

    private Matrix4fc pulseTransform(@Nullable ClientLevel level, int seed) {
        float time = level != null ? level.getGameTime() + seed : seed;
        float scale = 0.95F + (Mth.sin(time * 0.25F) + 1.0F) * 0.075F;
        float translation = (1.0F - scale) * 0.5F;
        return new Matrix4f(this.transformation)
                .translate(translation, translation, 0.0F)
                .scale(scale, scale, 1.0001F);
    }

    private void appendTridentLayer(ItemStackRenderState renderState, ItemDisplayContext displayContext) {
        ItemStackRenderState.LayerRenderState layer = renderState.newLayer();
        this.properties.applyToLayer(layer, displayContext);
        layer.setLocalTransform(this.transformation);
        layer.setupSpecialModel(AvaritiaItemModelRenderers.TRIDENT, new AvaritiaItemModelRenderers.TridentLayerArgument(this.tridentModels, displayContext));
    }

    private void appendArcLayer(ItemStackRenderState renderState, ItemDisplayContext displayContext,
                                @Nullable ClientLevel level, int seed) {
        ItemStackRenderState.LayerRenderState layer = renderState.newLayer();
        this.properties.applyToLayer(layer, displayContext);
        layer.setLocalTransform(this.transformation);
        long time = (level != null ? level.getGameTime() : 0L) + seed;
        layer.setupSpecialModel(AvaritiaItemModelRenderers.ARC, new AvaritiaItemModelRenderers.ArcLayerArgument(time));
        renderState.setAnimated();
    }

    private void appendEffectLayer(ItemStackRenderState renderState, ItemDisplayContext displayContext,
                                   AvaritiaItemModelRenderers.EffectLayerArgument argument, Vector3fc[] extents) {
        ItemStackRenderState.LayerRenderState layer = renderState.newLayer();
        layer.setExtents(() -> extents);
        layer.setLocalTransform(this.transformation);
        layer.setupSpecialModel(AvaritiaItemModelRenderers.EFFECT, argument);
        this.properties.applyToLayer(layer, displayContext);
        renderState.setAnimated();
        // 超尺寸 GUI 物品按 model identity 复用离屏纹理。RenderType 每次提交都会新建，不能进入 identity，
        // 否则每帧都会分配并关闭新的离屏纹理；只保留真正影响缓存画面的稳定参数。
        renderState.appendModelIdentityElement(this.effect);
        renderState.appendModelIdentityElement(Float.floatToIntBits(argument.opacity()));
    }
}
