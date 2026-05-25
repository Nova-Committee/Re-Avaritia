package com.avaritia.init.registry;

import com.avaritia.Avaritia;
import com.avaritia.api.common.item.BaseItem;
import com.avaritia.common.item.misc.*;
import com.avaritia.common.item.resources.*;
import com.avaritia.common.item.singularity.EternalSingularityItem;
import com.avaritia.common.item.singularity.SingularityItem;
import com.avaritia.common.item.tools.InfinityArmorItem;
import com.avaritia.common.item.tools.blaze.*;
import com.avaritia.common.item.tools.crystal.*;
import com.avaritia.common.item.tools.infinity.*;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.equipment.ArmorType;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * 注册模组中的所有物品。
 *
 * <p>NeoForge 26.1.2 要求每个物品的 {@link Item.Properties} 显式设置注册 id，
 * 因此所有工厂方法都必须使用注册回调传入的 {@link Identifier} 创建属性。</p>
 */
public class ModItems {
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(Avaritia.MOD_ID);
    public static final Map<String, Function<Identifier, ? extends BlockItem>> BLOCK_ITEMS = new LinkedHashMap<>();

    // curios
    public static final DeferredItem<Item> neutron_ring = item("neutron_ring", id -> new NeutronRingItem(new Item.Properties().setId(ResourceKey.create(Registries.ITEM, id))), false);
    public static final DeferredItem<Item> infinity_totem = item("infinity_totem", id -> new InfinityTotemItem(new Item.Properties().setId(ResourceKey.create(Registries.ITEM, id))));
    public static final DeferredItem<Item> infinity_ring = item("infinity_ring", id -> new InfinityRingItem(new Item.Properties().setId(ResourceKey.create(Registries.ITEM, id))), false);
    public static final DeferredItem<Item> infinity_umbrella = item("infinity_umbrella", id -> new InfinityUmbrellaItem(new Item.Properties().setId(ResourceKey.create(Registries.ITEM, id))));
    public static final DeferredItem<Item> infinity_clock = item("infinity_clock", id -> new InfinityClockItem(new Item.Properties().setId(ResourceKey.create(Registries.ITEM, id))));
    public static final DeferredItem<Item> side_config_card = item("side_config_card", id -> new SideConfigurationCardItem(new Item.Properties().setId(ResourceKey.create(Registries.ITEM, id))));

    /** 工具 */
    // infinity
    public static final DeferredItem<Item> infinity_sword = item("infinity_sword", id -> new InfinitySwordItem(new Item.Properties().setId(ResourceKey.create(Registries.ITEM, id))));
    public static final DeferredItem<Item> infinity_hoe = item("infinity_hoe", id -> new InfinityHoeItem(new Item.Properties().setId(ResourceKey.create(Registries.ITEM, id))));
    public static final DeferredItem<Item> infinity_pickaxe = item("infinity_pickaxe", id -> new InfinityPickaxeItem(new Item.Properties().setId(ResourceKey.create(Registries.ITEM, id))));
    public static final DeferredItem<Item> infinity_shovel = item("infinity_shovel", id -> new InfinityShovelItem(new Item.Properties().setId(ResourceKey.create(Registries.ITEM, id))));
    public static final DeferredItem<Item> infinity_axe = item("infinity_axe", id -> new InfinityAxeItem(new Item.Properties().setId(ResourceKey.create(Registries.ITEM, id))));
    public static final DeferredItem<Item> infinity_bucket = item("infinity_bucket", id -> new InfinityBucketItem(new Item.Properties().setId(ResourceKey.create(Registries.ITEM, id))));
    public static final DeferredItem<Item> infinity_bow = item("infinity_bow", id -> new InfinityBowItem(new Item.Properties().setId(ResourceKey.create(Registries.ITEM, id))));
    public static final DeferredItem<Item> infinity_crossbow = item("infinity_crossbow", id -> new InfinityCrossBowItem(new Item.Properties().setId(ResourceKey.create(Registries.ITEM, id))));
    public static final DeferredItem<Item> infinity_shield = item("infinity_shield", id -> new InfinityShieldItem(new Item.Properties().setId(ResourceKey.create(Registries.ITEM, id))));
    public static final DeferredItem<Item> infinity_trident = item("infinity_trident", id -> new InfinityTridentItem(new Item.Properties().setId(ResourceKey.create(Registries.ITEM, id))));
    public static final DeferredItem<Item> infinity_mace = item("infinity_mace", id -> new InfinityMaceItem(new Item.Properties().setId(ResourceKey.create(Registries.ITEM, id))));

