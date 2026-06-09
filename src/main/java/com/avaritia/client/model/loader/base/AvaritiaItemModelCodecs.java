package com.avaritia.client.model.loader.base;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.client.color.item.ItemTintSources;
import net.minecraft.client.renderer.item.ItemModel;
import net.minecraft.resources.Identifier;

import java.util.List;

/**
 * 统一维护 Avaritia 自定义 item model JSON 的字段格式。
 * <p>
 * 这里把 effect、halo、halo+effect 三类公共字段抽出来，避免每个 loader record 自己拼 codec 后出现默认值或字段名漂移。
 */
public class AvaritiaItemModelCodecs {
    private AvaritiaItemModelCodecs() {
    }

    /**
     * 星光/地狱/永恒等遮罩覆盖层通用格式：基础模型 + 一个或多个 mask + 可选 tint。
     */
    public static <T extends ItemModel.Unbaked & EffectFields> MapCodec<T> effect(EffectFactory<T> factory) {
        return RecordCodecBuilder.mapCodec(instance -> instance.group(
                Identifier.CODEC.fieldOf("model").forGetter((T model) -> model.model()),
                Identifier.CODEC.listOf().fieldOf("mask").forGetter((T model) -> model.mask()),
                ItemTintSources.CODEC.listOf().optionalFieldOf("tints", List.of()).forGetter((T model) -> model.tints())
        ).apply(instance, factory::create));
    }

    /**
     * 纯 halo 格式：基础模型 + halo 贴图 + 颜色/尺寸/脉冲参数。
     */
    public static <T extends ItemModel.Unbaked & HaloFields> MapCodec<T> halo(HaloFactory<T> factory) {
        return RecordCodecBuilder.mapCodec(instance -> instance.group(
                Identifier.CODEC.fieldOf("model").forGetter((T model) -> model.model()),
                Identifier.CODEC.fieldOf("texture").forGetter((T model) -> model.texture()),
                Codec.INT.optionalFieldOf("color", -16777216).forGetter((T model) -> model.color()),
                Codec.INT.optionalFieldOf("size", 10).forGetter((T model) -> model.size()),
                Codec.BOOL.optionalFieldOf("pulse", true).forGetter((T model) -> model.pulse()),
                ItemTintSources.CODEC.listOf().optionalFieldOf("tints", List.of()).forGetter((T model) -> model.tints())
        ).apply(instance, factory::create));
    }

    /**
     * halo 与遮罩效果组合格式，供 HaloCosmic/HaloEternal 共享。
     */
    public static <T extends ItemModel.Unbaked & HaloEffectFields> MapCodec<T> haloEffect(HaloEffectFactory<T> factory) {
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
}
