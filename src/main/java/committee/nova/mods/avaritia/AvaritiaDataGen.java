package committee.nova.mods.avaritia;

import committee.nova.mods.avaritia.init.data.ModRegistries;
import committee.nova.mods.avaritia.init.data.provider.*;
import committee.nova.mods.avaritia.init.data.provider.recipe.ModRecipes;
import io.github.fabricators_of_create.porting_lib.data.ExistingFileHelper;
import net.fabricmc.fabric.api.datagen.v1.DataGeneratorEntrypoint;
import net.fabricmc.fabric.api.datagen.v1.FabricDataGenerator;
import net.minecraft.DetectedVersion;
import net.minecraft.core.RegistrySetBuilder;
import net.minecraft.data.metadata.PackMetadataGenerator;
import net.minecraft.network.chat.Component;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.metadata.pack.PackMetadataSection;

/**
 * Author cnlimiter
 * CreateTime 2023/6/16 21:58
 * Name ModDataGen
 * Description
 */
public class AvaritiaDataGen implements DataGeneratorEntrypoint {

    @Override
    public void onInitializeDataGenerator(FabricDataGenerator generator) {
        ExistingFileHelper helper = ExistingFileHelper.withResourcesFromArg();
        FabricDataGenerator.Pack pack = generator.createPack();
        gatherData(pack, helper);

    }

    public void gatherData(FabricDataGenerator.Pack pack, ExistingFileHelper existingFileHelper) {
        pack.addProvider((output, registriesFuture) -> new ModSoundDefinitions(output, existingFileHelper));
        pack.addProvider(ModSpriteSource::new);
        pack.addProvider(ModRecipes::new);
        pack.addProvider(ModAdvancements::new);
        pack.addProvider(ModItemTags::new);
        pack.addProvider(ModBlockTags::new);
        pack.addProvider(ModEntityTags::new);
        pack.addProvider(ModRegistries::new);
        pack.addProvider((output, registriesFuture) -> new PackMetadataGenerator(output).add(PackMetadataSection.TYPE, new PackMetadataSection(
                Component.literal("Avaritia Resources"),
                DetectedVersion.BUILT_IN.getPackVersion(PackType.CLIENT_RESOURCES)
        )));

    }

    @Override
    public void buildRegistry(RegistrySetBuilder registryBuilder) {
        ModRegistries.addBootstraps(registryBuilder);
    }
}
