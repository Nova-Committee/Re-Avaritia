package com.avaritia.init.registry;

import com.avaritia.Const;
import com.avaritia.common.block.ResourceBlock;
import com.avaritia.common.block.cake.EndlessCakeBlock;
import com.avaritia.common.block.chest.CompressedChestBlock;
import com.avaritia.common.block.chest.InfinityChestBlock;
import com.avaritia.common.block.collector.NeutronCollectorBlock;
import com.avaritia.common.block.compressor.NeutronCompressorBlock;
import com.avaritia.common.block.craft.CompressedCraftTableBlock;
import com.avaritia.common.block.craft.DoubleCompressedCraftTableBlock;
import com.avaritia.common.block.craft.TierCraftTableBlock;
import com.avaritia.common.block.extreme.ExtremeAnvilBlock;
import com.avaritia.common.block.extreme.ExtremeSmithingTableBlock;
import com.avaritia.common.block.misc.BlazeCubeBlock;
import com.avaritia.common.block.misc.SoulFarmLandBlock;
import com.avaritia.common.item.resources.RefinedCoalItem;
import com.avaritia.init.registry.enums.ModCraftTier;
import com.avaritia.init.registry.enums.ModResourceBlocks;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.FuelValues;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.properties.NoteBlockInstrument;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredRegister;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jspecify.annotations.NonNull;

import java.util.function.Function;
import java.util.function.Supplier;

/**
 * 注册模组中的所有方块。
 */
public class ModBlocks {
    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(Const.MOD_ID);
    private static final ThreadLocal<ResourceKey<Block>> CURRENT_BLOCK_KEY = new ThreadLocal<>();

    // CRAFTING
    public static final DeferredBlock<Block> compressed_crafting_table = itemBlock("compressed_crafting_table", CompressedCraftTableBlock::new, ModRarities.UNCOMMON);
    public static final DeferredBlock<Block> double_compressed_crafting_table = itemBlock("double_compressed_crafting_table", DoubleCompressedCraftTableBlock::new, ModRarities.UNCOMMON);

    // RESOURCE
    public static final DeferredBlock<Block> neutron = itemBlock("neutron", () -> new ResourceBlock(ModResourceBlocks.NEUTRON), new Item.Properties().fireResistant().rarity(ModRarities.EPIC));
    public static final DeferredBlock<Block> infinity = itemBlock("infinity", () -> new ResourceBlock(ModResourceBlocks.INFINITY), new Item.Properties().fireResistant().rarity(ModRarities.COSMIC.getValue()));
    public static final DeferredBlock<Block> crystal_matrix = itemBlock("crystal_matrix", () -> new ResourceBlock(ModResourceBlocks.CRYSTAL), new Item.Properties().fireResistant().rarity(ModRarities.RARE));
    public static final DeferredBlock<Block> blaze_cube_block = itemBlock("blaze_cube_block", () -> new BlazeCubeBlock(ModResourceBlocks.BLAZE), ModRarities.RARE);
    public static final DeferredBlock<Block> compressed_chest = itemBlock("compressed_chest", CompressedChestBlock::new, ModRarities.RARE);
    public static final DeferredBlock<Block> infinity_chest = itemBlock("infinity_chest", InfinityChestBlock::new, new Item.Properties().rarity(ModRarities.LEGEND.getValue()));
    public static final DeferredBlock<Block> soul_farmland = itemBlock("soul_farmland", SoulFarmLandBlock::new, ModRarities.RARE);
    public static final DeferredBlock<Block> diamond_lattice_block = itemBlock("diamond_lattice_block",
            () -> new Block(properties()
                    .strength(100F, 100F)
                    .sound(SoundType.GLASS)),
            true,
            new Item.Properties().rarity(ModRarities.UNCOMMON));
    public static final DeferredBlock<Block> star_fuel_block = itemBurnBlock("star_fuel_block",
            () -> new Block(properties()
                    .strength(100F, 200F)
                    .sound(SoundType.STONE)),
            true,
            new Item.Properties().rarity(ModRarities.RARE),
            Integer.MAX_VALUE);
    public static final DeferredBlock<Block> refined_coal_block = itemBurnBlock("refined_coal_block",
            () -> new Block(properties()
                    .strength(50F, 50F)
                    .sound(SoundType.STONE)),
            true,
            new Item.Properties().rarity(ModRarities.UNCOMMON),
            RefinedCoalItem.BURN_TIME * 9);

