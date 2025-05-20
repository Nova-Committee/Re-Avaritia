package committee.nova.mods.avaritia.init.registry;

import net.minecraft.ChatFormatting;
import net.minecraft.world.item.Rarity;
import net.neoforged.fml.common.asm.enumextension.EnumProxy;

/**
 * @Project: Avaritia
 * @Author: cnlimiter
 * @CreateTime: 2024/8/5 下午12:46
 * @Description:
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
