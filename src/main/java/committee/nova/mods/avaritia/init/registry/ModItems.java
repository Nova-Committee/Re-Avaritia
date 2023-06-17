package committee.nova.mods.avaritia.init.registry;

import committee.nova.mods.avaritia.Static;
import committee.nova.mods.avaritia.api.common.item.BaseItem;
import committee.nova.mods.avaritia.common.item.ArmorInfinityItem;
import committee.nova.mods.avaritia.common.item.EndestPearlItem;
import committee.nova.mods.avaritia.common.item.MatterClusterItem;
import committee.nova.mods.avaritia.common.item.resources.ResourceItem;
import committee.nova.mods.avaritia.common.item.tools.*;
import net.minecraft.ChatFormatting;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import java.util.function.Supplier;

/**
 * Description:
 * Author: cnlimiter
 * Date: 2022/3/31 11:36
 * Version: 1.0
 */
public class ModItems {
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, Static.MOD_ID);

    public static Rarity COSMIC_RARITY = Rarity.create("COSMIC", ChatFormatting.RED);
    //tools
    public static RegistryObject<Item> pick_axe = item("infinity_pickaxe", PickaxeInfinityItem::new);
    public static RegistryObject<Item> infinity_shovel = item("infinity_shovel", ShovelInfinityItem::new);
    public static RegistryObject<Item> infinity_axe = item("infinity_axe", AxeInfinityItem::new);
    public static RegistryObject<Item> infinity_hoe = item("infinity_hoe", HoeInfinityItem::new);
    public static RegistryObject<Item> matter_cluster = item("matter_cluster", MatterClusterItem::new);
    //weapons
    public static RegistryObject<Item> infinity_sword = item("infinity_sword", SwordInfinityItem::new);
    public static RegistryObject<Item> skull_sword = item("skull_fire_sword", SwordSkullsItem::new);
    public static RegistryObject<Item> infinity_bow = item("infinity_bow", BowInfinityItem::new);
    //armors
    public static RegistryObject<Item> infinity_helmet = item("infinity_helmet", () -> new ArmorInfinityItem(ArmorItem.Type.HELMET));
    public static RegistryObject<Item> infinity_chestplate = item("infinity_chestplate", () -> new ArmorInfinityItem(ArmorItem.Type.CHESTPLATE));
    public static RegistryObject<Item> infinity_pants = item("infinity_pants", () -> new ArmorInfinityItem(ArmorItem.Type.LEGGINGS));
    public static RegistryObject<Item> infinity_boots = item("infinity_boots", () -> new ArmorInfinityItem(ArmorItem.Type.BOOTS));
    public static RegistryObject<Item> ultimate_stew = item("ultimate_stew", () -> new Item(new Item.Properties().rarity(Rarity.EPIC).food(ModFoods.ultimate_stew)));
    public static RegistryObject<Item> cosmic_meatballs = item("cosmic_meatballs", () -> new Item(new Item.Properties().rarity(Rarity.EPIC).food(ModFoods.cosmic_meatballs)));
    public static RegistryObject<Item> endest_pearl = item("endest_pearl", EndestPearlItem::new);
    public static RegistryObject<Item> diamond_lattice = item("diamond_lattice", () -> new ResourceItem(Rarity.UNCOMMON, "diamond_lattice", false));
    public static RegistryObject<Item> crystal_matrix_ingot = item("crystal_matrix_ingot", () -> new ResourceItem(Rarity.RARE, "crystal_matrix_ingot", false));
    public static RegistryObject<Item> neutron_pile = item("neutron_pile", () -> new ResourceItem(Rarity.UNCOMMON, "neutron_pile", false));
    public static RegistryObject<Item> neutron_nugget = item("neutron_nugget", () -> new ResourceItem(Rarity.UNCOMMON, "neutron_nugget", false));
    public static RegistryObject<Item> neutronium_ingot = item("neutronium_ingot", () -> new ResourceItem(Rarity.RARE, "neutronium_ingot", false));
    public static RegistryObject<Item> neutronium_gear = item("neutronium_gear", () -> new ResourceItem(Rarity.EPIC, "neutronium_gear", false));
    public static RegistryObject<Item> infinity_nugget = item("infinity_nugget", () -> new ResourceItem(Rarity.RARE, "infinity_nugget", false));
    public static RegistryObject<Item> infinity_catalyst = item("infinity_catalyst", () -> new ResourceItem(Rarity.UNCOMMON, "infinity_catalyst", false));
    public static RegistryObject<Item> infinity_ingot = item("infinity_ingot", () -> new ResourceItem(COSMIC_RARITY, "infinity_ingot", false));
    public static RegistryObject<Item> star_fuel = item("star_fuel", () -> new ResourceItem(Rarity.EPIC, "star_fuel", false));
    public static RegistryObject<Item> record_fragment = item("record_fragment", () -> new ResourceItem(COSMIC_RARITY, "record_fragment", false));
//    public static RegistryObject<Item> singularity = item("singularity", () -> new SingularityItem(properties -> properties));

    static {
        ModBlocks.BLOCK_ITEMS.forEach(ITEMS::register);
    }

    public static RegistryObject<Item> item(String name) {
        return item(name, BaseItem::new);
    }

    public static RegistryObject<Item> item(String name, Supplier<Item> item) {
        return ITEMS.register(name, item);
    }
}
