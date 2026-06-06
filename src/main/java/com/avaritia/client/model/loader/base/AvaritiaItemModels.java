package com.avaritia.client.model.loader.base;

import com.avaritia.api.client.model.ItemQuadBakery;
import com.avaritia.client.shader.AvaritiaRenderTypes;
import com.avaritia.client.shader.AvaritiaShaders;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import net.minecraft.client.color.item.ItemTintSource;
import net.minecraft.client.color.item.ItemTintSources;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.item.CuboidItemModelWrapper;
import net.minecraft.client.renderer.item.ItemModel;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.item.ModelRenderProperties;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.ModelBaker;
import net.minecraft.client.resources.model.ModelDebugName;
import net.minecraft.client.resources.model.ResolvableModel;
import net.minecraft.client.resources.model.ResolvedModel;
import net.minecraft.client.resources.model.geometry.BakedQuad;
import net.minecraft.client.resources.model.sprite.Material;
import net.minecraft.client.resources.model.sprite.MaterialBaker;
import net.minecraft.client.resources.model.sprite.TextureSlots;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.ItemOwner;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.joml.Matrix4fc;
import org.joml.Vector3fc;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static com.avaritia.client.shader.AvaritiaShaders.COSMIC_UVS;
import static com.avaritia.client.shader.AvaritiaShaders.ETERNAL_UVS;

public final class AvaritiaItemModels {
    private static final ModelDebugName DEBUG_NAME = () -> "AvaritiaItemModels";

    private AvaritiaItemModels() {
    }

    public record Cosmic(Identifier model, List<Identifier> mask, List<ItemTintSource> tints) implements ItemModel.Unbaked, EffectFields {
        public static final MapCodec<Cosmic> MAP_CODEC = effectCodec(Cosmic::new);

        public Cosmic(Identifier model, List<Identifier> mask) {
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
        ModelBaker baker = context.blockModelBaker();
        ResolvedModel resolvedModel = baker.getModel(model);
        TextureSlots textureSlots = resolvedModel.getTopTextureSlots();
        ItemModel wrapped = new CuboidItemModelWrapper.Unbaked(model, Optional.empty(), tints).bake(context, transformation);
        ModelRenderProperties properties = ModelRenderProperties.fromResolvedModel(baker, resolvedModel, textureSlots);
        List<BakedQuad> effectQuads = effect == null ? List.of() : bakeEffectQuads(baker, effect, masks);
        return new LayeredEffectItemModel(wrapped, properties, transformation, effect, effectQuads, haloLayer);
    }

    private static List<BakedQuad> bakeEffectQuads(ModelBaker baker, Effect effect, List<Identifier> masks) {
        MaterialBaker materials = baker.materials();
        List<TextureAtlasSprite> sprites = new ArrayList<>(masks.size());
        for (Identifier mask : masks) {
            sprites.add(materials.get(new Material(mask), DEBUG_NAME).sprite());
        }
        return ItemQuadBakery.bakeItem(effect.renderType(), sprites.toArray(TextureAtlasSprite[]::new));
    }

    private static HaloLayer toHaloLayer(ItemModel.BakingContext context, HaloFields halo) {
        TextureAtlasSprite sprite = context.blockModelBaker().materials().get(new Material(halo.texture()), DEBUG_NAME).sprite();
        HaloSetting setting = new HaloSetting(new IntArrayList(), halo.texture().toString(), halo.color(), halo.size(), halo.pulse());
        return new HaloLayer(List.of(HaloUtils.generateHaloQuad(sprite, setting.size(), setting.color())), setting);
    }

    private static final class LayeredEffectItemModel implements ItemModel {
        private final ItemModel wrapped;
        private final ModelRenderProperties properties;
        private final Matrix4fc transformation;
        private final @Nullable Effect effect;
        private final List<BakedQuad> effectQuads;
        private final Optional<HaloLayer> haloLayer;
        private final Vector3fc[] effectExtents;

        private LayeredEffectItemModel(ItemModel wrapped, ModelRenderProperties properties, Matrix4fc transformation,
                                       @Nullable Effect effect, List<BakedQuad> effectQuads, Optional<HaloLayer> haloLayer) {
            this.wrapped = wrapped;
            this.properties = properties;
            this.transformation = transformation;
            this.effect = effect;
            this.effectQuads = effectQuads;
            this.haloLayer = haloLayer;
            this.effectExtents = CuboidItemModelWrapper.computeExtents(effectQuads);
        }

