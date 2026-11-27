package committee.nova.mods.avaritia.client.model.loader.base;

import net.minecraft.client.color.item.ItemTintSource;
import net.minecraft.resources.Identifier;

import java.util.List;

/**
 * codec 反序列化后创建 halo+effect 组合 loader record 的函数接口。
 *
 * @author cnlimiter
 */
public interface HaloEffectFactory<T> {
    T create(Identifier model, List<Identifier> mask, Identifier texture, int color, int size, boolean pulse, List<ItemTintSource> tints);
}
