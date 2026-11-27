package committee.nova.mods.avaritia.client.model.loader;

import committee.nova.mods.avaritia.client.model.item.ItemEffect;
import committee.nova.mods.avaritia.client.model.loader.base.AvaritiaItemModelCodecs;
import committee.nova.mods.avaritia.client.model.loader.utils.EffectItemModelBaker;
import committee.nova.mods.avaritia.client.model.loader.base.EffectFields;
import committee.nova.mods.avaritia.client.model.loader.base.HaloEffectFields;
import committee.nova.mods.avaritia.client.model.loader.base.HaloFields;
import com.mojang.serialization.MapCodec;
import net.minecraft.client.color.item.ItemTintSource;
import net.minecraft.client.renderer.item.ItemModel;
import net.minecraft.client.resources.model.ResolvableModel;
import net.minecraft.resources.Identifier;
import org.joml.Matrix4fc;
import org.jspecify.annotations.NonNull;

import java.util.List;
import java.util.Optional;

/**
 * Avaritia 自定义物品模型的 JSON 入口集合。
 * <p>
 * 这些 record 会被 {@link committee.nova.mods.avaritia.client.AvaritiaClient#registerItemModels} 注册成
 * {@code avaritia:cosmic}、{@code avaritia:halo} 等 item model type。它们只描述数据格式和
 * 要烘焙的模型资源，真正的纹理拆分、遮罩 quad 和特殊渲染层由 {@link EffectItemModelBaker} 处理。
 */
public final class AvaritiaItemModelLoaders {
    private AvaritiaItemModelLoaders() {
    }

    /**
     * 普通星空覆盖层：保留原物品模型，再按 mask 追加 COSMIC 遮罩效果。
     */
    public record Cosmic(Identifier model, List<Identifier> mask, List<ItemTintSource> tints) implements ItemModel.Unbaked, EffectFields {
        public static final MapCodec<Cosmic> MAP_CODEC = AvaritiaItemModelCodecs.effect(Cosmic::new);

        public Cosmic(Identifier model, List<Identifier> mask) {
            this(model, mask, List.of());
        }

