package committee.nova.mods.avaritia.init.data.provider;

import committee.nova.mods.avaritia.Static;
import committee.nova.mods.avaritia.init.registry.ModItems;
import committee.nova.mods.avaritia.init.registry.ModTags;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.tags.IntrinsicHolderTagsProvider;
import net.minecraft.world.item.Item;
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

public class ModItemTags extends IntrinsicHolderTagsProvider<Item> {

    public ModItemTags(DataGenerator output, CompletableFuture<HolderLookup.Provider> future, @Nullable ExistingFileHelper existingFileHelper) {
        super(output.getPackOutput(), Registries.ITEM, future, block -> block.builtInRegistryHolder().key(), Static.MOD_ID, existingFileHelper);
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
