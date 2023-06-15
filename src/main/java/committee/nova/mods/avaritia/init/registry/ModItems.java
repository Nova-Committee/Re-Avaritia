package committee.nova.mods.avaritia.init.registry;

import committee.nova.mods.avaritia.common.item.ArmorInfinityItem;
import committee.nova.mods.avaritia.common.item.EndestPearlItem;
import committee.nova.mods.avaritia.common.item.MatterClusterItem;
import committee.nova.mods.avaritia.common.item.resources.ResourceItem;
import committee.nova.mods.avaritia.common.item.singularity.SingularityItem;
import committee.nova.mods.avaritia.common.item.tools.*;
import committee.nova.mods.avaritia.init.ModFoods;
import committee.nova.mods.avaritia.util.registry.RegistryUtil;
import net.minecraft.ChatFormatting;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;

/**
 * Description:
 * Author: cnlimiter
 * Date: 2022/3/31 11:36
 * Version: 1.0
 */
public class ModItems {

    public static Rarity COSMIC_RARITY = Rarity.create("COSMIC", ChatFormatting.RED);
    //tools
    public static Item pick_axe = RegistryUtil.item("infinity_pickaxe", PickaxeInfinityItem::new).get();
    public static Item infinity_shovel = RegistryUtil.item("infinity_shovel", ShovelInfinityItem::new).get();
    public static Item infinity_axe = RegistryUtil.item("infinity_axe", AxeInfinityItem::new).get();
    public static Item infinity_hoe = RegistryUtil.item("infinity_hoe", HoeInfinityItem::new).get();
    public static Item matter_cluster = RegistryUtil.item("matter_cluster", MatterClusterItem::new).get();
    //weapons
    public static Item infinity_sword = RegistryUtil.item("infinity_sword", SwordInfinityItem::new).get();
    public static Item skull_sword = RegistryUtil.item("skull_fire_sword", SwordSkullsItem::new).get();
    public static Item infinity_bow = RegistryUtil.item("infinity_bow", BowInfinityItem::new).get();
    //armors
    public static Item infinity_helmet = RegistryUtil.item("infinity_helmet", () -> new ArmorInfinityItem(ArmorItem.Type.HELMET)).get();
    public static Item infinity_chestplate = RegistryUtil.item("infinity_chestplate", () -> new ArmorInfinityItem(ArmorItem.Type.CHESTPLATE)).get();
    public static Item infinity_pants = RegistryUtil.item("infinity_pants", () -> new ArmorInfinityItem(ArmorItem.Type.LEGGINGS)).get();
    public static Item infinity_boots = RegistryUtil.item("infinity_boots", () -> new ArmorInfinityItem(ArmorItem.Type.BOOTS)).get();
    public static Item ultimate_stew = RegistryUtil.item("ultimate_stew", () -> new Item(new Item.Properties().rarity(Rarity.EPIC).food(ModFoods.ultimate_stew))).get();
    public static Item cosmic_meatballs = RegistryUtil.item("cosmic_meatballs", () -> new Item(new Item.Properties().rarity(Rarity.EPIC).food(ModFoods.cosmic_meatballs))).get();
    public static Item endest_pearl = RegistryUtil.item("endest_pearl", EndestPearlItem::new).get();
    public static Item diamond_lattice = RegistryUtil.item("diamond_lattice", () -> new ResourceItem(Rarity.UNCOMMON, "diamond_lattice", false)).get();
    public static Item crystal_matrix_ingot = RegistryUtil.item("crystal_matrix_ingot", () -> new ResourceItem(Rarity.RARE, "crystal_matrix_ingot", false)).get();
    public static Item neutron_pile = RegistryUtil.item("neutron_pile", () -> new ResourceItem(Rarity.UNCOMMON, "neutron_pile", false)).get();
    public static Item neutron_nugget = RegistryUtil.item("neutron_nugget", () -> new ResourceItem(Rarity.UNCOMMON, "neutron_nugget", false)).get();
    public static Item neutronium_ingot = RegistryUtil.item("neutronium_ingot", () -> new ResourceItem(Rarity.RARE, "neutronium_ingot", false)).get();
    public static Item neutronium_gear = RegistryUtil.item("neutronium_gear", () -> new ResourceItem(Rarity.EPIC, "neutronium_gear", false)).get();
    public static Item infinity_nugget = RegistryUtil.item("infinity_nugget", () -> new ResourceItem(Rarity.RARE, "infinity_nugget", false)).get();
    public static Item infinity_catalyst = RegistryUtil.item("infinity_catalyst", () -> new ResourceItem(Rarity.UNCOMMON, "infinity_catalyst", false)).get();
    public static Item infinity_ingot = RegistryUtil.item("infinity_ingot", () -> new ResourceItem(COSMIC_RARITY, "infinity_ingot", false)).get();
    public static Item star_fuel = RegistryUtil.item("star_fuel", () -> new ResourceItem(Rarity.EPIC, "star_fuel", false)).get();
    public static Item record_fragment = RegistryUtil.item("record_fragment", () -> new ResourceItem(COSMIC_RARITY, "record_fragment", false)).get();
    public static Item singularity = RegistryUtil.item("singularity", () -> new SingularityItem(properties -> properties)).get();

    static {
        RegistryUtil.BLOCK_ITEMS.forEach(RegistryUtil.ITEMS::register);
    }


}