        @Override
        public void update(ItemStackRenderState renderState, ItemStack stack, ItemModelResolver resolver,
                           ItemDisplayContext displayContext, @Nullable ClientLevel level, @Nullable ItemOwner owner, int seed) {
            if (displayContext == ItemDisplayContext.GUI) {
                this.haloLayer.ifPresent(halo -> appendLayer(renderState, displayContext, halo.quads(), halo.extents()));
            }

            this.wrapped.update(renderState, stack, resolver, displayContext, level, owner, seed);

            if (this.effect != null && !this.effectQuads.isEmpty()) {
                this.effect.updateUniforms(level, owner, displayContext);
                appendLayer(renderState, displayContext, this.effectQuads, this.effectExtents);
            }
        }

        private void appendLayer(ItemStackRenderState renderState, ItemDisplayContext displayContext, List<BakedQuad> quads, Vector3fc[] extents) {
            ItemStackRenderState.LayerRenderState layer = renderState.newLayer();
            this.properties.applyToLayer(layer, displayContext);
            layer.setLocalTransform(this.transformation);
            layer.setExtents(() -> extents);
            layer.prepareQuadList().addAll(quads);
            renderState.setAnimated();
        }
    }

    private record HaloLayer(List<BakedQuad> quads, HaloSetting setting, Vector3fc[] extents) {
        private HaloLayer(List<BakedQuad> quads, HaloSetting setting) {
            this(quads, setting, CuboidItemModelWrapper.computeExtents(quads));
        }
    }

    private enum Effect {
        COSMIC(AvaritiaRenderTypes.COSMIC),
        HELL(AvaritiaRenderTypes.HELL),
        ETERNAL(AvaritiaRenderTypes.ETERNAL),
        UNSTABLE(AvaritiaRenderTypes.UNSTABLE);

        private final RenderType renderType;

        Effect(RenderType renderType) {
            this.renderType = renderType;
        }

        private RenderType renderType() {
            return this.renderType;
        }

        private void updateUniforms(@Nullable ClientLevel level, @Nullable ItemOwner owner, ItemDisplayContext displayContext) {
            long time = level != null ? level.getGameTime() : 0L;
            LivingEntity entity = owner != null ? owner.asLivingEntity() : null;
            float yaw = entity != null && displayContext != ItemDisplayContext.GUI ? (float) (entity.getYRot() * 2.0F * Math.PI / 360.0F) : 0.0F;
            float pitch = entity != null && displayContext != ItemDisplayContext.GUI ? -(float) (entity.getXRot() * 2.0F * Math.PI / 360.0F) : 0.0F;
            float scale = displayContext == ItemDisplayContext.GUI ? 100.0F : 1.0F;

            switch (this) {
                case COSMIC -> {
                    AvaritiaShaders.cosmicTime.set(time % Integer.MAX_VALUE);
                    AvaritiaShaders.cosmicYaw.set(yaw);
                    AvaritiaShaders.cosmicPitch.set(pitch);
                    AvaritiaShaders.cosmicExternalScale.set(scale);
                    AvaritiaShaders.cosmicOpacity.set(1.0F);
                    if (AvaritiaShaders.cosmicUVs != null) {
                        AvaritiaShaders.cosmicUVs.set(COSMIC_UVS);
                    }
                }
                case HELL -> {
                    AvaritiaShaders.hellTime.set(time % Integer.MAX_VALUE);
                    AvaritiaShaders.hellYaw.set(yaw);
                    AvaritiaShaders.hellPitch.set(pitch);
                    AvaritiaShaders.hellExternalScale.set(scale);
                    AvaritiaShaders.hellOpacity.set(1.0F);
                    if (AvaritiaShaders.hellUVs != null) {
                        AvaritiaShaders.hellUVs.set(COSMIC_UVS);
                    }
                }
                case ETERNAL -> {
                    AvaritiaShaders.eternalTime.set(time % Integer.MAX_VALUE);
                    AvaritiaShaders.eternalYaw.set(yaw);
                    AvaritiaShaders.eternalPitch.set(pitch);
                    AvaritiaShaders.eternalExternalScale.set(scale);
                    AvaritiaShaders.eternalOpacity.set(1.0F);
                    if (AvaritiaShaders.eternalUVs != null) {
                        AvaritiaShaders.eternalUVs.set(ETERNAL_UVS);
                    }
                }
                case UNSTABLE -> {
                    AvaritiaShaders.unstableTime.set(time % Integer.MAX_VALUE);
                    AvaritiaShaders.unstableYaw.set(yaw);
                    AvaritiaShaders.unstablePitch.set(pitch);
                    AvaritiaShaders.unstableExternalScale.set(scale);
                    AvaritiaShaders.unstableOpacity.set(1.5F);
                    if (AvaritiaShaders.unstableUVs != null) {
                        AvaritiaShaders.unstableUVs.set(ETERNAL_UVS);
                    }
                }
            }
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
