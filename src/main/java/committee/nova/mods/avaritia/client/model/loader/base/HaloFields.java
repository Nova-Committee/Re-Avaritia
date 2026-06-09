package committee.nova.mods.avaritia.client.model.loader.base;

import net.minecraft.client.color.item.ItemTintSource;
import net.minecraft.resources.Identifier;

import java.util.List;

/**
 * halo loader record 需要暴露的公共字段，供通用 codec 读取。
 *
 * @author cnlimiter
 */
public interface HaloFields {
    Identifier model();

    Identifier texture();

    int color();

    int size();

    boolean pulse();

    List<ItemTintSource> tints();
}
