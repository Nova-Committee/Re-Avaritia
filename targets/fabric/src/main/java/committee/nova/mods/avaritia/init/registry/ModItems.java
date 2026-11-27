package committee.nova.mods.avaritia.init.registry;

import committee.nova.mods.avaritia.Const;
import committee.nova.mods.avaritia.api.common.item.BaseItem;
import committee.nova.mods.avaritia.common.item.block.InfinityChestItem;
import committee.nova.mods.avaritia.common.item.misc.*;
import committee.nova.mods.avaritia.common.item.resources.*;
import committee.nova.mods.avaritia.common.item.singularity.EternalSingularityItem;
import committee.nova.mods.avaritia.common.item.singularity.SingularityItem;
import committee.nova.mods.avaritia.common.item.tools.InfinityArmorItem;
import committee.nova.mods.avaritia.common.item.tools.blaze.*;
import committee.nova.mods.avaritia.common.item.tools.crystal.*;
import committee.nova.mods.avaritia.common.item.tools.infinity.*;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import committee.nova.mods.avaritia.api.init.registry.RegistryEntries;
import net.minecraft.core.registries.BuiltInRegistries;
import committee.nova.mods.avaritia.api.init.registry.RegistryEntry;

import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Description:
 * @author cnlimiter
 * Date: 2022/3/31 11:36
 * Version: 1.0
 */
public class ModItems {
    public static final RegistryEntries<Item> ITEMS = RegistryEntries.create(BuiltInRegistries.ITEM, Const.MOD_ID);

    //test
    public static RegistryEntry<Item> test_sword = item("test_sword", () -> new Item(new Item.Properties()), false);
    //curios
    public static RegistryEntry<Item> neutron_ring = item("neutron_ring", NeutronRingItem::new);
    public static RegistryEntry<Item> infinity_totem = item("infinity_totem", InfinityTotemItem::new);
    public static RegistryEntry<Item> infinity_ring = item("infinity_ring", InfinityRingItem::new);
    public static RegistryEntry<Item> infinity_umbrella = item("infinity_umbrella", InfinityUmbrellaItem::new);
    public static RegistryEntry<Item> infinity_clock = item("infinity_clock", InfinityClockItem::new);
    public static RegistryEntry<Item> infinity_chest = item("infinity_chest",
            () -> new InfinityChestItem(ModBlocks.infinity_chest.get()));
    public static RegistryEntry<Item> side_config_card = item("side_config_card", SideConfigurationCardItem::new);
    /**
     * Tools
     */
    //infinity
    public static RegistryEntry<Item> infinity_sword = item("infinity_sword", InfinitySwordItem::new);
    public static RegistryEntry<Item> infinity_hoe = item("infinity_hoe", InfinityHoeItem::new);
    public static RegistryEntry<Item> infinity_pickaxe = item("infinity_pickaxe", InfinityPickaxeItem::new);
    public static RegistryEntry<Item> infinity_shovel = item("infinity_shovel", InfinityShovelItem::new);
    public static RegistryEntry<Item> infinity_axe = item("infinity_axe", InfinityAxeItem::new);
    public static RegistryEntry<Item> infinity_bucket = item("infinity_bucket", InfinityBucketItem::new);
    public static RegistryEntry<Item> infinity_bow = item("infinity_bow", InfinityBowItem::new);
    public static RegistryEntry<Item> infinity_crossbow = item("infinity_crossbow", InfinityCrossBowItem::new);
    public static RegistryEntry<Item> infinity_shield = item("infinity_shield", InfinityShieldItem::new);
    public static RegistryEntry<Item> infinity_trident = item("infinity_trident", InfinityTridentItem::new);

    //crystal
    public static RegistryEntry<Item> crystal_sword = item("crystal_sword", CrystalSwordItem::new);
    public static RegistryEntry<Item> crystal_hoe = item("crystal_hoe", CrystalHoeItem::new);
    public static RegistryEntry<Item> crystal_pickaxe = item("crystal_pickaxe", CrystalPickaxeItem::new);
    public static RegistryEntry<Item> crystal_shovel = item("crystal_shovel", CrystalShovelItem::new);
    public static RegistryEntry<Item> crystal_axe = item("crystal_axe", CrystalAxeItem::new);
    public static RegistryEntry<Item> crystal_bow = item("crystal_bow", CrystalBowItem::new);
    //blaze
    public static RegistryEntry<Item> blaze_sword = item("blaze_sword", BlazeSwordItem::new);
    public static RegistryEntry<Item> blaze_hoe = item("blaze_hoe", BlazeHoeItem::new);
    public static RegistryEntry<Item> blaze_pickaxe = item("blaze_pickaxe", BlazePickaxeItem::new);
    public static RegistryEntry<Item> blaze_shovel = item("blaze_shovel", BlazeShovelItem::new);
    public static RegistryEntry<Item> blaze_axe = item("blaze_axe", BlazeAxeItem::new);
    public static RegistryEntry<Item> blaze_bow= item("blaze_bow", BlazeBowItem::new);

