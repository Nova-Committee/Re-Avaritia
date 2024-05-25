package committee.nova.mods.avaritia.init.registry;

import com.chocohead.mm.api.ClassTinkerers;
import committee.nova.mods.avaritia.Static;
import committee.nova.mods.avaritia.api.common.item.BaseBlockItem;
import committee.nova.mods.avaritia.api.common.item.BaseItem;
import committee.nova.mods.avaritia.common.block.CompressorBlock;
import committee.nova.mods.avaritia.common.block.ExtremeCraftingTableBlock;
import committee.nova.mods.avaritia.common.block.ResourceBlock;
import committee.nova.mods.avaritia.common.block.cake.EndlessCakeBlock;
import committee.nova.mods.avaritia.common.block.collector.DefaultNeutronCollectorBlock;
import committee.nova.mods.avaritia.common.block.collector.DenseNeutronCollectorBlock;
import committee.nova.mods.avaritia.common.block.collector.DenserNeutronCollectorBlock;
import committee.nova.mods.avaritia.common.block.collector.DensestNeutronCollectorBlock;
import committee.nova.mods.avaritia.common.block.craft.CompressedCraftingTableBlock;
import committee.nova.mods.avaritia.common.block.craft.DoubleCompressedCraftingTableBlock;
import committee.nova.mods.avaritia.common.item.ArmorInfinityItem;
import committee.nova.mods.avaritia.common.item.EndestPearlItem;
import committee.nova.mods.avaritia.common.item.MatterClusterItem;
import committee.nova.mods.avaritia.common.item.resources.ResourceItem;
import committee.nova.mods.avaritia.common.item.singularity.SingularityItem;
import committee.nova.mods.avaritia.common.item.tools.*;
import io.github.fabricators_of_create.porting_lib.extensions.extensions.BlockItemExtensions;
import io.github.fabricators_of_create.porting_lib.util.LazyRegistrar;
import io.github.fabricators_of_create.porting_lib.util.RegistryObject;
import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.level.block.Block;

import java.util.function.Supplier;

import static net.minecraft.world.item.Rarity.RARE;

/**
 * Description:
 * Author: cnlimiter
 * Date: 2022/3/31 11:36
 * Version: 1.0
 */
public class ModItems {
    public static final LazyRegistrar<Item> ITEMS = LazyRegistrar.create(BuiltInRegistries.ITEM, Static.MOD_ID);
    public static void init() {
        Static.LOGGER.info("Registering Mod Items...");
        ITEMS.register();
    }
    public static final Rarity COSMIC_RARITY = ClassTinkerers.getEnum(Rarity.class, "COSMIC");

    //CRAFTING
    public static RegistryObject<Item> compressed_crafting_table = blockItem("compressed_crafting_table", ModBlocks.compressed_crafting_table, Rarity.UNCOMMON);
    public static RegistryObject<Item> double_compressed_crafting_table = blockItem("double_compressed_crafting_table", ModBlocks.double_compressed_crafting_table, Rarity.UNCOMMON);
    //RESOURCE
    public static RegistryObject<Item> neutron = blockItem("neutron", ModBlocks.neutron, Rarity.EPIC);
    public static RegistryObject<Item> infinity = blockItem("infinity", ModBlocks.infinity, COSMIC_RARITY);
    public static RegistryObject<Item> crystal_matrix = blockItem("crystal_matrix", ModBlocks.crystal_matrix, Rarity.RARE);
    //MACHINE
    public static RegistryObject<Item> extreme_crafting_table = blockItem("extreme_crafting_table", ModBlocks.extreme_crafting_table, RARE);
    public static RegistryObject<Item> neutron_collector = blockItem("neutron_collector", ModBlocks.neutron_collector, Rarity.RARE);
    public static RegistryObject<Item> dense_neutron_collector = blockItem("dense_neutron_collector", ModBlocks.dense_neutron_collector, Rarity.RARE);
    public static RegistryObject<Item> denser_neutron_collector = blockItem("denser_neutron_collector", ModBlocks.denser_neutron_collector, Rarity.EPIC);
    public static RegistryObject<Item> densest_neutron_collector = blockItem("densest_neutron_collector", ModBlocks.densest_neutron_collector, COSMIC_RARITY);
    public static RegistryObject<Item> neutron_compressor = blockItem("neutron_compressor", ModBlocks.neutron_compressor, Rarity.RARE);
    //CAKE
    public static RegistryObject<Item> endless_cake = blockItem("endless_cake", ModBlocks.endless_cake, Rarity.UNCOMMON);

