package committee.nova.mods.avaritia.init.data;

import committee.nova.mods.avaritia.init.data.loot.ModLootTables;
import committee.nova.mods.avaritia.init.data.recipe.ModRecipes;
import net.minecraft.data.DataGenerator;
import net.minecraftforge.common.data.ExistingFileHelper;
import net.minecraftforge.data.event.GatherDataEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

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
        ExistingFileHelper helper = event.getExistingFileHelper();

        if (event.includeClient()) {
            generator.addProvider(true, new ModBlockStates(generator, helper));
//            generator.addProvider(true, new ModItemModels(output, helper));
//            generator.addProvider(true, new ModLang(output));
            generator.addProvider(true, new ModSoundDefinitions(generator, helper));
        }
        if (event.includeServer()) {
            generator.addProvider(true, new ModRecipes(generator));
            generator.addProvider(true, new ModLootTables(generator));
            generator.addProvider(true, new ModBlockTags(generator, helper));
            generator.addProvider(true, new ModEntityTags(generator, helper));
//            generator.addProvider(true, new ModAdvancements(output, future, helper));
//            generator.addProvider(true, new ModFluidTags(output, future, helper));

        }
    }
}
