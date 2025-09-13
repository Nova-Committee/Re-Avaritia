package committee.nova.mods.avaritia.init.registry;

import committee.nova.mods.avaritia.Const;
import committee.nova.mods.avaritia.api.common.block.BaseBlock;
import committee.nova.mods.avaritia.common.block.ResourceBlock;
import committee.nova.mods.avaritia.common.block.cake.EndlessCakeBlock;
import committee.nova.mods.avaritia.common.block.chest.TesseractBlock;
import committee.nova.mods.avaritia.common.block.chest.CompressedChestBlock;
import committee.nova.mods.avaritia.common.block.chest.InfinityChestBlock;
import committee.nova.mods.avaritia.common.block.chest.InfinityChestBlock2;
import committee.nova.mods.avaritia.common.block.collector.NeutronCollectorBlock;
import committee.nova.mods.avaritia.common.block.compressor.NeutronCompressorBlock;
import committee.nova.mods.avaritia.common.block.craft.CompressedCraftTableBlock;
import committee.nova.mods.avaritia.common.block.craft.DoubleCompressedCraftTableBlock;
import committee.nova.mods.avaritia.common.block.craft.RecipeGeneratorBlock;
import committee.nova.mods.avaritia.common.block.craft.TierCraftTableBlock;
import committee.nova.mods.avaritia.common.block.extreme.ExtremeAnvilBlock;
import committee.nova.mods.avaritia.common.block.extreme.ExtremeSmithingTableBlock;
import committee.nova.mods.avaritia.common.block.misc.SoulFarmLandBlock;
import committee.nova.mods.avaritia.common.item.resources.RefinedCoalItem;
import committee.nova.mods.avaritia.init.registry.enums.ModCraftTier;
import committee.nova.mods.avaritia.init.registry.enums.ModResourceBlocks;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EndPortalBlock;
import net.minecraft.world.level.block.EndPortalFrameBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.properties.NoteBlockInstrument;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import org.jetbrains.annotations.Nullable;

import java.util.function.Supplier;


/**
 * Description:
 * Author: cnlimiter
 * Date: 2022/4/2 6:47
 * Version: 1.0
 */
public class ModBlocks {
    public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, Const.MOD_ID);

    //CRAFTING
    public static RegistryObject<Block> compressed_crafting_table = itemBlock("compressed_crafting_table", CompressedCraftTableBlock::new, ModRarities.UNCOMMON);
    public static RegistryObject<Block> double_compressed_crafting_table = itemBlock("double_compressed_crafting_table", DoubleCompressedCraftTableBlock::new, ModRarities.UNCOMMON);

    //RESOURCE
    public static RegistryObject<Block> neutron = itemBlock("neutron", () -> new ResourceBlock(ModResourceBlocks.NEUTRON), ModRarities.EPIC);
    public static RegistryObject<Block> infinity = itemBlock("infinity", () -> new ResourceBlock(ModResourceBlocks.INFINITY), ModRarities.COSMIC);
    public static RegistryObject<Block> crystal_matrix = itemBlock("crystal_matrix", () -> new ResourceBlock(ModResourceBlocks.CRYSTAL), ModRarities.RARE);
    public static RegistryObject<Block> blaze_cube_block = itemBlock("blaze_cube_block", () -> new ResourceBlock(ModResourceBlocks.BLAZE), ModRarities.RARE);
    public static RegistryObject<Block> compressed_chest = itemBlock("compressed_chest", CompressedChestBlock::new, ModRarities.RARE);
    public static RegistryObject<Block> infinity_chest = itemBlock("infinity_chest", InfinityChestBlock::new, ModRarities.LEGEND);
    public static RegistryObject<Block> infinity_chest2 = itemBlock("infinity_chest2", InfinityChestBlock2::new, ModRarities.LEGEND);
    public static RegistryObject<Block> recipe_generator = itemBlock("recipe_generator_table", RecipeGeneratorBlock::new);
    public static RegistryObject<Block> tesseract = itemBlock("tesseract", TesseractBlock::new, ModRarities.LEGEND);
