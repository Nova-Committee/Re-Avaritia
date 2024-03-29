package committee.nova.mods.avaritia.init.data.provider.tags;

import committee.nova.mods.avaritia.Static;
import committee.nova.mods.avaritia.init.registry.ModBlocks;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.IntrinsicHolderTagsProvider;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.block.Block;
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

public class ModBlockTagProvider extends IntrinsicHolderTagsProvider<Block> {

    public ModBlockTagProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> future, @Nullable ExistingFileHelper existingFileHelper) {
        super(output, Registries.BLOCK, future, block -> block.builtInRegistryHolder().key(), Static.MOD_ID, existingFileHelper);
    }

    @Override
    public @NotNull String getName() {
        return "Avaritia Block Tags";
    }


    @Override
    protected void addTags(HolderLookup.Provider p_256380_) {
//        tag(BlockTags.MINEABLE_WITH_PICKAXE).add(ModBlocks.compressed_crafting_table.get(),
//                ModBlocks.compressor.get(), ModBlocks.crystal_matrix.get(),
//                ModBlocks.infinity.get(), ModBlocks.double_compressed_crafting_table.get(),
//                ModBlocks.extreme_crafting_table.get(), ModBlocks.neutron_collector.get(),
//                ModBlocks.neutronium.get(), ModBlocks.infinitato.get()
//        );
        tag(BlockTags.BEACON_BASE_BLOCKS).add(
                ModBlocks.crystal_matrix.get(),
                ModBlocks.infinity.get(),
                ModBlocks.neutron.get()
        );

        tag(BlockTags.PORTALS).add(
                ModBlocks.infinity.get(),
                ModBlocks.neutron.get()
        );

    }
}
