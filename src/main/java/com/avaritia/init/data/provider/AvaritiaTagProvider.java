package com.avaritia.init.data.provider;

import com.avaritia.Const;

import com.avaritia.init.registry.ModBlocks;
import com.avaritia.init.registry.ModItems;
import com.avaritia.init.registry.ModTags;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.IntrinsicHolderTagsProvider;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;

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
                tag(ModTags.SINGULARITY).add(
                        ModItems.singularity.get(),
                        ModItems.eternal_singularity.get()
                );

                // --- avaritia:endless —— 无尽（不朽）物品 ---
                tag(ModTags.IMMORTAL_ITEM).add(
                        // 无尽工具
                        ModItems.infinity_sword.get(),
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
            }
        };

        this.blockTags = new IntrinsicHolderTagsProvider<>(output, Registries.BLOCK, lookup,
                block -> block.builtInRegistryHolder().key(), Const.MOD_ID) {
            @Override
            protected void addTags(HolderLookup.Provider provider) {
                // ========== 方块标签 ==========

                // --- c:storage_blocks/neutronium —— 中子素块 ---
                tag(ModTags.NEUTRON_BLOCK).add(
                        ModBlocks.neutron.get()
                );

                // --- avaritia:extreme_anvil_unbreak —— 极压砧不可破坏的方块 ---
                // 这些方块硬度极高，极压砧下落时无法将其破坏
                tag(ModTags.EXTREME_ANVIL_UNBREAK).add(
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