    // crystal
    public static final DeferredItem<Item> crystal_sword = item("crystal_sword", id -> new CrystalSwordItem(new Item.Properties().setId(ResourceKey.create(Registries.ITEM, id))));
    public static final DeferredItem<Item> crystal_hoe = item("crystal_hoe", id -> new CrystalHoeItem(new Item.Properties().setId(ResourceKey.create(Registries.ITEM, id))));
    public static final DeferredItem<Item> crystal_pickaxe = item("crystal_pickaxe", id -> new CrystalPickaxeItem(new Item.Properties().setId(ResourceKey.create(Registries.ITEM, id))));
    public static final DeferredItem<Item> crystal_shovel = item("crystal_shovel", id -> new CrystalShovelItem(new Item.Properties().setId(ResourceKey.create(Registries.ITEM, id))));
    public static final DeferredItem<Item> crystal_axe = item("crystal_axe", id -> new CrystalAxeItem(new Item.Properties().setId(ResourceKey.create(Registries.ITEM, id))));
    public static final DeferredItem<Item> crystal_bow = item("crystal_bow", id -> new CrystalBowItem(new Item.Properties().setId(ResourceKey.create(Registries.ITEM, id))));

    // blaze
    public static final DeferredItem<Item> blaze_sword = item("blaze_sword", id -> new BlazeSwordItem(new Item.Properties().setId(ResourceKey.create(Registries.ITEM, id))));
    public static final DeferredItem<Item> blaze_hoe = item("blaze_hoe", id -> new BlazeHoeItem(new Item.Properties().setId(ResourceKey.create(Registries.ITEM, id))));
    public static final DeferredItem<Item> blaze_pickaxe = item("blaze_pickaxe", id -> new BlazePickaxeItem(new Item.Properties().setId(ResourceKey.create(Registries.ITEM, id))));
    public static final DeferredItem<Item> blaze_shovel = item("blaze_shovel", id -> new BlazeShovelItem(new Item.Properties().setId(ResourceKey.create(Registries.ITEM, id))));
    public static final DeferredItem<Item> blaze_axe = item("blaze_axe", id -> new BlazeAxeItem(new Item.Properties().setId(ResourceKey.create(Registries.ITEM, id))));
    public static final DeferredItem<Item> blaze_bow = item("blaze_bow", id -> new BlazeBowItem(new Item.Properties().setId(ResourceKey.create(Registries.ITEM, id))));

    /** 护甲 */
    public static final DeferredItem<Item> infinity_helmet = item("infinity_helmet", id -> new InfinityArmorItem(ArmorType.HELMET, new Item.Properties().setId(ResourceKey.create(Registries.ITEM, id))));
    public static final DeferredItem<Item> infinity_chestplate = item("infinity_chestplate", id -> new InfinityArmorItem(ArmorType.CHESTPLATE, new Item.Properties().setId(ResourceKey.create(Registries.ITEM, id))));
    public static final DeferredItem<Item> infinity_pants = item("infinity_pants", id -> new InfinityArmorItem(ArmorType.LEGGINGS, new Item.Properties().setId(ResourceKey.create(Registries.ITEM, id))));
    public static final DeferredItem<Item> infinity_boots = item("infinity_boots", id -> new InfinityArmorItem(ArmorType.BOOTS, new Item.Properties().setId(ResourceKey.create(Registries.ITEM, id))));
    public static final DeferredItem<Item> neutron_horse_armor = item("neutron_horse_armor", id -> new NeutronHorseArmorItem(new Item.Properties().setId(ResourceKey.create(Registries.ITEM, id))));
    public static final DeferredItem<Item> infinity_elytra = item("infinity_elytra", id -> new InfinityElytraItem(new Item.Properties().setId(ResourceKey.create(Registries.ITEM, id))));

