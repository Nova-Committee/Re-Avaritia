package committee.nova.mods.avaritia.init.registry;

import committee.nova.mods.avaritia.Static;
import committee.nova.mods.avaritia.api.common.item.BaseBlockItem;
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
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Description:
 * Author: cnlimiter
 * Date: 2022/4/2 6:47
 * Version: 1.0
 */
public class ModBlocks {
    public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, Static.MOD_ID);
    public static final Map<String, Supplier<BlockItem>> BLOCK_ITEMS = new LinkedHashMap<>();


    //CRAFTING
    public static RegistryObject<Block> compressed_crafting_table = block("compressed_crafting_table", CompressedCraftingTableBlock::new);
    public static RegistryObject<Block> double_compressed_crafting_table = block("double_compressed_crafting_table", DoubleCompressedCraftingTableBlock::new);

    //RESOURCE
    public static RegistryObject<Block> neutron = block("neutron", ResourceBlock::new);
    public static RegistryObject<Block> infinity = block("infinity", ResourceBlock::new);
    public static RegistryObject<Block> crystal_matrix = block("crystal_matrix", ResourceBlock::new);

    //MACHINE
    public static RegistryObject<Block> extreme_crafting_table = block("extreme_crafting_table", ExtremeCraftingTableBlock::new);
    public static RegistryObject<Block> neutron_collector = block("neutron_collector", DefaultNeutronCollectorBlock::new);
    public static RegistryObject<Block> dense_neutron_collector = block("dense_neutron_collector", DenseNeutronCollectorBlock::new);
    public static RegistryObject<Block> denser_neutron_collector = block("denser_neutron_collector", DenserNeutronCollectorBlock::new);
    public static RegistryObject<Block> densest_neutron_collector = block("densest_neutron_collector", DensestNeutronCollectorBlock::new);
    public static RegistryObject<Block> neutron_compressor = block("neutron_compressor", CompressorBlock::new);
    //public static RegistryObject<Block> infinitato = block("infinitato", InfinitatoBlock::new);

    //CAKE
    public static RegistryObject<Block> endless_cake = block("endless_cake", EndlessCakeBlock::new);
    public static RegistryObject<Block> candle_endless_cake = candleBlock("candle_endless_cake", () -> new EndlessCandleCakeBlock(Blocks.CANDLE, BlockBehaviour.Properties.copy(Blocks.CAKE).lightLevel((state) -> (Boolean)state.getValue(BlockStateProperties.LIT) ? 3 : 0)));
    public static RegistryObject<Block> white_candle_endless_cake = candleBlock("white_candle_endless_cake", () -> new EndlessCandleCakeBlock(Blocks.WHITE_CANDLE, BlockBehaviour.Properties.copy(Blocks.CANDLE_CAKE)));
    public static RegistryObject<Block> orange_candle_endless_cake = candleBlock("orange_candle_endless_cake", () -> new EndlessCandleCakeBlock(Blocks.ORANGE_CANDLE, BlockBehaviour.Properties.copy(Blocks.CANDLE_CAKE)));
    public static RegistryObject<Block> magenta_candle_endless_cake = candleBlock("magenta_candle_endless_cake", () -> new EndlessCandleCakeBlock(Blocks.MAGENTA_CANDLE, BlockBehaviour.Properties.copy(Blocks.CANDLE_CAKE)));
    public static RegistryObject<Block> elight_blue_candle_ndless_cake = candleBlock("light_blue_candle_endless_cake", () -> new EndlessCandleCakeBlock(Blocks.LIGHT_BLUE_CANDLE, BlockBehaviour.Properties.copy(Blocks.CANDLE_CAKE)));
    public static RegistryObject<Block> yellow_candle_endless_cake = candleBlock("yellow_candle_endless_cake", () -> new EndlessCandleCakeBlock(Blocks.YELLOW_CANDLE, BlockBehaviour.Properties.copy(Blocks.CANDLE_CAKE)));
    public static RegistryObject<Block> lime_candle_endless_cake = candleBlock("lime_candle_endless_cake", () -> new EndlessCandleCakeBlock(Blocks.LIME_CANDLE, BlockBehaviour.Properties.copy(Blocks.CANDLE_CAKE)));
    public static RegistryObject<Block> pink_candle_endless_cake = candleBlock("pink_candle_endless_cake", () -> new EndlessCandleCakeBlock(Blocks.PINK_CANDLE, BlockBehaviour.Properties.copy(Blocks.CANDLE_CAKE)));
    public static RegistryObject<Block> gray_candle_endless_cake = candleBlock("gray_candle_endless_cake", () -> new EndlessCandleCakeBlock(Blocks.GRAY_CANDLE, BlockBehaviour.Properties.copy(Blocks.CANDLE_CAKE)));
    public static RegistryObject<Block> light_gray_candle_endless_cake = candleBlock("light_gray_candle_endless_cake", () -> new EndlessCandleCakeBlock(Blocks.LIGHT_GRAY_CANDLE, BlockBehaviour.Properties.copy(Blocks.CANDLE_CAKE)));
    public static RegistryObject<Block> cyan_candle_endless_cake = candleBlock("cyan_candle_endless_cake", () -> new EndlessCandleCakeBlock(Blocks.CYAN_CANDLE, BlockBehaviour.Properties.copy(Blocks.CANDLE_CAKE)));
    public static RegistryObject<Block> purple_candle_endless_cake = candleBlock("purple_candle_endless_cake", () -> new EndlessCandleCakeBlock(Blocks.PURPLE_CANDLE, BlockBehaviour.Properties.copy(Blocks.CANDLE_CAKE)));
    public static RegistryObject<Block> blue_candle_endless_cake = candleBlock("blue_candle_endless_cake", () -> new EndlessCandleCakeBlock(Blocks.BLUE_CANDLE, BlockBehaviour.Properties.copy(Blocks.CANDLE_CAKE)));
    public static RegistryObject<Block> brown_candle_endless_cake = candleBlock("brown_candle_endless_cake", () -> new EndlessCandleCakeBlock(Blocks.BROWN_CANDLE, BlockBehaviour.Properties.copy(Blocks.CANDLE_CAKE)));
    public static RegistryObject<Block> green_candle_endless_cake = candleBlock("green_candle_endless_cake", () -> new EndlessCandleCakeBlock(Blocks.GREEN_CANDLE, BlockBehaviour.Properties.copy(Blocks.CANDLE_CAKE)));
    public static RegistryObject<Block> red_candle_endless_cake = candleBlock("red_candle_endless_cake", () -> new EndlessCandleCakeBlock(Blocks.RED_CANDLE, BlockBehaviour.Properties.copy(Blocks.CANDLE_CAKE)));
    public static RegistryObject<Block> black_candle_endless_cake = candleBlock("black_candle_endless_cake", () -> new EndlessCandleCakeBlock(Blocks.BLACK_CANDLE, BlockBehaviour.Properties.copy(Blocks.CANDLE_CAKE)));


    private static RegistryObject<Block> candleBlock(String name, Supplier<Block> block) {
        return BLOCKS.register(name, block);
    }

    public static RegistryObject<Block> block(String name, Supplier<Block> block) {
        return block(name, block, b -> () -> new BaseBlockItem(b.get()));
    }

    public static RegistryObject<Block> block(String name, Supplier<Block> block, Rarity rarity) {
        return block(name, block, b -> () -> new BaseBlockItem(b.get(), p -> p.rarity(rarity)));
    }

    public static RegistryObject<Block> block(String name, Supplier<Block> block, Function<RegistryObject<Block>, Supplier<? extends BlockItem>> item) {
        var reg = BLOCKS.register(name, block);
        BLOCK_ITEMS.put(name, () -> item.apply(reg).get());
        return reg;
    }
}
