package committee.nova.mods.avaritia.init.registry;

import net.minecraft.ChatFormatting;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;

/** Native rarity mechanics; legendary/cosmic names retain their original display colors. */
public final class ModRarities {
    public static final Rarity COMMON = Rarity.COMMON;
    public static final Rarity UNCOMMON = Rarity.UNCOMMON;
    public static final Rarity RARE = Rarity.RARE;
    public static final Rarity EPIC = Rarity.EPIC;
    public static final Rarity LEGEND = Rarity.UNCOMMON;
    public static final Rarity COSMIC = Rarity.EPIC;

    public static ChatFormatting nameColor(ItemStack stack) {
        ResourceLocation id = BuiltInRegistries.ITEM.getKey(stack.getItem());
        if (!id.getNamespace().equals("avaritia")) return null;
        return switch (id.getPath()) {
            case "infinity_chest", "infinity_bucket", "infinity_ring", "infinity_upgrade",
                 "enhancement_core", "infinity_catalyst", "tesseract", "denser_neutron_collector",
                 "denser_neutron_compressor", "extreme_smithing_table", "extreme_anvil" -> ChatFormatting.GOLD;
            case "infinity_clock", "infinity_elytra", "infinity_umbrella", "infinity_helmet",
                 "infinity_chestplate", "infinity_pants", "infinity_boots", "infinity_axe",
                 "infinity_bow", "infinity_crossbow", "infinity_hoe", "infinity_pickaxe",
                 "infinity_shield", "infinity_shovel", "infinity_sword", "infinity_trident",
                 "infinity_ingot", "infinity", "densest_neutron_collector",
                 "densest_neutron_compressor" -> ChatFormatting.RED;
            default -> null;
        };
    }

    private ModRarities() {}
}
