package com.avaritia.data;

import com.avaritia.Avaritia;
import com.avaritia.init.registry.ModBlocks;
import net.minecraft.data.loot.BlockLootSubProvider;
import net.minecraft.data.loot.LootTableProvider;
import net.minecraft.data.PackOutput;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Avaritia 方块战利品表提供程序。
 * <p>
 * 为所有已注册的拥有对应方块物品的方块生成自爆（self-drop）战利品表，
 * 使方块被破坏时掉落自身对应的物品。
 */
public class AvaritiaLootTableProvider extends LootTableProvider {

    public AvaritiaLootTableProvider(PackOutput output) {
        super(output, Set.of(), List.of(
                new LootTableProvider.SubProviderEntry(
                        new AvaritiaBlockLootSubProvider(),
                        LootContextParamSets.BLOCK
                )
        ));
    }

    /**
     * Avaritia 方块战利品表子提供程序。
     * <p>
     * 遍历所有通过 {@link ModBlocks} 注册的方块，
     * 调用 {@link #dropSelf(Block)} 为每个拥有对应物品的方块生成自爆战利品表。
     */
    private static class AvaritiaBlockLootSubProvider extends BlockLootSubProvider {

        protected AvaritiaBlockLootSubProvider() {
            super(Set.of(), FeatureFlags.REGISTRY.allFlags());
        }

        @Override
        protected void generate() {
            // 为所有拥有对应物品的方块生成自爆战利品表
            getKnownBlocks().forEach(this::dropSelf);
        }

        @Override
        protected Iterable<Block> getKnownBlocks() {
            return ModBlocks.BLOCKS.getEntries().stream()
                    .map(holder -> (Block) holder.get())
                    // 仅包含有对应方块物品的方块（排除无物品的假方块）
                    .filter(block -> block.asItem() != Items.AIR)
                    .collect(Collectors.toList());
        }
    }
}
