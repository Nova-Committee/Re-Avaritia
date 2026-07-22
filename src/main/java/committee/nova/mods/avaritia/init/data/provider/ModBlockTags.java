package committee.nova.mods.avaritia.init.data.provider;

import committee.nova.mods.avaritia.Const;
import committee.nova.mods.avaritia.init.registry.ModBlocks;
import committee.nova.mods.avaritia.init.registry.ModTags;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.IntrinsicHolderTagsProvider;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.concurrent.CompletableFuture;

/**
 * Author cnlimiter
 * CreateTime 2023/6/17 23:35
 * Name ModBlockTags
 * Description
 */

public class ModBlockTags extends IntrinsicHolderTagsProvider<Block> {

    public ModBlockTags(PackOutput output, CompletableFuture<HolderLookup.Provider> future, @Nullable ExistingFileHelper existingFileHelper) {
        super(output, Registries.BLOCK, future, block -> block.builtInRegistryHolder().key(), Const.MOD_ID, existingFileHelper);
    }

    @Override
    public @NotNull String getName() {
        return "Avaritia Block Tags";
    }


    @Override
    protected void addTags(HolderLookup.@NotNull Provider pProvider) {
        tag(BlockTags.MINEABLE_WITH_PICKAXE).add(
                ModBlocks.compressed_crafting_table.get(), ModBlocks.double_compressed_crafting_table.get(),
                ModBlocks.sculk_crafting_table.get(), ModBlocks.nether_crafting_table.get(), ModBlocks.end_crafting_table.get(), ModBlocks.extreme_crafting_table.get(),
                ModBlocks.crystal_matrix.get(), ModBlocks.infinity.get(), ModBlocks.neutron.get(),
                ModBlocks.neutron_collector.get(), ModBlocks.dense_neutron_collector.get(),
                ModBlocks.denser_neutron_collector.get(), ModBlocks.densest_neutron_collector.get(),
                ModBlocks.neutron_compressor.get(), ModBlocks.dense_neutron_compressor.get(), ModBlocks.denser_neutron_compressor.get(), ModBlocks.densest_neutron_compressor.get(),
                ModBlocks.extreme_anvil.get(),
                ModBlocks.infinity_chest.get(), ModBlocks.tesseract.get(), ModBlocks.extreme_smithing_table.get(),
                Blocks.BEDROCK, Blocks.END_PORTAL_FRAME, Blocks.END_PORTAL,
                ModBlocks.fake_bedrock.get(), ModBlocks.fake_end_portal_frame.get(), ModBlocks.fake_end_portal.get()
        );
        tag(BlockTags.MINEABLE_WITH_AXE).add(ModBlocks.compressed_chest.get());
        tag(BlockTags.MINEABLE_WITH_SHOVEL).add(ModBlocks.endless_cake.get(), ModBlocks.soul_farmland.get());
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
        tag(ModTags.NEUTRON_BLOCK).add(ModBlocks.neutron.get());
        tag(ModTags.EXTREME_ANVIL_UNBREAK).add(
                ModBlocks.dense_neutron_collector.get(), ModBlocks.denser_neutron_collector.get(), ModBlocks.densest_neutron_collector.get(),
                ModBlocks.extreme_crafting_table.get(), ModBlocks.extreme_smithing_table.get(), ModBlocks.neutron_compressor.get(),
                ModBlocks.neutron.get(), ModBlocks.infinity.get(),
                ModBlocks.infinity_chest.get(), ModBlocks.tesseract.get(), ModBlocks.endless_cake.get(), ModBlocks.extreme_anvil.get()
        );
        tag(BlockTags.WITHER_IMMUNE).add(ModBlocks.tesseract.get());
        tag(BlockTags.ANVIL).add(ModBlocks.extreme_anvil.get());
    }
}
