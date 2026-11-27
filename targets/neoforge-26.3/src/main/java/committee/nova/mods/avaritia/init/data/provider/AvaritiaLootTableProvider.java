package committee.nova.mods.avaritia.init.data.provider;

import committee.nova.mods.avaritia.init.registry.ModBlocks;
import net.minecraft.core.registries.SingleRegistryBootstrap;
import net.minecraft.data.loot.BlockLootSubProvider;
import net.minecraft.data.loot.LootTableProvider;
import net.minecraft.data.loot.LootTableSubProvider;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Avaritia 方块战利品表提供程序。
 * <p>
 * 为所有已注册的拥有对应方块物品的方块生成自爆（self-drop）战利品表，
 * 使方块被破坏时掉落自身对应的物品。
 * 26.3 起战利品表为数据包注册表对象，通过 {@link SingleRegistryBootstrap}
 * 接入 RegistrySetBuilder，由 {@code AvaritiaRegistriesProvider} 统一注册。
 */
public class AvaritiaLootTableProvider extends LootTableProvider {

    public AvaritiaLootTableProvider() {
        super(Set.of(), List.of(
                new LootTableProvider.SubProviderEntry(
                        AvaritiaBlockLootSubProvider::new,
                        LootContextParamSets.BLOCK
                )
        ));
    }

    /**
     * 创建 Avaritia 战利品表数据生成引导器。
     *
     * @return 战利品表注册表的 SingleRegistryBootstrap
     */
    public static SingleRegistryBootstrap<LootTable> create() {
        return new AvaritiaLootTableProvider();
    }

    /**
     * Avaritia 方块战利品表子提供程序。
     * <p>
     * 遍历所有通过 {@link ModBlocks} 注册的方块，
     * 调用 {@link #dropSelf(Block)} 为每个拥有对应物品的方块生成自爆战利品表。
     */
    private static class AvaritiaBlockLootSubProvider extends BlockLootSubProvider {

        protected AvaritiaBlockLootSubProvider(LootTableSubProvider.Context output) {
            super(Set.of(), FeatureFlags.REGISTRY.allFlags(), output);
        }

        @Override
        protected void generate() {
            // 为所有拥有对应物品的方块生成自爆战利品表
            for (Block block : getKnownBlocks()) {
                if (block == ModBlocks.infinity_chest.get()) {
                    this.add(block, noDrop());
                } else if (block == ModBlocks.fake_bedrock.get()
                        || block == ModBlocks.fake_end_portal_frame.get()
                        || block == ModBlocks.fake_end_portal.get()) {
                    this.add(block, noDrop());
                } else if (block.asItem() != Items.AIR) {
                    this.dropSelf(block);
                }
            }
        }

        @Override
        protected Iterable<Block> getKnownBlocks() {
            return ModBlocks.BLOCKS.getEntries().stream()
                    .map(holder -> (Block) holder.get())
                    // 仅包含有对应方块物品的方块（排除无物品的假方块）
                    .collect(Collectors.toList());
        }
    }
}