        @Override
        public @NonNull ItemModel bake(ItemModel.@NonNull BakingContext context, @NonNull Matrix4fc transformation) {
            return EffectItemModelBaker.bake(context, transformation, this.model, this.tints, this.mask, ItemEffect.COSMIC, Optional.empty());
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

    /**
     * 仅给无限三叉戟使用的星空模型：在普通 COSMIC 覆盖层之外追加世界/手持中的三叉戟几何和弧光。
     */
    public record CosmicArc(Identifier model, List<Identifier> mask, List<ItemTintSource> tints) implements ItemModel.Unbaked, EffectFields {
        public static final MapCodec<CosmicArc> MAP_CODEC = AvaritiaItemModelCodecs.effect(CosmicArc::new);

        public CosmicArc(Identifier model, List<Identifier> mask) {
            this(model, mask, List.of());
        }

        @Override
        public @NonNull ItemModel bake(ItemModel.@NonNull BakingContext context, @NonNull Matrix4fc transformation) {
            return EffectItemModelBaker.bake(context, transformation, this.model, this.tints, this.mask, ItemEffect.COSMIC, Optional.empty(), true);
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

    /**
     * 地狱效果覆盖层，主要用于击杀模式等红色动态遮罩。
     */
    public record Hell(Identifier model, List<Identifier> mask, List<ItemTintSource> tints) implements ItemModel.Unbaked, EffectFields {
        public static final MapCodec<Hell> MAP_CODEC = AvaritiaItemModelCodecs.effect(Hell::new);

        public Hell(Identifier model, List<Identifier> mask) {
            this(model, mask, List.of());
        }

        @Override
        public ItemModel bake(ItemModel.BakingContext context, Matrix4fc transformation) {
            return EffectItemModelBaker.bake(context, transformation, this.model, this.tints, this.mask, ItemEffect.HELL, Optional.empty());
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

    /**
     * 永恒效果覆盖层，使用 eternal 纹理序列和对应 shader 参数。
     */
    public record Eternal(Identifier model, List<Identifier> mask, List<ItemTintSource> tints) implements ItemModel.Unbaked, EffectFields {
        public static final MapCodec<Eternal> MAP_CODEC = AvaritiaItemModelCodecs.effect(Eternal::new);

        public Eternal(Identifier model, List<Identifier> mask) {
            this(model, mask, List.of());
        }

        @Override
        public ItemModel bake(ItemModel.BakingContext context, Matrix4fc transformation) {
            return EffectItemModelBaker.bake(context, transformation, this.model, this.tints, this.mask, ItemEffect.ETERNAL, Optional.empty());
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

    /**
     * 不稳定效果覆盖层，当前使用更高透明度表现闪烁/能量不稳定感。
     */
    public record Unstable(Identifier model, List<Identifier> mask, List<ItemTintSource> tints) implements ItemModel.Unbaked, EffectFields {
        public static final MapCodec<Unstable> MAP_CODEC = AvaritiaItemModelCodecs.effect(Unstable::new);

        public Unstable(Identifier model, List<Identifier> mask) {
            this(model, mask, List.of());
        }

        @Override
        public ItemModel bake(ItemModel.@NonNull BakingContext context, @NonNull Matrix4fc transformation) {
            return EffectItemModelBaker.bake(context, transformation, this.model, this.tints, this.mask, ItemEffect.UNSTABLE, Optional.empty());
        }

        @Override
        public void resolveDependencies(ResolvableModel.Resolver resolver) {
            resolver.markDependency(this.model);
        }

        @Override
        public @NonNull MapCodec<? extends ItemModel.Unbaked> type() {
            return MAP_CODEC;
        }
    }

    /**
     * 仅 halo 的模型类型：原模型正常渲染，GUI 中额外绘制圆形光环和可选脉冲。
     */
    public record Halo(Identifier model, Identifier texture, int color, int size, boolean pulse, List<ItemTintSource> tints) implements ItemModel.Unbaked, HaloFields {
        public static final MapCodec<Halo> MAP_CODEC = AvaritiaItemModelCodecs.halo(Halo::new);

        public Halo(Identifier model, Identifier texture, int color, int size, boolean pulse) {
            this(model, texture, color, size, pulse, List.of());
        }

        @Override
        public ItemModel bake(ItemModel.@NonNull BakingContext context, @NonNull Matrix4fc transformation) {
            return EffectItemModelBaker.bake(context, transformation, this.model, this.tints, List.of(), null, Optional.of(EffectItemModelBaker.toHaloLayer(context, this)));
        }

        @Override
        public void resolveDependencies(ResolvableModel.Resolver resolver) {
            resolver.markDependency(this.model);
        }

        @Override
        public @NonNull MapCodec<? extends ItemModel.Unbaked> type() {
            return MAP_CODEC;
        }
    }

    /**
     * halo + COSMIC 遮罩组合，常用于既要光环又要星空遮罩的物品。
     */
    public record HaloCosmic(Identifier model, List<Identifier> mask, Identifier texture, int color, int size, boolean pulse, List<ItemTintSource> tints) implements ItemModel.Unbaked, HaloEffectFields {
        public static final MapCodec<HaloCosmic> MAP_CODEC = AvaritiaItemModelCodecs.haloEffect(HaloCosmic::new);

        public HaloCosmic(Identifier model, List<Identifier> mask, Identifier texture, int color, int size, boolean pulse) {
            this(model, mask, texture, color, size, pulse, List.of());
        }

        @Override
        public ItemModel bake(ItemModel.@NonNull BakingContext context, @NonNull Matrix4fc transformation) {
            return EffectItemModelBaker.bake(context, transformation, this.model, this.tints, this.mask, ItemEffect.COSMIC, Optional.of(EffectItemModelBaker.toHaloLayer(context, this)));
        }

        @Override
        public void resolveDependencies(ResolvableModel.Resolver resolver) {
            resolver.markDependency(this.model);
        }

        @Override
        public @NonNull MapCodec<? extends ItemModel.Unbaked> type() {
            return MAP_CODEC;
        }
    }

    /**
     * halo + ETERNAL 遮罩组合，保留与 HaloCosmic 相同的数据格式，只切换效果枚举。
     */
    public record HaloEternal(Identifier model, List<Identifier> mask, Identifier texture, int color, int size, boolean pulse, List<ItemTintSource> tints) implements ItemModel.Unbaked, HaloEffectFields {
        public static final MapCodec<HaloEternal> MAP_CODEC = AvaritiaItemModelCodecs.haloEffect(HaloEternal::new);

        public HaloEternal(Identifier model, List<Identifier> mask, Identifier texture, int color, int size, boolean pulse) {
            this(model, mask, texture, color, size, pulse, List.of());
        }

        @Override
        public @NonNull ItemModel bake(ItemModel.@NonNull BakingContext context, @NonNull Matrix4fc transformation) {
            return EffectItemModelBaker.bake(context, transformation, this.model, this.tints, this.mask, ItemEffect.ETERNAL, Optional.of(EffectItemModelBaker.toHaloLayer(context, this)));
        }

        @Override
        public void resolveDependencies(ResolvableModel.Resolver resolver) {
            resolver.markDependency(this.model);
        }

        @Override
        public @NonNull MapCodec<? extends ItemModel.Unbaked> type() {
            return MAP_CODEC;
        }
    }
}
