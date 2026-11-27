package committee.nova.mods.avaritia.client.model.loader.base;

import net.minecraft.client.color.item.ItemTintSource;
import net.minecraft.resources.Identifier;

import java.util.List;

/**
 * codec 反序列化后创建具体 effect loader record 的函数接口。
 *
 * @author cnlimiter
 */
public interface EffectFactory<T> {
    T create(Identifier model, List<Identifier> mask, List<ItemTintSource> tints);
}
