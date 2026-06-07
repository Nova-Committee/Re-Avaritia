package com.avaritia.client.model.loader.base;

import com.avaritia.Const;
import com.avaritia.api.client.model.ItemQuadBakery;
import com.avaritia.api.utils.RenderUtils;
import com.avaritia.client.render.mesh.SimpleMesh;
import com.avaritia.client.render.mesh.SimpleObjMeshLoader;
import com.avaritia.client.render.util.ArcRender;
import com.avaritia.client.shader.AvaritiaRenderTypeHelper;
import com.avaritia.client.shader.AvaritiaRenderTypes;
import com.avaritia.client.shader.AvaritiaShaderUniforms;
import com.avaritia.client.shader.AvaritiaShaders;
import com.avaritia.common.item.resources.MatterClusterItem;
import com.avaritia.common.item.singularity.SingularityItem;
import com.avaritia.core.singularity.Singularity;
import com.avaritia.util.SingularityUtils;
import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.QuadInstance;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import net.minecraft.client.Minecraft;
import net.minecraft.client.color.item.ItemTintSource;
import net.minecraft.client.color.item.ItemTintSources;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.item.CuboidItemModelWrapper;
import net.minecraft.client.renderer.item.ItemModel;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.item.ModelRenderProperties;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.special.SpecialModelRenderer;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.ModelBaker;
import net.minecraft.client.resources.model.ModelDebugName;
import net.minecraft.client.resources.model.ResolvableModel;
import net.minecraft.client.resources.model.ResolvedModel;
import net.minecraft.client.resources.model.geometry.BakedQuad;
import net.minecraft.client.resources.model.sprite.Material;
import net.minecraft.client.resources.model.sprite.MaterialBaker;
import net.minecraft.client.resources.model.sprite.TextureSlots;
import net.minecraft.core.Holder;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.ItemOwner;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.client.NeoForgeRenderTypes;
import org.joml.Matrix4f;
import org.joml.Matrix4fc;
import org.joml.Vector3fc;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

import static com.avaritia.client.shader.AvaritiaShaders.COSMIC_UVS;
import static com.avaritia.client.shader.AvaritiaShaders.ETERNAL_UVS;

public final class AvaritiaItemModels {
    private static final ModelDebugName DEBUG_NAME = () -> "AvaritiaItemModels";
    private static final AtomicInteger EFFECT_RENDER_TYPE_SEQUENCE = new AtomicInteger();
    private static final EffectSpecialRenderer EFFECT_RENDERER = new EffectSpecialRenderer();
    private static final HaloSpecialRenderer HALO_RENDERER = new HaloSpecialRenderer();
    private static final PulseSpecialRenderer PULSE_RENDERER = new PulseSpecialRenderer();
    private static final TridentSpecialRenderer TRIDENT_RENDERER = new TridentSpecialRenderer();
    private static final ArcSpecialRenderer ARC_RENDERER = new ArcSpecialRenderer();
    private static final int PULSE_ALPHA_COLOR = 0x99FFFFFF;
    private static final float ITEM_EFFECT_OVERLAY_OPACITY = 0.65F;

    private AvaritiaItemModels() {
    }

    public record Cosmic(Identifier model, List<Identifier> mask, List<ItemTintSource> tints) implements ItemModel.Unbaked, EffectFields {
        public static final MapCodec<Cosmic> MAP_CODEC = effectCodec(Cosmic::new);

        public Cosmic(Identifier model, List<Identifier> mask) {
            this(model, mask, List.of());
        }

        @Override
        public ItemModel bake(ItemModel.BakingContext context, Matrix4fc transformation) {
            return bakeEffect(context, transformation, this.model, this.tints, this.mask, Effect.COSMIC, Optional.empty(), true);
        }

        @Override
        public void resolveDependencies(ResolvableModel.Resolver resolver) {
            resolver.markDependency(this.model);
        }

        @Override
        public MapCodec<? extends ItemModel.Unbaked> type() {
            return MAP_CODEC;
        }
    }

    public record CosmicArc(Identifier model, List<Identifier> mask, List<ItemTintSource> tints) implements ItemModel.Unbaked, EffectFields {
        public static final MapCodec<CosmicArc> MAP_CODEC = effectCodec(CosmicArc::new);

        public CosmicArc(Identifier model, List<Identifier> mask) {
            this(model, mask, List.of());
        }

