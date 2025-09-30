package committee.nova.mods.avaritia.init.data.provider;

import committee.nova.mods.avaritia.Const;
import committee.nova.mods.avaritia.init.registry.ModItems;
import committee.nova.mods.avaritia.init.registry.ModTags;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.PackOutput;
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

    public ModItemTags(PackOutput output, CompletableFuture<HolderLookup.Provider> future, @Nullable ExistingFileHelper existingFileHelper) {
        super(output, Registries.ITEM, future, block -> block.builtInRegistryHolder().key(), Const.MOD_ID, existingFileHelper);
    }

    @Override
    public @NotNull String getName() {
        return "Avaritia Item Tags";
    }


    @Override
    protected void addTags(HolderLookup.@NotNull Provider p_256380_) {
        tag(ModTags.SINGULARITY).add(ModItems.singularity.get());
        tag(ModTags.NEUTRON_DUST).add(ModItems.neutron_pile.get());
        tag(ModTags.NEUTRON_NUGGET).add(ModItems.neutron_nugget.get());
        tag(ModTags.NEUTRON_INGOT).add(ModItems.neutron_ingot.get());
        tag(ModTags.ELYTRA_SLOT).add(ModItems.infinity_elytra.get());
        tag(ModTags.IMMORTAL_ITEM).add(ModItems.infinity_bow.get());
        tag(ModTags.IMMORTAL_ITEM).add(ModItems.infinity_crossbow.get());
        tag(ModTags.IMMORTAL_ITEM).add(ModItems.infinity_sword.get());
        tag(ModTags.IMMORTAL_ITEM).add(ModItems.infinity_pickaxe.get());
        tag(ModTags.IMMORTAL_ITEM).add(ModItems.infinity_axe.get());
        tag(ModTags.IMMORTAL_ITEM).add(ModItems.infinity_hoe.get());
        tag(ModTags.IMMORTAL_ITEM).add(ModItems.infinity_shovel.get());
        tag(ModTags.IMMORTAL_ITEM).add(ModItems.eternal_singularity.get());
        tag(ModTags.IMMORTAL_ITEM).add(ModItems.singularity.get());
        tag(ModTags.IMMORTAL_ITEM).add(ModItems.matter_cluster.get());
//        tag(ModTags.IMMORTAL_ITEM).add(ModItems.full_matter_cluster.get());
        tag(ModTags.IMMORTAL_ITEM).add(ModItems.neutron_horse_armor.get());
        tag(ModTags.IMMORTAL_ITEM).add(ModItems.infinity_helmet.get());
        tag(ModTags.IMMORTAL_ITEM).add(ModItems.infinity_chestplate.get());
        tag(ModTags.IMMORTAL_ITEM).add(ModItems.infinity_pants.get());
        tag(ModTags.IMMORTAL_ITEM).add(ModItems.infinity_boots.get());
    }
}
