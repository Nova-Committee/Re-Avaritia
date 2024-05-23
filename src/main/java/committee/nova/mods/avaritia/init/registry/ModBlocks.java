package committee.nova.mods.avaritia.init.registry;

import committee.nova.mods.avaritia.common.block.CompressorBlock;
import committee.nova.mods.avaritia.common.block.ExtremeCraftingTableBlock;
import committee.nova.mods.avaritia.common.block.ResourceBlock;
import committee.nova.mods.avaritia.common.block.cake.EndlessCakeBlock;
import committee.nova.mods.avaritia.common.block.cake.EndlessCandleCakeBlock;
import committee.nova.mods.avaritia.common.block.collector.DefaultNeutronCollectorBlock;
import committee.nova.mods.avaritia.common.block.collector.DenseNeutronCollectorBlock;
import committee.nova.mods.avaritia.common.block.collector.DenserNeutronCollectorBlock;
import committee.nova.mods.avaritia.common.block.collector.DensestNeutronCollectorBlock;
import committee.nova.mods.avaritia.common.block.craft.CompressedCraftingTableBlock;
import committee.nova.mods.avaritia.common.block.craft.DoubleCompressedCraftingTableBlock;
import committee.nova.mods.avaritia.util.registry.FabricRegistry;
import committee.nova.mods.avaritia.util.registry.RegistryHolder;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;

import java.util.function.Supplier;

import static committee.nova.mods.avaritia.init.registry.ModItems.COSMIC_RARITY;
import static net.minecraft.world.item.Rarity.RARE;

/**
 * Description:
 * Author: cnlimiter
 * Date: 2022/4/2 6:47
 * Version: 1.0
 */
public class ModBlocks {
    public static final RegistryHolder<Block> BLOCKS = FabricRegistry.INSTANCE.createBlockRegistryHolder();

    public static void init() {}

    //CRAFTING
    public static Supplier<Block> compressed_crafting_table = block("compressed_crafting_table", CompressedCraftingTableBlock::new, Rarity.UNCOMMON);
    public static Supplier<Block> double_compressed_crafting_table = block("double_compressed_crafting_table", DoubleCompressedCraftingTableBlock::new, Rarity.UNCOMMON);

    //RESOURCE
    public static Supplier<Block> neutron = block("neutron", ResourceBlock::new, Rarity.EPIC);
    public static Supplier<Block> infinity = block("infinity", ResourceBlock::new, COSMIC_RARITY);
    public static Supplier<Block> crystal_matrix = block("crystal_matrix", ResourceBlock::new, Rarity.RARE);

    //MACHINE
    public static Supplier<Block> extreme_crafting_table = block("extreme_crafting_table", ExtremeCraftingTableBlock::new, RARE);
    public static Supplier<Block> neutron_collector = block("neutron_collector", DefaultNeutronCollectorBlock::new, Rarity.RARE);
    public static Supplier<Block> dense_neutron_collector = block("dense_neutron_collector", DenseNeutronCollectorBlock::new, Rarity.RARE);
    public static Supplier<Block> denser_neutron_collector = block("denser_neutron_collector", DenserNeutronCollectorBlock::new, Rarity.EPIC);
    public static Supplier<Block> densest_neutron_collector = block("densest_neutron_collector", DensestNeutronCollectorBlock::new, COSMIC_RARITY);
    public static Supplier<Block> neutron_compressor = block("neutron_compressor", CompressorBlock::new, Rarity.RARE);
    //public static Supplier<Block> infinitato = block("infinitato", InfinitatoBlock::new);