    /** 资源 */
    // fire
    public static final DeferredItem<Item> blaze_cube = item("blaze_cube", id -> new ResourceItem(ModRarities.UNCOMMON, true, new Item.Properties().setId(ResourceKey.create(Registries.ITEM, id)).fireResistant()));
    // wind
    public static final DeferredItem<Item> diamond_lattice = item("diamond_lattice", id -> new ResourceItem(ModRarities.UNCOMMON, true, new Item.Properties().setId(ResourceKey.create(Registries.ITEM, id))));
    public static final DeferredItem<Item> crystal_matrix_ingot = item("crystal_matrix_ingot", id -> new ResourceItem(ModRarities.RARE, true, new Item.Properties().setId(ResourceKey.create(Registries.ITEM, id)).fireResistant()));
    // earth
    public static final DeferredItem<Item> neutron_pile = item("neutron_pile", id -> new ResourceItem(ModRarities.UNCOMMON, true, new Item.Properties().setId(ResourceKey.create(Registries.ITEM, id))));
    public static final DeferredItem<Item> neutron_nugget = item("neutron_nugget", id -> new ResourceItem(ModRarities.RARE, true, new Item.Properties().setId(ResourceKey.create(Registries.ITEM, id))));
    public static final DeferredItem<Item> neutron_ingot = item("neutron_ingot", id -> new ResourceItem(ModRarities.EPIC, true, new Item.Properties().setId(ResourceKey.create(Registries.ITEM, id))));
    public static final DeferredItem<Item> neutron_gear = item("neutron_gear", id -> new NeutronGearItem(new Item.Properties().setId(ResourceKey.create(Registries.ITEM, id))));
    // infinity
    public static final DeferredItem<Item> infinity_nugget = item("infinity_nugget", id -> new ResourceItem(ModRarities.EPIC, true, new Item.Properties().setId(ResourceKey.create(Registries.ITEM, id)).fireResistant()));
    public static final DeferredItem<Item> infinity_catalyst = item("infinity_catalyst", id -> new ResourceItem(ModRarities.LEGEND.getValue(), true, new Item.Properties().setId(ResourceKey.create(Registries.ITEM, id)).fireResistant()));
    public static final DeferredItem<Item> infinity_ingot = item("infinity_ingot", id -> new ResourceItem(ModRarities.COSMIC.getValue(), true, new Item.Properties().setId(ResourceKey.create(Registries.ITEM, id)).fireResistant()));
    // singularity
    public static final DeferredItem<Item> singularity = item("singularity", id -> new SingularityItem(new Item.Properties().setId(ResourceKey.create(Registries.ITEM, id))));
    public static final DeferredItem<Item> eternal_singularity = item("eternal_singularity", id -> new EternalSingularityItem(new Item.Properties().setId(ResourceKey.create(Registries.ITEM, id))));
    // misc
    public static final DeferredItem<Item> record_fragment = item("record_fragment", id -> new ResourceItem(ModRarities.RARE, true, new Item.Properties().setId(ResourceKey.create(Registries.ITEM, id))));
    public static final DeferredItem<Item> star_fuel = item("star_fuel", id -> new StarFuelItem(new Item.Properties().setId(ResourceKey.create(Registries.ITEM, id))));
    public static final DeferredItem<Item> refined_coal = item("refined_coal", id -> new RefinedCoalItem(new Item.Properties().setId(ResourceKey.create(Registries.ITEM, id))));
    public static final DeferredItem<Item> endest_pearl = item("endest_pearl", id -> new EndestPearlItem(new Item.Properties().setId(ResourceKey.create(Registries.ITEM, id))));
    public static final DeferredItem<Item> matter_cluster = item("matter_cluster", id -> new MatterClusterItem(new Item.Properties().setId(ResourceKey.create(Registries.ITEM, id))));
    public static final DeferredItem<Item> full_matter_cluster = item("full_matter_cluster", id -> new Item(new Item.Properties().setId(ResourceKey.create(Registries.ITEM, id)).stacksTo(1).rarity(ModRarities.RARE)));
    public static final DeferredItem<Item> enhancement_core = item("enhancement_core", id -> new EnhancementCoreItem(new Item.Properties().setId(ResourceKey.create(Registries.ITEM, id))));
    public static final DeferredItem<Item> upgrade_smithing_template = item("upgrade_smithing_template", id -> new UpgradeSmithingTemplateItem(new Item.Properties().setId(ResourceKey.create(Registries.ITEM, id))));
    public static final DeferredItem<Item> infinity_upgrade = item("infinity_upgrade", id -> new InfinityUpgradeItem(new Item.Properties().setId(ResourceKey.create(Registries.ITEM, id))));
    // food
    public static final DeferredItem<Item> ultimate_stew = item("ultimate_stew", id -> new BaseItem(pro -> pro.setId(ResourceKey.create(Registries.ITEM, id)).rarity(ModRarities.EPIC).food(ModFoods.ultimate_stew)));
    public static final DeferredItem<Item> cosmic_meatballs = item("cosmic_meatballs", id -> new BaseItem(pro -> pro.setId(ResourceKey.create(Registries.ITEM, id)).rarity(ModRarities.EPIC).food(ModFoods.cosmic_meatballs)));
    public static final DeferredItem<Item> forge_energy = item("forge_energy", false);

