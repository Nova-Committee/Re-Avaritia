package committee.nova.mods.avaritia.init.data.provider;

import committee.nova.mods.avaritia.init.registry.ModItems;
import committee.nova.mods.avaritia.init.registry.ModTags;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.IntrinsicHolderTagsProvider;
import net.minecraft.world.item.Item;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.CompletableFuture;

/**
 * Author cnlimiter
 * CreateTime 2023/6/17 23:35
 * Name ModBlockTags
 * Description
 */

public class ModItemTags extends IntrinsicHolderTagsProvider<Item> {

    public ModItemTags(PackOutput output, CompletableFuture<HolderLookup.Provider> future) {
        super(output, Registries.ITEM, future, block -> block.builtInRegistryHolder().key());
    }

    @Override
    public @NotNull String getName() {
        return "Avaritia Item Tags";
    }


    @Override
    protected void addTags(HolderLookup.@NotNull Provider p_256380_) {
        tag(ModTags.SINGULARITY).add(ModItems.singularity.get());
    }
}
