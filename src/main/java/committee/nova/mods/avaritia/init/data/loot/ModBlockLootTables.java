package committee.nova.mods.avaritia.init.data.loot;

import committee.nova.mods.avaritia.Static;
import committee.nova.mods.avaritia.init.registry.ModBlocks;
import net.minecraft.data.loot.BlockLootSubProvider;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Name: Avaritia-forge / ModBlockLootTables
 * Author: cnlimiter
 * CreateTime: 2023/8/24 13:39
 * Description:
 */

public class ModBlockLootTables extends BlockLootSubProvider {

    protected ModBlockLootTables() {
        super(Collections.emptySet(), FeatureFlags.REGISTRY.allFlags());
    }
    @Override
    protected void generate() {
        dropSelf(ModBlocks.neutron.get());
        dropSelf(ModBlocks.infinity.get());
        dropSelf(ModBlocks.crystal_matrix.get());
        dropSelf(ModBlocks.neutron_compressor.get());
        dropSelf(ModBlocks.compressed_crafting_table.get());
        dropSelf(ModBlocks.double_compressed_crafting_table.get());
        dropSelf(ModBlocks.extreme_crafting_table.get());
        dropSelf(ModBlocks.neutron_collector.get());
    }

    @Override
    protected @NotNull Iterable<Block> getKnownBlocks() {
        return ForgeRegistries.BLOCKS.getValues().stream()
                .filter(block -> Static.MOD_ID.equals(Objects.requireNonNull(ForgeRegistries.BLOCKS.getKey(block)).getNamespace()))
                .collect(Collectors.toSet());
    }
}
