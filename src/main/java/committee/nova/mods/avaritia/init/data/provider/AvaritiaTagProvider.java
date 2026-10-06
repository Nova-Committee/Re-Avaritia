package committee.nova.mods.avaritia.init.data.provider;

import committee.nova.mods.avaritia.Const;

import committee.nova.mods.avaritia.init.registry.ModArmorMaterial;
import committee.nova.mods.avaritia.init.registry.ModBlocks;
import committee.nova.mods.avaritia.init.registry.ModItems;
import committee.nova.mods.avaritia.init.registry.ModTags;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.neoforge.common.Tags;
import net.neoforged.neoforge.common.data.BlockTagsProvider;
import net.neoforged.neoforge.common.data.ItemTagsProvider;

import java.util.concurrent.CompletableFuture;

/**
 * Avaritia 标签生成提供程序。
 * <p>
 * 在 {@code runData} 时为模组中的物品和方块注册标签，
 * 自动输出到 {@code src/generated/resources/data/<namespace>/tags/} 下。
 * 内部使用 NeoForge 的 {@link ItemTagsProvider} 与 {@link BlockTagsProvider}
 * 分别处理物品与方块标签（26.3 起 {@code IntrinsicHolderTagsProvider} 已被移除）。
 */
public class AvaritiaTagProvider implements DataProvider {

    /** 物品标签提供者 */
    private final ItemTagsProvider itemTags;

    /** 方块标签提供者 */
    private final BlockTagsProvider blockTags;

