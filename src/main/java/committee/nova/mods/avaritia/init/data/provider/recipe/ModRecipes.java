package committee.nova.mods.avaritia.init.data.provider.recipe;

import committee.nova.mods.avaritia.Static;
import committee.nova.mods.avaritia.init.registry.ModBlocks;
import committee.nova.mods.avaritia.init.registry.ModItems;
import committee.nova.mods.avaritia.init.registry.ModSingularities;
import committee.nova.mods.avaritia.util.SingularityUtils;
import net.minecraft.advancements.critereon.InventoryChangeTrigger;
import net.minecraft.advancements.critereon.ItemPredicate;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.*;
import net.minecraft.nbt.ByteTag;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraftforge.common.Tags;
import net.minecraftforge.common.crafting.ConditionalRecipe;
import net.minecraftforge.common.crafting.conditions.IConditionBuilder;
import org.jetbrains.annotations.NotNull;

import java.util.function.Consumer;

/**
 * Name: Avaritia-forge / ModRecipes
 * Author: cnlimiter
 * CreateTime: 2023/8/24 13:48
 * Description:
 */

public class ModRecipes extends RecipeProvider implements IConditionBuilder{
    public ModRecipes(PackOutput output) {
        super(output);
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
        nineBlockStorageRecipesRecipesWithCustomUnpacking(consumer, RecipeCategory.MISC, Blocks.CRAFTING_TABLE, RecipeCategory.BUILDING_BLOCKS, ModBlocks.compressed_crafting_table.get(),
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

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModItems.neutron_gear.get())
                .pattern(" n ")
                .pattern("ncn")
                .pattern(" n ")
                .define('n', ModItems.neutron_ingot.get())
                .define('c', ModItems.crystal_matrix_ingot.get())
                .unlockedBy("has_item", has(ModItems.neutron_ingot.get())).save(consumer);

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModItems.star_fuel.get())
                .pattern("ccc")
                .pattern("cxc")
                .pattern("ccc")
                .define('c', Tags.Items.STORAGE_BLOCKS_COAL)
                .define('x', ModItems.infinity_catalyst.get())
                .unlockedBy("has_item", has(ModItems.infinity_catalyst.get())).save(consumer);

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

        ShapedRecipeBuilder.shaped(RecipeCategory.TOOLS, ModBlocks.compressed_chest.get())
                .pattern("ccc")
                .pattern("cgc")
                .pattern("ccc")
                .define('c', Blocks.CHEST)
                .define('g', ModItems.neutron_gear.get())
                .unlockedBy("has_item", has(Blocks.CHEST)).save(consumer);

