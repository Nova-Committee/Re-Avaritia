package committee.nova.mods.avaritia.init.data;

import committee.nova.mods.avaritia.Static;
import committee.nova.mods.avaritia.init.registry.ModDamageTypes;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.RegistrySetBuilder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.PackOutput;
import net.minecraftforge.common.data.DatapackBuiltinEntriesProvider;
import net.minecraftforge.common.data.ExistingFileHelper;

import java.util.Set;
import java.util.concurrent.CompletableFuture;
/**
 * Name: Avaritia-forge / ModRegistries
 * Author: cnlimiter
 * CreateTime: 2023/9/10 0:40
 * Description:
 */

public class ModRegistries extends DatapackBuiltinEntriesProvider {

    public static final RegistrySetBuilder BUILDER = new RegistrySetBuilder()
            .add(Registries.DAMAGE_TYPE, ModDamageTypes::bootstrap);

    private ModRegistries(PackOutput output, CompletableFuture<HolderLookup.Provider> future) {
        super(output, future, BUILDER, Set.of("minecraft", Static.MOD_ID));
    }

    public static void addProviders(boolean isServer, DataGenerator generator, PackOutput output, CompletableFuture<HolderLookup.Provider> provider, ExistingFileHelper helper) {
        generator.addProvider(isServer, new ModRegistries(output, provider));
        generator.addProvider(isServer, new ModDamageTypeTags(output, provider.thenApply(r -> append(r, BUILDER)), helper));
    }

    private static HolderLookup.Provider append(HolderLookup.Provider original, RegistrySetBuilder builder) {
        return builder.buildPatch(RegistryAccess.fromRegistryOfRegistries(BuiltInRegistries.REGISTRY), original);
    }
}