        @Override
        public ItemModel bake(ItemModel.BakingContext context, Matrix4fc transformation) {
            return bakeEffect(context, transformation, this.model, this.tints, this.mask, Effect.COSMIC, Optional.empty());
        }

        @Override
        public void resolveDependencies(ResolvableModel.Resolver resolver) {
            resolver.markDependency(this.model);
        }

        @Override
        public MapCodec<? extends ItemModel.Unbaked> type() {
            return MAP_CODEC;
        }
    }

    public record Hell(Identifier model, List<Identifier> mask, List<ItemTintSource> tints) implements ItemModel.Unbaked, EffectFields {
        public static final MapCodec<Hell> MAP_CODEC = effectCodec(Hell::new);

        public Hell(Identifier model, List<Identifier> mask) {
            this(model, mask, List.of());
        }

        @Override
        public ItemModel bake(ItemModel.BakingContext context, Matrix4fc transformation) {
            return bakeEffect(context, transformation, this.model, this.tints, this.mask, Effect.HELL, Optional.empty());
        }

        @Override
        public void resolveDependencies(ResolvableModel.Resolver resolver) {
            resolver.markDependency(this.model);
        }

        @Override
        public MapCodec<? extends ItemModel.Unbaked> type() {
            return MAP_CODEC;
        }
    }

    public record Eternal(Identifier model, List<Identifier> mask, List<ItemTintSource> tints) implements ItemModel.Unbaked, EffectFields {
        public static final MapCodec<Eternal> MAP_CODEC = effectCodec(Eternal::new);

        public Eternal(Identifier model, List<Identifier> mask) {
            this(model, mask, List.of());
        }

        @Override
        public ItemModel bake(ItemModel.BakingContext context, Matrix4fc transformation) {
            return bakeEffect(context, transformation, this.model, this.tints, this.mask, Effect.ETERNAL, Optional.empty());
        }

        @Override
        public void resolveDependencies(ResolvableModel.Resolver resolver) {
            resolver.markDependency(this.model);
        }

        @Override
        public MapCodec<? extends ItemModel.Unbaked> type() {
            return MAP_CODEC;
        }
    }

    public record Unstable(Identifier model, List<Identifier> mask, List<ItemTintSource> tints) implements ItemModel.Unbaked, EffectFields {
        public static final MapCodec<Unstable> MAP_CODEC = effectCodec(Unstable::new);

        public Unstable(Identifier model, List<Identifier> mask) {
            this(model, mask, List.of());
        }

        @Override
        public ItemModel bake(ItemModel.BakingContext context, Matrix4fc transformation) {
            return bakeEffect(context, transformation, this.model, this.tints, this.mask, Effect.UNSTABLE, Optional.empty());
        }

        @Override
        public void resolveDependencies(ResolvableModel.Resolver resolver) {
            resolver.markDependency(this.model);
        }

        @Override
        public MapCodec<? extends ItemModel.Unbaked> type() {
            return MAP_CODEC;
        }
    }

    public record Halo(Identifier model, Identifier texture, int color, int size, boolean pulse, List<ItemTintSource> tints) implements ItemModel.Unbaked, HaloFields {
        public static final MapCodec<Halo> MAP_CODEC = haloCodec(Halo::new);

        public Halo(Identifier model, Identifier texture, int color, int size, boolean pulse) {
            this(model, texture, color, size, pulse, List.of());
        }

        @Override
        public ItemModel bake(ItemModel.BakingContext context, Matrix4fc transformation) {
            return bakeEffect(context, transformation, this.model, this.tints, List.of(), null, Optional.of(toHaloLayer(context, this)));
        }

        @Override
        public void resolveDependencies(ResolvableModel.Resolver resolver) {
            resolver.markDependency(this.model);
        }

        @Override
        public MapCodec<? extends ItemModel.Unbaked> type() {
            return MAP_CODEC;
        }
    }

    public record HaloCosmic(Identifier model, List<Identifier> mask, Identifier texture, int color, int size, boolean pulse, List<ItemTintSource> tints) implements ItemModel.Unbaked, HaloEffectFields {
        public static final MapCodec<HaloCosmic> MAP_CODEC = haloEffectCodec(HaloCosmic::new);

