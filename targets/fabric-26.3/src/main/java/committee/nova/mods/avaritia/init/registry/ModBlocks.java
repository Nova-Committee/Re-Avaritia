package committee.nova.mods.avaritia.init.registry;

import committee.nova.mods.avaritia.Const;
import committee.nova.mods.avaritia.api.common.block.BaseBlock;
import committee.nova.mods.avaritia.common.block.ResourceBlock;
import committee.nova.mods.avaritia.common.block.cake.EndlessCakeBlock;
import committee.nova.mods.avaritia.common.block.chest.CompressedChestBlock;
import committee.nova.mods.avaritia.common.block.chest.InfinityChestBlock;
import committee.nova.mods.avaritia.common.block.chest.TesseractBlock;
import committee.nova.mods.avaritia.common.block.collector.NeutronCollectorBlock;
import committee.nova.mods.avaritia.common.block.compressor.NeutronCompressorBlock;
import committee.nova.mods.avaritia.common.block.craft.CompressedCraftTableBlock;
import committee.nova.mods.avaritia.common.block.craft.DoubleCompressedCraftTableBlock;
import committee.nova.mods.avaritia.common.block.craft.TierCraftTableBlock;
import committee.nova.mods.avaritia.common.block.extreme.ExtremeAnvilBlock;
import committee.nova.mods.avaritia.common.block.extreme.ExtremeSmithingTableBlock;
import committee.nova.mods.avaritia.common.block.misc.SoulFarmLandBlock;
import committee.nova.mods.avaritia.common.item.resources.RefinedCoalItem;
import committee.nova.mods.avaritia.init.registry.enums.ModCraftTier;
import committee.nova.mods.avaritia.init.registry.enums.ModResourceBlocks;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EndPortalFrameBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.properties.NoteBlockInstrument;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;
import committee.nova.mods.avaritia.api.init.registry.RegistryEntries;
import committee.nova.mods.avaritia.api.init.registry.RegistryEntry;
import committee.nova.mods.avaritia.api.init.registry.FuelBlockItem;

import java.util.function.Supplier;


/**
 * Description:
 * @author cnlimiter
 * Date: 2022/4/2 6:47
 * Version: 1.0
 */
public class ModBlocks {
    public static final RegistryEntries<Block> BLOCKS = RegistryEntries.create(BuiltInRegistries.BLOCK, Const.MOD_ID);

    //CRAFTING
    public static RegistryEntry<Block> compressed_crafting_table = itemBlock("compressed_crafting_table", CompressedCraftTableBlock::new, ModRarities.UNCOMMON);
    public static RegistryEntry<Block> double_compressed_crafting_table = itemBlock("double_compressed_crafting_table", DoubleCompressedCraftTableBlock::new, ModRarities.UNCOMMON);

    //RESOURCE
    public static RegistryEntry<Block> neutron = itemBlock("neutron", () -> new ResourceBlock(ModResourceBlocks.NEUTRON), ModRarities.EPIC);
    public static RegistryEntry<Block> infinity = itemBlock("infinity", () -> new ResourceBlock(ModResourceBlocks.INFINITY), ModRarities.COSMIC);
    public static RegistryEntry<Block> crystal_matrix = itemBlock("crystal_matrix", () -> new ResourceBlock(ModResourceBlocks.CRYSTAL), ModRarities.RARE);
    public static RegistryEntry<Block> blaze_cube_block = itemBlock("blaze_cube_block", () -> new ResourceBlock(ModResourceBlocks.BLAZE), ModRarities.RARE);
    public static RegistryEntry<Block> compressed_chest = itemBlock("compressed_chest", CompressedChestBlock::new, ModRarities.RARE);
    public static RegistryEntry<Block> infinity_chest = baseBlock("infinity_chest", InfinityChestBlock::new);
    public static RegistryEntry<Block> tesseract = itemBlock("tesseract", TesseractBlock::new, ModRarities.LEGEND);
    public static RegistryEntry<Block> soul_farmland = itemBlock("soul_farmland", SoulFarmLandBlock::new, ModRarities.RARE);
    public static RegistryEntry<Block> diamond_lattice_block = itemBlock("diamond_lattice_block",
            () -> new BaseBlock(properties()
                    .strength(100F, 100F)
                    .sound(SoundType.GLASS)
            ), true,
            new Item.Properties().rarity(ModRarities.UNCOMMON)
    );
    public static RegistryEntry<Block> star_fuel_block = itemBurnBlock("star_fuel_block", () -> new BaseBlock(properties()
                    .strength(100F, 200F)
                    .sound(SoundType.STONE)
            ), true,
            new Item.Properties().rarity(ModRarities.RARE), Integer.MAX_VALUE);

    public static RegistryEntry<Block> refined_coal_block = itemBurnBlock("refined_coal_block", () -> new BaseBlock(properties()
                    .strength(50F, 50F)
                    .sound(SoundType.STONE)
            ), true,
            new Item.Properties().rarity(ModRarities.UNCOMMON), RefinedCoalItem.BURN_TIME * 9);