    // MACHINE
    public static final DeferredBlock<Block> sculk_crafting_table = itemBlock("sculk_crafting_table", () -> new TierCraftTableBlock(ModCraftTier.SCULK, properties().lightLevel(state -> 15)), ModRarities.COMMON);
    public static final DeferredBlock<Block> nether_crafting_table = itemBlock("nether_crafting_table", () -> new TierCraftTableBlock(ModCraftTier.NETHER, properties().lightLevel(state -> 15)), ModRarities.UNCOMMON);
    public static final DeferredBlock<Block> end_crafting_table = itemBlock("end_crafting_table", () -> new TierCraftTableBlock(ModCraftTier.END, properties().lightLevel(state -> 15)), ModRarities.RARE);
    public static final DeferredBlock<Block> extreme_crafting_table = itemBlock("extreme_crafting_table", () -> new TierCraftTableBlock(ModCraftTier.EXTREME, properties().lightLevel(state -> 15)), ModRarities.EPIC);
    public static final DeferredBlock<Block> neutron_collector = itemBlock("neutron_collector", NeutronCollectorBlock::new, ModRarities.RARE);
    public static final DeferredBlock<Block> dense_neutron_collector = itemBlock("dense_neutron_collector", NeutronCollectorBlock::new, ModRarities.EPIC);
    public static final DeferredBlock<Block> denser_neutron_collector = itemBlock("denser_neutron_collector", NeutronCollectorBlock::new, ModRarities.LEGEND.getValue());
    public static final DeferredBlock<Block> densest_neutron_collector = itemBlock("densest_neutron_collector", NeutronCollectorBlock::new, ModRarities.COSMIC.getValue());
    public static final DeferredBlock<Block> neutron_compressor = itemBlock("neutron_compressor", NeutronCompressorBlock::new, ModRarities.RARE);
    public static final DeferredBlock<Block> dense_neutron_compressor = itemBlock("dense_neutron_compressor", NeutronCompressorBlock::new, ModRarities.EPIC);
    public static final DeferredBlock<Block> denser_neutron_compressor = itemBlock("denser_neutron_compressor", NeutronCompressorBlock::new, ModRarities.LEGEND.getValue());
    public static final DeferredBlock<Block> densest_neutron_compressor = itemBlock("densest_neutron_compressor", NeutronCompressorBlock::new, ModRarities.COSMIC.getValue());
    public static final DeferredBlock<Block> extreme_smithing_table = itemBlock("extreme_smithing_table", ExtremeSmithingTableBlock::new, ModRarities.LEGEND.getValue());
    public static final DeferredBlock<Block> extreme_anvil = itemBlock("extreme_anvil", ExtremeAnvilBlock::new, ModRarities.LEGEND.getValue());

    // CAKE
    public static final DeferredBlock<Block> endless_cake = itemBlock("endless_cake", EndlessCakeBlock::new, ModRarities.UNCOMMON);

    // MISC / FAKE BLOCKS
    public static final DeferredBlock<Block> fake_bedrock = itemBlock("fake_bedrock", () -> new Block(
            properties()
                    .mapColor(MapColor.STONE)
                    .instrument(NoteBlockInstrument.BASEDRUM)
                    .strength(1000F, 3600000.0F)
                    .isValidSpawn((state, level, pos, value) -> false)), false);
    public static final DeferredBlock<Block> fake_end_portal_frame = itemBlock("fake_end_portal_frame", () -> new Block(
            properties()
                    .mapColor(MapColor.COLOR_GREEN)
                    .instrument(NoteBlockInstrument.BASEDRUM)
                    .sound(SoundType.GLASS)
                    .lightLevel(blockState -> 1)
                    .strength(400F, 3600000.0F)), false);
    public static final DeferredBlock<Block> fake_end_portal = itemBlock("fake_end_portal", () -> new Block(
            properties()
                    .mapColor(MapColor.COLOR_BLACK)
                    .noCollision()
                    .lightLevel(state -> 15)
                    .strength(400F, 3600000.0F)
                    .pushReaction(PushReaction.BLOCK)), false);

    /**
     * 注册基础方块，不自动创建对应方块物品。
     *
     * @param name  方块注册名（modid:name 中的 path）
     * @param block 方块工厂
     * @return 延迟注册方块引用
     */
    private static DeferredBlock<Block> baseBlock(String name, Supplier<Block> block) {
        return BLOCKS.register(name, id -> {
            CURRENT_BLOCK_KEY.set(ResourceKey.create(Registries.BLOCK, id));
            try {
                return block.get();
            } finally {
                CURRENT_BLOCK_KEY.remove();
            }
        });
    }

