package committee.nova.mods.avaritia.init.registry;

import committee.nova.mods.avaritia.common.block.*;
import committee.nova.mods.avaritia.common.block.craft.CompressedCraftingTableBlock;
import committee.nova.mods.avaritia.common.block.craft.DoubleCompressedCraftingTableBlock;
import committee.nova.mods.avaritia.util.registry.RegistryUtil;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.material.MapColor;

/**
 * Description:
 * Author: cnlimiter
 * Date: 2022/4/2 6:47
 * Version: 1.0
 */
public class ModBlocks {


    public static Block compressed_crafting_table = RegistryUtil.block("compressed_crafting_table", CompressedCraftingTableBlock::new).get();
    public static Block double_compressed_crafting_table = RegistryUtil.block("double_compressed_crafting_table", DoubleCompressedCraftingTableBlock::new).get();

    public static Block neutronium = RegistryUtil.block("neutronium", () -> new ResourceBlock(MapColor.METAL, SoundType.METAL)).get();
    public static Block infinity = RegistryUtil.block("infinity", () -> new ResourceBlock(MapColor.METAL, SoundType.METAL)).get();
    public static Block crystal_matrix = RegistryUtil.block("crystal_matrix", () -> new ResourceBlock(MapColor.METAL, SoundType.METAL)).get();

    public static Block extreme_crafting_table = RegistryUtil.block("extreme_crafting_table", ExtremeCraftingTableBlock::new).get();
    public static Block neutron_collector = RegistryUtil.block("neutron_collector", NeutronCollectorBlock::new).get();
    public static Block compressor = RegistryUtil.block("neutronium_compressor", CompressorBlock::new).get();
    public static Block infinitato = RegistryUtil.block("infinitato", InfinitatoBlock::new).get();


}