        ShapedRecipeBuilder.shaped(RecipeCategory.TOOLS, ModItems.neutron_horse_armor.get())
                .pattern("  a")
                .pattern("cnc")
                .pattern("b b")
                .define('a', Items.DIAMOND_HELMET)
                .define('n', ModItems.neutron_ingot.get())
                .define('c', Items.LEATHER_HORSE_ARMOR)
                .define('b', Items.DIAMOND_LEGGINGS)
                .unlockedBy("has_item", has(Items.LEATHER_HORSE_ARMOR)).save(consumer);

        ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, ModItems.record_fragment.get(), 4)
                .requires(ItemTags.MUSIC_DISCS)
                .unlockedBy("has_item", has(ModItems.record_fragment.get()))
                .save(consumer);

        ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, ModItems.infinity_nugget.get(), 9)
                .requires(ModItems.infinity_ingot.get())
                .unlockedBy("has_item", has(ModItems.infinity_ingot.get())).save(consumer);


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
                .group("default")
                .unlockedBy("has_item", has(ModItems.neutron_ingot.get())).save(consumer);

        ModCatalystRecipeBuilder.shapeless(RecipeCategory.MISC)
                .requires(Items.EMERALD_BLOCK)
                .requires(ModItems.crystal_matrix_ingot.get())
                .requires(ModItems.neutron_ingot.get())
                .requires(ModItems.cosmic_meatballs.get())
                .requires(ModItems.ultimate_stew.get())
                .requires(ModItems.endest_pearl.get())
                .requires(ModItems.record_fragment.get())
                .requires(ModItems.eternal_singularity.get())
                .group("eternal_singularity")
                .unlockedBy("has_item", has(ModItems.eternal_singularity.get())).save(consumer, Static.rl("infinity_catalyst_eternal"));

        ModEternalRecipeBuilder.shapeless(RecipeCategory.MISC)
                .unlockedBy("has_item", has(ModItems.singularity.get())).save(consumer);

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
                .pattern("        N")
                .pattern("   IIIII ")
                .pattern("   IIIX  ")
                .pattern("    IN   ")
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

        ModShapedRecipeBuilder.shaped(RecipeCategory.TOOLS, ModItems.infinity_hoe.get())
                .pattern("        N")
                .pattern("     IIII")
                .pattern("   IIIII ")
                .pattern("     XI  ")
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
                .pattern("     II  ")
                .pattern("    I W  ")
                .pattern("   I  W  ")
                .pattern("  I   W  ")
                .pattern("  X   W  ")
                .pattern("  I   W  ")
                .pattern("   I  W  ")
                .pattern("    I W  ")
                .pattern("     II  ")
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
                .pattern("         ")
                .pattern("         ")
                .pattern("         ")
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

        ModShapedRecipeBuilder.shaped(RecipeCategory.TOOLS, ModItems.crystal_pickaxe.get())
                .pattern(" CCCWCCC ")
                .pattern("CWWWWWWWC")
                .pattern("CW  N  WC")
                .pattern("    N    ")
                .pattern("    N    ")
                .pattern("    N    ")
                .pattern("    N    ")
                .pattern("    N    ")
                .pattern("    N    ")
                .define('C', ModItems.crystal_matrix_ingot.get())
                .define('W', ModBlocks.crystal_matrix.get())
                .define('N', ModBlocks.neutron.get())
                .showNotification(true)
                .unlockedBy("has_item", has(ModItems.crystal_matrix_ingot.get())).save(consumer);

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
                .define('Y', SingularityUtils.getItemForSingularity(ModSingularities.REDSTONE))
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

        ModShapedRecipeBuilder.shaped(RecipeCategory.TOOLS, ModItems.infinity_totem.get())
                .pattern("   NNN   ")
                .pattern("  NIIIN  ")
                .pattern("  NYIYN  ")
                .pattern("CCCIIICCC")
                .pattern(" CCIIICC ")
                .pattern("  NIIIN  ")
                .pattern("  NNNNN  ")
                .pattern("   CCC   ")
                .pattern("    C    ")
                .define('N', ModItems.neutron_ingot.get())
                .define('I', ModItems.infinity_nugget.get())
                .define('Y', Items.TOTEM_OF_UNDYING)
                .define('C', ModItems.crystal_matrix_ingot.get())

                .unlockedBy("has_item", has(Items.TOTEM_OF_UNDYING)).save(consumer);

        ModShapedRecipeBuilder.shaped(RecipeCategory.TOOLS, ModItems.infinity_bucket.get())
                .pattern("NN     NN")
                .pattern("NNX   XNN")
                .pattern("NBBX XBBN")
                .pattern("NBBI IBBN")
                .pattern("NBBI IBBN")
                .pattern("NBBIIIBBN")
                .pattern("NNBBBBBNN")
                .pattern(" NNBBBNN ")
                .pattern("  NNNNN  ")
                .define('N', ModItems.neutron_ingot.get())
                .define('X', ModItems.infinity_catalyst.get())
                .define('I', ModItems.infinity_ingot.get())
                .define('B', Items.BUCKET)

                .unlockedBy("has_item", has(Items.BUCKET)).save(consumer);

        ConditionalRecipe.builder().addCondition(modLoaded("ae2")).addRecipe(
                ModShapedRecipeBuilder.shaped(RecipeCategory.TOOLS, ResourceLocation.tryBuild("ae2", "creative_energy_cell"))
                        .pattern("YYYYXYYYY")
                        .pattern("YCACXCACY")
                        .pattern("YACBXBCAY")
                        .pattern("YCBBXBBCY")
                        .pattern("XXXXDXXXX")
                        .pattern("YCBBXBBCY")
                        .pattern("YACBXBCAY")
                        .pattern("YCACXCACY")
                        .pattern("YYYYXYYYY")

                        .define('A', Static.getIngredient("ae2", "vibration_chamber"))
                        .define('B', Static.getIngredient("ae2", "calculation_processor"))
                        .define('C', ModItems.infinity_ingot.get())
                        .define('D', Static.getIngredient("ae2", "cell_component_256k"))
                        .define('Y', Static.getIngredient("ae2", "dense_energy_cell"))
                        .define('X', Static.getIngredient("ae2", "singularity"))
                        .unlockedBy("has_item", has(Static.getItem("ae2", "dense_energy_cell")))::save
        ).build(consumer, Static.rl( "ae2_creative_energy_cell"));

        var mana_tablet_tag = new CompoundTag();
        mana_tablet_tag.putInt("mana", 500000);
        mana_tablet_tag.putBoolean("creative", true);

        ConditionalRecipe.builder().addCondition(modLoaded("botania")).addRecipe(
                ModShapedRecipeBuilder.shaped(RecipeCategory.TOOLS, ResourceLocation.tryBuild("botania", "mana_tablet"), mana_tablet_tag)
                        .pattern("BAAACAAAD")
                        .pattern("ATTJKLTTA")
                        .pattern("ATUUMUUTA")
                        .pattern("ANUOPOUQA")
                        .pattern("EUUPRPUUF")
                        .pattern("ASUOPOUSA")
                        .pattern("ATUUUUUTA")
                        .pattern("ATTSISTTA")
                        .pattern("GAAAHAAAA")

                        .define('A', ModItems.infinity_ingot.get())
                        .define('B', Static.getIngredient("botania", "rune_envy"))
                        .define('C', Static.getIngredient("botania", "rune_gluttony"))
                        .define('D', Static.getIngredient("botania", "rune_winter"))
                        .define('E', Static.getIngredient("botania", "rune_lust"))
                        .define('F', Static.getIngredient("botania", "rune_pride"))
                        .define('G', Static.getIngredient("botania", "rune_wrath"))
                        .define('H', Static.getIngredient("botania", "rune_greed"))
                        .define('I', Static.getIngredient("botania", "rune_sloth"))
                        .define('J', Static.getIngredient("botania", "infinite_fruit"))
                        .define('K', Static.getIngredient("botania", "flight_tiara"))
                        .define('L', Static.getIngredient("botania", "king_key"))
                        .define('M', Static.getIngredient("botania", "flugel_eye"))
                        .define('N', Static.getIngredient("botania", "odin_ring"))
                        .define('O', Static.getIngredient("botania", "spawner_mover"))
                        .define('P', Static.getIngredient("botania", "mana_mirror"))
                        .define('Q', Static.getIngredient("botania", "thor_ring"))
                        .define('R', Static.getIngredient("botania", "mana_tablet"))
                        .define('S', Static.getIngredient("botania", "dice"))
                        .define('T', Static.getIngredient("botania", "fabulous_pool"))
                        .define('U', Static.getIngredient("botania", "terrasteel_block"))
                        .unlockedBy("has_item", has(Static.getItem("botania", "terrasteel_block")))::save
        ).build(consumer, Static.rl( "botania_mana_tablet"));

        ConditionalRecipe.builder().addCondition(modLoaded("botania")).addRecipe(
                ModShapedRecipeBuilder.shaped(RecipeCategory.TOOLS, ResourceLocation.tryBuild("botania", "creative_pool"))
                        .pattern("NNNNNNNNN")
                        .pattern("NXCXYXCXN")
                        .pattern("NCXEYEXCN")
                        .pattern("NXEEYEEXN")
                        .pattern("YYYYFYYYY")
                        .pattern("NXEEYEEXN")
                        .pattern("NCXEYEXCN")
                        .pattern("NXCXYXCXN")
                        .pattern("NNNNNNNNN")

                        .define('X', ModItems.infinity_catalyst.get())
                        .define('N', ModItems.neutron_ingot.get())
                        .define('C', Static.getIngredient("botania", "mana_pool"))
                        .define('Y', Static.getIngredient("botania", "fabulous_pool"))
                        .define('E', Static.getIngredient("botania", "dragonstone_block"))
                        .define('F', Static.getIngredient("botania", "mana_tablet"))
                        .unlockedBy("has_item", has(Static.getItem("botania", "mana_tablet")))::save
        ).build(consumer, Static.rl( "botania_creative_pool"));

        ConditionalRecipe.builder().addCondition(modLoaded("draconicevolution")).addRecipe(
                ModShapedRecipeBuilder.shaped(RecipeCategory.TOOLS, ResourceLocation.tryBuild("draconicevolution", "creative_capacitor"))
                        .pattern("AAAACAAAA")
                        .pattern("AEEBCBEEA")
                        .pattern("AEBFCFBEA")
                        .pattern("ABFFCFFBA")
                        .pattern("CCCCDCCCC")
                        .pattern("ABFFCFFBA")
                        .pattern("AEBFCFBEA")
                        .pattern("AEEBCBEEA")
                        .pattern("AAAACAAAA")

                        .define('A', ModItems.infinity_ingot.get())
                        .define('B', Static.getIngredient("draconicevolution", "chaotic_crafting_injector"))
                        .define('C', Static.getIngredient("draconicevolution", "reactor_stabilizer"))
                        .define('D', Static.getIngredient("draconicevolution", "reactor_core"))
                        .define('E', Static.getIngredient("draconicevolution", "chaotic_core"))
                        .define('F', Static.getIngredient("draconicevolution", "chaotic_capacitor"))
                        .unlockedBy("has_item", has(Static.getItem("draconicevolution", "chaotic_capacitor")))::save
        ).build(consumer, Static.rl( "de_creative_capacitor"));

        ConditionalRecipe.builder().addCondition(modLoaded("draconicevolution")).addRecipe(
                ModShapedRecipeBuilder.shaped(RecipeCategory.TOOLS, ResourceLocation.tryBuild("draconicevolution", "creative_op_capacitor"))
                        .pattern("BBCCCCCBB")
                        .pattern("BBBBBBBBB")
                        .pattern("CBAAAAABC")
                        .pattern("CBACECABC")
                        .pattern("CBAEDEABC")
                        .pattern("CBACECABC")
                        .pattern("CBAAAAABC")
                        .pattern("BBBBBBBBB")
                        .pattern("BBCCCCCBB")

                        .define('A', ModItems.infinity_ingot.get())
                        .define('B', ModBlocks.infinity.get())
                        .define('C', Static.getIngredient("draconicevolution", "reactor_stabilizer"))
                        .define('D', Static.getIngredient("draconicevolution", "reactor_core"))
                        .define('E', Static.getIngredient("draconicevolution", "creative_capacitor"))
                        .unlockedBy("has_item", has(Static.getItem("draconicevolution", "creative_capacitor")))::save
        ).build(consumer, Static.rl( "de_creative_op_capacitor"));

        ConditionalRecipe.builder().addCondition(modLoaded("refinedstorage")).addRecipe(
                ModShapedRecipeBuilder.shaped(RecipeCategory.TOOLS, ResourceLocation.tryBuild("refinedstorage", "creative_controller"))
                        .pattern("ABBBCBBBA")
                        .pattern("BDDDCDDDB")
                        .pattern("BDDCCCDDB")
                        .pattern("BDCCFCCDB")
                        .pattern("CCCFAFCCC")
                        .pattern("BECCFCCEB")
                        .pattern("BEECCCEEB")
                        .pattern("BEEECEEEB")
                        .pattern("ABBBCBBBA")

                        .define('A', ModItems.infinity_catalyst.get())
                        .define('B', ModItems.neutron_ingot.get())
                        .define('C', Static.getIngredient("refinedstorage", "advanced_processor"))
                        .define('D', Static.getIngredient("refinedstorage", "4096k_fluid_storage_part"))
                        .define('E', Static.getIngredient("refinedstorage", "64k_storage_part"))
                        .define('F', ItemTags.create(new ResourceLocation("refinedstorage", "controller")))
                        .unlockedBy("has_item", has(ItemTags.create(new ResourceLocation("refinedstorage", "controller"))))::save
        ).build(consumer, Static.rl("rs_creative_controller"));

        ConditionalRecipe.builder().addCondition(modLoaded("refinedstorage")).addRecipe(
                ModShapedRecipeBuilder.shaped(RecipeCategory.TOOLS, ResourceLocation.tryBuild("refinedstorage", "creative_fluid_storage_disk"))
                        .pattern("CAAABAAAC")
                        .pattern("AAAABAAAA")
                        .pattern("AAAABAAAA")
                        .pattern("AAADCDAAA")
                        .pattern("BBBCDCBBB")
                        .pattern("AAADCDAAA")
                        .pattern("AAAABAAAA")
                        .pattern("AAAABAAAA")
                        .pattern("CAAABAAAC")

                        .define('A', ModBlocks.infinity.get())
                        .define('B', ModBlocks.neutron_compressor.get())
                        .define('C', Static.getIngredient("refinedstorage", "creative_controller"))
                        .define('D', Static.getIngredient("refinedstorage", "4096k_fluid_storage_part"))
                        .unlockedBy("has_item", has(Static.getItem("refinedstorage", "creative_controller")))::save
        ).build(consumer, Static.rl( "rs_creative_fluid_storage_disk"));

        ConditionalRecipe.builder().addCondition(modLoaded("refinedstorage")).addRecipe(
                ModShapedRecipeBuilder.shaped(RecipeCategory.TOOLS, ResourceLocation.tryBuild("refinedstorage", "creative_storage_disk"))
                        .pattern("CAAABAAAC")
                        .pattern("AAAABAAAA")
                        .pattern("AAAABAAAA")
                        .pattern("AAADCDAAA")
                        .pattern("BBBCDCBBB")
                        .pattern("AAADCDAAA")
                        .pattern("AAAABAAAA")
                        .pattern("AAAABAAAA")
                        .pattern("CAAABAAAC")

                        .define('A', ModBlocks.infinity.get())
                        .define('B', ModBlocks.neutron_compressor.get())
                        .define('C', Static.getIngredient("refinedstorage", "creative_controller"))
                        .define('D', Static.getIngredient("refinedstorage", "64k_storage_part"))
                        .unlockedBy("has_item", has(Static.getItem("refinedstorage", "creative_controller")))::save
        ).build(consumer, Static.rl( "rs_creative_storage_disk"));

        ConditionalRecipe.builder().addCondition(modLoaded("refinedstorage")).addRecipe(
                ModShapedRecipeBuilder.shaped(RecipeCategory.TOOLS, ResourceLocation.tryBuild("refinedstorage", "creative_wireless_grid"))
                        .pattern("HH     HH")
                        .pattern("H       H")
                        .pattern(" BCCCCCB ")
                        .pattern(" CDDFEEC ")
                        .pattern(" CGFAFGC ")
                        .pattern(" CDDFEEC ")
                        .pattern(" BCCCCCB ")
                        .pattern("H       H")
                        .pattern("HH     HH")


                        .define('A', ModItems.endest_pearl.get())
                        .define('B', Static.getIngredient("refinedstorage", "range_upgrade"))
                        .define('C', Static.getIngredient("refinedstorage", "wireless_transmitter"))
                        .define('D', Static.getIngredient("refinedstorage", "destruction_core"))
                        .define('E', Static.getIngredient("refinedstorage", "construction_core"))
                        .define('F', Static.getIngredient("refinedstorage", "wireless_grid"))
                        .define('G', Static.getIngredient("refinedstorage", "network_receiver"))
                        .define('H', Static.getIngredient("refinedstorage", "storage_housing"))
                        .unlockedBy("has_item", has(Static.getItem("refinedstorage", "wireless_grid")))::save
        ).build(consumer, Static.rl( "rs_creative_wireless_grid"));

        var creative_slot_abilities = new CompoundTag();
        creative_slot_abilities.putString("slot", "abilities");

        ConditionalRecipe.builder().addCondition(modLoaded("tconstruct")).addRecipe(
                ModShapedRecipeBuilder.shaped(RecipeCategory.TOOLS, ResourceLocation.tryBuild("tconstruct", "creative_slot"), creative_slot_abilities)
                        .pattern("GGBHHHBGG")
                        .pattern("GCCCCCCCG")
                        .pattern("BCBFFFBCB")
                        .pattern("HCFFEFFCH")
                        .pattern("HCFEAEFCH")
                        .pattern("HCFFEFFCH")
                        .pattern("BCBFFFBCB")
                        .pattern("GCCCCCCCG")
                        .pattern("GGBHHHBGG")

                        .define('A', ModItems.infinity_catalyst.get())
                        .define('B', Static.getIngredient("tconstruct", "iron_reinforcement"))
                        .define('C', Static.getIngredient("tconstruct", "knightslime_ingot"))
                        .define('E', Static.getIngredient("tconstruct", "manyullyn_block"))
                        .define('F', Static.getIngredient("tconstruct", "jeweled_apple"))
                        .define('G', Static.getIngredient("tconstruct", "iron_reinforcement"))
                        .define('H', Static.getIngredient("tconstruct", "ichor_slime_crystal"))
                        .unlockedBy("has_item", has(Static.getItem("tconstruct", "ichor_slime_crystal")))::save
        ).build(consumer, Static.rl( "tc3_creative_slot_ability"));

        var creative_slot_defense = new CompoundTag();
        creative_slot_defense.putString("slot", "defense");

        ConditionalRecipe.builder().addCondition(modLoaded("tconstruct")).addRecipe(
                ModShapedRecipeBuilder.shaped(RecipeCategory.TOOLS, ResourceLocation.tryBuild("tconstruct", "creative_slot"), creative_slot_defense)
                        .pattern("GGBHHHBGG")
                        .pattern("GCCCCCCCG")
                        .pattern("BCBFFFBCB")
                        .pattern("HCFFEFFCH")
                        .pattern("HCFEAEFCH")
                        .pattern("HCFFEFFCH")
                        .pattern("BCBFFFBCB")
                        .pattern("GCCCCCCCG")
                        .pattern("GGBHHHBGG")

                        .define('A', ModItems.infinity_catalyst.get())
                        .define('B', Static.getIngredient("tconstruct", "iron_reinforcement"))
                        .define('C', Static.getIngredient("tconstruct", "knightslime_ingot"))
                        .define('E', Static.getIngredient("tconstruct", "manyullyn_block"))
                        .define('F', Static.getIngredient("tconstruct", "jeweled_apple"))
                        .define('G', Static.getIngredient("tconstruct", "iron_reinforcement"))
                        .define('H', Static.getIngredient("tconstruct", "earth_slime_crystal"))
                        .unlockedBy("has_item", has(Static.getItem("tconstruct", "earth_slime_crystal")))::save
        ).build(consumer, Static.rl("tc3_creative_slot_defense"));


        var creative_slot_souls = new CompoundTag();
        creative_slot_souls.putString("slot", "souls");

        ConditionalRecipe.builder().addCondition(modLoaded("tconstruct")).addRecipe(
                ModShapedRecipeBuilder.shaped(RecipeCategory.TOOLS, ResourceLocation.tryBuild("tconstruct", "creative_slot"), creative_slot_souls)
                        .pattern("GGBHHHBGG")
                        .pattern("GCCCCCCCG")
                        .pattern("BCBFFFBCB")
                        .pattern("HCFFEFFCH")
                        .pattern("HCFEAEFCH")
                        .pattern("HCFFEFFCH")
                        .pattern("BCBFFFBCB")
                        .pattern("GCCCCCCCG")
                        .pattern("GGBHHHBGG")

                        .define('A', ModItems.infinity_catalyst.get())
                        .define('B', Static.getIngredient("tconstruct", "iron_reinforcement"))
                        .define('C', Static.getIngredient("tconstruct", "knightslime_ingot"))
                        .define('E', Static.getIngredient("tconstruct", "manyullyn_block"))
                        .define('F', Static.getIngredient("tconstruct", "jeweled_apple"))
                        .define('G', Static.getIngredient("tconstruct", "iron_reinforcement"))
                        .define('H', Static.getIngredient("tconstruct", "sky_slime_crystal"))
                        .unlockedBy("has_item", has(Static.getItem("tconstruct", "sky_slime_crystal")))::save
        ).build(consumer, Static.rl("tc3_creative_slot_souls"));

        var creative_slot_upgrades = new CompoundTag();
        creative_slot_upgrades.putString("slot", "upgrades");

        ConditionalRecipe.builder().addCondition(modLoaded("tconstruct")).addRecipe(
                ModShapedRecipeBuilder.shaped(RecipeCategory.TOOLS, ResourceLocation.tryBuild("tconstruct", "creative_slot"), creative_slot_upgrades)
                        .pattern("GGBHHHBGG")
                        .pattern("GCCCCCCCG")
                        .pattern("BCBFFFBCB")
                        .pattern("HCFFEFFCH")
                        .pattern("HCFEAEFCH")
                        .pattern("HCFFEFFCH")
                        .pattern("BCBFFFBCB")
                        .pattern("GCCCCCCCG")
                        .pattern("GGBHHHBGG")

                        .define('A', ModItems.infinity_catalyst.get())
                        .define('B', Static.getIngredient("tconstruct", "iron_reinforcement"))
                        .define('C', Static.getIngredient("tconstruct", "knightslime_ingot"))
                        .define('E', Static.getIngredient("tconstruct", "manyullyn_block"))
                        .define('F', Static.getIngredient("tconstruct", "jeweled_apple"))
                        .define('G', Static.getIngredient("tconstruct", "iron_reinforcement"))
                        .define('H', Static.getIngredient("tconstruct", "ender_slime_crystal"))
                        .unlockedBy("has_item", has(Static.getItem("tconstruct", "ender_slime_crystal")))::save
        ).build(consumer, Static.rl("tc3_creative_slot_upgrades"));

        ConditionalRecipe.builder().addCondition(modLoaded("storagedrawers")).addRecipe(
                ModShapedRecipeBuilder.shaped(RecipeCategory.TOOLS, ResourceLocation.tryBuild("storagedrawers", "creative_vending_upgrade"))
                        .pattern("    S    ")
                        .pattern(" NDDDDDN ")
                        .pattern(" DNDDDND ")
                        .pattern(" DDNDNDD ")
                        .pattern("SDDDEDDDS")
                        .pattern(" DDNDNDD ")
                        .pattern(" DNDDDND ")
                        .pattern(" NDDDDDN ")
                        .pattern("    S    ")

                        .define('N', ModItems.infinity_ingot.get())
                        .define('S', ModItems.infinity_catalyst.get())
                        .define('D', Static.getIngredient("storagedrawers", "oak_full_drawers_1"))
                        .define('E', Static.getIngredient("storagedrawers", "emerald_storage_upgrade"))
                        .unlockedBy("has_item", has(Static.getItem("storagedrawers", "emerald_storage_upgrade")))::save
        ).build(consumer, Static.rl("sd_creative_vending_upgrade"));

        var creative_energy_cube_main = new CompoundTag();
        var energyContainers = new ListTag();
        var stored = new CompoundTag();
        var mekData = new CompoundTag();
        stored.putString("stored", "18446744073709551615.9999");
        stored.putBoolean("Container", false);
        energyContainers.add(stored);
        mekData.put("EnergyContainers", energyContainers);
        creative_energy_cube_main.put("mekData", mekData);

        ConditionalRecipe.builder().addCondition(modLoaded("mekanism")).addRecipe(
                ModShapedRecipeBuilder.shaped(RecipeCategory.TOOLS, ResourceLocation.tryBuild("mekanism", "creative_energy_cube"),
                                creative_energy_cube_main
                        )
                        .pattern("BBBCECBBB")
                        .pattern("BDDDADDDB")
                        .pattern("BDSDADSDB")
                        .pattern("CDDDIDDDC")
                        .pattern("EAAISIAAE")
                        .pattern("CDDDIDDDC")
                        .pattern("BDSDADSDB")
                        .pattern("BDDDADDDB")
                        .pattern("BBBCECBBB")

                        .define('I', ModItems.infinity_ingot.get())
                        .define('S', ModItems.infinity_catalyst.get())
                        .define('A', Static.getIngredient("mekanism", "ultimate_energy_cube"))
                        .define('B', Static.getIngredient("mekanism", "induction_casing"))
                        .define('C', Static.getIngredient("mekanism", "induction_port"))
                        .define('D', Static.getIngredient("mekanism", "ultimate_induction_cell"))
                        .define('E', Static.getIngredient("mekanism", "ultimate_induction_provider"))
                        .unlockedBy("has_item", has(Static.getItem("mekanism", "ultimate_energy_cube")))::save
        ).build(consumer, Static.rl("mek_creative_energy_cube"));

        ConditionalRecipe.builder().addCondition(modLoaded("mekanism")).addRecipe(
                ModShapedRecipeBuilder.shaped(RecipeCategory.TOOLS, ResourceLocation.tryBuild("mekanism", "creative_fluid_tank"))
                        .pattern("         ")
                        .pattern(" SAADAAS ")
                        .pattern(" ABBCBBA ")
                        .pattern(" ABBCBBA ")
                        .pattern(" ACCICCA ")
                        .pattern(" ABBCBBA ")
                        .pattern(" ABBCBBA ")
                        .pattern(" SAADAAS ")
                        .pattern("         ")

                        .define('I', ModItems.infinity_ingot.get())
                        .define('S', ModItems.infinity_catalyst.get())
                        .define('A', Static.getIngredient("mekanism", "ultimate_fluid_tank"))
                        .define('B', Static.getIngredient("mekanism", "dynamic_tank"))
                        .define('C', Static.getIngredient("mekanism", "structural_glass"))
                        .define('D', Static.getIngredient("mekanism", "dynamic_valve"))
                        .unlockedBy("has_item", has(Static.getItem("mekanism", "ultimate_fluid_tank")))::save
        ).build(consumer, Static.rl("mek_creative_fluid_tank"));

        ConditionalRecipe.builder().addCondition(modLoaded("mekanism")).addRecipe(
                ModShapedRecipeBuilder.shaped(RecipeCategory.TOOLS, ResourceLocation.tryBuild("mekanism", "creative_chemical_tank"))
                        .pattern("   B B   ")
                        .pattern(" SAADAAS ")
                        .pattern(" ABBBBBA ")
                        .pattern(" ABBCBBA ")
                        .pattern(" ABCICBA ")
                        .pattern(" ABBCBBA ")
                        .pattern(" ABBBBBA ")
                        .pattern(" SAADAAS ")
                        .pattern("         ")

                        .define('I', ModItems.infinity_ingot.get())
                        .define('S', ModItems.infinity_catalyst.get())
                        .define('A', Static.getIngredient("mekanism", "ultimate_chemical_tank"))
                        .define('B', Static.getIngredient("mekanism", "dynamic_tank"))
                        .define('C', Static.getIngredient("mekanism", "structural_glass"))
                        .define('D', Static.getIngredient("mekanism", "dynamic_valve"))
                        .unlockedBy("has_item", has(Static.getItem("mekanism", "ultimate_chemical_tank")))::save
        ).build(consumer, Static.rl("mek_creative_chemical_tank"));

        ConditionalRecipe.builder()
                .addCondition(modLoaded("mekanism"))
                .addCondition(modLoaded("mekanismgenerators"))
                .addRecipe(
                ModShapedRecipeBuilder.shaped(RecipeCategory.TOOLS, ResourceLocation.tryBuild("mekanism", "creative_bin"))
                        .pattern("AAAAAAAAA")
                        .pattern("AEIIIIIEA")
                        .pattern("AEIIIIIEA")
                        .pattern("AEIIIIIEA")
                        .pattern("AEEESEEEA")
                        .pattern("AEEEEEEEA")
                        .pattern("AEBCDCBEA")
                        .pattern("AEBCDCBEA")
                        .pattern("AAAAAAAAA")

                        .define('I', ModItems.infinity_ingot.get())
                        .define('S', ModItems.infinity_catalyst.get())
                        .define('A', Static.getIngredient("mekanismgenerators", "fusion_reactor_frame"))
                        .define('B', Static.getIngredient("mekanism", "ultimate_energy_cube"))
                        .define('C', Static.getIngredient("mekanism", "ultimate_fluid_tank"))
                        .define('D', Static.getIngredient("mekanism", "ultimate_chemical_tank"))
                        .define('E', Static.getIngredient("mekanism", "ultimate_bin"))
                        .unlockedBy("has_item", has(Static.getItem("mekanism", "ultimate_bin")))::save
        ).build(consumer, Static.rl("mek_creative_bin"));

        ConditionalRecipe.builder().addCondition(modLoaded("enderio")).addRecipe(
                ModShapedRecipeBuilder.shaped(RecipeCategory.TOOLS, ResourceLocation.tryBuild("enderio", "creative_power"))
                        .pattern("INIIIIINI")
                        .pattern("NZEEEEEZN")
                        .pattern("IECWWWCEI")
                        .pattern("IEWZIZWEI")
                        .pattern("IEWIVIWEI")
                        .pattern("IEWZIZWEI")
                        .pattern("IECWWWCEI")
                        .pattern("NZEEEEEZN")
                        .pattern("INIIIIINI")

                        .define('I', ModItems.infinity_ingot.get())
                        .define('N', ModItems.neutron_ingot.get())
                        .define('W', Static.getIngredient("enderio", "weather_crystal"))
                        .define('V', Static.getIngredient("enderio", "vibrant_capacitor_bank"))
                        .define('Z', Static.getIngredient("enderio", "frank_n_zombie"))
                        .define('E', Static.getIngredient("enderio", "sentient_ender"))
                        .define('C', Static.getIngredient("enderio", "ender_crystal"))
                        .unlockedBy("has_item", has(ModItems.infinity_ingot.get()))::save
        ).build(consumer, Static.rl("eio_creative_power"));

    }

    protected static InventoryChangeTrigger.TriggerInstance has(@NotNull TagKey<Item> tagKey) {
        return inventoryTrigger(ItemPredicate.Builder.item().of(tagKey).build());
    }
}
