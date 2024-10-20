package committee.nova.mods.avaritia.init.registry;

import committee.nova.mods.avaritia.Static;
import committee.nova.mods.avaritia.api.common.item.BaseItem;
import committee.nova.mods.avaritia.common.item.InfinityArmorItem;
import committee.nova.mods.avaritia.common.item.EndestPearlItem;
import committee.nova.mods.avaritia.common.item.MatterClusterItem;
import committee.nova.mods.avaritia.common.item.misc.*;
import committee.nova.mods.avaritia.common.item.resources.*;
import committee.nova.mods.avaritia.common.item.singularity.EternalSingularityItem;
import committee.nova.mods.avaritia.common.item.singularity.SingularityItem;
import committee.nova.mods.avaritia.common.item.tools.*;
import committee.nova.mods.avaritia.common.item.tools.blaze.*;
import committee.nova.mods.avaritia.common.item.tools.crystal.*;
import committee.nova.mods.avaritia.common.item.tools.infinity.*;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.Item;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Description:
 * Author: cnlimiter
 * Date: 2022/3/31 11:36
 * Version: 1.0
 */
public class ModItems {
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, Static.MOD_ID);

    //curios
    public static RegistryObject<Item> neutron_ring = item("neutron_ring", NeutronRingItem::new);
    public static RegistryObject<Item> infinity_totem = item("infinity_totem", InfinityTotemItem::new);
    public static RegistryObject<Item> infinity_ring = item("infinity_ring", InfinityRingItem::new);
    public static RegistryObject<Item> infinity_umbrella = item("infinity_umbrella", InfinityUmbrellaItem::new);
    /**
     * Tools
     */
    //infinity
    public static RegistryObject<Item> infinity_sword = item("infinity_sword", InfinitySwordItem::new);
    public static RegistryObject<Item> infinity_hoe = item("infinity_hoe", InfinityHoeItem::new);
    public static RegistryObject<Item> infinity_pickaxe = item("infinity_pickaxe", InfinityPickaxeItem::new);
    public static RegistryObject<Item> infinity_shovel = item("infinity_shovel", InfinityShovelItem::new);
    public static RegistryObject<Item> infinity_axe = item("infinity_axe", InfinityAxeItem::new);
    public static RegistryObject<Item> infinity_bucket = item("infinity_bucket", InfinityBucketItem::new);
    public static RegistryObject<Item> infinity_bow = item("infinity_bow", InfinityBowItem::new);
    //crystal
    public static RegistryObject<Item> crystal_sword = item("crystal_sword", CrystalSwordItem::new);
    public static RegistryObject<Item> crystal_hoe = item("crystal_hoe", CrystalHoeItem::new);
    public static RegistryObject<Item> crystal_pickaxe = item("crystal_pickaxe", CrystalPickaxeItem::new);
    public static RegistryObject<Item> crystal_shovel = item("crystal_shovel", CrystalShovelItem::new);
    public static RegistryObject<Item> crystal_axe = item("crystal_axe", CrystalAxeItem::new);
    //blaze
    public static RegistryObject<Item> blaze_sword = item("blaze_sword", BlazeSwordItem::new);
    public static RegistryObject<Item> blaze_hoe = item("blaze_hoe", BlazeHoeItem::new);
    public static RegistryObject<Item> blaze_pickaxe = item("blaze_pickaxe", BlazePickaxeItem::new);
    public static RegistryObject<Item> blaze_shovel = item("blaze_shovel", BlazeShovelItem::new);
    public static RegistryObject<Item> blaze_axe = item("blaze_axe", BlazeAxeItem::new);

    /**
     * Armor
     */
    public static RegistryObject<Item> infinity_helmet = item("infinity_helmet", () -> new InfinityArmorItem(ArmorItem.Type.HELMET));
    public static RegistryObject<Item> infinity_chestplate = item("infinity_chestplate", () -> new InfinityArmorItem(ArmorItem.Type.CHESTPLATE));
    public static RegistryObject<Item> infinity_pants = item("infinity_pants", () -> new InfinityArmorItem(ArmorItem.Type.LEGGINGS));
    public static RegistryObject<Item> infinity_boots = item("infinity_boots", () -> new InfinityArmorItem(ArmorItem.Type.BOOTS));
    public static RegistryObject<Item> neutron_horse_armor = item("neutron_horse_armor", NeutronHorseArmorItem::new);

    /**
     * Resource
     */
    //fire
    public static RegistryObject<Item> blaze_cube = item("blaze_cube",  (s) -> new ResourceItem(ModRarities.UNCOMMON, s, true));
    //wind
    public static RegistryObject<Item> diamond_lattice = item("diamond_lattice", (s) -> new ResourceItem(ModRarities.UNCOMMON, s, true));
    public static RegistryObject<Item> crystal_matrix_ingot = item("crystal_matrix_ingot", (s) -> new ResourceItem(ModRarities.RARE, s, true));
    //earth
    public static RegistryObject<Item> neutron_pile = item("neutron_pile", (s) -> new ResourceItem(ModRarities.UNCOMMON, s, true));
    public static RegistryObject<Item> neutron_nugget = item("neutron_nugget", (s) -> new ResourceItem(ModRarities.RARE, s, true));
    public static RegistryObject<Item> neutron_ingot = item("neutron_ingot", (s) -> new ResourceItem(ModRarities.EPIC, s, true));
    public static RegistryObject<Item> neutron_gear = item("neutron_gear", NeutronGearItem::new);
    //infinity
    public static RegistryObject<Item> infinity_nugget = item("infinity_nugget", (s) -> new ResourceItem(ModRarities.EPIC, s, true));
    public static RegistryObject<Item> infinity_catalyst = item("infinity_catalyst", (s) -> new ResourceItem(ModRarities.LEGEND, s, true));
    public static RegistryObject<Item> infinity_ingot = item("infinity_ingot", (s) -> new ResourceItem(ModRarities.COSMIC, s, true));
    //singularity
    public static RegistryObject<Item> singularity = item("singularity", () -> new SingularityItem(properties -> properties));
    public static RegistryObject<Item> eternal_singularity = item("eternal_singularity", () -> new EternalSingularityItem(properties -> properties));
    //misc
    public static RegistryObject<Item> record_fragment = item("record_fragment", (s) -> new ResourceItem(ModRarities.RARE, s, true));
    public static RegistryObject<Item> star_fuel = item("star_fuel", StarFuelItem::new);
    public static RegistryObject<Item> endest_pearl = item("endest_pearl", EndestPearlItem::new);
    public static RegistryObject<Item> matter_cluster = item("matter_cluster", MatterClusterItem::new);
    //food
    public static RegistryObject<Item> ultimate_stew = item("ultimate_stew", () -> new BaseItem(pro -> pro.rarity(ModRarities.EPIC).food(ModFoods.ultimate_stew)));
    public static RegistryObject<Item> cosmic_meatballs = item("cosmic_meatballs", () -> new BaseItem(pro -> pro.rarity(ModRarities.EPIC).food(ModFoods.cosmic_meatballs)));

    public static RegistryObject<Item> item(String name) {
        return item(name, (e) -> new BaseItem());
    }

    public static RegistryObject<Item> item(String name, Supplier<Item> item) {
        return ITEMS.register(name, item);
    }

    public static RegistryObject<Item> item(String name, Function<String, Item> item) {
        return ITEMS.register(name, () -> item.apply(name));
    }


}
