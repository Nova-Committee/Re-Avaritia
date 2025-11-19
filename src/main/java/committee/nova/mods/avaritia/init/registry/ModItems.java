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
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Description:
 * Author: cnlimiter
 * Date: 2022/3/31 11:36
 * Version: 1.0
 */
public class ModItems {
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems( Const.MOD_ID);

    //curios
    public static DeferredItem<Item> neutron_ring = item("neutron_ring", NeutronRingItem::new);
    public static DeferredItem<Item> infinity_totem = item("infinity_totem", InfinityTotemItem::new);
    public static DeferredItem<Item> infinity_ring = item("infinity_ring", InfinityRingItem::new);
    public static DeferredItem<Item> infinity_umbrella = item("infinity_umbrella", InfinityUmbrellaItem::new);
    public static DeferredItem<Item> infinity_clock = item("infinity_clock", InfinityClockItem::new);
    public static DeferredItem<Item> side_config_card = item("side_config_card", SideConfigurationCardItem::new);
    /**
     * Tools
     */
    //infinity
    public static DeferredItem<Item> infinity_sword = item("infinity_sword", InfinitySwordItem::new);
    public static DeferredItem<Item> infinity_hoe = item("infinity_hoe", InfinityHoeItem::new);
    public static DeferredItem<Item> infinity_pickaxe = item("infinity_pickaxe", InfinityPickaxeItem::new);
    public static DeferredItem<Item> infinity_shovel = item("infinity_shovel", InfinityShovelItem::new);
    public static DeferredItem<Item> infinity_axe = item("infinity_axe", InfinityAxeItem::new);
    public static DeferredItem<Item> infinity_bucket = item("infinity_bucket", InfinityBucketItem::new);
    public static DeferredItem<Item> infinity_bow = item("infinity_bow", InfinityBowItem::new);
    public static DeferredItem<Item> infinity_crossbow = item("infinity_crossbow", InfinityCrossBowItem::new);
    public static DeferredItem<Item> infinity_shield = item("infinity_shield", InfinityShieldItem::new);
    public static DeferredItem<Item> infinity_trident = item("infinity_trident", InfinityTridentItem::new);
    //crystal
    public static DeferredItem<Item> crystal_sword = item("crystal_sword", CrystalSwordItem::new);
    public static DeferredItem<Item> crystal_hoe = item("crystal_hoe", name -> new CrystalHoeItem());
    public static DeferredItem<Item> crystal_pickaxe = item("crystal_pickaxe", CrystalPickaxeItem::new);
    public static DeferredItem<Item> crystal_shovel = item("crystal_shovel", name -> new CrystalShovelItem());
    public static DeferredItem<Item> crystal_axe = item("crystal_axe", CrystalAxeItem::new);
    public static DeferredItem<Item> crystal_bow = item("crystal_bow", name -> new CrystalBowItem());
    //blaze
    public static DeferredItem<Item> blaze_sword = item("blaze_sword", name -> new BlazeSwordItem());
    public static DeferredItem<Item> blaze_hoe = item("blaze_hoe", name -> new BlazeHoeItem());
    public static DeferredItem<Item> blaze_pickaxe = item("blaze_pickaxe", name -> new BlazePickaxeItem());
    public static DeferredItem<Item> blaze_shovel = item("blaze_shovel", name -> new BlazeShovelItem());
    public static DeferredItem<Item> blaze_axe = item("blaze_axe", name -> new BlazeAxeItem());
    public static DeferredItem<Item> blaze_bow= item("blaze_bow", BlazeBowItem::new);

    /**
     * Armor
     */
    public static DeferredItem<Item> infinity_helmet = item("infinity_helmet", () -> new InfinityArmorItem(ArmorItem.Type.HELMET));
    public static DeferredItem<Item> infinity_chestplate = item("infinity_chestplate", () -> new InfinityArmorItem(ArmorItem.Type.CHESTPLATE));
    public static DeferredItem<Item> infinity_pants = item("infinity_pants", () -> new InfinityArmorItem(ArmorItem.Type.LEGGINGS));
    public static DeferredItem<Item> infinity_boots = item("infinity_boots", () -> new InfinityArmorItem(ArmorItem.Type.BOOTS));
    public static DeferredItem<Item> neutron_horse_armor = item("neutron_horse_armor", NeutronHorseArmorItem::new);
    public static DeferredItem<Item> infinity_elytra = item("infinity_elytra", InfinityElytraItem::new);

