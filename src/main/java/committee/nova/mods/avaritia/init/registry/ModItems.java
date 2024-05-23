package committee.nova.mods.avaritia.init.registry;

import com.chocohead.mm.api.ClassTinkerers;
import committee.nova.mods.avaritia.api.common.item.BaseItem;
import committee.nova.mods.avaritia.common.item.ArmorInfinityItem;
import committee.nova.mods.avaritia.common.item.EndestPearlItem;
import committee.nova.mods.avaritia.common.item.MatterClusterItem;
import committee.nova.mods.avaritia.common.item.resources.ResourceItem;
import committee.nova.mods.avaritia.common.item.singularity.SingularityItem;
import committee.nova.mods.avaritia.common.item.tools.*;
import committee.nova.mods.avaritia.util.registry.FabricRegistry;
import committee.nova.mods.avaritia.util.registry.RegistryHolder;
import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.minecraft.ChatFormatting;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;

import java.util.function.Supplier;

/**
 * Description:
 * Author: cnlimiter
 * Date: 2022/3/31 11:36
 * Version: 1.0
 */
public class ModItems {
    public static final RegistryHolder<Item> ITEMS = FabricRegistry.INSTANCE.createItemRegistryHolder();
    public static void init() {}
    public static final Rarity COSMIC_RARITY = ClassTinkerers.getEnum(Rarity.class, "COSMIC");
    //tools
    public static Supplier<Item> infinity_pickaxe = item("infinity_pickaxe", InfinityPickaxeItem::new);
    public static Supplier<Item> infinity_shovel = item("infinity_shovel", InfinityShovelItem::new);
    public static Supplier<Item> infinity_axe = item("infinity_axe", InfinityAxeItem::new);
    public static Supplier<Item> infinity_hoe = item("infinity_hoe", InfinityHoeItem::new);
    public static Supplier<Item> matter_cluster = item("matter_cluster", MatterClusterItem::new);
    //weapons
    public static Supplier<Item> infinity_sword = item("infinity_sword", InfinitySwordItem::new);
    public static Supplier<Item> skull_sword = item("skull_fire_sword", SkullsSwordItem::new);
    public static Supplier<Item> infinity_bow = item("infinity_bow", InfinityBowItem::new);
    //armors
    public static Supplier<Item> infinity_helmet = item("infinity_helmet", () -> new ArmorInfinityItem(ArmorItem.Type.HELMET));
    public static Supplier<Item> infinity_chestplate = item("infinity_chestplate", () -> new ArmorInfinityItem(ArmorItem.Type.CHESTPLATE));
    public static Supplier<Item> infinity_pants = item("infinity_pants", () -> new ArmorInfinityItem(ArmorItem.Type.LEGGINGS));
    public static Supplier<Item> infinity_boots = item("infinity_boots", () -> new ArmorInfinityItem(ArmorItem.Type.BOOTS));
    public static Supplier<Item> ultimate_stew = item("ultimate_stew", () -> new Item(new Item.Properties().rarity(Rarity.EPIC).food(ModFoods.ultimate_stew)));
    public static Supplier<Item> cosmic_meatballs = item("cosmic_meatballs", () -> new Item(new Item.Properties().rarity(Rarity.EPIC).food(ModFoods.cosmic_meatballs)));
    //resource
    public static Supplier<Item> endest_pearl = item("endest_pearl", EndestPearlItem::new);
    public static Supplier<Item> diamond_lattice = item("diamond_lattice", () -> new ResourceItem(Rarity.UNCOMMON, "diamond_lattice", true));
    public static Supplier<Item> crystal_matrix_ingot = item("crystal_matrix_ingot", () -> new ResourceItem(Rarity.RARE, "crystal_matrix_ingot", true));
    public static Supplier<Item> neutron_pile = item("neutron_pile", () -> new ResourceItem(Rarity.UNCOMMON, "neutron_pile", true));
    public static Supplier<Item> neutron_nugget = item("neutron_nugget", () -> new ResourceItem(Rarity.UNCOMMON, "neutron_nugget", true));
    public static Supplier<Item> neutron_ingot = item("neutron_ingot", () -> new ResourceItem(Rarity.RARE, "neutron_ingot", true));
    public static Supplier<Item> neutron_gear = item("neutron_gear", () -> new ResourceItem(Rarity.RARE, "neutron_gear", true));
    public static Supplier<Item> infinity_nugget = item("infinity_nugget", () -> new ResourceItem(Rarity.EPIC, "infinity_nugget", true));
    public static Supplier<Item> infinity_catalyst = item("infinity_catalyst", () -> new ResourceItem(Rarity.EPIC, "infinity_catalyst", true));
    public static Supplier<Item> infinity_ingot = item("infinity_ingot", () -> new ResourceItem(COSMIC_RARITY, "infinity_ingot", true));

    public static Supplier<Item> singularity = item("singularity", () -> new SingularityItem(properties -> properties));

    public static Supplier<Item> infinity_totem = item("infinity_totem",
            () -> new ResourceItem(Rarity.UNCOMMON, "infinity_totem", true,
                    new FabricItemSettings().stacksTo(1).durability(999)));
    public static Supplier<Item> infinity_horse_armor = item("infinity_horse_armor",
            () -> new InfinityHorseArmorItem(
                    new FabricItemSettings().stacksTo(1).rarity(COSMIC_RARITY)
                            .fireResistant()
            ));

    public static Supplier<Item> star_fuel = item("star_fuel", () -> new ResourceItem(Rarity.UNCOMMON, "diamond_lattice", true));
    public static Supplier<Item> record_fragment = item("record_fragment", () -> new ResourceItem(Rarity.EPIC, "record_fragment", true));



    public static Supplier<Item> item(String name) {
        return item(name, BaseItem::new);
    }

    public static Supplier<Item> item(String name, Supplier<Item> item) {
        return ITEMS.register(name, item);
    }


}
