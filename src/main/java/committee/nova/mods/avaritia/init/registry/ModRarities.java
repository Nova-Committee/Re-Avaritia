package committee.nova.mods.avaritia.init.registry;

import net.minecraft.ChatFormatting;
import net.minecraft.world.item.Rarity;

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
    public static Rarity LEGEND = Rarity.create("LEGEND", ChatFormatting.GOLD);
    public static Rarity COSMIC = Rarity.create("COSMIC", ChatFormatting.RED);
}