    /**
     * 构造标签生成器。
     *
     * @param output 数据生成输出目录
     * @param lookup 注册表查找句柄
     */
    public AvaritiaTagProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> lookup) {
        this.itemTags = new ItemTagsProvider(output, lookup, Const.MOD_ID) {
            @Override
            protected void addTags(HolderLookup.Provider provider) {
                // ========== 物品标签 ==========

                // --- avaritia:singularity —— 奇点物品 ---
                tag(ItemTags.HOES).add(
                        itemKey(ModItems.infinity_hoe.get()),
                        itemKey(ModItems.crystal_hoe.get()),
                        itemKey(ModItems.blaze_hoe.get())
                );

                tag(ItemTags.SHOVELS).add(
                        itemKey(ModItems.infinity_shovel.get()),
                        itemKey(ModItems.crystal_shovel.get()),
                        itemKey(ModItems.blaze_shovel.get())
                );

                tag(ItemTags.SWORDS).add(
                        itemKey(ModItems.infinity_sword.get()),
                        itemKey(ModItems.crystal_sword.get()),
                        itemKey(ModItems.blaze_sword.get())
                );

                tag(ItemTags.AXES).add(
                        itemKey(ModItems.infinity_axe.get()),
                        itemKey(ModItems.crystal_axe.get()),
                        itemKey(ModItems.blaze_axe.get())
                );

                tag(ItemTags.PICKAXES).add(
                        itemKey(ModItems.infinity_pickaxe.get()),
                        itemKey(ModItems.crystal_pickaxe.get()),
                        itemKey(ModItems.blaze_pickaxe.get())
                );

                tag(ItemTags.FREEZE_IMMUNE_WEARABLES).add(
                        itemKey(ModItems.infinity_helmet.get()),
                        itemKey(ModItems.infinity_chestplate.get()),
                        itemKey(ModItems.infinity_pants.get()),
                        itemKey(ModItems.infinity_boots.get())
                );

                tag(ModTags.CURIOS_RING).add(itemKey(ModItems.neutron_ring.get()), itemKey(ModItems.infinity_ring.get()));
                tag(ModTags.CURIOS_CHARM).add(itemKey(ModItems.infinity_totem.get()));
                tag(ModTags.CURIOS_BACK).add(itemKey(ModItems.infinity_elytra.get()));

                tag(ModTags.SINGULARITY).add(
                        itemKey(ModItems.singularity.get()),
                        itemKey(ModItems.eternal_singularity.get())
                );

                // --- avaritia:endless —— 无尽（不朽）物品 ---
                tag(ModTags.IMMORTAL_ITEM).add(
                        // 无尽工具
                        itemKey(ModItems.infinity_sword.get()),
                        itemKey(ModItems.infinity_spear.get()),
                        itemKey(ModItems.infinity_hoe.get()),
                        itemKey(ModItems.infinity_pickaxe.get()),
                        itemKey(ModItems.infinity_shovel.get()),
                        itemKey(ModItems.infinity_axe.get()),
                        itemKey(ModItems.infinity_bucket.get()),
                        itemKey(ModItems.infinity_bow.get()),
                        itemKey(ModItems.infinity_crossbow.get()),
                        itemKey(ModItems.infinity_shield.get()),
                        itemKey(ModItems.infinity_trident.get()),
                        itemKey(ModItems.infinity_mace.get()),
                        // 无尽护甲
                        itemKey(ModItems.infinity_helmet.get()),
                        itemKey(ModItems.infinity_chestplate.get()),
                        itemKey(ModItems.infinity_pants.get()),
                        itemKey(ModItems.infinity_boots.get()),
                        // 无尽饰品
                        itemKey(ModItems.infinity_totem.get()),
                        itemKey(ModItems.infinity_ring.get()),
                        itemKey(ModItems.infinity_umbrella.get()),
                        itemKey(ModItems.infinity_clock.get()),
                        // 无尽资源
                        itemKey(ModItems.infinity_nugget.get()),
                        itemKey(ModItems.infinity_catalyst.get()),
                        itemKey(ModItems.infinity_ingot.get()),
                        // 特殊物品
                        itemKey(ModItems.infinity_elytra.get()),
                        itemKey(ModItems.infinity_upgrade.get()),
                        itemKey(ModItems.singularity.get()),
                        itemKey(ModItems.matter_cluster.get()),
                        itemKey(ModItems.full_matter_cluster.get()),
                        itemKey(ModItems.eternal_singularity.get())
                );

                // --- elytraslot:elytra —— 鞘翅槽位兼容（使无限鞘翅可放入 Elytra Slot 槽位）---
                tag(ModTags.ELYTRA_SLOT).add(
                        itemKey(ModItems.infinity_elytra.get())
                );

                // --- c:gears/neutronium —— 中子素齿轮 ---
                tag(ModTags.NEUTRON_GEAR).add(
                        itemKey(ModItems.neutron_gear.get())
                );

                tag(ModTags.NEUTRON_DUST).add(
                        itemKey(ModItems.neutron_pile.get())
                );

                // --- c:nuggets/neutronium —— 中子素粒 ---
                tag(ModTags.NEUTRON_NUGGET).add(
                        itemKey(ModItems.neutron_nugget.get())
                );

                // --- c:ingots/neutronium —— 中子素锭 ---
                tag(ModTags.NEUTRON_INGOT).add(
                        itemKey(ModItems.neutron_ingot.get())
                );

                // --- c:storage_blocks/neutronium —— 中子素块（物品形式）---
                tag(ModTags.NEUTRON_BLOCK_ITEM).add(
                        itemKey(ModBlocks.neutron.get().asItem())
                );
                tag(Tags.Items.NUGGETS).add(itemKey(ModItems.neutron_nugget.get()), itemKey(ModItems.infinity_nugget.get()));
                tag(Tags.Items.INGOTS).add(itemKey(ModItems.neutron_ingot.get()), itemKey(ModItems.infinity_ingot.get()), itemKey(ModItems.crystal_matrix_ingot.get()));
                tag(Tags.Items.STORAGE_BLOCKS).add(itemKey(ModBlocks.neutron.get()), itemKey(ModBlocks.crystal_matrix.get()), itemKey(ModBlocks.infinity.get()));
                tag(Tags.Items.GEMS).add(itemKey(ModBlocks.crystal_matrix.get()));

                tag(ModTags.REPAIRS_BLAZE_TOOLS).add(
                        itemKey(ModItems.blaze_cube.get())
                );

                tag(ModTags.REPAIRS_CRYSTAL_TOOLS).add(
                        itemKey(ModItems.crystal_matrix_ingot.get())
                );

                tag(ModTags.REPAIRS_INFINITY_TOOLS).add(
                        itemKey(ModItems.infinity_ingot.get())
                );

                tag(ModArmorMaterial.REPAIRS_INFINITY_ARMOR).add(
                        itemKey(ModItems.infinity_ingot.get())
                );
            }
        };

        this.blockTags = new BlockTagsProvider(output, lookup, Const.MOD_ID) {
            @Override
            protected void addTags(HolderLookup.Provider provider) {
                // ========== 方块标签 ==========

                // --- c:storage_blocks/neutronium —— 中子素块 ---
                tag(BlockTags.MINEABLE_WITH_PICKAXE).add(
                        blockKey(ModBlocks.compressed_crafting_table.get()),
                        blockKey(ModBlocks.double_compressed_crafting_table.get()),
                        blockKey(ModBlocks.sculk_crafting_table.get()),
                        blockKey(ModBlocks.nether_crafting_table.get()),
                        blockKey(ModBlocks.end_crafting_table.get()),
                        blockKey(ModBlocks.extreme_crafting_table.get()),
                        blockKey(ModBlocks.crystal_matrix.get()),
                        blockKey(ModBlocks.infinity.get()),
                        blockKey(ModBlocks.neutron.get()),
                        blockKey(ModBlocks.neutron_collector.get()),
                        blockKey(ModBlocks.dense_neutron_collector.get()),
                        blockKey(ModBlocks.denser_neutron_collector.get()),
                        blockKey(ModBlocks.densest_neutron_collector.get()),
                        blockKey(ModBlocks.neutron_compressor.get()),
                        blockKey(ModBlocks.dense_neutron_compressor.get()),
                        blockKey(ModBlocks.denser_neutron_compressor.get()),
                        blockKey(ModBlocks.densest_neutron_compressor.get()),
                        blockKey(ModBlocks.extreme_anvil.get()),
                        blockKey(ModBlocks.infinity_chest.get()),
                        blockKey(ModBlocks.tesseract.get()),
                        blockKey(ModBlocks.extreme_smithing_table.get()),
                        blockKey(Blocks.BEDROCK),
                        blockKey(Blocks.END_PORTAL_FRAME),
                        blockKey(Blocks.END_PORTAL),
                        blockKey(ModBlocks.fake_bedrock.get()),
                        blockKey(ModBlocks.fake_end_portal_frame.get()),
                        blockKey(ModBlocks.fake_end_portal.get())
                );

                tag(BlockTags.MINEABLE_WITH_AXE).add(blockKey(ModBlocks.compressed_chest.get()));

                tag(BlockTags.MINEABLE_WITH_SHOVEL).add(
                        blockKey(ModBlocks.endless_cake.get()),
                        blockKey(ModBlocks.soul_farmland.get())
                );

                tag(BlockTags.BEACON_BASE_BLOCKS).add(
                        blockKey(ModBlocks.crystal_matrix.get()),
                        blockKey(ModBlocks.infinity.get()),
                        blockKey(ModBlocks.neutron.get()),
                        blockKey(ModBlocks.endless_cake.get())
                );

                tag(BlockTags.PORTALS).add(
                        blockKey(ModBlocks.infinity.get()),
                        blockKey(ModBlocks.neutron.get())
                );

                tag(BlockTags.ANVIL).add(blockKey(ModBlocks.extreme_anvil.get()));
                tag(BlockTags.WITHER_IMMUNE).add(blockKey(ModBlocks.tesseract.get()));

                tag(ModTags.NEUTRON_BLOCK).add(
                        blockKey(ModBlocks.neutron.get())
                );
                tag(Tags.Blocks.STORAGE_BLOCKS).add(blockKey(ModBlocks.neutron.get()), blockKey(ModBlocks.crystal_matrix.get()), blockKey(ModBlocks.infinity.get()));

                // --- avaritia:extreme_anvil_unbreak —— 极压砧不可破坏的方块 ---
                // 这些方块硬度极高，极压砧下落时无法将其破坏
                tag(ModTags.EXTREME_ANVIL_UNBREAK).add(
                        blockKey(ModBlocks.dense_neutron_collector.get()),
                        blockKey(ModBlocks.denser_neutron_collector.get()),
                        blockKey(ModBlocks.densest_neutron_collector.get()),
                        blockKey(ModBlocks.extreme_crafting_table.get()),
                        blockKey(ModBlocks.extreme_smithing_table.get()),
                        blockKey(ModBlocks.neutron_compressor.get()),
                        blockKey(ModBlocks.dense_neutron_compressor.get()),
                        blockKey(ModBlocks.denser_neutron_compressor.get()),
                        blockKey(ModBlocks.densest_neutron_compressor.get()),
                        blockKey(ModBlocks.neutron.get()),
                        blockKey(ModBlocks.infinity.get()),
                        blockKey(ModBlocks.infinity_chest.get()),
                        blockKey(ModBlocks.tesseract.get()),
                        blockKey(ModBlocks.endless_cake.get()),
                        blockKey(ModBlocks.extreme_anvil.get()),
                        blockKey(ModBlocks.fake_bedrock.get()),
                        blockKey(ModBlocks.fake_end_portal_frame.get()),
                        blockKey(ModBlocks.fake_end_portal.get())
                );

                // --- avaritia:needs_crystal_tool —— 需要水晶品质工具挖掘 ---
                tag(ModTags.NEEDS_CRYSTAL_TOOL).add(
                        blockKey(ModBlocks.crystal_matrix.get())
                );

                // --- avaritia:needs_blaze_tool —— 需要烈焰品质工具挖掘 ---
                tag(ModTags.NEEDS_BLAZE_TOOL).add(
                        blockKey(ModBlocks.blaze_cube_block.get())
                );

                // --- avaritia:needs_infinity_tool —— 需要无尽品质工具挖掘 ---
                tag(ModTags.NEEDS_INFINITY_TOOL).add(
                        blockKey(ModBlocks.neutron.get()),
                        blockKey(ModBlocks.infinity.get())
                );

                tag(ModTags.INCORRECT_FOR_BLAZE_TOOL)
                        .addTag(ModTags.NEEDS_CRYSTAL_TOOL)
                        .addTag(ModTags.NEEDS_INFINITY_TOOL);

                tag(ModTags.INCORRECT_FOR_CRYSTAL_TOOL)
                        .addTag(ModTags.NEEDS_INFINITY_TOOL);

                tag(ModTags.INCORRECT_FOR_INFINITY_TOOL);
            }
        };
    }

    /** 26.3 起 TagAppender 只接受 ResourceKey，物品统一转换为注册表键。 */
    private static ResourceKey<Item> itemKey(ItemLike item) {
        return item.asItem().builtInRegistryHolder().key();
    }

    /** 26.3 起 TagAppender 只接受 ResourceKey，方块统一转换为注册表键。 */
    private static ResourceKey<Block> blockKey(Block block) {
        return block.builtInRegistryHolder().key();
    }

    @Override
    public CompletableFuture<?> run(CachedOutput cache) {
        return CompletableFuture.allOf(
                itemTags.run(cache),
                blockTags.run(cache)
        );
    }

    @Override
    public String getName() {
        return "Avaritia Tags";
    }
}
