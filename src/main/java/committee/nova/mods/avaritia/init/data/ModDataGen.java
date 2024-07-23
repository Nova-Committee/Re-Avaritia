package committee.nova.mods.avaritia.init.data;

import committee.nova.mods.avaritia.init.data.provider.loot.ModLootTables;
import committee.nova.mods.avaritia.init.data.provider.*;
import committee.nova.mods.avaritia.init.data.provider.recipe.ModRecipes;
import net.minecraft.DetectedVersion;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.PackOutput;
import net.minecraft.data.metadata.PackMetadataGenerator;
import net.minecraft.network.chat.Component;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.metadata.pack.PackMetadataSection;
import net.minecraftforge.common.data.ExistingFileHelper;
import net.minecraftforge.data.event.GatherDataEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.util.Arrays;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Author cnlimiter
 * CreateTime 2023/6/16 21:58
 * Name ModDataGen
 * Description
 */
public final class ModDataGen {
    @SubscribeEvent
    public static void gatherData(GatherDataEvent event) {
        DataGenerator generator = event.getGenerator();
        PackOutput output = generator.getPackOutput();
        ExistingFileHelper helper = event.getExistingFileHelper();
        CompletableFuture<HolderLookup.Provider> future = event.getLookupProvider();

        if (event.includeClient()) {
            generator.addProvider(true, new ModBlockStates(output, helper));
            generator.addProvider(true, new ModSpriteSource(output, helper));
//            generator.addProvider(true, new ModItemModels(output, helper));
//            generator.addProvider(true, new ModLang(output));
            generator.addProvider(true, new ModSoundDefinitions(output, helper));
        }
        if (event.includeServer()) {
            generator.addProvider(true, new ModRecipes(output));
            generator.addProvider(true, new ModLootTables(output));
            generator.addProvider(true, new ModItemTags(output, future, helper));
            generator.addProvider(true, new ModBlockTags(output, future, helper));
            generator.addProvider(true, new ModEntityTags(output, future, helper));
            generator.addProvider(true, new ModAdvancements(output, future, helper));
//            generator.addProvider(true, new ModFluidTags(output, future, helper));

            ModRegistries.addProviders(true, generator, future, helper);
            generator.addProvider(true, new PackMetadataGenerator(output).add(PackMetadataSection.TYPE, new PackMetadataSection(
                    Component.literal("Avaritia Resources"),
                    DetectedVersion.BUILT_IN.getPackVersion(PackType.CLIENT_RESOURCES),
                    Arrays.stream(PackType.values()).collect(Collectors.toMap(Function.identity(), DetectedVersion.BUILT_IN::getPackVersion)))));
        }
    }
}
