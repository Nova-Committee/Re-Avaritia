package committee.nova.mods.avaritia.init.data;

import committee.nova.mods.avaritia.init.registry.ModRegistries;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.PackOutput;
import net.minecraftforge.common.data.ExistingFileHelper;
import net.minecraftforge.data.event.GatherDataEvent;

import java.util.concurrent.CompletableFuture;

/**
 * Author cnlimiter
 * CreateTime 2023/6/16 21:58
 * Name ModDataGen
 * Description
 */

public class ModDataGen {


    public void gatherData(GatherDataEvent event) {
        DataGenerator generator = event.getGenerator();
        PackOutput output = generator.getPackOutput();
        CompletableFuture<HolderLookup.Provider> future = event.getLookupProvider();
        ExistingFileHelper helper = event.getExistingFileHelper();

        if (event.includeClient()) {
            generator.addProvider(true, new ModBlockStates(output, helper));
//            generator.addProvider(true, new UGItemModels(output, helper));
//            generator.addProvider(true, new UGLang(output));
//            generator.addProvider(true, new UGSoundDefinitions(output, helper));
        }
        if (event.includeServer()) {
//            generator.addProvider(true, new UGRecipes(output));
//            generator.addProvider(true, new UGLootTables(output));
//            UGBlockTags blockTags = new UGBlockTags(output, future, helper);
//            generator.addProvider(true, blockTags);
//            generator.addProvider(true, new UGItemTags(output, future, blockTags.contentsGetter(), helper));
//            generator.addProvider(true, new UGEntityTags(output, future, helper));
//            generator.addProvider(true, new UGAdvancements(output, future, helper));
//            generator.addProvider(true, new UGFluidTags(output, future, helper));
            ModRegistries.addProviders(true, generator, output, future, helper);
//            generator.addProvider(true, new PackMetadataGenerator(output).add(PackMetadataSection.TYPE, new PackMetadataSection(
//                    Component.literal("Undergarden resources"),
//                    DetectedVersion.BUILT_IN.getPackVersion(PackType.CLIENT_RESOURCES),
//                    Arrays.stream(PackType.values()).collect(Collectors.toMap(Function.identity(), DetectedVersion.BUILT_IN::getPackVersion)))));
        }
    }
}