    static {
        // 触发 ModBlocks 类初始化，将方块物品工厂填充到 BLOCK_ITEMS 后统一注册。
        @SuppressWarnings("unused")
        var ignored = ModBlocks.BLOCKS;
        BLOCK_ITEMS.forEach((name, blockItemFactory) -> ModItems.item(name, id -> blockItemFactory.apply(id)));
    }

    /**
     * 注册一个普通物品。
     *
     * @param name 注册名（modid:name 中的 path）
     * @return 延迟注册物品引用
     */
    public static DeferredItem<Item> item(String name) {
        return item(name, true);
    }

    /**
     * 注册方块物品，并为传入属性补齐 NeoForge 26.1.2 要求的 item id。
     *
     * @param name       注册名（modid:name 中的 path）
     * @param block      方块提供器
     * @param properties 方块物品属性
     * @param exist      是否加入主创造模式物品栏
     * @return 延迟注册物品引用
     */
    public static DeferredItem<Item> blockItem(String name, Supplier<Block> block, Item.Properties properties, boolean exist) {
        return item(name, id -> new BlockItem(block.get(), properties.setId(ResourceKey.create(Registries.ITEM, id))), exist);
    }

    /**
     * 注册一个普通 {@link BaseItem}。
     *
     * @param name  注册名（modid:name 中的 path）
     * @param exist 是否加入主创造模式物品栏
     * @return 延迟注册物品引用
     */
    public static DeferredItem<Item> item(String name, boolean exist) {
        return item(name, id -> new Item(new Item.Properties().setId(ResourceKey.create(Registries.ITEM, id))), exist);
    }

    /**
     * Register an item with the DeferredRegister.
     *
     * @param name registry key path (modid:name)
     * @param fn   factory that receives Identifier id for setId()
     * @return deferred item holder
     */
    public static DeferredItem<Item> item(String name, Function<Identifier, Item> fn) {
        return item(name, fn, true);
    }

    /**
     * Register an item with the DeferredRegister.
     *
     * @param name  registry key path (modid:name)
     * @param fn    factory that receives Identifier id for setId()
     * @param exist whether to add to creative tab
     * @return deferred item holder
     */
    public static DeferredItem<Item> item(String name, Function<Identifier, Item> fn, boolean exist) {
        DeferredItem<Item> regItem = ITEMS.register(name, fn);
        if (exist) {
            ModCreativeModeTabs.ACCEPT_ITEM.add(regItem);
        }
        return regItem;
    }

    /**
     * 创建已绑定注册 id 的物品属性。
     *
     * @param id NeoForge 注册回调传入的完整资源定位符
     * @return 已调用 {@link Item.Properties#setId(ResourceKey)} 的属性对象
     */
}
