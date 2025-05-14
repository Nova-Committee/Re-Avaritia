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
    public static Rarity COMMON = Rarity.COMMON;
    public static Rarity UNCOMMON = Rarity.UNCOMMON;
    public static Rarity RARE = Rarity.RARE;
    public static Rarity EPIC = Rarity.EPIC;
    public static Rarity LEGEND = new EnumProxy<>(
            Rarity.class, 8888, "avaritia:legend", ChatFormatting.GOLD
    ).getValue();
    public static Rarity COSMIC = new EnumProxy<>(
            Rarity.class, 9999, "avaritia:cosmic", ChatFormatting.RED
    ).getValue();
}