        public HaloCosmic(Identifier model, List<Identifier> mask, Identifier texture, int color, int size, boolean pulse) {
            this(model, mask, texture, color, size, pulse, List.of());
        }

        @Override
        public ItemModel bake(ItemModel.BakingContext context, Matrix4fc transformation) {
            return bakeEffect(context, transformation, this.model, this.tints, this.mask, Effect.COSMIC, Optional.of(toHaloLayer(context, this)));
        }

        @Override
        public void resolveDependencies(ResolvableModel.Resolver resolver) {
            resolver.markDependency(this.model);
        }

        @Override
        public MapCodec<? extends ItemModel.Unbaked> type() {
            return MAP_CODEC;
        }
    }

    public record HaloEternal(Identifier model, List<Identifier> mask, Identifier texture, int color, int size, boolean pulse, List<ItemTintSource> tints) implements ItemModel.Unbaked, HaloEffectFields {
        public static final MapCodec<HaloEternal> MAP_CODEC = haloEffectCodec(HaloEternal::new);

        public HaloEternal(Identifier model, List<Identifier> mask, Identifier texture, int color, int size, boolean pulse) {
            this(model, mask, texture, color, size, pulse, List.of());
        }

        @Override
        public ItemModel bake(ItemModel.BakingContext context, Matrix4fc transformation) {
            return bakeEffect(context, transformation, this.model, this.tints, this.mask, Effect.ETERNAL, Optional.of(toHaloLayer(context, this)));
        }

        @Override
        public void resolveDependencies(ResolvableModel.Resolver resolver) {
            resolver.markDependency(this.model);
        }

        @Override
        public MapCodec<? extends ItemModel.Unbaked> type() {
            return MAP_CODEC;
        }
    }

    private static <T extends ItemModel.Unbaked & EffectFields> MapCodec<T> effectCodec(EffectFactory<T> factory) {
        return RecordCodecBuilder.mapCodec(instance -> instance.group(
                Identifier.CODEC.fieldOf("model").forGetter((T model) -> model.model()),
                Identifier.CODEC.listOf().fieldOf("mask").forGetter((T model) -> model.mask()),
                ItemTintSources.CODEC.listOf().optionalFieldOf("tints", List.of()).forGetter((T model) -> model.tints())
        ).apply(instance, factory::create));
    }

    private static <T extends ItemModel.Unbaked & HaloFields> MapCodec<T> haloCodec(HaloFactory<T> factory) {
        return RecordCodecBuilder.mapCodec(instance -> instance.group(
                Identifier.CODEC.fieldOf("model").forGetter((T model) -> model.model()),
                Identifier.CODEC.fieldOf("texture").forGetter((T model) -> model.texture()),
                Codec.INT.optionalFieldOf("color", -16777216).forGetter((T model) -> model.color()),
                Codec.INT.optionalFieldOf("size", 10).forGetter((T model) -> model.size()),
                Codec.BOOL.optionalFieldOf("pulse", true).forGetter((T model) -> model.pulse()),
                ItemTintSources.CODEC.listOf().optionalFieldOf("tints", List.of()).forGetter((T model) -> model.tints())
        ).apply(instance, factory::create));
    }

    private static <T extends ItemModel.Unbaked & HaloEffectFields> MapCodec<T> haloEffectCodec(HaloEffectFactory<T> factory) {
        return RecordCodecBuilder.mapCodec(instance -> instance.group(
                Identifier.CODEC.fieldOf("model").forGetter((T model) -> model.model()),
                Identifier.CODEC.listOf().fieldOf("mask").forGetter((T model) -> model.mask()),
                Identifier.CODEC.fieldOf("texture").forGetter((T model) -> model.texture()),
                Codec.INT.optionalFieldOf("color", -16777216).forGetter((T model) -> model.color()),
                Codec.INT.optionalFieldOf("size", 10).forGetter((T model) -> model.size()),
                Codec.BOOL.optionalFieldOf("pulse", false).forGetter((T model) -> model.pulse()),
                ItemTintSources.CODEC.listOf().optionalFieldOf("tints", List.of()).forGetter((T model) -> model.tints())
        ).apply(instance, factory::create));
    }

    private static ItemModel bakeEffect(ItemModel.BakingContext context, Matrix4fc transformation, Identifier model,
                                        List<ItemTintSource> tints, List<Identifier> masks, @Nullable Effect effect, Optional<HaloLayer> haloLayer) {
        return bakeEffect(context, transformation, model, tints, masks, effect, haloLayer, false);
    }

