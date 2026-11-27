package committee.nova.mods.avaritia.init.registry;

import net.minecraft.ChatFormatting;
import net.minecraft.world.item.Rarity;
import net.neoforged.fml.common.asm.enumextension.EnumProxy;

/**
 * 注册模组中的所有稀有度等级。
 *
 * <p>包含原版四种稀有度引用＋两个自定义稀有度（传奇、宇宙），
 * 自定义稀有度通过 {@link EnumProxy} 在 {@code enumextensions.json} 中扩展。</p>
 */
public class ModRarities {
    public static final Rarity COMMON = Rarity.COMMON;
    public static final Rarity UNCOMMON = Rarity.UNCOMMON;
    public static final Rarity RARE = Rarity.RARE;
    public static final Rarity EPIC = Rarity.EPIC;
    public static final EnumProxy<Rarity> LEGEND = new EnumProxy<>(
            Rarity.class, 8888, "avaritia:legend", ChatFormatting.GOLD
    );
    public static final EnumProxy<Rarity> COSMIC = new EnumProxy<>(
            Rarity.class, 9999, "avaritia:cosmic", ChatFormatting.RED
    );
}
