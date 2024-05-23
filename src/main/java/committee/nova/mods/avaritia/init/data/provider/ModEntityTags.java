package committee.nova.mods.avaritia.init.data.provider;

import committee.nova.mods.avaritia.init.registry.ModEntities;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.EntityTypeTagsProvider;
import net.minecraft.tags.EntityTypeTags;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.CompletableFuture;

/**
 * Author cnlimiter
 * CreateTime 2023/6/17 23:51
 * Name ModEntityTags
 * Description
 */

public class ModEntityTags extends EntityTypeTagsProvider {

    public ModEntityTags(PackOutput output, CompletableFuture<HolderLookup.Provider> future) {
        super(output, future);
    }

    @Override
    public @NotNull String getName() {
        return "Avaritia Entity Type Tags";
    }

    @Override
    protected void addTags(HolderLookup.@NotNull Provider provider) {
        //vanilla
        tag(EntityTypeTags.IMPACT_PROJECTILES).add(ModEntities.ENDER_PEARL.get());
    }
}
