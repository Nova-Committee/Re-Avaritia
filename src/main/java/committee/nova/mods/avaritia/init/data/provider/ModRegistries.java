package committee.nova.mods.avaritia.init.data.provider;

import committee.nova.mods.avaritia.Const;
import committee.nova.mods.avaritia.init.registry.ModDamageTypes;
import net.minecraft.core.Cloner;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.RegistrySetBuilder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.PackOutput;
import net.neoforged.neoforge.common.data.DatapackBuiltinEntriesProvider;

import java.util.Set;
import java.util.concurrent.CompletableFuture;

/**
 * @Project: Avaritia
 * @Author: cnlimiter
 * @CreateTime: 2025/5/26 22:41
 * @Note:
 */
public class ModRegistries extends DatapackBuiltinEntriesProvider {

    public static final RegistrySetBuilder BUILDER = new RegistrySetBuilder()
            .add(Registries.DAMAGE_TYPE, ModDamageTypes::bootstrap);

//            .add(Registries.BIOME, ModBiomes::bootstrap)
//            .add(Registries.CONFIGURED_FEATURE, ModConfiguredFeatures::bootstrap)
//            .add(Registries.CONFIGURED_CARVER, ModConfiguredCarvers::bootstrap)
//            .add(Registries.DIMENSION_TYPE, ModDimensions::bootstrapType)
//            .add(Registries.LEVEL_STEM, ModDimensions::bootstrapStem)
//            .add(Registries.NOISE_SETTINGS, ModDimensions::bootstrapNoise)
//            .add(Registries.PLACED_FEATURE, ModPlacedFeatures::bootstrap)
//            .add(Registries.PROCESSOR_LIST, ModStructures::bootstrapProcessors)
//            .add(Registries.STRUCTURE, ModStructures::bootstrapStructures)
//            .add(Registries.STRUCTURE_SET, ModStructures::bootstrapSets)
//            .add(Registries.TEMPLATE_POOL, ModStructures::bootstrapPools)
//            .add(Registries.TRIM_MATERIAL, ModTrimMaterials::bootstrap)
//            .add(Registries.ENCHANTMENT, ModEnchantments::bootstrap)
//            .add(Registries.JUKEBOX_SONG, ModJukeboxSongs::bootstrap)
            ;

    public static HolderLookup.Provider append(HolderLookup.Provider original) {
        return BUILDER.buildPatch(RegistryAccess.fromRegistryOfRegistries(BuiltInRegistries.REGISTRY), original, new Cloner.Factory()).patches();
    }

    public ModRegistries(PackOutput output, CompletableFuture<HolderLookup.Provider> future) {
        super(output, future, BUILDER, Set.of("minecraft", Const.MOD_ID));
    }
}