    private static ItemModel bakeEffect(ItemModel.BakingContext context, Matrix4fc transformation, Identifier model,
                                        List<ItemTintSource> tints, List<Identifier> masks, @Nullable Effect effect,
                                        Optional<HaloLayer> haloLayer, boolean cosmicArc) {
        ModelBaker baker = context.blockModelBaker();
        ResolvedModel resolvedModel = baker.getModel(model);
        TextureSlots textureSlots = resolvedModel.getTopTextureSlots();
        ItemModel wrapped = new CuboidItemModelWrapper.Unbaked(model, Optional.empty(), tints).bake(context, transformation);
        ModelRenderProperties properties = ModelRenderProperties.fromResolvedModel(baker, resolvedModel, textureSlots);
        List<BakedQuad> baseQuads = bakeBaseQuads(baker, textureSlots);
        List<BakedQuad> effectQuads = effect == null ? List.of() : bakeEffectQuads(baker, effect, masks);
        Map<String, SimpleMesh> tridentModels = cosmicArc ? loadTridentModels() : Map.of();
        return new LayeredEffectItemModel(wrapped, properties, transformation, effect, effectQuads, baseQuads, haloLayer, cosmicArc, tridentModels);
    }

    private static List<BakedQuad> bakeEffectQuads(ModelBaker baker, Effect effect, List<Identifier> masks) {
        MaterialBaker materials = baker.materials();
        List<TextureAtlasSprite> sprites = new ArrayList<>(masks.size());
        for (Identifier mask : masks) {
            sprites.add(materials.get(new Material(mask), DEBUG_NAME).sprite());
        }
        return ItemQuadBakery.bakeGeneratedItem(baker, effect.renderType(), sprites.toArray(TextureAtlasSprite[]::new));
    }

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

    private static HaloLayer toHaloLayer(ItemModel.BakingContext context, HaloFields halo) {
        TextureAtlasSprite sprite = context.blockModelBaker().materials().get(new Material(halo.texture()), DEBUG_NAME).sprite();
        HaloSetting setting = new HaloSetting(new IntArrayList(), halo.texture().toString(), halo.color(), halo.size(), halo.pulse());
        return new HaloLayer(sprite, setting);
    }

    private static Map<String, SimpleMesh> loadTridentModels() {
        try {
            return SimpleObjMeshLoader.load(Const.rl("models/infinity_trident.obj"), true);
        } catch (Exception exception) {
            Const.LOGGER.warn("Failed to load infinity trident item model OBJ; falling back to flat item model.", exception);
            return Map.of();
        }
    }

    private static final class LayeredEffectItemModel implements ItemModel {
        private final ItemModel wrapped;
        private final ModelRenderProperties properties;
        private final Matrix4fc transformation;
        private final @Nullable Effect effect;
        private final List<BakedQuad> effectQuads;
        private final List<BakedQuad> baseQuads;
        private final Optional<HaloLayer> haloLayer;
        private final Vector3fc[] effectExtents;
        private final Vector3fc[] baseExtents;
        private final boolean cosmicArc;
        private final Map<String, SimpleMesh> tridentModels;

        private LayeredEffectItemModel(ItemModel wrapped, ModelRenderProperties properties, Matrix4fc transformation,
                                       @Nullable Effect effect, List<BakedQuad> effectQuads, List<BakedQuad> baseQuads,
                                       Optional<HaloLayer> haloLayer, boolean cosmicArc, Map<String, SimpleMesh> tridentModels) {
            this.wrapped = wrapped;
            this.properties = properties;
            this.transformation = transformation;
            this.effect = effect;
            this.effectQuads = effectQuads;
            this.baseQuads = baseQuads;
            this.haloLayer = haloLayer;
            this.effectExtents = CuboidItemModelWrapper.computeExtents(effectQuads);
            this.baseExtents = CuboidItemModelWrapper.computeExtents(baseQuads);
            this.cosmicArc = cosmicArc;
            this.tridentModels = tridentModels;
        }