    /**
     * Resource
     */
    //fire
    public static DeferredItem<Item> blaze_cube = item("blaze_cube", () -> new ResourceItem(ModRarities.UNCOMMON, true));
    //wind
    public static DeferredItem<Item> diamond_lattice = item("diamond_lattice", () -> new ResourceItem(ModRarities.UNCOMMON, true));
    public static DeferredItem<Item> crystal_matrix_ingot = item("crystal_matrix_ingot", () -> new ResourceItem(ModRarities.RARE, true));
    //earth
    public static DeferredItem<Item> neutron_pile = item("neutron_pile", () -> new ResourceItem(ModRarities.UNCOMMON, true));
    public static DeferredItem<Item> neutron_nugget = item("neutron_nugget", () -> new ResourceItem(ModRarities.RARE, true));
    public static DeferredItem<Item> neutron_ingot = item("neutron_ingot", () -> new ResourceItem(ModRarities.EPIC, true));
    public static DeferredItem<Item> neutron_gear = item("neutron_gear", NeutronGearItem::new);
    //infinity
    public static DeferredItem<Item> infinity_nugget = item("infinity_nugget", () -> new ResourceItem(ModRarities.EPIC, true));
    public static DeferredItem<Item> infinity_catalyst = item("infinity_catalyst", () -> new ResourceItem(ModRarities.LEGEND.getValue(), true));
    public static DeferredItem<Item> infinity_ingot = item("infinity_ingot", () -> new ResourceItem(ModRarities.COSMIC.getValue(), true));
    //singularity
    public static DeferredItem<Item> singularity = item("singularity", SingularityItem::new);
    public static DeferredItem<Item> eternal_singularity = item("eternal_singularity", EternalSingularityItem::new);
    //misc
    public static DeferredItem<Item> record_fragment = item("record_fragment", () -> new ResourceItem(ModRarities.RARE, true));
    public static DeferredItem<Item> star_fuel = item("star_fuel", StarFuelItem::new);
    public static DeferredItem<Item> refined_coal = item("refined_coal", RefinedCoalItem::new);
    public static DeferredItem<Item> endest_pearl = item("endest_pearl", EndestPearlItem::new);
    public static DeferredItem<Item> matter_cluster = item("matter_cluster", MatterClusterItem::new);
    public static DeferredItem<Item> full_matter_cluster = item("full_matter_cluster", () -> new Item(new Item.Properties().stacksTo(1).rarity(ModRarities.RARE)));
    public static DeferredItem<Item> enhancement_core = item("enhancement_core", EnhancementCoreItem::new);
    public static DeferredItem<Item> upgrade_smithing_template = item("upgrade_smithing_template", UpgradeSmithingTemplateItem::new);
    public static DeferredItem<Item> infinity_upgrade = item("infinity_upgrade", InfinityUpgradeItem::new);
    //food
    public static DeferredItem<Item> ultimate_stew = item("ultimate_stew", () -> new BaseItem(pro -> pro.rarity(ModRarities.EPIC).food(ModFoods.ultimate_stew)));
    public static DeferredItem<Item> cosmic_meatballs = item("cosmic_meatballs", () -> new BaseItem(pro -> pro.rarity(ModRarities.EPIC).food(ModFoods.cosmic_meatballs)));
    public static DeferredItem<Item> forge_energy = item("forge_energy", false);

    static {
        ModBlocks.BLOCK_ITEMS.forEach((s, blockItemSupplier) -> ModItems.item(s, blockItemSupplier::get));
    }

    public static DeferredItem<Item> item(String name) {
        return item(name, true);
    }

    public static DeferredItem<Item> blockItem(String name, Supplier<Block> block, Item.Properties properties, boolean exist) {
        return item(name, (e) -> new BlockItem(block.get(), properties), exist);
    }

    public static DeferredItem<Item> item(String name, boolean exist) {
        return item(name, (e) -> new BaseItem(), exist);
    }

    public static DeferredItem<Item> item(String name, Function<String, Item> item) {
        return item(name, item, true);
    }

    public static DeferredItem<Item> item(String name, Function<String, Item> item, boolean exist) {
        return item(name, () -> item.apply(name), exist);
    }

    public static DeferredItem<Item> item(String name, Supplier<Item> item) {
        return item(name, item, true);
    }

    public static DeferredItem<Item> item(String name, Supplier<Item> item, boolean exist) {
        var regItem = ITEMS.register(name, item);
        if (exist) ModCreativeModeTabs.ACCEPT_ITEM.add(regItem);
        return regItem;
    }

}
