package committee.nova.mods.avaritia.init.data;

import committee.nova.mods.avaritia.init.data.provider.*;
import committee.nova.mods.avaritia.init.data.provider.loot.ModLootTables;
import committee.nova.mods.avaritia.init.registry.ModDamageTypes;
import net.minecraft.DetectedVersion;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.PackOutput;
import net.minecraft.data.metadata.PackMetadataGenerator;
import net.minecraft.network.chat.Component;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.metadata.pack.PackMetadataSection;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import net.neoforged.neoforge.data.event.GatherDataEvent;

import java.util.concurrent.CompletableFuture;

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
        PackOutput output = generator.getPackOutput();
        ExistingFileHelper helper = event.getExistingFileHelper();
        CompletableFuture<HolderLookup.Provider> lookupProvider = event.getLookupProvider();

        if (event.includeClient()) {
            generator.addProvider(true, new ModBlockStates(output, helper));
            generator.addProvider(true, new ModSpriteSource(output, lookupProvider, helper));
//            generator.addProvider(true, new ModItemModels(output, helper));
//            generator.addProvider(true, new ModLang(output));
            generator.addProvider(true, new ModSoundDefinitions(output, helper));
        }
        if (event.includeServer()) {
            generator.addProvider(true, new ModRecipes(output, lookupProvider));
            generator.addProvider(true, new ModLootTables(output, lookupProvider));
            generator.addProvider(true, new ModItemTags(output, lookupProvider, helper));
            generator.addProvider(true, new ModBlockTags(output, lookupProvider, helper));
            generator.addProvider(true, new ModEntityTags(output, lookupProvider, helper));
            generator.addProvider(true, new ModAdvancements(output, lookupProvider, helper));
            var datapackProvider = new ModRegistries(output, event.getLookupProvider());
            generator.addProvider(true, datapackProvider);
            //generator.addProvider(true, new ModDamageTypeTags(output, lookupProvider.thenApply(ModRegistries::append), helper));
            generator.addProvider(true, new ModDamageTypeTags(output, datapackProvider.getRegistryProvider(), helper));
//            generator.addProvider(true, new ModFluidTags(output, lookupProvider, helper));

            generator.addProvider(true, new ModSingularityProvider(generator, helper));
            generator.addProvider(true, new PackMetadataGenerator(output).add(PackMetadataSection.TYPE, new PackMetadataSection(
                    Component.literal("Re:Avaritia Modern Resources"),
                    DetectedVersion.BUILT_IN.getPackVersion(PackType.CLIENT_RESOURCES)
            )));
        }




    }




}
