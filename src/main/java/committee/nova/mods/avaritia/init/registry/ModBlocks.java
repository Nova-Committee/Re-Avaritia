package committee.nova.mods.avaritia.init.registry;

import committee.nova.mods.avaritia.Static;
import committee.nova.mods.avaritia.common.block.chest.CompressedChestBlock;
import committee.nova.mods.avaritia.common.block.collector.BaseNeutronCollectorBlock;
import committee.nova.mods.avaritia.common.block.compressor.CompressorBlock;
import committee.nova.mods.avaritia.common.block.craft.ModCraftTableBlock;
import committee.nova.mods.avaritia.common.block.ResourceBlock;
import committee.nova.mods.avaritia.common.block.cake.EndlessCakeBlock;
import committee.nova.mods.avaritia.common.block.craft.CompressedCraftTableBlock;
import committee.nova.mods.avaritia.common.block.craft.DoubleCompressedCraftTableBlock;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import java.util.function.Supplier;


/**
 * Description:
 * Author: cnlimiter
 * Date: 2022/4/2 6:47
 * Version: 1.0
 */
public class ModBlocks {
    public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, Static.MOD_ID);

    //CRAFTING
    public static RegistryObject<Block> compressed_crafting_table = block("compressed_crafting_table", CompressedCraftTableBlock::new, ModRarities.UNCOMMON);
    public static RegistryObject<Block> double_compressed_crafting_table = block("double_compressed_crafting_table", DoubleCompressedCraftTableBlock::new, ModRarities.UNCOMMON);

    //RESOURCE
    public static RegistryObject<Block> neutron = block("neutron", ResourceBlock::new, ModRarities.EPIC);
    public static RegistryObject<Block> infinity = block("infinity", ResourceBlock::new, ModRarities.COSMIC);
    public static RegistryObject<Block> crystal_matrix = block("crystal_matrix", ResourceBlock::new, ModRarities.RARE);
    public static RegistryObject<Block> compressed_chest = block("compressed_chest", CompressedChestBlock::new, ModRarities.RARE);

    //MACHINE
    public static RegistryObject<Block> sculk_crafting_table = block("sculk_crafting_table", () -> new ModCraftTableBlock(ModCraftTier.SCULK), ModRarities.COMMON);
    public static RegistryObject<Block> nether_crafting_table = block("nether_crafting_table", () -> new ModCraftTableBlock(ModCraftTier.NETHER), ModRarities.UNCOMMON);
    public static RegistryObject<Block> end_crafting_table = block("end_crafting_table", () -> new ModCraftTableBlock(ModCraftTier.END), ModRarities.RARE);
    public static RegistryObject<Block> extreme_crafting_table = block("extreme_crafting_table", () -> new ModCraftTableBlock(ModCraftTier.EXTREME), ModRarities.EPIC);
    public static RegistryObject<Block> neutron_collector = block("neutron_collector", BaseNeutronCollectorBlock::new, ModRarities.RARE);
    public static RegistryObject<Block> dense_neutron_collector = block("dense_neutron_collector", BaseNeutronCollectorBlock::new, ModRarities.EPIC);
    public static RegistryObject<Block> denser_neutron_collector = block("denser_neutron_collector", BaseNeutronCollectorBlock::new, ModRarities.LEGEND);
    public static RegistryObject<Block> densest_neutron_collector = block("densest_neutron_collector", BaseNeutronCollectorBlock::new, ModRarities.COSMIC);
    public static RegistryObject<Block> neutron_compressor = block("neutron_compressor", CompressorBlock::new, ModRarities.RARE);

    //CAKE
    public static RegistryObject<Block> endless_cake = block("endless_cake", EndlessCakeBlock::new, ModRarities.UNCOMMON);

    private static RegistryObject<Block> candleBlock(String name, Supplier<Block> block) {
        return BLOCKS.register(name, block);
    }

    public static RegistryObject<Block> block(String name, Supplier<Block> block, Rarity rarity) {
        var reg = BLOCKS.register(name, block);
        ModItems.item(name, () -> new BlockItem(reg.get(), new Item.Properties().rarity(rarity)));
        return reg;
    }
}