    //MACHINE
    public static RegistryEntry<Block> sculk_crafting_table = itemBlock("sculk_crafting_table", () -> new TierCraftTableBlock(ModCraftTier.SCULK, properties().lightLevel(state -> 15)), ModRarities.COMMON);
    public static RegistryEntry<Block> nether_crafting_table = itemBlock("nether_crafting_table", () -> new TierCraftTableBlock(ModCraftTier.NETHER, properties().lightLevel(state -> 15)), ModRarities.UNCOMMON);
    public static RegistryEntry<Block> end_crafting_table = itemBlock("end_crafting_table", () -> new TierCraftTableBlock(ModCraftTier.END, properties().lightLevel(state -> 15)),ModRarities.RARE);
    public static RegistryEntry<Block> extreme_crafting_table = itemBlock("extreme_crafting_table", () -> new TierCraftTableBlock(ModCraftTier.EXTREME, properties().lightLevel(state -> 15)), ModRarities.EPIC);
    public static RegistryEntry<Block> neutron_collector = itemBlock("neutron_collector", NeutronCollectorBlock::new, ModRarities.RARE);
    public static RegistryEntry<Block> dense_neutron_collector = itemBlock("dense_neutron_collector", NeutronCollectorBlock::new, ModRarities.EPIC);
    public static RegistryEntry<Block> denser_neutron_collector = itemBlock("denser_neutron_collector", NeutronCollectorBlock::new, ModRarities.LEGEND);
    public static RegistryEntry<Block> densest_neutron_collector = itemBlock("densest_neutron_collector", NeutronCollectorBlock::new, ModRarities.COSMIC);
    public static RegistryEntry<Block> neutron_compressor = itemBlock("neutron_compressor", NeutronCompressorBlock::new, ModRarities.RARE);
    public static RegistryEntry<Block> dense_neutron_compressor = itemBlock("dense_neutron_compressor", NeutronCompressorBlock::new, ModRarities.EPIC);
    public static RegistryEntry<Block> denser_neutron_compressor = itemBlock("denser_neutron_compressor", NeutronCompressorBlock::new, ModRarities.LEGEND);
    public static RegistryEntry<Block> densest_neutron_compressor = itemBlock("densest_neutron_compressor", NeutronCompressorBlock::new, ModRarities.COSMIC);
    public static RegistryEntry<Block> extreme_smithing_table = itemBlock("extreme_smithing_table", ExtremeSmithingTableBlock::new, ModRarities.LEGEND);

    public static RegistryEntry<Block> extreme_anvil = itemBlock("extreme_anvil", ExtremeAnvilBlock::new, ModRarities.LEGEND);

    //CAKE
    public static RegistryEntry<Block> endless_cake = itemBlock("endless_cake", EndlessCakeBlock::new, ModRarities.UNCOMMON);


    public static RegistryEntry<Block> fake_bedrock = itemBlock("fake_bedrock", () -> new Block(
            properties()
                    .mapColor(MapColor.STONE)
                    .instrument(NoteBlockInstrument.BASEDRUM)
                    .strength(1000F, 3600000.0F)
                    .isValidSpawn((state, level, pos, value) -> false)), false);
    public static RegistryEntry<Block> fake_end_portal_frame = itemBlock("fake_end_portal_frame", () -> new EndPortalFrameBlock(
            properties()
                    .mapColor(MapColor.COLOR_GREEN)
                    .instrument(NoteBlockInstrument.BASEDRUM)
                    .sound(SoundType.GLASS)
                    .lightLevel((blockState) -> 1)
                    .strength(400F, 3600000.0F)), false);
    public static RegistryEntry<Block> fake_end_portal = itemBlock("fake_end_portal", () -> new Block(
            properties()
                    .mapColor(MapColor.COLOR_BLACK)
                    .noCollision()
                    .lightLevel((state) -> 15)
                    .strength(400F, 3600000.0F)
                    .pushReaction(PushReaction.IMMOVEABLE)), false);


    private static RegistryEntry<Block> baseBlock(String name, Supplier<Block> block) {
        return BLOCKS.register(name, (Identifier id) -> {
            BaseBlock.beginRegistration(ResourceKey.create(Registries.BLOCK, id));
            try {
                return block.get();
            } finally {
                BaseBlock.endRegistration();
            }
        });
    }

    /** 26.3 要求 BlockBehaviour.Properties 显式携带注册 id，仅在注册工厂执行期间可用。 */
    public static BlockBehaviour.Properties properties() {
        return BaseBlock.newProperties();
    }

    public static RegistryEntry<Block> itemBlock(String name, Supplier<Block> block) {
        return itemBlock(name, block, true);
    }

    public static RegistryEntry<Block> itemBlock(String name, Supplier<Block> block, boolean hasItem) {
        return itemBlock(name, block, hasItem, true, new Item.Properties());
    }

    public static RegistryEntry<Block> itemBlock(String name, Supplier<Block> block, boolean hasItem, boolean exist) {
        return itemBlock(name, block, hasItem, exist, new Item.Properties());
    }

    public static RegistryEntry<Block> itemBlock(String name, Supplier<Block> block, Rarity rarity) {
        return itemBlock(name, block, true, true, new Item.Properties().rarity(rarity));
    }

    public static RegistryEntry<Block> itemBlock(String name, Supplier<Block> block, boolean hasItem, Item.Properties properties) {
        return itemBlock(name, block, hasItem, true, properties);
    }

    public static RegistryEntry<Block> itemBlock(String name, Supplier<Block> block, boolean hasItem, boolean exist, Item.Properties properties) {
        var reg = baseBlock(name, block);
        if (hasItem) ModItems.blockItem(name, reg, properties, exist);
        return reg;
    }

    public static RegistryEntry<Block> itemBurnBlock(String name, Supplier<Block> block, boolean hasItem, Item.Properties properties, int burnTime) {
        var reg = baseBlock(name, block);
        if (hasItem) ModItems.item(name, () -> new FuelBlockItem(reg.get(), ModItems.applyId(properties).useBlockDescriptionPrefix(), burnTime));
        return reg;
    }

}
