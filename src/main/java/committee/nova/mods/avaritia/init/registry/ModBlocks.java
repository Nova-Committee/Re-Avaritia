package committee.nova.mods.avaritia.init.registry;

import committee.nova.mods.avaritia.Static;
import committee.nova.mods.avaritia.api.common.item.BaseBlockItem;
import committee.nova.mods.avaritia.common.block.ResourceBlock;
import committee.nova.mods.avaritia.common.block.craft.*;
import committee.nova.mods.avaritia.common.block.*;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.level.block.Block;
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


    public static RegistryObject<Block> compressed_crafting_table = block("compressed_crafting_table", CompressedCraftingTableBlock::new);
    public static RegistryObject<Block> double_compressed_crafting_table = block("double_compressed_crafting_table", DoubleCompressedCraftingTableBlock::new);

    public static RegistryObject<Block> neutronium = block("neutronium", ResourceBlock::new);
    public static RegistryObject<Block> infinity = block("infinity", ResourceBlock::new);
    public static RegistryObject<Block> crystal_matrix = block("crystal_matrix", ResourceBlock::new);


    public static RegistryObject<Block> extreme_crafting_table = block("extreme_crafting_table", ExtremeCraftingTableBlock::new);
    public static RegistryObject<Block> neutron_collector = block("neutron_collector", NeutronCollectorBlock::new);
    public static RegistryObject<Block> compressor = block("neutronium_compressor", CompressorBlock::new);
    //public static RegistryObject<Block> infinitato = block("infinitato", InfinitatoBlock::new);

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