//    public static RegistryObject<Block> infinity_clock.json = itemBlock("infinity_clock.json", InfinityClockBlock::new, ModRarities.LEGEND);
    public static RegistryObject<Block> soul_farmland = itemBlock("soul_farmland", SoulFarmLandBlock::new, ModRarities.RARE);
    public static RegistryObject<Block> diamond_lattice_block = itemBlock("diamond_lattice_block",
            () -> new BaseBlock(BlockBehaviour.Properties.of()
                    .strength(100F, 100F)
                    .sound(SoundType.GLASS)
            ), true,
            new Item.Properties().rarity(ModRarities.UNCOMMON)
            );
    public static RegistryObject<Block> star_fuel_block = itemBurnBlock("star_fuel_block", () -> new BaseBlock(BlockBehaviour.Properties.of()
                    .strength(100F, 200F)
                    .sound(SoundType.STONE)
            ),  true,
            new Item.Properties().rarity(ModRarities.RARE), Integer.MAX_VALUE);

    public static RegistryObject<Block> refined_coal_block = itemBurnBlock("refined_coal_block", () -> new BaseBlock(BlockBehaviour.Properties.of()
                    .strength(50F, 50F)
                    .sound(SoundType.STONE)
            ),  true,
            new Item.Properties().rarity(ModRarities.UNCOMMON), RefinedCoalItem.BURN_TIME * 9);

    //MACHINE
    public static RegistryObject<Block> sculk_crafting_table = itemBlock("sculk_crafting_table", () -> new TierCraftTableBlock(ModCraftTier.SCULK), ModRarities.COMMON);
    public static RegistryObject<Block> nether_crafting_table = itemBlock("nether_crafting_table", () -> new TierCraftTableBlock(ModCraftTier.NETHER), ModRarities.UNCOMMON);
    public static RegistryObject<Block> end_crafting_table = itemBlock("end_crafting_table", () -> new TierCraftTableBlock(ModCraftTier.END), ModRarities.RARE);
    public static RegistryObject<Block> extreme_crafting_table = itemBlock("extreme_crafting_table", () -> new TierCraftTableBlock(ModCraftTier.EXTREME), ModRarities.EPIC);
    public static RegistryObject<Block> neutron_collector = itemBlock("neutron_collector", NeutronCollectorBlock::new, ModRarities.RARE);
    public static RegistryObject<Block> dense_neutron_collector = itemBlock("dense_neutron_collector", NeutronCollectorBlock::new, ModRarities.EPIC);
    public static RegistryObject<Block> denser_neutron_collector = itemBlock("denser_neutron_collector", NeutronCollectorBlock::new, ModRarities.LEGEND);
    public static RegistryObject<Block> densest_neutron_collector = itemBlock("densest_neutron_collector", NeutronCollectorBlock::new, ModRarities.COSMIC);
    public static RegistryObject<Block> neutron_compressor = itemBlock("neutron_compressor", NeutronCompressorBlock::new, ModRarities.RARE);
    public static RegistryObject<Block> dense_neutron_compressor = itemBlock("dense_neutron_compressor", NeutronCompressorBlock::new, ModRarities.EPIC);
    public static RegistryObject<Block> denser_neutron_compressor = itemBlock("denser_neutron_compressor", NeutronCompressorBlock::new, ModRarities.LEGEND);
    public static RegistryObject<Block> densest_neutron_compressor = itemBlock("densest_neutron_compressor", NeutronCompressorBlock::new, ModRarities.COSMIC);
    public static RegistryObject<Block> extreme_smithing_table = itemBlock("extreme_smithing_table", ExtremeSmithingTableBlock::new, ModRarities.LEGEND);

    public static RegistryObject<Block> extreme_anvil = itemBlock("extreme_anvil", ExtremeAnvilBlock::new, ModRarities.LEGEND);

    //CAKE
    public static RegistryObject<Block> endless_cake = itemBlock("endless_cake", EndlessCakeBlock::new, ModRarities.UNCOMMON);


    public static RegistryObject<Block> fake_bedrock = itemBlock("fake_bedrock", ()-> new Block(
            BlockBehaviour.Properties.of()
                    .mapColor(MapColor.STONE)
                    .instrument(NoteBlockInstrument.BASEDRUM)
                    .strength(1000F, 3600000.0F)
                    .isValidSpawn((state, level, pos, value) -> false)), false);
    public static RegistryObject<Block> fake_end_portal_frame = itemBlock("fake_end_portal_frame", ()-> new EndPortalFrameBlock(
            BlockBehaviour.Properties.of()
                    .mapColor(MapColor.COLOR_GREEN)
                    .instrument(NoteBlockInstrument.BASEDRUM)
                    .sound(SoundType.GLASS)
                    .lightLevel((blockState) -> 1)
                    .strength(400F, 3600000.0F)), false);
    public static RegistryObject<Block> fake_end_portal = itemBlock("fake_end_portal", () -> new EndPortalBlock(
            BlockBehaviour.Properties.of()
                    .mapColor(MapColor.COLOR_BLACK)
                    .noCollission()
                    .lightLevel((state) -> 15)
                    .strength(400F, 3600000.0F)
                    .pushReaction(PushReaction.BLOCK)), false);


    private static RegistryObject<Block> baseBlock(String name, Supplier<Block> block) {
        return BLOCKS.register(name, block);
    }

    public static RegistryObject<Block> itemBlock(String name, Supplier<Block> block) {
        return itemBlock(name, block, true);
    }

    public static RegistryObject<Block> itemBlock(String name, Supplier<Block> block, boolean hasItem) {
        return itemBlock(name, block, hasItem, true, new Item.Properties());
    }

    public static RegistryObject<Block> itemBlock(String name, Supplier<Block> block, boolean hasItem, boolean exist) {
        return itemBlock(name, block, hasItem, exist, new Item.Properties());
    }

    public static RegistryObject<Block> itemBlock(String name, Supplier<Block> block, Rarity rarity) {
        return itemBlock(name, block, true, true, new Item.Properties().rarity(rarity));
    }

    public static RegistryObject<Block> itemBlock(String name, Supplier<Block> block,  boolean hasItem, Item.Properties properties) {
        return itemBlock(name, block, hasItem, true, properties);
    }

    public static RegistryObject<Block> itemBlock(String name, Supplier<Block> block, boolean hasItem, boolean exist, Item.Properties properties) {
        var reg = BLOCKS.register(name, block);
        if (hasItem) ModItems.item(name, () -> new BlockItem(reg.get(), properties), exist);
        return reg;
    }

    public static RegistryObject<Block> itemBurnBlock(String name, Supplier<Block> block, boolean hasItem, Item.Properties properties, int burnTime) {
        var reg = BLOCKS.register(name, block);
        if (hasItem) ModItems.item(name, () -> new BlockItem(reg.get(), properties){
            @Override
            public int getBurnTime(ItemStack itemStack, @Nullable RecipeType<?> recipeType) {
                return burnTime;
            }
        });
        return reg;
    }
}
