package committee.nova.mods.avaritia.init.data.recipe;

import committee.nova.mods.avaritia.init.registry.ModBlocks;
import committee.nova.mods.avaritia.init.registry.ModItems;
import net.minecraft.advancements.critereon.InventoryChangeTrigger;
import net.minecraft.advancements.critereon.ItemPredicate;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.*;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
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
    public ModRecipes(PackOutput output) {
        super(output);
    }

    @Override
    protected void buildRecipes(@NotNull Consumer<FinishedRecipe> consumer) {
        InventoryChangeTrigger.TriggerInstance lul = has(Items.AIR);
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModBlocks.compressed_crafting_table.get())
                .pattern("xxx")
                .pattern("xxx")
                .pattern("xxx")
                .define('x', Items.CRAFTING_TABLE)
                .showNotification(false)
                .unlockedBy("", lul).save(consumer);
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModBlocks.crystal_matrix.get())
                .pattern("xxx")
                .pattern("xxx")
                .pattern("xxx")
                .define('x', ModItems.crystal_matrix_ingot.get())
                .showNotification(false)
                .unlockedBy("has_item", has(ModItems.crystal_matrix_ingot.get())).save(consumer);
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModItems.crystal_matrix_ingot.get())
                .pattern("xyx")
                .pattern("xyx")
                .define('x', ModItems.diamond_lattice.get())
                .define('y', Items.NETHER_STAR)
                .showNotification(false)
                .unlockedBy("has_item", has(ModItems.diamond_lattice.get())).save(consumer);
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModItems.diamond_lattice.get())
                .pattern("x x")
                .pattern(" y ")
                .pattern("x x")
                .define('x', Items.DIAMOND)
                .define('y', Items.NETHERITE_SCRAP)
                .showNotification(false)
                .unlockedBy("", lul).save(consumer);
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModBlocks.double_compressed_crafting_table.get())
                .pattern("xxx")
                .pattern("xxx")
                .pattern("xxx")
                .define('x', ModBlocks.compressed_crafting_table.get())
                .showNotification(false)
                .unlockedBy("has_item", has(ModBlocks.compressed_crafting_table.get())).save(consumer);
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModBlocks.extreme_crafting_table.get())
                .pattern("xxx")
                .pattern("xyx")
                .pattern("xxx")
                .define('x', ModItems.crystal_matrix_ingot.get())
                .define('y', ModBlocks.double_compressed_crafting_table.get())
                .showNotification(false)
                .unlockedBy("has_item", has(ModBlocks.double_compressed_crafting_table.get())).save(consumer);

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModBlocks.infinity.get())
                .pattern("xxx")
                .pattern("xxx")
                .pattern("xxx")
                .define('x', ModItems.infinity_ingot.get())
                .showNotification(false)
                .unlockedBy("has_item", has(ModItems.infinity_ingot.get())).save(consumer);
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModBlocks.neutronium.get())
                .pattern("xxx")
                .pattern("xxx")
                .pattern("xxx")
                .define('x', ModItems.neutron_ingot.get())
                .showNotification(false)
                .unlockedBy("has_item", has(ModItems.neutron_ingot.get())).save(consumer);
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModItems.neutron_ingot.get())
                .pattern("xxx")
                .pattern("xxx")
                .pattern("xxx")
                .define('x', ModItems.neutron_nugget.get())
                .showNotification(false)
                .unlockedBy("has_item", has(ModItems.neutron_nugget.get())).save(consumer);
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModItems.neutron_nugget.get())
                .pattern("xxx")
                .pattern("xxx")
                .pattern("xxx")
                .define('x', ModItems.neutron_pile.get())
                .showNotification(false)
                .unlockedBy("has_item", has(ModItems.neutron_pile.get())).save(consumer);

        ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, ModItems.record_fragment.get(), 4)
                .requires(ItemTags.MUSIC_DISCS)
                .unlockedBy("has_item", has(ItemTags.MUSIC_DISCS)).save(consumer);



        ModShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, ModItems.cosmic_meatballs.get())
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

        ModShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, ModItems.ultimate_stew.get())
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

        ModShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModItems.endest_pearl.get())
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
                .showNotification(false)
                .unlockedBy("has_item", has(ModItems.neutron_ingot.get())).save(consumer);


    }

    protected static InventoryChangeTrigger.TriggerInstance has(@NotNull TagKey<Item> tagKey) {
        return inventoryTrigger(ItemPredicate.Builder.item().of(tagKey).build());
    }
}
