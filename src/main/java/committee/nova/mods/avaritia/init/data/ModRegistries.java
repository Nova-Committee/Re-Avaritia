package committee.nova.mods.avaritia.init.data;

import committee.nova.mods.avaritia.Static;
import committee.nova.mods.avaritia.init.data.provider.ModDamageTypeTags;
import committee.nova.mods.avaritia.init.registry.ModDamageTypes;
import io.github.fabricators_of_create.porting_lib.data.DatapackBuiltinEntriesProvider;
import net.minecraft.Util;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.RegistrySetBuilder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.PackOutput;

import java.util.Set;
import java.util.concurrent.CompletableFuture;
/**
 * Name: Avaritia-forge / ModRegistries
 * Author: cnlimiter
 * CreateTime: 2023/9/10 0:40
 * Description:
 */

public class ModRegistries extends DatapackBuiltinEntriesProvider {

    public static final RegistrySetBuilder BUILDER =
            Util.make(new RegistrySetBuilder(), ModRegistries::addBootstraps);


    public ModRegistries(PackOutput output, CompletableFuture<HolderLookup.Provider> future) {
        super(output, future, BUILDER, Set.of("minecraft", Static.MOD_ID));
    }

    public static void addBootstraps(RegistrySetBuilder builder) {
        builder.add(Registries.DAMAGE_TYPE, ModDamageTypes::bootstrap);
    }

}