        @Override
        public void update(ItemStackRenderState renderState, ItemStack stack, ItemModelResolver resolver,
                           ItemDisplayContext displayContext, @Nullable ClientLevel level, @Nullable ItemOwner owner, int seed) {
            if (displayContext == ItemDisplayContext.GUI && stack.getItem() instanceof SingularityItem && hasShiftDown()) {
                ItemStack ingredientPreview = singularityIngredientPreview(stack);
                if (!ingredientPreview.isEmpty()) {
                    resolver.updateForTopItem(renderState, ingredientPreview, displayContext, level, owner, seed);
                    return;
                }
            }

            if (shouldRenderTridentGeometry(displayContext)) {
                appendTridentLayer(renderState, displayContext);
            } else {
                this.wrapped.update(renderState, stack, resolver, displayContext, level, owner, seed);
            }

            if (displayContext == ItemDisplayContext.GUI) {
                this.haloLayer.ifPresent(halo -> {
                    appendHaloLayer(renderState, displayContext, halo);
                    if (halo.setting().pulse()) {
                        appendPulseLayer(renderState, displayContext, level, seed);
                    }
                });
            }

            if (this.effect != null && !this.effectQuads.isEmpty()) {
                appendEffectLayer(renderState, displayContext, this.effect.createArgument(this.effectQuads, level, owner, displayContext, stack), this.effectExtents);
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

        private void appendHaloLayer(ItemStackRenderState renderState, ItemDisplayContext displayContext, HaloLayer halo) {
            ItemStackRenderState.LayerRenderState layer = renderState.newLayer();
            this.properties.applyToLayer(layer, displayContext);
            layer.setLocalTransform(this.transformation);
            layer.setExtents(halo::extents);
            layer.setupSpecialModel(HALO_RENDERER, new HaloLayerArgument(halo.sprite(), halo.setting()));
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
            layer.setExtents(() -> this.baseExtents);
            layer.setupSpecialModel(PULSE_RENDERER, new PulseLayerArgument(this.baseQuads));
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
            layer.setupSpecialModel(TRIDENT_RENDERER, new TridentLayerArgument(this.tridentModels, displayContext));
        }

        private void appendArcLayer(ItemStackRenderState renderState, ItemDisplayContext displayContext,
                                    @Nullable ClientLevel level, int seed) {
            ItemStackRenderState.LayerRenderState layer = renderState.newLayer();
            this.properties.applyToLayer(layer, displayContext);
            layer.setLocalTransform(this.transformation);
            long time = (level != null ? level.getGameTime() : 0L) + seed;
            layer.setupSpecialModel(ARC_RENDERER, new ArcLayerArgument(time));
            renderState.setAnimated();
        }

        private void appendEffectLayer(ItemStackRenderState renderState, ItemDisplayContext displayContext,
                                       EffectLayerArgument argument, Vector3fc[] extents) {
            ItemStackRenderState.LayerRenderState layer = renderState.newLayer();
            layer.setExtents(() -> extents);
            layer.setLocalTransform(this.transformation);
            layer.setupSpecialModel(EFFECT_RENDERER, argument);
            this.properties.applyToLayer(layer, displayContext);
            renderState.setAnimated();
            renderState.appendModelIdentityElement(argument);
        }
    }

    private record HaloLayer(TextureAtlasSprite sprite, HaloSetting setting, Vector3fc[] extents) {
        private HaloLayer(TextureAtlasSprite sprite, HaloSetting setting) {
            this(sprite, setting, CuboidItemModelWrapper.computeExtents(
                    HaloUtils.generateHaloQuads(sprite, setting.size(), setting.color())));
        }
    }

    private record EffectLayerArgument(List<BakedQuad> quads, RenderType renderType, AvaritiaShaderUniforms.Effect effect,
                                       float time, float yaw, float pitch, float scale, float opacity, float[] uvs) {
        private void applyUniforms() {
            AvaritiaShaderUniforms.set(this.renderType, this.effect, this.time, this.yaw, this.pitch, this.scale, this.opacity, this.uvs);
        }
    }

    private record HaloLayerArgument(TextureAtlasSprite sprite, HaloSetting setting) {
    }

    private record PulseLayerArgument(List<BakedQuad> quads) {
    }

    private record TridentLayerArgument(Map<String, SimpleMesh> models, ItemDisplayContext displayContext) {
    }

    private record ArcLayerArgument(long time) {
    }

    private static final class HaloSpecialRenderer implements SpecialModelRenderer<HaloLayerArgument> {
        @Override
        public void submit(@Nullable HaloLayerArgument argument, PoseStack poseStack, SubmitNodeCollector submitNodeCollector,
                           int lightCoords, int overlayCoords, boolean hasFoil, int outlineColor) {
            if (argument == null) {
                return;
            }

            submitNodeCollector.submitCustomGeometry(poseStack, NeoForgeRenderTypes.BLOCK_ITEM_LAYERED_TRANSLUCENT.get(), (pose, buffer) -> {
                renderCircularHalo(pose, buffer, argument.sprite(), argument.setting(), lightCoords, overlayCoords);
            });
        }

        private static void renderCircularHalo(PoseStack.Pose pose, VertexConsumer buffer, TextureAtlasSprite sprite,
                                               HaloSetting setting, int lightCoords, int overlayCoords) {
            int segments = HaloUtils.circleSegments(setting.size());
            double radius = HaloUtils.haloRadius(setting.size());
            double min = 0.5 - radius;
            double diameter = radius * 2.0;
            int color = setting.color();
            int alpha = color >>> 24 & 255;
            int red = color >> 16 & 255;
            int green = color >> 8 & 255;
            int blue = color & 255;

            for (int i = 0; i < segments; i++) {
                double current = Math.PI * 2.0 * i / segments;
                double next = Math.PI * 2.0 * (i + 1) / segments;
                double currentX = 0.5 + Math.cos(current) * radius;
                double currentY = 0.5 + Math.sin(current) * radius;
                double nextX = 0.5 + Math.cos(next) * radius;
                double nextY = 0.5 + Math.sin(next) * radius;

                putHaloVertex(pose, buffer, sprite, 0.5, 0.5, min, diameter, red, green, blue, alpha, lightCoords, overlayCoords);
                putHaloVertex(pose, buffer, sprite, currentX, currentY, min, diameter, red, green, blue, alpha, lightCoords, overlayCoords);
                putHaloVertex(pose, buffer, sprite, nextX, nextY, min, diameter, red, green, blue, alpha, lightCoords, overlayCoords);
                putHaloVertex(pose, buffer, sprite, 0.5, 0.5, min, diameter, red, green, blue, alpha, lightCoords, overlayCoords);
            }
        }

        private static void putHaloVertex(PoseStack.Pose pose, VertexConsumer buffer, TextureAtlasSprite sprite,
                                          double x, double y, double min, double diameter,
                                          int red, int green, int blue, int alpha, int lightCoords, int overlayCoords) {
            double u = (x - min) / diameter;
            double v = (y - min) / diameter;
            float atlasU = sprite.getU0() + (float) u * (sprite.getU1() - sprite.getU0());
            float atlasV = sprite.getV1() - (float) v * (sprite.getV1() - sprite.getV0());
            buffer.addVertex(pose, (float) x, (float) y, 0.0F)
                    .setColor(red, green, blue, alpha)
                    .setUv(atlasU, atlasV)
                    .setOverlay(overlayCoords)
                    .setLight(lightCoords)
                    .setNormal(pose, 0.0F, 0.0F, 1.0F);
        }

        @Override
        public void getExtents(Consumer<Vector3fc> output) {
        }

        @Override
        public @Nullable HaloLayerArgument extractArgument(ItemStack stack) {
            return null;
        }
    }

    private static final class PulseSpecialRenderer implements SpecialModelRenderer<PulseLayerArgument> {
        @Override
        public void submit(@Nullable PulseLayerArgument argument, PoseStack poseStack, SubmitNodeCollector submitNodeCollector,
                           int lightCoords, int overlayCoords, boolean hasFoil, int outlineColor) {
            if (argument == null || argument.quads().isEmpty()) {
                return;
            }

            submitNodeCollector.submitCustomGeometry(poseStack, NeoForgeRenderTypes.BLOCK_ITEM_LAYERED_TRANSLUCENT.get(), (pose, buffer) -> {
                QuadInstance instance = new QuadInstance();
                instance.setColor(PULSE_ALPHA_COLOR);
                instance.setLightCoords(lightCoords);
                instance.setOverlayCoords(overlayCoords);

                for (BakedQuad quad : argument.quads()) {
                    buffer.putBakedQuad(pose, quad, instance);
                }
            });
        }

        @Override
        public void getExtents(Consumer<Vector3fc> output) {
        }

        @Override
        public @Nullable PulseLayerArgument extractArgument(ItemStack stack) {
            return null;
        }
    }

    private static final class TridentSpecialRenderer implements SpecialModelRenderer<TridentLayerArgument> {
        @Override
        public void submit(@Nullable TridentLayerArgument argument, PoseStack poseStack, SubmitNodeCollector submitNodeCollector,
                           int lightCoords, int overlayCoords, boolean hasFoil, int outlineColor) {
            if (argument == null || argument.models().isEmpty()) {
                return;
            }

            poseStack.pushPose();
            try {
                transformTrident(argument.displayContext(), poseStack);
                submitNodeCollector.submitCustomGeometry(poseStack, AvaritiaRenderTypes.TRIDENT,
                        (poseState, vertexConsumer) -> argument.models().values().forEach(model ->
                                model.render(poseState, vertexConsumer, 0xFFFFFFFF, lightCoords, overlayCoords)));
            } finally {
                poseStack.popPose();
            }
        }

        private static void transformTrident(ItemDisplayContext displayContext, PoseStack poseStack) {
            switch (displayContext) {
                case FIRST_PERSON_LEFT_HAND, FIRST_PERSON_RIGHT_HAND -> {
                    poseStack.mulPose(Axis.XP.rotationDegrees(90.0F));
                    poseStack.mulPose(Axis.YP.rotationDegrees(180.0F));
                    poseStack.translate(0.2D, -0.2D, -1.3D);
                }
                case THIRD_PERSON_LEFT_HAND, THIRD_PERSON_RIGHT_HAND -> {
                    poseStack.mulPose(Axis.XP.rotationDegrees(90.0F));
                    poseStack.mulPose(Axis.YP.rotationDegrees(180.0F));
                    poseStack.translate(0.0D, 0.0D, -1.5D);
                }
                default -> poseStack.mulPose(Axis.XP.rotationDegrees(90.0F));
            }
        }

        @Override
        public void getExtents(Consumer<Vector3fc> output) {
        }

        @Override
        public @Nullable TridentLayerArgument extractArgument(ItemStack stack) {
            return null;
        }
    }

    private static final class ArcSpecialRenderer implements SpecialModelRenderer<ArcLayerArgument> {
        @Override
        public void submit(@Nullable ArcLayerArgument argument, PoseStack poseStack, SubmitNodeCollector submitNodeCollector,
                           int lightCoords, int overlayCoords, boolean hasFoil, int outlineColor) {
            if (argument == null) {
                return;
            }

            poseStack.pushPose();
            try {
                poseStack.mulPose(Axis.YP.rotationDegrees((argument.time() * 8L) % 360L));
                submitNodeCollector.submitCustomGeometry(poseStack, ArcRender.ARC_RENDER_TYPE, (pose, buffer) ->
                        ArcRender.renderArc(pose, buffer, argument.time(),
                                -0.5F, 0.0F, -0.5F,
                                0.5F, 0.0F, 0.5F,
                                0.05F, 8));
            } finally {
                poseStack.popPose();
            }
        }

        @Override
        public void getExtents(Consumer<Vector3fc> output) {
        }

        @Override
        public @Nullable ArcLayerArgument extractArgument(ItemStack stack) {
            return null;
        }
    }

    private static final class EffectSpecialRenderer implements SpecialModelRenderer<EffectLayerArgument> {
        @Override
        public void submit(@Nullable EffectLayerArgument argument, PoseStack poseStack, SubmitNodeCollector submitNodeCollector,
                           int lightCoords, int overlayCoords, boolean hasFoil, int outlineColor) {
            if (argument == null || argument.quads().isEmpty()) {
                return;
            }

            argument.applyUniforms();
            submitNodeCollector.submitCustomGeometry(poseStack, argument.renderType(), (pose, buffer) -> {
                QuadInstance instance = new QuadInstance();
                instance.setColor(-1);
                instance.setLightCoords(lightCoords);
                instance.setOverlayCoords(overlayCoords);

                for (BakedQuad quad : argument.quads()) {
                    buffer.putBakedQuad(pose, quad, instance);
                }
            });
        }

        @Override
        public void getExtents(Consumer<Vector3fc> output) {
        }

        @Override
        public @Nullable EffectLayerArgument extractArgument(ItemStack stack) {
            return null;
        }
    }

    private enum Effect {
        COSMIC,
        HELL,
        ETERNAL,
        UNSTABLE;

        private RenderType renderType() {
            return switch (this) {
                case COSMIC -> AvaritiaRenderTypes.COSMIC;
                case HELL -> AvaritiaRenderTypes.HELL;
                case ETERNAL -> AvaritiaRenderTypes.ETERNAL;
                case UNSTABLE -> AvaritiaRenderTypes.UNSTABLE;
            };
        }

        private EffectLayerArgument createArgument(List<BakedQuad> quads, @Nullable ClientLevel level, @Nullable ItemOwner owner,
                                                   ItemDisplayContext displayContext, ItemStack stack) {
            long time = level != null ? level.getGameTime() : 0L;
            LivingEntity entity = owner != null ? owner.asLivingEntity() : null;
            float yaw = entity != null && displayContext != ItemDisplayContext.GUI ? (float) (entity.getYRot() * 2.0F * Math.PI / 360.0F) : 0.0F;
            float pitch = entity != null && displayContext != ItemDisplayContext.GUI ? -(float) (entity.getXRot() * 2.0F * Math.PI / 360.0F) : 0.0F;
            float scale = displayContext == ItemDisplayContext.GUI ? 100.0F : 1.0F;
            return new EffectLayerArgument(quads, this.newRenderType(), this.uniformEffect(), time % Integer.MAX_VALUE,
                    yaw, pitch, scale, this.opacity(stack) * ITEM_EFFECT_OVERLAY_OPACITY, this.uvs());
        }

        private RenderType newRenderType() {
            RenderPipeline pipeline = this.pipeline();
            if (pipeline == null) {
                return this.renderType();
            }
            String name = "avaritia_" + this.name().toLowerCase(Locale.ROOT) + "_item_" + EFFECT_RENDER_TYPE_SEQUENCE.incrementAndGet();
            return AvaritiaRenderTypeHelper.textured(name, pipeline, RenderUtils.COSMIC_TEXTURE_ISOLATED, true, true, true, true);
        }

        private @Nullable RenderPipeline pipeline() {
            return switch (this) {
                case COSMIC -> AvaritiaShaders.COSMIC_SHADER;
                case HELL -> AvaritiaShaders.HELL_SHADER;
                case ETERNAL -> AvaritiaShaders.ETERNAL_SHADER;
                case UNSTABLE -> AvaritiaShaders.UNSTABLE_SHADER;
            };
        }

        private AvaritiaShaderUniforms.Effect uniformEffect() {
            return switch (this) {
                case COSMIC -> AvaritiaShaderUniforms.Effect.COSMIC;
                case HELL -> AvaritiaShaderUniforms.Effect.HELL;
                case ETERNAL -> AvaritiaShaderUniforms.Effect.ETERNAL;
                case UNSTABLE -> AvaritiaShaderUniforms.Effect.UNSTABLE;
            };
        }

        private float[] uvs() {
            return switch (this) {
                case COSMIC, HELL -> COSMIC_UVS;
                case ETERNAL, UNSTABLE -> ETERNAL_UVS;
            };
        }

        private float opacity(ItemStack stack) {
            if (stack.getItem() instanceof MatterClusterItem) {
                return MatterClusterItem.getClusterSize(MatterClusterItem.getClusterItems(stack)) / (float) MatterClusterItem.CAPACITY;
            }
            return this == UNSTABLE ? 1.5F : 1.0F;
        }
    }

    private interface EffectFactory<T> {
        T create(Identifier model, List<Identifier> mask, List<ItemTintSource> tints);
    }

    private interface HaloFactory<T> {
        T create(Identifier model, Identifier texture, int color, int size, boolean pulse, List<ItemTintSource> tints);
    }

    private interface HaloEffectFactory<T> {
        T create(Identifier model, List<Identifier> mask, Identifier texture, int color, int size, boolean pulse, List<ItemTintSource> tints);
    }

    private interface EffectFields {
        Identifier model();

        List<Identifier> mask();

        List<ItemTintSource> tints();
    }

    private interface HaloFields {
        Identifier model();

        Identifier texture();

        int color();

        int size();

        boolean pulse();

        List<ItemTintSource> tints();
    }

    private interface HaloEffectFields extends EffectFields, HaloFields {
    }
}
