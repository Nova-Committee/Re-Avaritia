package committee.nova.mods.avaritia.init.data.provider;

import committee.nova.mods.avaritia.init.data.provider.base.SingularityProvider;
import committee.nova.mods.avaritia.init.registry.ModSingularities;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.DataGenerator;
import net.neoforged.neoforge.common.data.ExistingFileHelper;

import java.util.concurrent.CompletableFuture;

/**
 * @author cnlimiter
 */
public class ModSingularityGen extends SingularityProvider {
    public ModSingularityGen(DataGenerator generator, CompletableFuture<HolderLookup.Provider> registries, ExistingFileHelper fileHelper) {
        super(generator, registries, fileHelper);
    }

    @Override
    public void generate(HolderLookup.Provider registries, ExistingFileHelper fileHelper) {
        addSingularity(ModSingularities.getDefaults());
    }
}