    //CAKE
    public static Supplier<Block> endless_cake = block("endless_cake", EndlessCakeBlock::new, Rarity.UNCOMMON);
    public static Supplier<Block> candle_endless_cake = candleBlock("candle_endless_cake", () -> new EndlessCandleCakeBlock(Blocks.CANDLE, BlockBehaviour.Properties.copy(Blocks.CAKE).lightLevel((state) -> (Boolean)state.getValue(BlockStateProperties.LIT) ? 3 : 0)));
    public static Supplier<Block> white_candle_endless_cake = candleBlock("white_candle_endless_cake", () -> new EndlessCandleCakeBlock(Blocks.WHITE_CANDLE, BlockBehaviour.Properties.copy(Blocks.CANDLE_CAKE)));
    public static Supplier<Block> orange_candle_endless_cake = candleBlock("orange_candle_endless_cake", () -> new EndlessCandleCakeBlock(Blocks.ORANGE_CANDLE, BlockBehaviour.Properties.copy(Blocks.CANDLE_CAKE)));
    public static Supplier<Block> magenta_candle_endless_cake = candleBlock("magenta_candle_endless_cake", () -> new EndlessCandleCakeBlock(Blocks.MAGENTA_CANDLE, BlockBehaviour.Properties.copy(Blocks.CANDLE_CAKE)));
    public static Supplier<Block> elight_blue_candle_ndless_cake = candleBlock("light_blue_candle_endless_cake", () -> new EndlessCandleCakeBlock(Blocks.LIGHT_BLUE_CANDLE, BlockBehaviour.Properties.copy(Blocks.CANDLE_CAKE)));
    public static Supplier<Block> yellow_candle_endless_cake = candleBlock("yellow_candle_endless_cake", () -> new EndlessCandleCakeBlock(Blocks.YELLOW_CANDLE, BlockBehaviour.Properties.copy(Blocks.CANDLE_CAKE)));
    public static Supplier<Block> lime_candle_endless_cake = candleBlock("lime_candle_endless_cake", () -> new EndlessCandleCakeBlock(Blocks.LIME_CANDLE, BlockBehaviour.Properties.copy(Blocks.CANDLE_CAKE)));
    public static Supplier<Block> pink_candle_endless_cake = candleBlock("pink_candle_endless_cake", () -> new EndlessCandleCakeBlock(Blocks.PINK_CANDLE, BlockBehaviour.Properties.copy(Blocks.CANDLE_CAKE)));
    public static Supplier<Block> gray_candle_endless_cake = candleBlock("gray_candle_endless_cake", () -> new EndlessCandleCakeBlock(Blocks.GRAY_CANDLE, BlockBehaviour.Properties.copy(Blocks.CANDLE_CAKE)));
    public static Supplier<Block> light_gray_candle_endless_cake = candleBlock("light_gray_candle_endless_cake", () -> new EndlessCandleCakeBlock(Blocks.LIGHT_GRAY_CANDLE, BlockBehaviour.Properties.copy(Blocks.CANDLE_CAKE)));
    public static Supplier<Block> cyan_candle_endless_cake = candleBlock("cyan_candle_endless_cake", () -> new EndlessCandleCakeBlock(Blocks.CYAN_CANDLE, BlockBehaviour.Properties.copy(Blocks.CANDLE_CAKE)));
    public static Supplier<Block> purple_candle_endless_cake = candleBlock("purple_candle_endless_cake", () -> new EndlessCandleCakeBlock(Blocks.PURPLE_CANDLE, BlockBehaviour.Properties.copy(Blocks.CANDLE_CAKE)));
    public static Supplier<Block> blue_candle_endless_cake = candleBlock("blue_candle_endless_cake", () -> new EndlessCandleCakeBlock(Blocks.BLUE_CANDLE, BlockBehaviour.Properties.copy(Blocks.CANDLE_CAKE)));
    public static Supplier<Block> brown_candle_endless_cake = candleBlock("brown_candle_endless_cake", () -> new EndlessCandleCakeBlock(Blocks.BROWN_CANDLE, BlockBehaviour.Properties.copy(Blocks.CANDLE_CAKE)));
    public static Supplier<Block> green_candle_endless_cake = candleBlock("green_candle_endless_cake", () -> new EndlessCandleCakeBlock(Blocks.GREEN_CANDLE, BlockBehaviour.Properties.copy(Blocks.CANDLE_CAKE)));
    public static Supplier<Block> red_candle_endless_cake = candleBlock("red_candle_endless_cake", () -> new EndlessCandleCakeBlock(Blocks.RED_CANDLE, BlockBehaviour.Properties.copy(Blocks.CANDLE_CAKE)));
    public static Supplier<Block> black_candle_endless_cake = candleBlock("black_candle_endless_cake", () -> new EndlessCandleCakeBlock(Blocks.BLACK_CANDLE, BlockBehaviour.Properties.copy(Blocks.CANDLE_CAKE)));


    private static Supplier<Block> candleBlock(String name, Supplier<Block> block) {
        return BLOCKS.register(name, block);
    }

    public static Supplier<Block> block(String name, Supplier<Block> block, Rarity rarity) {
        var reg = BLOCKS.register(name, block);
        ModItems.item(name, () -> new BlockItem(reg.get(), new Item.Properties().rarity(rarity)));
        return reg;
    }
}
