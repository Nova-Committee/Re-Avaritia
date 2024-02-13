package committee.nova.mods.avaritia.init.data;

import committee.nova.mods.avaritia.Static;
import committee.nova.mods.avaritia.init.data.provider.*;
import committee.nova.mods.avaritia.init.data.provider.loot.ModLootTables;
import committee.nova.mods.avaritia.init.data.provider.recipe.ModRecipes;
import committee.nova.mods.avaritia.init.data.provider.tags.*;
import committee.nova.mods.avaritia.init.mixins.RegistriesDataPackGeneratorAccessor;
import committee.nova.mods.avaritia.init.registry.ModRegistries;
import net.minecraft.DetectedVersion;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
import net.minecraft.data.metadata.PackMetadataGenerator;
import net.minecraft.network.chat.Component;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.metadata.pack.PackMetadataSection;
import net.minecraft.util.InclusiveRange;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.common.data.DatapackBuiltinEntriesProvider;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import net.neoforged.neoforge.data.event.GatherDataEvent;

import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiFunction;

/**
 * Author cnlimiter
 * CreateTime 2023/6/16 21:58
 * Name ModDataGen
 * Description
 */
public class ModDataGen {


    @SubscribeEvent
    public static void gatherData(GatherDataEvent event) {
        DataGenerator generator = event.getGenerator();
        PackOutput packOutput = generator.getPackOutput();
        ExistingFileHelper existingFileHelper = event.getExistingFileHelper();
        CompletableFuture<HolderLookup.Provider> lookupProviderFuture = event.getLookupProvider();

        if (event.includeClient()) {
            generator.addProvider(true, new ModBlockStates(generator, existingFileHelper));
            generator.addProvider(true, new ModSpriteSource(generator, lookupProviderFuture, existingFileHelper));
//            generator.addProvider(true, new ModItemModels(output, helper));
//            generator.addProvider(true, new ModLang(output));
            generator.addProvider(true, new ModSoundDefinitions(generator, existingFileHelper));
        }

        if (event.includeServer()) {
            DatapackBuiltinEntriesProvider provider = new DatapackBuiltinEntriesProvider(packOutput, lookupProviderFuture, ModRegistries.DATA_BUILDER, Set.of(Static.MOD_ID));
            generator.addProvider(true, provider);
            TagProvider.register(generator, event, packOutput, provider.getRegistryProvider(), existingFileHelper);
            generator.addProvider(true, new ModRecipes(generator, provider.getRegistryProvider()));
            generator.addProvider(true, new ModLootTables(generator));
//            generator.addProvider(true, new ModAdvancements(output, future, helper));
//            generator.addProvider(true, new ModFluidTags(output, future, helper));

            generator.addProvider(true, new PackMetadataGenerator(packOutput)
                    .add(PackMetadataSection.TYPE, new PackMetadataSection(
                            Component.literal("Avaritia Resources"),
                            DetectedVersion.BUILT_IN.getPackVersion(PackType.SERVER_DATA),
                            Optional.of(new InclusiveRange<>(0, Integer.MAX_VALUE)))));
        }
    }


    private static <T extends DataProvider> DataProvider.Factory<T> bindRegistries(
            BiFunction<PackOutput, CompletableFuture<HolderLookup.Provider>, T> pTagProviderFactory, CompletableFuture<HolderLookup.Provider> pLookupProvider
    ) {
        return p_255476_ -> pTagProviderFactory.apply(p_255476_, pLookupProvider);
    }
}