    //tools
    public static RegistryObject<Item> infinity_pickaxe = item("infinity_pickaxe", InfinityPickaxeItem::new);
    public static RegistryObject<Item> infinity_shovel = item("infinity_shovel", InfinityShovelItem::new);
    public static RegistryObject<Item> infinity_axe = item("infinity_axe", InfinityAxeItem::new);
    public static RegistryObject<Item> infinity_hoe = item("infinity_hoe", InfinityHoeItem::new);
    public static RegistryObject<Item> matter_cluster = item("matter_cluster", MatterClusterItem::new);
    //weapons
    public static RegistryObject<Item> infinity_sword = item("infinity_sword", InfinitySwordItem::new);
    public static RegistryObject<Item> skull_sword = item("skull_fire_sword", SkullsSwordItem::new);
    public static RegistryObject<Item> infinity_bow = item("infinity_bow", InfinityBowItem::new);
    //armors
    public static RegistryObject<Item> infinity_helmet = item("infinity_helmet", () -> new ArmorInfinityItem(ArmorItem.Type.HELMET));
    public static RegistryObject<Item> infinity_chestplate = item("infinity_chestplate", () -> new ArmorInfinityItem(ArmorItem.Type.CHESTPLATE));
    public static RegistryObject<Item> infinity_pants = item("infinity_pants", () -> new ArmorInfinityItem(ArmorItem.Type.LEGGINGS));
    public static RegistryObject<Item> infinity_boots = item("infinity_boots", () -> new ArmorInfinityItem(ArmorItem.Type.BOOTS));
    public static RegistryObject<Item> ultimate_stew = item("ultimate_stew", () -> new Item(new Item.Properties().rarity(Rarity.EPIC).food(ModFoods.ultimate_stew)));
    public static RegistryObject<Item> cosmic_meatballs = item("cosmic_meatballs", () -> new Item(new Item.Properties().rarity(Rarity.EPIC).food(ModFoods.cosmic_meatballs)));
    //resource
    public static RegistryObject<Item> endest_pearl = item("endest_pearl", EndestPearlItem::new);
    public static RegistryObject<Item> diamond_lattice = item("diamond_lattice", () -> new ResourceItem(Rarity.UNCOMMON, "diamond_lattice", true));
    public static RegistryObject<Item> crystal_matrix_ingot = item("crystal_matrix_ingot", () -> new ResourceItem(Rarity.RARE, "crystal_matrix_ingot", true));
    public static RegistryObject<Item> neutron_pile = item("neutron_pile", () -> new ResourceItem(Rarity.UNCOMMON, "neutron_pile", true));
    public static RegistryObject<Item> neutron_nugget = item("neutron_nugget", () -> new ResourceItem(Rarity.UNCOMMON, "neutron_nugget", true));
    public static RegistryObject<Item> neutron_ingot = item("neutron_ingot", () -> new ResourceItem(Rarity.RARE, "neutron_ingot", true));
    public static RegistryObject<Item> neutron_gear = item("neutron_gear", () -> new ResourceItem(Rarity.RARE, "neutron_gear", true));
    public static RegistryObject<Item> infinity_nugget = item("infinity_nugget", () -> new ResourceItem(Rarity.EPIC, "infinity_nugget", true));
    public static RegistryObject<Item> infinity_catalyst = item("infinity_catalyst", () -> new ResourceItem(Rarity.EPIC, "infinity_catalyst", true));
    public static RegistryObject<Item> infinity_ingot = item("infinity_ingot", () -> new ResourceItem(COSMIC_RARITY, "infinity_ingot", true));

    public static RegistryObject<Item> singularity = item("singularity", () -> new SingularityItem(properties -> properties));

    public static RegistryObject<Item> infinity_totem = item("infinity_totem",
            () -> new ResourceItem(Rarity.UNCOMMON, "infinity_totem", true,
                    new FabricItemSettings().stacksTo(1).durability(999)));
    public static RegistryObject<Item> infinity_horse_armor = item("infinity_horse_armor",
            () -> new InfinityHorseArmorItem(
                    new FabricItemSettings().stacksTo(1).rarity(COSMIC_RARITY)
                            .fireResistant()
            ));

    public static RegistryObject<Item> star_fuel = item("star_fuel", () -> new ResourceItem(Rarity.UNCOMMON, "diamond_lattice", true));
    public static RegistryObject<Item> record_fragment = item("record_fragment", () -> new ResourceItem(Rarity.EPIC, "record_fragment", true));



    public static RegistryObject<Item> item(String name) {
        return item(name, BaseItem::new);
    }

    public static RegistryObject<Item> item(String name, Supplier<Item> item) {
        return ITEMS.register(name, item);
    }

    public static RegistryObject<Item> blockItem(String name, Supplier<Block> block, Rarity rarity) {
        return ITEMS.register(name, new BlockItem(block.get(), new FabricItemSettings().rarity(rarity)));
    }


}
