package committee.nova.mods.avaritia.init.data.provider;

import committee.nova.mods.avaritia.Const;

import committee.nova.mods.avaritia.init.registry.ModArmorMaterial;
import committee.nova.mods.avaritia.init.registry.ModBlocks;
import committee.nova.mods.avaritia.init.registry.ModItems;
import committee.nova.mods.avaritia.init.registry.ModTags;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.IntrinsicHolderTagsProvider;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;

import java.util.concurrent.CompletableFuture;

/**
 * Avaritia 标签生成提供程序。
 * <p>
 * 在 {@code runData} 时为模组中的物品和方块注册标签，
 * 自动输出到 {@code src/generated/resources/data/<namespace>/tags/} 下。
 * 内部使用两个 {@link IntrinsicHolderTagsProvider} 分别处理物品与方块标签。
 */
public class AvaritiaTagProvider implements DataProvider {

    /** 物品标签提供者 */
    private final IntrinsicHolderTagsProvider<Item> itemTags;

    /** 方块标签提供者 */
    private final IntrinsicHolderTagsProvider<Block> blockTags;

    /**
     * 构造标签生成器。
     *
     * @param output 数据生成输出目录
     * @param lookup 注册表查找句柄
     */
    public AvaritiaTagProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> lookup) {
        this.itemTags = new IntrinsicHolderTagsProvider<>(output, Registries.ITEM, lookup,
                item -> item.builtInRegistryHolder().key(), Const.MOD_ID) {
            @Override
            protected void addTags(HolderLookup.Provider provider) {
                // ========== 物品标签 ==========

                // --- avaritia:singularity —— 奇点物品 ---
                tag(ItemTags.HOES).add(
                        ModItems.infinity_hoe.get(),
                        ModItems.crystal_hoe.get(),
                        ModItems.blaze_hoe.get()
                );

                tag(ItemTags.SHOVELS).add(
                        ModItems.infinity_shovel.get(),
                        ModItems.crystal_shovel.get(),
                        ModItems.blaze_shovel.get()
                );

                tag(ItemTags.SWORDS).add(
                        ModItems.infinity_sword.get(),
                        ModItems.crystal_sword.get(),
                        ModItems.blaze_sword.get()
                );

                tag(ItemTags.AXES).add(
                        ModItems.infinity_axe.get(),
                        ModItems.crystal_axe.get(),
                        ModItems.blaze_axe.get()
                );

                tag(ItemTags.PICKAXES).add(
                        ModItems.infinity_pickaxe.get(),
                        ModItems.crystal_pickaxe.get(),
                        ModItems.blaze_pickaxe.get()
                );

                tag(ItemTags.FREEZE_IMMUNE_WEARABLES).add(
                        ModItems.infinity_helmet.get(),
                        ModItems.infinity_chestplate.get(),
                        ModItems.infinity_pants.get(),
                        ModItems.infinity_boots.get()
                );

                tag(ModTags.CURIOS_RING).add(ModItems.neutron_ring.get(), ModItems.infinity_ring.get());
                tag(ModTags.CURIOS_CHARM).add(ModItems.infinity_totem.get());
                tag(ModTags.CURIOS_BACK).add(ModItems.infinity_elytra.get());

                tag(ModTags.SINGULARITY).add(
                        ModItems.singularity.get(),
                        ModItems.eternal_singularity.get()
                );

                // --- avaritia:endless —— 无尽（不朽）物品 ---
                tag(ModTags.IMMORTAL_ITEM).add(
                        // 无尽工具
                        ModItems.infinity_sword.get(),
                        ModItems.infinity_spear.get(),
                        ModItems.infinity_hoe.get(),
                        ModItems.infinity_pickaxe.get(),
                        ModItems.infinity_shovel.get(),
                        ModItems.infinity_axe.get(),
                        ModItems.infinity_bucket.get(),
                        ModItems.infinity_bow.get(),
                        ModItems.infinity_crossbow.get(),
                        ModItems.infinity_shield.get(),
                        ModItems.infinity_trident.get(),
                        ModItems.infinity_mace.get(),
                        // 无尽护甲
                        ModItems.infinity_helmet.get(),
                        ModItems.infinity_chestplate.get(),
                        ModItems.infinity_pants.get(),
                        ModItems.infinity_boots.get(),
                        // 无尽饰品
                        ModItems.infinity_totem.get(),
                        ModItems.infinity_ring.get(),
                        ModItems.infinity_umbrella.get(),
                        ModItems.infinity_clock.get(),
                        // 无尽资源
                        ModItems.infinity_nugget.get(),
                        ModItems.infinity_catalyst.get(),
                        ModItems.infinity_ingot.get(),
                        // 特殊物品
                        ModItems.infinity_elytra.get(),
                        ModItems.infinity_upgrade.get(),
                        ModItems.singularity.get(),
                        ModItems.matter_cluster.get(),
                        ModItems.full_matter_cluster.get(),
                        ModItems.eternal_singularity.get()
                );

                // --- elytraslot:elytra —— 鞘翅槽位兼容（使无限鞘翅可放入 Elytra Slot 槽位）---
                tag(ModTags.ELYTRA_SLOT).add(
                        ModItems.infinity_elytra.get()
                );

                // --- c:gears/neutronium —— 中子素齿轮 ---
                tag(ModTags.NEUTRON_GEAR).add(
                        ModItems.neutron_gear.get()
                );

                tag(ModTags.NEUTRON_DUST).add(
                        ModItems.neutron_pile.get()
                );

                // --- c:nuggets/neutronium —— 中子素粒 ---
                tag(ModTags.NEUTRON_NUGGET).add(
                        ModItems.neutron_nugget.get()
                );

                // --- c:ingots/neutronium —— 中子素锭 ---
                tag(ModTags.NEUTRON_INGOT).add(
                        ModItems.neutron_ingot.get()
                );

                // --- c:storage_blocks/neutronium —— 中子素块（物品形式）---
                tag(ModTags.NEUTRON_BLOCK_ITEM).add(
                        ModBlocks.neutron.get().asItem()
                );

                tag(ModTags.REPAIRS_BLAZE_TOOLS).add(
                        ModItems.blaze_cube.get()
                );

                tag(ModTags.REPAIRS_CRYSTAL_TOOLS).add(
                        ModItems.crystal_matrix_ingot.get()
                );

                tag(ModTags.REPAIRS_INFINITY_TOOLS).add(
                        ModItems.infinity_ingot.get()
                );

                tag(ModArmorMaterial.REPAIRS_INFINITY_ARMOR).add(
                        ModItems.infinity_ingot.get()
                );
            }
        };

        this.blockTags = new IntrinsicHolderTagsProvider<>(output, Registries.BLOCK, lookup,
                block -> block.builtInRegistryHolder().key(), Const.MOD_ID) {
            @Override
            protected void addTags(HolderLookup.Provider provider) {
                // ========== 方块标签 ==========

                // --- c:storage_blocks/neutronium —— 中子素块 ---
                tag(BlockTags.MINEABLE_WITH_PICKAXE).add(
                        ModBlocks.compressed_crafting_table.get(),
                        ModBlocks.double_compressed_crafting_table.get(),
                        ModBlocks.sculk_crafting_table.get(),
                        ModBlocks.nether_crafting_table.get(),
                        ModBlocks.end_crafting_table.get(),
                        ModBlocks.extreme_crafting_table.get(),
                        ModBlocks.crystal_matrix.get(),
                        ModBlocks.infinity.get(),
                        ModBlocks.neutron.get(),
                        ModBlocks.neutron_collector.get(),
                        ModBlocks.dense_neutron_collector.get(),
                        ModBlocks.denser_neutron_collector.get(),
                        ModBlocks.densest_neutron_collector.get(),
                        ModBlocks.neutron_compressor.get(),
                        ModBlocks.dense_neutron_compressor.get(),
                        ModBlocks.denser_neutron_compressor.get(),
                        ModBlocks.densest_neutron_compressor.get(),
                        ModBlocks.extreme_anvil.get(),
                        ModBlocks.infinity_chest.get(),
                        ModBlocks.tesseract.get(),
                        ModBlocks.extreme_smithing_table.get(),
                        Blocks.BEDROCK,
                        Blocks.END_PORTAL_FRAME,
                        Blocks.END_PORTAL,
                        ModBlocks.fake_bedrock.get(),
                        ModBlocks.fake_end_portal_frame.get(),
                        ModBlocks.fake_end_portal.get()
                );

                tag(BlockTags.MINEABLE_WITH_AXE).add(ModBlocks.compressed_chest.get());

                tag(BlockTags.MINEABLE_WITH_SHOVEL).add(
                        ModBlocks.endless_cake.get(),
                        ModBlocks.soul_farmland.get()
                );

                tag(BlockTags.BEACON_BASE_BLOCKS).add(
                        ModBlocks.crystal_matrix.get(),
                        ModBlocks.infinity.get(),
                        ModBlocks.neutron.get(),
                        ModBlocks.endless_cake.get()
                );

                tag(BlockTags.PORTALS).add(
                        ModBlocks.infinity.get(),
                        ModBlocks.neutron.get()
                );

                tag(BlockTags.ANVIL).add(ModBlocks.extreme_anvil.get());
                tag(BlockTags.WITHER_IMMUNE).add(ModBlocks.tesseract.get());

                tag(ModTags.NEUTRON_BLOCK).add(
                        ModBlocks.neutron.get()
                );

                // --- avaritia:extreme_anvil_unbreak —— 极压砧不可破坏的方块 ---
                // 这些方块硬度极高，极压砧下落时无法将其破坏
                tag(ModTags.EXTREME_ANVIL_UNBREAK).add(
                        ModBlocks.dense_neutron_collector.get(),
                        ModBlocks.denser_neutron_collector.get(),
                        ModBlocks.densest_neutron_collector.get(),
                        ModBlocks.extreme_crafting_table.get(),
                        ModBlocks.extreme_smithing_table.get(),
                        ModBlocks.neutron_compressor.get(),
                        ModBlocks.dense_neutron_compressor.get(),
                        ModBlocks.denser_neutron_compressor.get(),
                        ModBlocks.densest_neutron_compressor.get(),
                        ModBlocks.neutron.get(),
                        ModBlocks.infinity.get(),
                        ModBlocks.infinity_chest.get(),
                        ModBlocks.tesseract.get(),
                        ModBlocks.endless_cake.get(),
                        ModBlocks.extreme_anvil.get(),
                        ModBlocks.fake_bedrock.get(),
                        ModBlocks.fake_end_portal_frame.get(),
                        ModBlocks.fake_end_portal.get()
                );

                // --- avaritia:needs_crystal_tool —— 需要水晶品质工具挖掘 ---
                tag(ModTags.NEEDS_CRYSTAL_TOOL).add(
                        ModBlocks.crystal_matrix.get()
                );

                // --- avaritia:needs_blaze_tool —— 需要烈焰品质工具挖掘 ---
                tag(ModTags.NEEDS_BLAZE_TOOL).add(
                        ModBlocks.blaze_cube_block.get()
                );

                // --- avaritia:needs_infinity_tool —— 需要无尽品质工具挖掘 ---
                tag(ModTags.NEEDS_INFINITY_TOOL).add(
                        ModBlocks.neutron.get(),
                        ModBlocks.infinity.get()
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
