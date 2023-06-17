package committee.nova.mods.avaritia.init.registry;

import committee.nova.mods.avaritia.Static;
import committee.nova.mods.avaritia.api.common.item.BaseBlockItem;
import committee.nova.mods.avaritia.common.block.ResourceBlock;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegisterEvent;
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
    public static final ResourceLocation NEUTRONIUM_KEY = Static.rl("neutronium");
    public static final ResourceLocation INFINITY_KEY = Static.rl("infinity");
    public static final ResourceLocation CRYSTAL_MATRIX_KEY = Static.rl("crystal_matrix");


//    public static RegistryObject<Block> compressed_crafting_table = block("compressed_crafting_table", CompressedCraftingTableBlock::new);
//    public static RegistryObject<Block> double_compressed_crafting_table = block("double_compressed_crafting_table", DoubleCompressedCraftingTableBlock::new);

    public static RegistryObject<Block> NEUTRONIUM = RegistryObject.create(NEUTRONIUM_KEY, ForgeRegistries.BLOCKS);

    public static RegistryObject<Block> INFINITY = RegistryObject.create(INFINITY_KEY, ForgeRegistries.BLOCKS);
    public static RegistryObject<Block> CRYSTAL_MATRIX = RegistryObject.create(CRYSTAL_MATRIX_KEY, ForgeRegistries.BLOCKS);

//    public static RegistryObject<Block> extreme_crafting_table = block("extreme_crafting_table", ExtremeCraftingTableBlock::new);
//    public static RegistryObject<Block> neutron_collector = block("neutron_collector", NeutronCollectorBlock::new);
//    public static RegistryObject<Block> compressor = block("neutronium_compressor", CompressorBlock::new);
//    public static RegistryObject<Block> infinitato = block("infinitato", InfinitatoBlock::new);

    static void register(RegisterEvent.RegisterHelper<Block> helper) {
        BlockBehaviour.Properties resourceProps = BlockBehaviour.Properties.of().mapColor(MapColor.METAL).sound(SoundType.METAL)
                .strength(25.0F, 1000F);


        BlockBehaviour.Properties deviceProps = BlockBehaviour.Properties.of().mapColor(MapColor.METAL).sound(SoundType.METAL)
                .strength(1.0F, 1000F).noOcclusion();

        helper.register(NEUTRONIUM_KEY, new ResourceBlock(resourceProps));
        helper.register(INFINITY_KEY, new ResourceBlock(resourceProps));
        helper.register(CRYSTAL_MATRIX_KEY, new ResourceBlock(resourceProps));

    }

    private ModBlocks() {}
}
