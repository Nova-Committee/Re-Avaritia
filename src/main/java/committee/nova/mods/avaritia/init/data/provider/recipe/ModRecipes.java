package committee.nova.mods.avaritia.init.data.provider.recipe;

import committee.nova.mods.avaritia.init.registry.ModBlocks;
import committee.nova.mods.avaritia.init.registry.ModItems;
import committee.nova.mods.avaritia.init.registry.ModSingularities;
import committee.nova.mods.avaritia.util.SingularityUtil;
import net.minecraft.advancements.critereon.InventoryChangeTrigger;
import net.minecraft.advancements.critereon.ItemPredicate;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.recipes.*;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.block.Blocks;
import net.minecraftforge.common.Tags;
import org.jetbrains.annotations.NotNull;

import java.util.function.Consumer;

/**
 * Name: Avaritia-forge / ModRecipes
 * Author: cnlimiter
 * CreateTime: 2023/8/24 13:48
 * Description:
 */

public class ModRecipes extends RecipeProvider {
    public ModRecipes(DataGenerator output) {
        super(output.getPackOutput());
    }

    @Override
    protected void buildRecipes(@NotNull Consumer<FinishedRecipe> consumer) {
        InventoryChangeTrigger.TriggerInstance lul = has(Items.AIR);

        nineBlockStorageRecipesRecipesWithCustomUnpacking(consumer, RecipeCategory.MISC, ModItems.neutron_ingot.get(), RecipeCategory.BUILDING_BLOCKS, ModBlocks.neutron.get(), "neutron_ingot_from_neutron_block", "neutron_ingot");
        nineBlockStorageRecipesWithCustomPacking(consumer, RecipeCategory.MISC, ModItems.neutron_nugget.get(), RecipeCategory.MISC, ModItems.neutron_ingot.get(), "neutron_ingot_from_nuggets", "neutron_ingot");
        nineBlockStorageRecipesWithCustomPacking(consumer, RecipeCategory.MISC, ModItems.neutron_pile.get(), RecipeCategory.MISC, ModItems.neutron_nugget.get(), "neutron_pile_from_ingots", "neutron_pile");
        nineBlockStorageRecipes(consumer, RecipeCategory.MISC, ModItems.infinity_ingot.get(), RecipeCategory.BUILDING_BLOCKS, ModBlocks.infinity.get());
        nineBlockStorageRecipes(consumer, RecipeCategory.MISC, ModItems.crystal_matrix_ingot.get(), RecipeCategory.BUILDING_BLOCKS, ModBlocks.crystal_matrix.get());
        nineBlockStorageRecipesRecipesWithCustomUnpacking(consumer, RecipeCategory.BUILDING_BLOCKS, ModBlocks.compressed_crafting_table.get(), RecipeCategory.BUILDING_BLOCKS, ModBlocks.double_compressed_crafting_table.get(),
                "compressed_crafting_table_from_double_compressed_crafting_table", "compressed_crafting_table");
        nineBlockStorageRecipesWithCustomPacking(consumer, RecipeCategory.BUILDING_BLOCKS, Blocks.CRAFTING_TABLE, RecipeCategory.BUILDING_BLOCKS, ModBlocks.compressed_crafting_table.get(),
                "crafting_table_from_compressed_crafting_table", "crafting_table");


        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModItems.diamond_lattice.get())
                .pattern("x x")
                .pattern(" y ")
                .pattern("x x")
                .define('x', Items.DIAMOND)
                .define('y', Items.NETHERITE_SCRAP)
                .unlockedBy("", lul).save(consumer);

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModItems.crystal_matrix_ingot.get())
                .pattern("xyx")
                .pattern("xyx")
                .define('x', ModItems.diamond_lattice.get())
                .define('y', Items.NETHER_STAR)
                
                .unlockedBy("has_item", has(ModItems.diamond_lattice.get())).save(consumer);

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModBlocks.extreme_crafting_table.get())
                .pattern("xxx")
                .pattern("xyx")
                .pattern("xxx")
                .define('x', ModBlocks.crystal_matrix.get())
                .define('y', ModBlocks.double_compressed_crafting_table.get())
                .unlockedBy("has_block", has(ModBlocks.double_compressed_crafting_table.get())).save(consumer);

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModBlocks.endless_cake.get())
                .pattern("aaa")
                .pattern("bcb")
                .pattern("ded")
                .define('a', Items.MILK_BUCKET)
                .define('b', Items.SUGAR)
                .define('c', Items.DRAGON_EGG)
                .define('d', Tags.Items.CROPS_WHEAT)
                .define('e', ModItems.infinity_ingot.get())
                .unlockedBy("has_item", has(Items.DRAGON_EGG)).save(consumer);

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModBlocks.dense_neutron_collector.get())
                .pattern("aaa")
                .pattern("aga")
                .pattern("aaa")
                .define('a', ModBlocks.neutron_collector.get())
                .define('g', ModItems.neutron_gear.get())
                .unlockedBy("has_item", has(ModItems.neutron_gear.get())).save(consumer);

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModBlocks.denser_neutron_collector.get())
                .pattern("aaa")
                .pattern("aga")
                .pattern("aaa")
                .define('a', ModBlocks.dense_neutron_collector.get())
                .define('g', ModItems.neutron_gear.get())
                .unlockedBy("has_item", has(ModItems.neutron_gear.get())).save(consumer);


        ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, ModItems.record_fragment.get(), 4)
                .requires(ItemTags.MUSIC_DISCS)
                .unlockedBy("has_item", has(ModItems.record_fragment.get()))
                .save(consumer);




        ModShapelessRecipeBuilder.shapeless(RecipeCategory.TOOLS, ModItems.cosmic_meatballs.get())
                .requires(Items.PORKCHOP)
                .requires(Items.BEEF)
                .requires(Items.MUTTON)
                .requires(Items.COD)
                .requires(Items.SALMON)
                .requires(Items.TROPICAL_FISH)
                .requires(Items.PUFFERFISH)
                .requires(Items.RABBIT)
                .requires(Items.CHICKEN)
                .requires(Items.ROTTEN_FLESH)
                .requires(Items.SPIDER_EYE)
                .requires(Tags.Items.EGGS)
                .requires(ModItems.neutron_nugget.get())
                .unlockedBy("has_item", has(ModItems.neutron_nugget.get())).save(consumer);

        ModShapelessRecipeBuilder.shapeless(RecipeCategory.TOOLS, ModItems.ultimate_stew.get())
                .requires(Items.APPLE)
                .requires(Items.GOLDEN_APPLE)
                .requires(Items.BREAD)
                .requires(Items.KELP)
                .requires(Items.COCOA_BEANS)
                .requires(Items.CAKE)
                .requires(Items.GLISTERING_MELON_SLICE)
                .requires(Items.CARROT)
                .requires(Items.POISONOUS_POTATO)
                .requires(Items.CHORUS_FRUIT)
                .requires(Items.BEETROOT)
                .requires(Items.MUSHROOM_STEW)
                .requires(Items.HONEY_BOTTLE)
                .requires(Items.SWEET_BERRIES)
                .requires(ModItems.neutron_nugget.get())
                .unlockedBy("has_item", has(ModItems.neutron_nugget.get())).save(consumer);

        ModCatalystRecipeBuilder.shapeless(RecipeCategory.MISC)
                .requires(Items.EMERALD_BLOCK)
                .requires(ModItems.crystal_matrix_ingot.get())
                .requires(ModItems.neutron_ingot.get())
                .requires(ModItems.cosmic_meatballs.get())
                .requires(ModItems.ultimate_stew.get())
                .requires(ModItems.endest_pearl.get())
                .requires(ModItems.record_fragment.get())
                .unlockedBy("has_item", has(ModItems.neutron_ingot.get())).save(consumer);

        ModShapedRecipeBuilder.shaped(RecipeCategory.TOOLS, ModItems.endest_pearl.get())
                .pattern("   EEE   ")
                .pattern(" EEPPPEE ")
                .pattern(" EPPPPPE ")
                .pattern("EPPPNPPPE")
                .pattern("EPPNSNPPE")
                .pattern("EPPPNPPPE")
                .pattern(" EPPPPPE ")
                .pattern(" EEPPPEE ")
                .pattern("   EEE   ")
                .define('E', Tags.Items.END_STONES)
                .define('P', Tags.Items.ENDER_PEARLS)
                .define('S', Tags.Items.NETHER_STARS)
                .define('N', ModItems.neutron_ingot.get())
                .showNotification(true)
                .unlockedBy("has_item", has(ModItems.neutron_ingot.get())).save(consumer);

        ModShapedRecipeBuilder.shaped(RecipeCategory.TOOLS, ModItems.infinity_axe.get())
                .pattern("   I   ")
                .pattern("  IIIII")
                .pattern("  IIXI ")
                .pattern("   IN  ")
                .pattern("    N  ")
                .pattern("    N  ")
                .pattern("    N  ")
                .pattern("    N  ")
                .pattern("    N  ")
                .define('I', ModItems.infinity_ingot.get())
                .define('N', ModItems.neutron_ingot.get())
                .define('X', ModItems.infinity_catalyst.get())
                .showNotification(true)
                .unlockedBy("has_item", has(ModItems.infinity_ingot.get())).save(consumer);

        ModShapedRecipeBuilder.shaped(RecipeCategory.TOOLS, ModItems.infinity_hoe.get())
                .pattern("     N ")
                .pattern("   IIII")
                .pattern("  IIIII")
                .pattern("  I  XI")
                .pattern("     N ")
                .pattern("     N ")
                .pattern("     N ")
                .pattern("     N ")
                .pattern("     N ")
                .define('I', ModItems.infinity_ingot.get())
                .define('N', ModItems.neutron_ingot.get())
                .define('X', ModItems.infinity_catalyst.get())
                .showNotification(true)
                .unlockedBy("has_item", has(ModItems.infinity_ingot.get())).save(consumer);

        ModShapedRecipeBuilder.shaped(RecipeCategory.TOOLS, ModItems.infinity_pickaxe.get())
                .pattern(" IIIIIII ")
                .pattern("IIIIXIIII")
                .pattern("II  N  II")
                .pattern("    N    ")
                .pattern("    N    ")
                .pattern("    N    ")
                .pattern("    N    ")
                .pattern("    N    ")
                .pattern("    N    ")
                .define('I', ModItems.infinity_ingot.get())
                .define('N', ModItems.neutron_ingot.get())
                .define('X', ModItems.infinity_catalyst.get())
                .showNotification(true)
                .unlockedBy("has_item", has(ModItems.infinity_ingot.get())).save(consumer);

        ModShapedRecipeBuilder.shaped(RecipeCategory.TOOLS, ModItems.infinity_shovel.get())
                .pattern("      III")
                .pattern("     IIXI")
                .pattern("      III")
                .pattern("     N I ")
                .pattern("    N    ")
                .pattern("   N     ")
                .pattern("  N      ")
                .pattern(" N       ")
                .pattern("N        ")
                .define('I', ModItems.infinity_ingot.get())
                .define('N', ModItems.neutron_ingot.get())
                .define('X', ModItems.infinity_catalyst.get())
                .showNotification(true)
                .unlockedBy("has_item", has(ModItems.infinity_ingot.get())).save(consumer);

        ModShapedRecipeBuilder.shaped(RecipeCategory.TOOLS, ModItems.infinity_sword.get())
                .pattern("       II")
                .pattern("      III")
                .pattern("     III ")
                .pattern("    III  ")
                .pattern(" C III   ")
                .pattern("  CII    ")
                .pattern("  NC     ")
                .pattern(" N  C    ")
                .pattern("X        ")
                .define('I', ModItems.infinity_ingot.get())
                .define('N', ModItems.neutron_ingot.get())
                .define('C', ModItems.crystal_matrix_ingot.get())
                .define('X', ModItems.infinity_catalyst.get())
                .showNotification(true)
                .unlockedBy("has_item", has(ModItems.infinity_ingot.get())).save(consumer);

        ModShapedRecipeBuilder.shaped(RecipeCategory.TOOLS, ModItems.infinity_bow.get())
                .pattern("   II")
                .pattern("  I W")
                .pattern(" I  W")
                .pattern("I   W")
                .pattern("X   W")
                .pattern("I   W")
                .pattern(" I  W")
                .pattern("  I W")
                .pattern("   II")
                .define('I', ModItems.infinity_ingot.get())
                .define('W', ItemTags.WOOL)
                .define('X', ModBlocks.crystal_matrix.get())
                .showNotification(true)
                .unlockedBy("has_item", has(ModItems.infinity_ingot.get())).save(consumer);

        ModShapedRecipeBuilder.shaped(RecipeCategory.TOOLS, ModItems.infinity_boots.get())
                .pattern(" NNN NNN ")
                .pattern(" NIN NIN ")
                .pattern(" NIN NIN ")
                .pattern("NNIN NINN")
                .pattern("NIIN NIIN")
                .pattern("NNNN NNNN")
                .define('I', ModItems.infinity_ingot.get())
                .define('N', ModItems.neutron_ingot.get())
                .showNotification(true)
                .unlockedBy("has_item", has(ModItems.infinity_ingot.get())).save(consumer);

        ModShapedRecipeBuilder.shaped(RecipeCategory.TOOLS, ModItems.infinity_chestplate.get())
                .pattern(" NN   NN ")
                .pattern("NNN   NNN")
                .pattern("NNN   NNN")
                .pattern(" NIIIIIN ")
                .pattern(" NIIXIIN ")
                .pattern(" NIIIIIN ")
                .pattern(" NIIIIIN ")
                .pattern(" NIIIIIN ")
                .pattern("  NNNNN  ")
                .define('I', ModItems.infinity_ingot.get())
                .define('N', ModItems.neutron_ingot.get())
                .define('X', ModBlocks.crystal_matrix.get())
                .showNotification(true)
                .unlockedBy("has_item", has(ModItems.infinity_ingot.get())).save(consumer);

        ModShapedRecipeBuilder.shaped(RecipeCategory.TOOLS, ModItems.infinity_helmet.get())
                .pattern("  NNNNN  ")
                .pattern(" NIIIIIN ")
                .pattern(" N XIX N ")
                .pattern(" NIIIIIN ")
                .pattern(" NIIIIIN ")
                .pattern(" NI I IN ")
                .define('I', ModItems.infinity_ingot.get())
                .define('N', ModItems.neutron_ingot.get())
                .define('X', ModItems.infinity_catalyst.get())
                .showNotification(true)
                .unlockedBy("has_item", has(ModItems.infinity_ingot.get())).save(consumer);

        ModShapedRecipeBuilder.shaped(RecipeCategory.TOOLS, ModItems.infinity_pants.get())
                .pattern("NNNNNNNNN")
                .pattern("NIIIXIIIN")
                .pattern("NINNXNNIN")
                .pattern("NIN   NIN")
                .pattern("NCN   NCN")
                .pattern("NIN   NIN")
                .pattern("NIN   NIN")
                .pattern("NIN   NIN")
                .pattern("NNN   NNN")
                .define('I', ModItems.infinity_ingot.get())
                .define('N', ModItems.neutron_ingot.get())
                .define('C', ModItems.crystal_matrix_ingot.get())
                .define('X', ModItems.infinity_catalyst.get())
                .showNotification(true)
                .unlockedBy("has_item", has(ModItems.infinity_ingot.get())).save(consumer);

        ModShapedRecipeBuilder.shaped(RecipeCategory.TOOLS, ModItems.skull_sword.get())
                .pattern("       CX")
                .pattern("      CXC")
                .pattern("     CXC ")
                .pattern("    CXC  ")
                .pattern(" B CXC   ")
                .pattern("  BXC    ")
                .pattern("  WB     ")
                .pattern(" W  B    ")
                .pattern("D        ")
                .define('C', ModItems.crystal_matrix_ingot.get())
                .define('W', ItemTags.LOGS)
                .define('D', Tags.Items.NETHER_STARS)
                .define('X', Items.BLAZE_POWDER)
                .define('B', Tags.Items.BONES)
                .showNotification(true)
                .unlockedBy("has_item", has(ModItems.infinity_ingot.get())).save(consumer);

        ModShapedRecipeBuilder.shaped(RecipeCategory.TOOLS, ModBlocks.neutron_collector.get())
                .pattern("IIQQQQQII")
                .pattern("I QQQQQ I")
                .pattern("I  RRR  I")
                .pattern("C RRRRR C")
                .pattern("I RRCRR I")
                .pattern("C RRRRR C")
                .pattern("I  RRR  I")
                .pattern("I       I")
                .pattern("IIICICIII")
                .define('I', Items.IRON_BLOCK)
                .define('R', Items.REDSTONE_BLOCK)
                .define('C', ModItems.crystal_matrix_ingot.get())
                .define('Q', Items.QUARTZ_BLOCK)
                .showNotification(true)
                .unlockedBy("has_item", has(ModItems.infinity_catalyst.get())).save(consumer);

        ModShapedRecipeBuilder.shaped(RecipeCategory.TOOLS, ModBlocks.neutron_compressor.get())
                .pattern("IIIHHHIII")
                .pattern("C N   N C")
                .pattern("I N   N I")
                .pattern("C N   N C")
                .pattern("RNN O NNR")
                .pattern("C N   N C")
                .pattern("I N   N I")
                .pattern("C N   N C")
                .pattern("IIICICIII")
                .define('I', Items.IRON_BLOCK)
                .define('R', Items.REDSTONE_BLOCK)
                .define('C', ModItems.crystal_matrix_ingot.get())
                .define('O', ModBlocks.neutron.get())
                .define('H', Items.HOPPER)
                .define('N', ModItems.neutron_ingot.get())
                .showNotification(true)
                .unlockedBy("has_item", has(ModItems.neutron_ingot.get())).save(consumer);


        ModShapedRecipeBuilder.shaped(RecipeCategory.TOOLS, ModBlocks.densest_neutron_collector.get())
                .pattern("CC     CC")
                .pattern("C  BBB  C")
                .pattern("  AAAAA  ")
                .pattern(" BAXXXAB ")
                .pattern(" BAXYXAB ")
                .pattern(" BAXXXAB ")
                .pattern("  AAAAA  ")
                .pattern("C  BBB  C")
                .pattern("CC     CC")
                .define('A', Items.REDSTONE_BLOCK)
                .define('B', ModItems.neutron_ingot.get())
                .define('C', ModItems.neutron_gear.get())
                .define('X', ModBlocks.denser_neutron_collector.get())
                .define('Y', SingularityUtil.getItemForSingularity(ModSingularities.REDSTONE))
                .showNotification(true)
                .unlockedBy("has_item", has(ModBlocks.denser_neutron_collector.get())).save(consumer);

        ModShapedRecipeBuilder.shaped(RecipeCategory.TOOLS, ModItems.infinity_ingot.get())
                .pattern("NNNNNNNNN")
                .pattern("NCXXCXXCN")
                .pattern("NXCCXCCXN")
                .pattern("NCXXCXXCN")
                .pattern("NNNNNNNNN")
                .define('N', ModItems.neutron_ingot.get())
                .define('C', ModItems.crystal_matrix_ingot.get())
                .define('X', ModItems.infinity_catalyst.get())
                
                .unlockedBy("has_item", has(ModItems.neutron_ingot.get())).save(consumer);
    }

    protected static InventoryChangeTrigger.TriggerInstance has(@NotNull TagKey<Item> tagKey) {
        return inventoryTrigger(ItemPredicate.Builder.item().of(tagKey).build());
    }
}
