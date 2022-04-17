package nova.committee.avaritia.init.registry;

import net.minecraft.ChatFormatting;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.IForgeRegistry;
import nova.committee.avaritia.Static;
import nova.committee.avaritia.common.item.EndestPearlItem;
import nova.committee.avaritia.common.item.MatterClusterItem;
import nova.committee.avaritia.common.item.ResourceItem;
import nova.committee.avaritia.common.item.singularity.SingularityItem;
import nova.committee.avaritia.common.item.tools.BowInfinityItem;
import nova.committee.avaritia.common.item.tools.PickaxeInfinityItem;
import nova.committee.avaritia.common.item.tools.SwordInfinityItem;
import nova.committee.avaritia.common.item.tools.SwordSkullsItem;
import nova.committee.avaritia.init.ModFoods;

/**
 * Description:
 * Author: cnlimiter
 * Date: 2022/3/31 11:36
 * Version: 1.0
 */
@Mod.EventBusSubscriber(modid = Static.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class ModItems {
    public static Item pick_axe;
    public static Item matter_cluster;

    public static Item infinity_sword;
    public static Item skull_sword;
    public static Item infinity_bow;


    public static Item ultimate_stew;
    public static Item cosmic_meatballs;
    public static Item endest_pearl;


    public static Item diamond_lattice;
    public static Item crystal_matrix_ingot;
    public static Item neutron_pile;
    public static Item neutron_nugget;
    public static Item neutronium_ingot;
    public static Item infinity_catalyst;
    public static Item infinity_ingot;
    public static Item record_fragment;

    public static Item singularity;


    public static Rarity COSMIC_RARITY = Rarity.create("COSMIC", ChatFormatting.RED);


    @SubscribeEvent
    public static void registerItems(RegistryEvent.Register<Item> event) {
        final IForgeRegistry<Item> registry = event.getRegistry();

        registry.registerAll(
                pick_axe = new PickaxeInfinityItem(),
                matter_cluster = new MatterClusterItem(),

                infinity_sword = new SwordInfinityItem(),
                skull_sword = new SwordSkullsItem(),
                infinity_bow = new BowInfinityItem(),
                endest_pearl = new EndestPearlItem(),

                ultimate_stew = new Item(new Item.Properties().tab(ModTab.TAB).food(ModFoods.ultimate_stew)).setRegistryName("ultimate_stew"),
                cosmic_meatballs = new Item(new Item.Properties().tab(ModTab.TAB).food(ModFoods.cosmic_meatballs)).setRegistryName("cosmic_meatballs"),

                diamond_lattice = new ResourceItem(Rarity.UNCOMMON, "diamond_lattice"),
                crystal_matrix_ingot = new ResourceItem(Rarity.RARE, "crystal_matrix_ingot"),
                neutron_pile = new ResourceItem(Rarity.UNCOMMON, "neutron_pile"),
                neutron_nugget = new ResourceItem(Rarity.UNCOMMON, "neutron_nugget"),
                neutronium_ingot = new ResourceItem(Rarity.RARE, "neutronium_ingot"),
                infinity_catalyst = new ResourceItem(Rarity.EPIC, "infinity_catalyst"),
                infinity_ingot = new ResourceItem(COSMIC_RARITY, "infinity_ingot"),
                record_fragment = new ResourceItem(COSMIC_RARITY, "record_fragment"),
                singularity = new SingularityItem()
        );

    }


}
