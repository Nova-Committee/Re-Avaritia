package committee.nova.mods.avaritia.init.registry;

import committee.nova.mods.avaritia.Static;
import committee.nova.mods.avaritia.api.common.item.BaseItem;
import committee.nova.mods.avaritia.common.item.ArmorInfinityItem;
import committee.nova.mods.avaritia.common.item.EndestPearlItem;
import committee.nova.mods.avaritia.common.item.MatterClusterItem;
import committee.nova.mods.avaritia.common.item.resources.ResourceItem;
import committee.nova.mods.avaritia.common.item.resources.StarFuelItem;
import committee.nova.mods.avaritia.common.item.singularity.SingularityItem;
import committee.nova.mods.avaritia.common.item.tools.*;
import net.minecraft.ChatFormatting;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

/**
 * Description:
 * Author: cnlimiter
 * Date: 2022/3/31 11:36
 * Version: 1.0
 */
public class ModItems {
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(Static.MOD_ID);

    public static Rarity COSMIC_RARITY = Rarity.create("COSMIC", ChatFormatting.RED);
    //tools
    public static DeferredItem<Item> infinity_pickaxe = item("infinity_pickaxe", PickaxeInfinityItem::new);
    public static DeferredItem<Item> infinity_shovel = item("infinity_shovel", ShovelInfinityItem::new);
    public static DeferredItem<Item> infinity_axe = item("infinity_axe", AxeInfinityItem::new);
    public static DeferredItem<Item> infinity_hoe = item("infinity_hoe", HoeInfinityItem::new);
    public static DeferredItem<Item> matter_cluster = item("matter_cluster", MatterClusterItem::new);
    //weapons
    public static DeferredItem<Item> infinity_sword = item("infinity_sword", SwordInfinityItem::new);
    public static DeferredItem<Item> skull_sword = item("skull_fire_sword", SwordSkullsItem::new);
    public static DeferredItem<Item> infinity_bow = item("infinity_bow", BowInfinityItem::new);
    //armors
    public static DeferredItem<Item> infinity_helmet = item("infinity_helmet", () -> new ArmorInfinityItem(ArmorItem.Type.HELMET));
    public static DeferredItem<Item> infinity_chestplate = item("infinity_chestplate", () -> new ArmorInfinityItem(ArmorItem.Type.CHESTPLATE));
    public static DeferredItem<Item> infinity_pants = item("infinity_pants", () -> new ArmorInfinityItem(ArmorItem.Type.LEGGINGS));
    public static DeferredItem<Item> infinity_boots = item("infinity_boots", () -> new ArmorInfinityItem(ArmorItem.Type.BOOTS));
    public static DeferredItem<Item> ultimate_stew = item("ultimate_stew", () -> new Item(new Item.Properties().rarity(Rarity.EPIC).food(ModFoods.ultimate_stew)));
    public static DeferredItem<Item> cosmic_meatballs = item("cosmic_meatballs", () -> new Item(new Item.Properties().rarity(Rarity.EPIC).food(ModFoods.cosmic_meatballs)));
    //resource
    public static DeferredItem<Item> endest_pearl = item("endest_pearl", EndestPearlItem::new);
    public static DeferredItem<Item> diamond_lattice = item("diamond_lattice", () -> new ResourceItem(Rarity.UNCOMMON, "diamond_lattice", true));
    public static DeferredItem<Item> crystal_matrix_ingot = item("crystal_matrix_ingot", () -> new ResourceItem(Rarity.RARE, "crystal_matrix_ingot", true));
    public static DeferredItem<Item> neutron_pile = item("neutron_pile", () -> new ResourceItem(Rarity.UNCOMMON, "neutron_pile", true));
    public static DeferredItem<Item> neutron_nugget = item("neutron_nugget", () -> new ResourceItem(Rarity.UNCOMMON, "neutron_nugget", true));
    public static DeferredItem<Item> neutron_ingot = item("neutron_ingot", () -> new ResourceItem(Rarity.RARE, "neutron_ingot", true));
    public static DeferredItem<Item> neutron_gear = item("neutron_gear", () -> new ResourceItem(Rarity.EPIC, "neutron_gear", true));
    public static DeferredItem<Item> infinity_nugget = item("infinity_nugget", () -> new ResourceItem(Rarity.RARE, "infinity_nugget", true));
    public static DeferredItem<Item> infinity_catalyst = item("infinity_catalyst", () -> new ResourceItem(Rarity.UNCOMMON, "infinity_catalyst", true));
    public static DeferredItem<Item> infinity_ingot = item("infinity_ingot", () -> new ResourceItem(COSMIC_RARITY, "infinity_ingot", true));
    public static DeferredItem<Item> star_fuel = item("star_fuel", () -> new StarFuelItem(Rarity.EPIC));
    public static DeferredItem<Item> record_fragment = item("record_fragment", () -> new ResourceItem(COSMIC_RARITY, "record_fragment", true));
    public static DeferredItem<Item> singularity = item("singularity", () -> new SingularityItem(properties -> properties));

    static {
        ModBlocks.BLOCK_ITEMS.forEach(ITEMS::register);
    }

    public static DeferredItem<Item> item(String name) {
        return item(name, BaseItem::new);
    }

    public static DeferredItem<Item> item(String name, Supplier<Item> item) {
        return ITEMS.register(name, item);
    }


}