    public static BlockBehaviour.Properties properties() {
        BlockBehaviour.Properties properties = BlockBehaviour.Properties.of();
        ResourceKey<Block> key = CURRENT_BLOCK_KEY.get();
        return key == null ? properties : properties.setId(key);
    }

    /**
     * 注册带默认方块物品的方块。
     *
     * @param name  方块与方块物品注册名
     * @param block 方块工厂
     * @return 延迟注册方块引用
     */
    public static DeferredBlock<Block> itemBlock(String name, Supplier<Block> block) {
        return itemBlock(name, block, true);
    }

    /**
     * 注册方块，并按需注册默认方块物品。
     *
     * @param name    方块与方块物品注册名
     * @param block   方块工厂
     * @param hasItem 是否生成对应方块物品
     * @return 延迟注册方块引用
     */
    public static DeferredBlock<Block> itemBlock(String name, Supplier<Block> block, boolean hasItem) {
        return itemBlock(name, block, hasItem, b -> id -> new BlockItem(b.get(), blockItemProperties(id)));
    }

    /**
     * 注册方块，并为方块物品设置稀有度。
     *
     * @param name   方块与方块物品注册名
     * @param block  方块工厂
     * @param rarity 方块物品稀有度
     * @return 延迟注册方块引用
     */
    public static DeferredBlock<Block> itemBlock(String name, Supplier<Block> block, Rarity rarity) {
        return itemBlock(name, block, true, b -> id -> new BlockItem(b.get(), blockItemProperties(id).rarity(rarity)));
    }

    /**
     * 注册方块，并使用指定属性创建方块物品。
     *
     * @param name       方块与方块物品注册名
     * @param block      方块工厂
     * @param properties 方块物品属性，会在注册时补齐 item id
     * @return 延迟注册方块引用
     */
    public static DeferredBlock<Block> itemBlock(String name, Supplier<Block> block, Item.Properties properties) {
        return itemBlock(name, block, true, b -> id -> new BlockItem(b.get(), blockItemProperties(id, properties)));
    }

    /**
     * 注册方块，并按需使用指定属性创建方块物品。
     *
     * @param name       方块与方块物品注册名
     * @param block      方块工厂
     * @param hasItem    是否生成对应方块物品
     * @param properties 方块物品属性，会在注册时补齐 item id
     * @return 延迟注册方块引用
     */
    public static DeferredBlock<Block> itemBlock(String name, Supplier<Block> block, boolean hasItem, Item.Properties properties) {
        return itemBlock(name, block, hasItem, b -> id -> new BlockItem(b.get(), blockItemProperties(id, properties)));
    }

    /**
     * 注册方块，并把方块物品工厂写入 {@link ModItems#BLOCK_ITEMS}，由物品注册表统一注册。
     *
     * @param name    方块与方块物品注册名
     * @param block   方块工厂
     * @param hasItem 是否生成对应方块物品
     * @param item    接收方块引用并返回方块物品工厂的函数，工厂会接收物品注册 id
     * @return 延迟注册方块引用
     */
    public static DeferredBlock<Block> itemBlock(String name, Supplier<Block> block, boolean hasItem, Function<DeferredBlock<Block>, Function<Identifier, ? extends BlockItem>> item) {
        DeferredBlock<Block> reg = baseBlock(name, block);
        if (hasItem) {
            ModItems.BLOCK_ITEMS.put(name, item.apply(reg));
        }
        return reg;
    }

    /**
     * 注册可作为燃料的方块，并为对应方块物品覆写燃烧时间。
     *
     * @param name       方块与方块物品注册名
     * @param block      方块工厂
     * @param hasItem    是否生成对应方块物品
     * @param properties 方块物品属性，会在注册时补齐 item id
     * @param burnTime   燃烧时间（tick）
     * @return 延迟注册方块引用
     */
    public static DeferredBlock<Block> itemBurnBlock(String name, Supplier<Block> block, boolean hasItem, Item.Properties properties, int burnTime) {
        return itemBlock(name, block, hasItem, b -> id -> new BlockItem(b.get(), blockItemProperties(id, properties)) {
            @Override
            public int getBurnTime(@NonNull ItemStack itemStack, @Nullable RecipeType<?> recipeType, @NonNull FuelValues fuelValues) {
                return burnTime;
            }
        });
    }

    private static Item.Properties blockItemProperties(Identifier id) {
        return new Item.Properties()
                .setId(ResourceKey.create(Registries.ITEM, id))
                .useBlockDescriptionPrefix();
    }

    private static Item.Properties blockItemProperties(Identifier id, Item.Properties properties) {
        return properties
                .setId(ResourceKey.create(Registries.ITEM, id))
                .useBlockDescriptionPrefix();
    }
}