    /**
     * Armor
     */
    public static RegistryEntry<Item> infinity_helmet = item("infinity_helmet", () -> new InfinityArmorItem(ArmorItem.Type.HELMET));
    public static RegistryEntry<Item> infinity_chestplate = item("infinity_chestplate", () -> new InfinityArmorItem(ArmorItem.Type.CHESTPLATE));
    public static RegistryEntry<Item> infinity_pants = item("infinity_pants", () -> new InfinityArmorItem(ArmorItem.Type.LEGGINGS));
    public static RegistryEntry<Item> infinity_boots = item("infinity_boots", () -> new InfinityArmorItem(ArmorItem.Type.BOOTS));
    public static RegistryEntry<Item> neutron_horse_armor = item("neutron_horse_armor", NeutronHorseArmorItem::new);
    public static RegistryEntry<Item> infinity_elytra = item("infinity_elytra", InfinityElytraItem::new);

    /**
     * Resource
     */
    //fire
    public static RegistryEntry<Item> blaze_cube = item("blaze_cube", () -> new ResourceItem(ModRarities.UNCOMMON, true));
    //wind
    public static RegistryEntry<Item> diamond_lattice = item("diamond_lattice", () -> new ResourceItem(ModRarities.UNCOMMON, true));
    public static RegistryEntry<Item> crystal_matrix_ingot = item("crystal_matrix_ingot", (s) -> new ResourceItem(ModRarities.RARE, true));
    //earth
    public static RegistryEntry<Item> neutron_pile = item("neutron_pile", () -> new ResourceItem(ModRarities.UNCOMMON, true));
    public static RegistryEntry<Item> neutron_nugget = item("neutron_nugget", () -> new ResourceItem(ModRarities.RARE, true));
    public static RegistryEntry<Item> neutron_ingot = item("neutron_ingot", () -> new ResourceItem(ModRarities.EPIC, true));
    public static RegistryEntry<Item> neutron_gear = item("neutron_gear", NeutronGearItem::new);
    //infinity
    public static RegistryEntry<Item> infinity_nugget = item("infinity_nugget", () -> new ResourceItem(ModRarities.EPIC, true));
    public static RegistryEntry<Item> infinity_catalyst = item("infinity_catalyst", () -> new ResourceItem(ModRarities.LEGEND, true));
    public static RegistryEntry<Item> infinity_ingot = item("infinity_ingot", () -> new ResourceItem(ModRarities.COSMIC, true));
    //singularity
    public static RegistryEntry<Item> singularity = item("singularity", SingularityItem::new);
    public static RegistryEntry<Item> eternal_singularity = item("eternal_singularity", EternalSingularityItem::new);
    //misc
    public static RegistryEntry<Item> record_fragment = item("record_fragment", () -> new ResourceItem(ModRarities.RARE, true));
    public static RegistryEntry<Item> star_fuel = item("star_fuel", StarFuelItem::new);
    public static RegistryEntry<Item> refined_coal = item("refined_coal", RefinedCoalItem::new);
    public static RegistryEntry<Item> endest_pearl = item("endest_pearl", EndestPearlItem::new);
    public static RegistryEntry<Item> matter_cluster = item("matter_cluster", MatterClusterItem::new);
    public static RegistryEntry<Item> full_matter_cluster = item("full_matter_cluster", () -> new Item(new Item.Properties().stacksTo(1).rarity(ModRarities.RARE)));
    public static RegistryEntry<Item> enhancement_core = item("enhancement_core", EnhancementCoreItem::new);
    public static RegistryEntry<Item> upgrade_smithing_template = item("upgrade_smithing_template", UpgradeSmithingTemplateItem::new);
    public static RegistryEntry<Item> infinity_upgrade = item("infinity_upgrade", InfinityUpgradeItem::new);
    //food
    public static RegistryEntry<Item> ultimate_stew = item("ultimate_stew", () -> new BaseItem(pro -> pro.rarity(ModRarities.EPIC).food(ModFoods.ultimate_stew)));
    public static RegistryEntry<Item> cosmic_meatballs = item("cosmic_meatballs", () -> new BaseItem(pro -> pro.rarity(ModRarities.EPIC).food(ModFoods.cosmic_meatballs)));
    public static RegistryEntry<Item> forge_energy = item("forge_energy", false);

    public static RegistryEntry<Item> item(String name) {
        return item(name, true);
    }

    public static RegistryEntry<Item> blockItem(String name, Supplier<Block> block, Item.Properties properties, boolean exist) {
        return item(name, (e) -> new BlockItem(block.get(), properties), exist);
    }

    public static RegistryEntry<Item> item(String name, boolean exist) {
        return item(name, (e) -> new BaseItem(), exist);
    }

    public static RegistryEntry<Item> item(String name, Function<String, Item> item) {
        return item(name, item, true);
    }

    public static RegistryEntry<Item> item(String name, Function<String, Item> item, boolean exist) {
        return item(name, () -> item.apply(name), exist);
    }

    public static RegistryEntry<Item> item(String name, Supplier<Item> item) {
        return item(name, item, true);
    }

    public static RegistryEntry<Item> item(String name, Supplier<Item> item, boolean exist) {
        var regItem = ITEMS.register(name, item);
        if (exist) ModCreativeModeTabs.ACCEPT_ITEM.add(regItem);
        return regItem;
    }

}
