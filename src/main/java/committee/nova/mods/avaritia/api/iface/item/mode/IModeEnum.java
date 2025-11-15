package committee.nova.mods.avaritia.api.iface.item.mode;

import committee.nova.mods.avaritia.api.utils.text.IHasTranslationKey;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.item.ItemStack;

/**
 * @project: Avaritia
 * @author: cnlimiter
 * @createTime: 2025/5/24 19:51
 * @apiNote:
 */
public interface IModeEnum<MODE extends Enum<MODE> & IModeEnum<MODE>> extends IHasTranslationKey.IHasEnumNameTranslationKey, StringRepresentable {

    MODE next(ItemStack stack);
}
