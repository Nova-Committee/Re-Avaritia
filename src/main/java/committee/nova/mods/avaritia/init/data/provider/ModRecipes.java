package committee.nova.mods.avaritia.init.data.provider;

import appeng.core.definitions.AEBlocks;
import com.enderio.machines.common.init.MachineBlocks;
import com.refinedmods.refinedstorage.common.storage.FluidStorageVariant;
import com.refinedmods.refinedstorage.common.storage.ItemStorageVariant;
import committee.nova.mods.avaritia.Static;
import committee.nova.mods.avaritia.init.data.provider.recipe.*;
import committee.nova.mods.avaritia.init.registry.ModBlocks;
import committee.nova.mods.avaritia.init.registry.ModItems;
import committee.nova.mods.avaritia.init.registry.ModSingularities;
import committee.nova.mods.avaritia.init.registry.ModTags;
import committee.nova.mods.avaritia.util.SingularityUtils;
import mekanism.common.registries.MekanismBlocks;
import net.minecraft.advancements.Criterion;
import net.minecraft.advancements.critereon.InventoryChangeTrigger;
import net.minecraft.advancements.critereon.ItemPredicate;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.data.recipes.RecipeProvider;
import net.minecraft.data.recipes.ShapedRecipeBuilder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.neoforge.common.Tags;
import net.neoforged.neoforge.common.conditions.IConditionBuilder;
import net.neoforged.neoforge.common.crafting.CompoundIngredient;
import net.neoforged.neoforge.common.crafting.DataComponentIngredient;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.CompletableFuture;

/**
 * Name: Avaritia-forge / ModRecipes
 * Author: cnlimiter
 * CreateTime: 2023/8/24 13:48
 * Description:
 */

public class ModRecipes extends RecipeProvider implements IConditionBuilder {
    public ModRecipes(PackOutput output, CompletableFuture<HolderLookup.Provider> future) {
        super(output, future);
    }

    protected static Criterion<InventoryChangeTrigger.TriggerInstance> has(@NotNull TagKey<Item> tagKey) {
        return inventoryTrigger(ItemPredicate.Builder.item().of(tagKey).build());
    }

    protected static String getModItemName(ItemLike pItemLike) {
        return BuiltInRegistries.ITEM.getKey(pItemLike.asItem()).getPath();
    }

    @Override
    protected void buildRecipes(@NotNull RecipeOutput consumer) {
        var lul = has(Items.AIR);

        nineBlockStorageRecipesRecipesWithCustomUnpacking(consumer, RecipeCategory.MISC, ModItems.neutron_ingot.get(), RecipeCategory.BUILDING_BLOCKS, ModBlocks.neutron.get(), "neutron_ingot_from_neutron_block", "neutron_ingot");
        nineBlockStorageRecipesWithCustomPacking(consumer, RecipeCategory.MISC, ModItems.neutron_nugget.get(), RecipeCategory.MISC, ModItems.neutron_ingot.get(), "neutron_ingot_from_nuggets", "neutron_ingot");
        nineBlockStorageRecipesWithCustomPacking(consumer, RecipeCategory.MISC, ModItems.neutron_pile.get(), RecipeCategory.MISC, ModItems.neutron_nugget.get(), "neutron_pile_from_ingots", "neutron_pile");
        nineBlockStorageRecipesWithCustomPacking(consumer, RecipeCategory.MISC, ModItems.infinity_ingot.get(), RecipeCategory.BUILDING_BLOCKS, ModBlocks.infinity.get(), "infinity_block_from_infinity_ingot", "infinity_block");
        nineBlockStorageRecipesWithCustomPacking(consumer, RecipeCategory.MISC, ModItems.infinity_nugget.get(), RecipeCategory.MISC, ModItems.infinity_ingot.get(), "infinity_ingot_from_infinity_nugget", "infinity_ingot");
        nineBlockStorageRecipes(consumer, RecipeCategory.MISC, ModItems.crystal_matrix_ingot.get(), RecipeCategory.BUILDING_BLOCKS, ModBlocks.crystal_matrix.get());
        nineBlockStorageRecipes(consumer, RecipeCategory.MISC, ModItems.blaze_cube.get(), RecipeCategory.BUILDING_BLOCKS, ModBlocks.blaze_cube_block.get());
        nineBlockStorageRecipes(consumer, RecipeCategory.MISC, ModItems.diamond_lattice.get(), RecipeCategory.BUILDING_BLOCKS, ModBlocks.diamond_lattice_block.get());
        nineBlockStorageRecipes(consumer, RecipeCategory.MISC, ModItems.star_fuel.get(), RecipeCategory.BUILDING_BLOCKS, ModBlocks.star_fuel_block.get());
        nineBlockStorageRecipes(consumer, RecipeCategory.MISC, ModItems.refined_coal.get(), RecipeCategory.BUILDING_BLOCKS, ModBlocks.refined_coal_block.get());
        nineBlockStorageRecipesRecipesWithCustomUnpacking(consumer, RecipeCategory.BUILDING_BLOCKS, ModBlocks.compressed_crafting_table.get(), RecipeCategory.BUILDING_BLOCKS, ModBlocks.double_compressed_crafting_table.get(),
                "compressed_crafting_table_from_double_compressed_crafting_table", "compressed_crafting_table");
        nineBlockStorageRecipesRecipesWithCustomUnpacking(consumer, RecipeCategory.MISC, Blocks.CRAFTING_TABLE, RecipeCategory.BUILDING_BLOCKS, ModBlocks.compressed_crafting_table.get(),
                "crafting_table_from_compressed_crafting_table", "crafting_table");

        ModShapedRecipeBuilder.shaped(RecipeCategory.MISC, Blocks.REINFORCED_DEEPSLATE, 1)
                .pattern("ada")
                .pattern("cbc")
                .pattern("aba")
                .define('a', Blocks.OBSIDIAN)
                .define('b', Blocks.DEEPSLATE)
                .define('c', Blocks.DRIPSTONE_BLOCK)
                .define('d', Blocks.SCULK_CATALYST)
                .unlockedBy("", lul).save(consumer);

        ModShapedRecipeBuilder.shaped(RecipeCategory.MISC, Blocks.CRYING_OBSIDIAN, 1)
                .pattern("bab")
                .pattern("aba")
                .pattern("bab")
                .define('a', Blocks.AMETHYST_BLOCK)
                .define('b', Blocks.OBSIDIAN)
                .unlockedBy("", lul).save(consumer);

        ModShapedRecipeBuilder.shaped(RecipeCategory.MISC, Blocks.BUDDING_AMETHYST, 1)
                .pattern("cac")
                .pattern("aba")
                .pattern("cac")
                .define('a', Blocks.SOUL_SOIL)
                .define('b', Blocks.AMETHYST_BLOCK)
                .define('c', Items.AMETHYST_CLUSTER)
                .unlockedBy("", lul).save(consumer);

        ModShapedRecipeBuilder.shaped(RecipeCategory.MISC, Blocks.DEEPSLATE_GOLD_ORE, 1)
                .pattern(" a ")
                .pattern("aba")
                .pattern(" a ")
                .define('a', Items.GOLD_NUGGET)
                .define('b', Blocks.BLACKSTONE)
                .unlockedBy("", lul).save(consumer);

        ModShapedRecipeBuilder.shaped(RecipeCategory.MISC, Blocks.COBWEB, 1)
                .pattern("a a")
                .pattern(" a ")
                .pattern("a a")
                .define('a', Items.STRING)
                .unlockedBy("", lul).save(consumer);

        ModShapedRecipeBuilder.shaped(RecipeCategory.MISC, Blocks.ANCIENT_DEBRIS, 1)
                .pattern(" a ")
                .pattern("aba")
                .pattern(" a ")
                .define('a', Items.NETHERITE_SCRAP)
                .define('b', Blocks.CRYING_OBSIDIAN)
                .unlockedBy("", lul).save(consumer);

        ModShapedRecipeBuilder.shaped(RecipeCategory.MISC, Items.DRAGON_BREATH, 1)
                .pattern(" a ")
                .pattern("a a")
                .pattern(" a ")
                .define('a', Items.END_CRYSTAL)
                .unlockedBy("", lul).save(consumer);

        ModShapedRecipeBuilder.shaped(RecipeCategory.MISC, Items.SPORE_BLOSSOM, 1)
                .pattern("aba")
                .pattern("bcb")
                .pattern("aba")
                .define('a', Items.BONE_MEAL)
                .define('b', Items.PINK_PETALS)
                .define('c', Items.TORCHFLOWER_SEEDS)
                .unlockedBy("", lul).save(consumer);

        ModShapedRecipeBuilder.shaped(RecipeCategory.MISC, Items.DRAGON_HEAD, 1)
                .pattern(" a ")
                .pattern("bcb")
                .pattern(" b ")
                .define('a', Items.WITHER_SKELETON_SKULL)
                .define('b', Items.END_CRYSTAL)
                .define('c', Items.DRAGON_EGG)
                .unlockedBy("", lul).save(consumer);

        ModShapedRecipeBuilder.shaped(RecipeCategory.MISC, Items.TRIDENT, 1)
                .pattern(" ba")
                .pattern(" cb")
                .pattern("d  ")
                .define('a', Items.NAUTILUS_SHELL)
                .define('b', Items.PRISMARINE_CRYSTALS)
                .define('c', Items.HEART_OF_THE_SEA)
                .define('d', Items.PRISMARINE_SHARD)
                .unlockedBy("", lul).save(consumer);

        ModShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModItems.diamond_lattice.get(), 1)
                .pattern("x x")
                .pattern(" y ")
                .pattern("x x")
                .define('x', Items.DIAMOND)
                .define('y', Items.NETHERITE_SCRAP)
                .unlockedBy("", lul).save(consumer);

        ModShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModItems.blaze_cube.get(), 2)
                .pattern(" bcb ")
                .pattern("byxyb")
                .pattern("cxaxc")
                .pattern("byxyb")
                .pattern(" bcb ")
                .define('x', Items.BLAZE_ROD)
                .define('y', Items.BONE)
                .define('a', Blocks.ANCIENT_DEBRIS)
                .define('b', Items.BLAZE_POWDER)
                .define('c', Items.FIRE_CHARGE)
                .unlockedBy("has_item", has(Items.BLAZE_ROD)).save(consumer);

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModItems.crystal_matrix_ingot.get())
                .pattern("xyx")
                .pattern("xyx")
                .define('x', ModItems.diamond_lattice.get())
                .define('y', Items.NETHER_STAR)

                .unlockedBy("has_item", has(ModItems.diamond_lattice.get())).save(consumer);

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModBlocks.sculk_crafting_table.get())
                .pattern("aba")
                .pattern("cxc")
                .pattern("ada")
                .define('a', Items.ECHO_SHARD)
                .define('b', Blocks.SCULK_SHRIEKER)
                .define('c', Blocks.SCULK)
                .define('d', Blocks.SCULK_CATALYST)
                .define('x', ModBlocks.double_compressed_crafting_table.get())
                .unlockedBy("has_block", has(ModBlocks.double_compressed_crafting_table.get())).save(consumer);

        ModShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModBlocks.nether_crafting_table.get(), 1)
                .pattern("cbc")
                .pattern("dad")
                .pattern("efe")
                .define('a', ModBlocks.sculk_crafting_table.get())
                .define('b', Blocks.RESPAWN_ANCHOR)
                .define('c', Blocks.WITHER_SKELETON_SKULL)
                .define('d', Blocks.NETHERRACK)
                .define('e', Items.NETHERITE_INGOT)
                .define('f', Items.NETHER_STAR)
                .unlockedBy("has_block", has(ModBlocks.sculk_crafting_table.get())).save(consumer);

        ModShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModBlocks.end_crafting_table.get(), 2)
                .pattern("bcccb")
                .pattern("dfifd")
                .pattern("dgagd")
                .pattern("dhjhd")
                .pattern("beeeb")
                .define('a', ModBlocks.nether_crafting_table.get())
                .define('b', Items.END_CRYSTAL)
                .define('c', Items.END_PORTAL_FRAME)
                .define('d', Items.OBSIDIAN)
                .define('e', Items.DRAGON_BREATH)
                .define('f', Items.PURPUR_PILLAR)
                .define('g', Items.END_STONE_BRICKS)
                .define('h', Items.END_STONE)
                .define('i', Items.ENDER_EYE)
                .define('j', Items.ENDER_CHEST)
                .unlockedBy("has_block", has(ModBlocks.nether_crafting_table.get())).save(consumer);

        ModShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModBlocks.extreme_crafting_table.get(), 3)
                .pattern("bccfccb")
                .pattern("cddgddc")
                .pattern("cdihidc")
                .pattern("cdiaidc")
                .pattern("cdjkjdc")
                .pattern("cdddddc")
                .pattern("beeeeeb")
                .define('a', ModBlocks.end_crafting_table.get())
                .define('b', Blocks.LODESTONE)
                .define('c', ModItems.diamond_lattice.get())
                .define('d', ModItems.crystal_matrix_ingot.get())
                .define('e', ModBlocks.crystal_matrix.get())
                .define('f', Items.RECOVERY_COMPASS)
                .define('g', Items.DRAGON_EGG)
                .define('h', Items.BEACON)
                .define('i', Items.REINFORCED_DEEPSLATE)
                .define('j', Blocks.NETHERITE_BLOCK)
                .define('k', Items.HEART_OF_THE_SEA)
                .unlockedBy("has_block", has(ModBlocks.end_crafting_table.get())).save(consumer);

        ModShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModItems.neutron_ring.get(), 3)
                .pattern("  aaa  ")
                .pattern(" cbbbc ")
                .pattern("ab d ba")
                .pattern("abdedba")
                .pattern("ab d ba")
                .pattern(" cbbbc ")
                .pattern("  aaa  ")
                .define('a', ModItems.neutron_ingot.get())
                .define('b', ModItems.crystal_matrix_ingot.get())
                .define('c', ModBlocks.crystal_matrix.get())
                .define('d', ModItems.endest_pearl.get())
                .define('e', ModItems.infinity_catalyst.get())
                .unlockedBy("has_block", has(ModBlocks.end_crafting_table.get())).save(consumer);

        ModShapedRecipeBuilder.shaped(RecipeCategory.MISC, Blocks.END_PORTAL_FRAME, 2)
                .pattern("     ")
                .pattern("fghgf")
                .pattern("ecace")
                .pattern("dcccd")
                .pattern("bbbbb")
                .define('a', Items.END_CRYSTAL)
                .define('b', Blocks.END_STONE_BRICKS)
                .define('c', Blocks.END_STONE)
                .define('d', Blocks.END_STONE_BRICK_WALL)
                .define('e', Blocks.EMERALD_BLOCK)
                .define('f', ModItems.crystal_matrix_ingot.get())
                .define('g', Items.ENDER_EYE)
                .define('h', Items.SCULK_SHRIEKER)
                .unlockedBy("has_item", has(ModItems.crystal_matrix_ingot.get())).save(consumer);

        ModShapedRecipeBuilder.shaped(RecipeCategory.MISC, Items.DRAGON_EGG, 3)
                .pattern("  ggg  ")
                .pattern(" gfefg ")
                .pattern("gfbdbfg")
                .pattern("gecaceg")
                .pattern("gfbcbfg")
                .pattern("ggfefgg")
                .pattern(" ggggg ")
                .define('a', Items.EGG)
                .define('b', Items.DRAGON_BREATH)
                .define('c', Items.EXPERIENCE_BOTTLE)
                .define('d', Items.DRAGON_HEAD)
                .define('e', Blocks.ENDER_CHEST)
                .define('f', Items.END_CRYSTAL)
                .define('g', ModItems.neutron_pile.get())
                .unlockedBy("has_item", has(ModItems.neutron_pile.get())).save(consumer);

        ModShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModItems.record_fragment.get(), 3)
                .pattern("       ")
                .pattern("   a   ")
                .pattern("  aaa  ")
                .pattern(" aabaa ")
                .pattern("  aaa  ")
                .pattern("   a   ")
                .pattern("       ")
                .define('b', Tags.Items.MUSIC_DISCS)
                .define('a', ModItems.neutron_pile.get())
                .unlockedBy("has_item", has(Tags.Items.MUSIC_DISCS)).save(consumer);

        ModShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModItems.refined_coal.get(), 2)
                .pattern("     ")
                .pattern(" aaa ")
                .pattern(" aba ")
                .pattern(" aaa ")
                .pattern("     ")
                .define('a', Items.COAL)
                .define('b', ModItems.neutron_nugget.get())
                .unlockedBy("has_block", has(ModItems.neutron_pile.get())).save(consumer);

        ModShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModItems.star_fuel.get(), 4)
                .pattern("         ")
                .pattern("  aaaaa  ")
                .pattern(" abbcbba ")
                .pattern(" abeeeba ")
                .pattern(" acedeca ")
                .pattern(" abeeeba ")
                .pattern(" abbcbba ")
                .pattern("  aaaaa  ")
                .pattern("         ")
                .define('a', Blocks.COAL_BLOCK)
                .define('b', Blocks.MAGMA_BLOCK)
                .define('c', Items.LAVA_BUCKET)
                .define('d', ModItems.eternal_singularity.get())
                .define('e', ModBlocks.refined_coal_block.get())
                .unlockedBy("has_item", has(ModItems.eternal_singularity.get())).save(consumer);

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModItems.neutron_gear.get())
                .pattern(" n ")
                .pattern("ncn")
                .pattern(" n ")
                .define('n', ModItems.neutron_ingot.get())
                .define('c', ModItems.crystal_matrix_ingot.get())
                .unlockedBy("has_item", has(ModItems.neutron_ingot.get())).save(consumer);


        ShapedRecipeBuilder.shaped(RecipeCategory.TOOLS, ModBlocks.compressed_chest.get())
                .pattern("ccc")
                .pattern("cgc")
                .pattern("ccc")
                .define('c', Blocks.CHEST)
                .define('g', ModItems.neutron_gear.get())
                .unlockedBy("has_item", has(Blocks.CHEST)).save(consumer);

        ModExtremeSmithingRecipeBuilder.smithing(
                Ingredient.of(ModItems.upgrade_smithing_template.get()),
                Ingredient.of(Items.TOTEM_OF_UNDYING),
                CompoundIngredient.of(Ingredient.of(Items.EXPERIENCE_BOTTLE), Ingredient.of(Items.BEACON), Ingredient.of(ModItems.enhancement_core.get())),
                RecipeCategory.MISC,
                ModItems.infinity_totem.get().getDefaultInstance())
                .unlockedBy("has_item", has(ModItems.upgrade_smithing_template.get()))
                .save(consumer);

        ModExtremeSmithingRecipeBuilder.smithing(
                        Ingredient.of(ModItems.upgrade_smithing_template.get()),
                        Ingredient.of(Items.CAKE),
                        CompoundIngredient.of(Ingredient.of(Items.GOLDEN_CARROT), Ingredient.of(Items.DRAGON_EGG), Ingredient.of(ModItems.enhancement_core.get())),
                        RecipeCategory.MISC,
                        ModBlocks.endless_cake.get().asItem().getDefaultInstance())
                .unlockedBy("has_item", has(ModItems.upgrade_smithing_template.get()))
                .save(consumer);

        ModExtremeSmithingRecipeBuilder.smithing(
                        Ingredient.of(ModItems.upgrade_smithing_template.get()),
                        Ingredient.of(Items.DIAMOND_HORSE_ARMOR),
                        CompoundIngredient.of(DataComponentIngredient.of(false, DataComponents.POTION_CONTENTS, new PotionContents(Potions.SWIFTNESS), Items.POTION),
                                Ingredient.of(Items.BLUE_ICE), Ingredient.of(ModItems.enhancement_core.get())),
                        RecipeCategory.MISC,
                        ModItems.neutron_horse_armor.get().getDefaultInstance())
                .unlockedBy("has_item", has(ModItems.upgrade_smithing_template.get()))
                .save(consumer);

        ModExtremeSmithingRecipeBuilder.smithing(
                        Ingredient.of(ModItems.upgrade_smithing_template.get()),
                        Ingredient.of(Items.BUCKET),
                        CompoundIngredient.of(Ingredient.of(Items.LAVA_BUCKET), Ingredient.of(Items.POWDER_SNOW_BUCKET), Ingredient.of(ModItems.enhancement_core.get())),
                        RecipeCategory.MISC,
                        ModItems.infinity_bucket.get().getDefaultInstance())
                .unlockedBy("has_item", has(ModItems.upgrade_smithing_template.get()))
                .save(consumer);

        ModExtremeSmithingRecipeBuilder.smithing(
                        Ingredient.of(ModItems.upgrade_smithing_template.get()),
                        Ingredient.of(Items.ANVIL),
                        CompoundIngredient.of(Ingredient.of(ModItems.matter_cluster.get()), Ingredient.of(ModBlocks.neutron.get()), Ingredient.of(ModItems.enhancement_core.get())),
                        RecipeCategory.MISC,
                        ModBlocks.extreme_anvil.get().asItem().getDefaultInstance())
                .unlockedBy("has_item", has(ModItems.upgrade_smithing_template.get()))
                .save(consumer);

        ModExtremeSmithingRecipeBuilder.smithing(
                        Ingredient.of(ModItems.upgrade_smithing_template.get()),
                        Ingredient.of(Items.ELYTRA),
                        CompoundIngredient.of(Ingredient.of(ModBlocks.crystal_matrix.get()), Ingredient.of(ModBlocks.neutron.get()), Ingredient.of(ModItems.enhancement_core.get())),
                        RecipeCategory.MISC,
                        ModItems.infinity_elytra.get().getDefaultInstance())
                .unlockedBy("has_item", has(ModItems.upgrade_smithing_template.get()))
                .save(consumer);

        ModExtremeSmithingRecipeBuilder.smithing(
                        Ingredient.of(ModItems.upgrade_smithing_template.get()),
                        Ingredient.of(ModBlocks.compressed_chest.get()),
                        CompoundIngredient.of(Ingredient.of(ModBlocks.neutron.get()), Ingredient.of(ModBlocks.infinity.get()), Ingredient.of(ModItems.enhancement_core.get())),
                        RecipeCategory.MISC,
                        ModBlocks.infinity_chest.get().asItem().getDefaultInstance())
                .unlockedBy("has_item", has(ModItems.upgrade_smithing_template.get()))
                .save(consumer);

        ModShapedRecipeBuilder.shaped(RecipeCategory.TOOLS, ModItems.upgrade_smithing_template.get())
                .pattern(" aaabaaa ")
                .pattern(" accecca ")
                .pattern(" acefeca ")
                .pattern("dijkelmnd")
                .pattern("dogphqgrd")
                .pattern("dstufvwxd")
                .pattern(" acdfdca ")
                .pattern(" accecca ")
                .pattern(" aaabaaa ")
                .define('a', ModItems.crystal_matrix_ingot.get())
                .define('b', ModBlocks.crystal_matrix.get())
                .define('c', ModItems.neutron_ingot.get())
                .define('d', ModItems.neutron_pile.get())
                .define('e', ModItems.infinity_nugget.get())
                .define('f', ModItems.infinity_ingot.get())
                .define('g', ModItems.infinity_catalyst.get())
                .define('h', Items.NETHERITE_UPGRADE_SMITHING_TEMPLATE)
                .define('i', Items.SENTRY_ARMOR_TRIM_SMITHING_TEMPLATE)
                .define('j', Items.DUNE_ARMOR_TRIM_SMITHING_TEMPLATE)
                .define('k', Items.COAST_ARMOR_TRIM_SMITHING_TEMPLATE)
                .define('l', Items.WILD_ARMOR_TRIM_SMITHING_TEMPLATE)
                .define('m', Items.WARD_ARMOR_TRIM_SMITHING_TEMPLATE)
                .define('n', Items.EYE_ARMOR_TRIM_SMITHING_TEMPLATE)
                .define('o', Items.VEX_ARMOR_TRIM_SMITHING_TEMPLATE)
                .define('p', Items.TIDE_ARMOR_TRIM_SMITHING_TEMPLATE)
                .define('q', Items.SNOUT_ARMOR_TRIM_SMITHING_TEMPLATE)
                .define('r', Items.RIB_ARMOR_TRIM_SMITHING_TEMPLATE)
                .define('s', Items.SPIRE_ARMOR_TRIM_SMITHING_TEMPLATE)
                .define('t', Items.WAYFINDER_ARMOR_TRIM_SMITHING_TEMPLATE)
                .define('u', Items.SHAPER_ARMOR_TRIM_SMITHING_TEMPLATE)
                .define('v', Items.SILENCE_ARMOR_TRIM_SMITHING_TEMPLATE)
                .define('w', Items.RAISER_ARMOR_TRIM_SMITHING_TEMPLATE)
                .define('x', Items.HOST_ARMOR_TRIM_SMITHING_TEMPLATE)

                .unlockedBy("has_item", has(ModItems.neutron_ingot.get())).save(consumer);

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

        ModShapedRecipeBuilder.shaped(RecipeCategory.TOOLS, ModBlocks.extreme_smithing_table.get())
                .pattern("aaaaaaaaa")
                .pattern("bccfgfccb")
                .pattern("bcdhhhdcb")
                .pattern("lfhijihfl")
                .pattern("eghjkjhge")
                .pattern("lfhijihfl")
                .pattern("bcdhhhdcb")
                .pattern("bccfgfccb")
                .pattern("bleeeeelb")
                .define('a', ModBlocks.neutron.get())
                .define('b', ModItems.neutron_ingot.get())
                .define('c', ModItems.diamond_lattice.get())
                .define('d', ModItems.blaze_cube.get())
                .define('e', ModBlocks.crystal_matrix.get())
                .define('f', ModItems.infinity_nugget.get())
                .define('g', ModItems.infinity_ingot.get())
                .define('h', ModItems.neutron_gear.get())
                .define('i', ModItems.infinity_catalyst.get())
                .define('j', Blocks.SMITHING_TABLE)
                .define('k', ModBlocks.extreme_crafting_table.get())
                .define('l', ModItems.crystal_matrix_ingot.get())
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
                .unlockedBy("has_item", has(ModItems.neutron_ingot.get())).save(consumer);

        ModShapedRecipeBuilder.shaped(RecipeCategory.TOOLS, ModItems.enhancement_core.get())
                .pattern("   PPP   ")
                .pattern(" NPCCCPN ")
                .pattern(" PABBBAP ")
                .pattern("PCBBXBBCP")
                .pattern("PCBXEXBCP")
                .pattern("PCBBXBBCP")
                .pattern(" PABBBAP ")
                .pattern(" NPCCCPN ")
                .pattern("   PPP   ")
                .define('E', ModItems.endest_pearl.get())
                .define('B', ModItems.infinity_nugget.get())
                .define('C', ModItems.crystal_matrix_ingot.get())
                .define('X', ModItems.infinity_catalyst.get())
                .define('N', ModItems.neutron_ingot.get())
                .define('P', ModItems.neutron_pile.get())
                .define('A', ModBlocks.crystal_matrix.get())
                .unlockedBy("has_item", has(ModItems.endest_pearl.get())).save(consumer);


        ModShapedRecipeBuilder.shaped(RecipeCategory.TOOLS, ModItems.blaze_sword.get(), 2)
                .pattern("   DC")
                .pattern("A DCD")
                .pattern("ADCD ")
                .pattern(" ED  ")
                .pattern("B AA ")
                .define('A', Blocks.BONE_BLOCK)
                .define('B', ModItems.diamond_lattice.get())
                .define('C', ModItems.blaze_cube.get())
                .define('D', Items.BLAZE_POWDER)
                .define('E', Blocks.SOUL_SOIL)
                .unlockedBy("has_item", has(ModItems.blaze_cube.get())).save(consumer);

        ModShapedRecipeBuilder.shaped(RecipeCategory.TOOLS, ModItems.blaze_axe.get(), 2)
                .pattern("  DDA")
                .pattern(" DCA ")
                .pattern(" DACD")
                .pattern(" E DD")
                .pattern("B    ")
                .define('A', Blocks.BONE_BLOCK)
                .define('B', ModItems.diamond_lattice.get())
                .define('C', ModItems.blaze_cube.get())
                .define('D', Items.BLAZE_POWDER)
                .define('E', Blocks.SOUL_SOIL)
                .unlockedBy("has_item", has(ModItems.blaze_cube.get())).save(consumer);

        ModShapedRecipeBuilder.shaped(RecipeCategory.TOOLS, ModItems.blaze_pickaxe.get(), 2)
                .pattern("DCCCA")
                .pattern(" DDAC")
                .pattern("  ADC")
                .pattern(" E DC")
                .pattern("B   D")
                .define('A', Blocks.BONE_BLOCK)
                .define('B', ModItems.diamond_lattice.get())
                .define('C', ModItems.blaze_cube.get())
                .define('D', Items.BLAZE_POWDER)
                .define('E', Blocks.SOUL_SOIL)
                .unlockedBy("has_item", has(ModItems.blaze_cube.get())).save(consumer);

        ModShapedRecipeBuilder.shaped(RecipeCategory.TOOLS, ModItems.blaze_hoe.get(), 2)
                .pattern("DDCCA")
                .pattern(" DDAC")
                .pattern("  A D")
                .pattern(" E   ")
                .pattern("B    ")
                .define('A', Blocks.BONE_BLOCK)
                .define('B', ModItems.diamond_lattice.get())
                .define('C', ModItems.blaze_cube.get())
                .define('D', Items.BLAZE_POWDER)
                .define('E', Blocks.SOUL_SOIL)
                .unlockedBy("has_item", has(ModItems.blaze_cube.get())).save(consumer);

        ModShapedRecipeBuilder.shaped(RecipeCategory.TOOLS, ModItems.blaze_shovel.get(), 2)
                .pattern("   DC")
                .pattern("  DCD")
                .pattern("  AD ")
                .pattern(" E   ")
                .pattern("B    ")
                .define('A', Blocks.BONE_BLOCK)
                .define('B', ModItems.diamond_lattice.get())
                .define('C', ModItems.blaze_cube.get())
                .define('D', Items.BLAZE_POWDER)
                .define('E', Blocks.SOUL_SOIL)
                .unlockedBy("has_item", has(ModItems.blaze_cube.get())).save(consumer);

        ModShapedRecipeBuilder.shaped(RecipeCategory.TOOLS, ModItems.crystal_sword.get(), 3)
                .pattern("     CA")
                .pattern("    CAC")
                .pattern(" C CAC ")
                .pattern(" CCAC  ")
                .pattern("CAAC   ")
                .pattern(" BACC  ")
                .pattern("A C    ")
                .define('A', ModBlocks.crystal_matrix.get())
                .define('B', ModBlocks.neutron.get())
                .define('C', ModItems.crystal_matrix_ingot.get())
                .unlockedBy("has_item", has(ModItems.crystal_matrix_ingot.get())).save(consumer);

        ModShapedRecipeBuilder.shaped(RecipeCategory.TOOLS, ModItems.crystal_hoe.get(), 3)
                .pattern("CAAAAA ")
                .pattern(" CCCA A")
                .pattern("     AA")
                .pattern("   B CA")
                .pattern("  B   C")
                .pattern(" B     ")
                .pattern("A      ")
                .define('A', ModBlocks.crystal_matrix.get())
                .define('B', ModItems.neutron_ingot.get())
                .define('C', ModItems.crystal_matrix_ingot.get())
                .unlockedBy("has_item", has(ModItems.crystal_matrix_ingot.get())).save(consumer);

        ModShapedRecipeBuilder.shaped(RecipeCategory.TOOLS, ModItems.crystal_pickaxe.get(), 3)
                .pattern("CAAAA D")
                .pattern(" CCCC  ")
                .pattern("    CCA")
                .pattern("   B CA")
                .pattern("  B  CA")
                .pattern(" B   CA")
                .pattern("A     C")
                .define('A', ModBlocks.crystal_matrix.get())
                .define('B', ModItems.neutron_ingot.get())
                .define('C', ModItems.crystal_matrix_ingot.get())
                .define('D', ModBlocks.neutron.get())
                .unlockedBy("has_item", has(ModItems.crystal_matrix_ingot.get())).save(consumer);

        ModShapedRecipeBuilder.shaped(RecipeCategory.TOOLS, ModItems.crystal_axe.get(), 3)
                .pattern("  CCC  ")
                .pattern(" CAA D ")
                .pattern(" CAA   ")
                .pattern(" C  AC ")
                .pattern("  B CC ")
                .pattern(" B     ")
                .pattern("A      ")
                .define('A', ModBlocks.crystal_matrix.get())
                .define('B', ModItems.neutron_ingot.get())
                .define('C', ModItems.crystal_matrix_ingot.get())
                .define('D', ModBlocks.neutron.get())
                .unlockedBy("has_item", has(ModItems.crystal_matrix_ingot.get())).save(consumer);

        ModShapedRecipeBuilder.shaped(RecipeCategory.TOOLS, ModItems.crystal_shovel.get(), 3)
                .pattern("    CCC")
                .pattern("   CCCC")
                .pattern("    CCC")
                .pattern("   B C ")
                .pattern("  B    ")
                .pattern(" B     ")
                .pattern("A      ")
                .define('A', ModBlocks.crystal_matrix.get())
                .define('B', ModItems.neutron_ingot.get())
                .define('C', ModItems.crystal_matrix_ingot.get())
                .unlockedBy("has_item", has(ModItems.crystal_matrix_ingot.get())).save(consumer);

        ModShapedRecipeBuilder.shaped(RecipeCategory.TOOLS, ModItems.infinity_axe.get())
                .pattern("    II   ")
                .pattern("   II  B ")
                .pattern("  IIIAA  ")
                .pattern("  IIAXA  ")
                .pattern("  I AAII ")
                .pattern("   N  II ")
                .pattern("  N      ")
                .pattern(" N       ")
                .pattern("A        ")
                .define('I', ModItems.infinity_ingot.get())
                .define('N', ModItems.neutron_ingot.get())
                .define('X', ModItems.infinity_catalyst.get())
                .define('A', ModBlocks.crystal_matrix.get())
                .define('B', ModBlocks.neutron.get())
                .unlockedBy("has_item", has(ModItems.infinity_ingot.get())).save(consumer);

        ModShapedRecipeBuilder.shaped(RecipeCategory.TOOLS, ModItems.infinity_shovel.get())
                .pattern("      III")
                .pattern("     IIII")
                .pattern("    CIIII")
                .pattern("    ACII ")
                .pattern("   AXAC  ")
                .pattern("   NA    ")
                .pattern("  N      ")
                .pattern(" N       ")
                .pattern("A        ")
                .define('I', ModItems.infinity_ingot.get())
                .define('N', ModItems.neutron_ingot.get())
                .define('X', ModItems.infinity_catalyst.get())
                .define('A', ModBlocks.crystal_matrix.get())
                .define('C', ModItems.crystal_matrix_ingot.get())
                .unlockedBy("has_item", has(ModItems.infinity_ingot.get())).save(consumer);

        ModShapedRecipeBuilder.shaped(RecipeCategory.TOOLS, ModItems.infinity_pickaxe.get())
                .pattern(" IIIIII B")
                .pattern("    IIAA ")
                .pattern("     AXAI")
                .pattern("     AAII")
                .pattern("    N  II")
                .pattern("   N    I")
                .pattern("  N     I")
                .pattern(" N      I")
                .pattern("A        ")
                .define('I', ModItems.infinity_ingot.get())
                .define('N', ModItems.neutron_ingot.get())
                .define('X', ModItems.infinity_catalyst.get())
                .define('A', ModBlocks.crystal_matrix.get())
                .define('B', ModBlocks.neutron.get())
                .unlockedBy("has_item", has(ModItems.infinity_ingot.get())).save(consumer);

        ModShapedRecipeBuilder.shaped(RecipeCategory.TOOLS, ModItems.infinity_hoe.get())
                .pattern(" IIIIIIAA")
                .pattern("  IIIIAXA")
                .pattern("      AAI")
                .pattern("     N II")
                .pattern("    N    ")
                .pattern("   N     ")
                .pattern("  N      ")
                .pattern(" N       ")
                .pattern("A        ")
                .define('I', ModItems.infinity_ingot.get())
                .define('N', ModItems.neutron_ingot.get())
                .define('X', ModItems.infinity_catalyst.get())
                .define('A', ModBlocks.crystal_matrix.get())
                .unlockedBy("has_item", has(ModItems.infinity_ingot.get())).save(consumer);

        ModShapedRecipeBuilder.shaped(RecipeCategory.TOOLS, ModItems.infinity_sword.get())
                .pattern("      NNN")
                .pattern("     NIIN")
                .pattern("    NIIIN")
                .pattern("   NIIIN ")
                .pattern("CCNIIIN  ")
                .pattern(" AACIN   ")
                .pattern(" ABAN    ")
                .pattern(" NAAC    ")
                .pattern("X   C    ")
                .define('I', ModItems.infinity_ingot.get())
                .define('N', ModItems.neutron_ingot.get())
                .define('C', ModItems.crystal_matrix_ingot.get())
                .define('X', ModItems.infinity_catalyst.get())
                .define('A', ModBlocks.crystal_matrix.get())
                .define('B', ModBlocks.neutron.get())
                .unlockedBy("has_item", has(ModItems.infinity_ingot.get())).save(consumer);

        ModShapedRecipeBuilder.shaped(RecipeCategory.TOOLS, ModItems.infinity_bow.get())
                .pattern("      III")
                .pattern("  AAIINNP")
                .pattern(" AXA   C ")
                .pattern(" AA   C  ")
                .pattern(" I   C   ")
                .pattern(" I  C    ")
                .pattern("IN C     ")
                .pattern("INC      ")
                .pattern("IP       ")
                .define('I', ModItems.infinity_ingot.get())
                .define('C', ModItems.crystal_matrix_ingot.get())
                .define('X', ModItems.infinity_catalyst.get())
                .define('A', ModBlocks.crystal_matrix.get())
                .define('N', ModItems.neutron_ingot.get())
                .define('P', ModItems.neutron_pile.get())
                .unlockedBy("has_item", has(ModItems.infinity_ingot.get())).save(consumer);

        ModShapedRecipeBuilder.shaped(RecipeCategory.TOOLS, ModItems.infinity_crossbow.get())
                .pattern("   IIIIIP")
                .pattern(" AC N  C ")
                .pattern(" CXN  C  ")
                .pattern("I NIPC   ")
                .pattern("IN PCN   ")
                .pattern("I  CNIN  ")
                .pattern("I C  NNA ")
                .pattern("IC    AAN")
                .pattern("P      NN")
                .define('I', ModItems.infinity_ingot.get())
                .define('C', ModItems.crystal_matrix_ingot.get())
                .define('X', ModItems.infinity_catalyst.get())
                .define('A', ModBlocks.crystal_matrix.get())
                .define('N', ModItems.neutron_ingot.get())
                .define('P', ModItems.neutron_pile.get())
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
                .unlockedBy("has_item", has(ModItems.infinity_catalyst.get())).save(consumer);

        ModShapedRecipeBuilder.shaped(RecipeCategory.TOOLS, ModBlocks.dense_neutron_collector.get())
                .pattern("AAC   CAA")
                .pattern("AB     BA")
                .pattern("C DEEED C")
                .pattern("  EGGGE  ")
                .pattern("  EGFGE  ")
                .pattern("  EGGGE  ")
                .pattern("C DEEED C")
                .pattern("AB     BA")
                .pattern("AAC   CAA")
                .define('A', Items.ENDER_PEARL)
                .define('B', Items.NETHER_STAR)
                .define('C', ModItems.diamond_lattice.get())
                .define('D', ModItems.neutron_ingot.get())
                .define('E', Blocks.EMERALD_BLOCK)
                .define('F', ModItems.endest_pearl.get())
                .define('G', ModBlocks.neutron_collector.get())
                .unlockedBy("has_item", has(ModBlocks.neutron_collector.get())).save(consumer);

        ModShapedRecipeBuilder.shaped(RecipeCategory.TOOLS, ModBlocks.denser_neutron_collector.get())
                .pattern("ABB F BBA")
                .pattern("BCC   CCB")
                .pattern("BCDEEEDCB")
                .pattern("  EGGGE  ")
                .pattern("F EGEGE F")
                .pattern("  EGGGE  ")
                .pattern("BCDEEEDCB")
                .pattern("BCC   CCB")
                .pattern("ABB F BBA")
                .define('A', ModItems.neutron_gear.get())
                .define('B', ModItems.neutron_pile.get())
                .define('C', ModItems.blaze_cube.get())
                .define('D', ModItems.infinity_catalyst.get())
                .define('E', ModBlocks.blaze_cube_block.get())
                .define('F', Blocks.GOLD_BLOCK)
                .define('G', ModBlocks.dense_neutron_collector.get())
                .unlockedBy("has_item", has(ModBlocks.dense_neutron_collector.get())).save(consumer);

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


        ModShapedRecipeBuilder.shaped(RecipeCategory.TOOLS, AEBlocks.CREATIVE_ENERGY_CELL)
                .conditions(modLoaded("ae2"))
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
                .unlockedBy("has_item", has(Static.getItem("ae2", "dense_energy_cell")))
                .save(consumer, Static.rl("ae2_creative_energy_cell"));

//        var mana_tablet_tag = new CompoundTag();
//        mana_tablet_tag.putInt("mana", 500000);
//        mana_tablet_tag.putBoolean("creative", true);
//
//        consumer.withConditions(modLoaded("botania")).accept(
//                ModShapedRecipeBuilder.shaped(RecipeCategory.TOOLS, ResourceLocation.tryBuild("botania", "mana_tablet"), mana_tablet_tag)
//                        .pattern("BAAACAAAD")
//                        .pattern("ATTJKLTTA")
//                        .pattern("ATUUMUUTA")
//                        .pattern("ANUOPOUQA")
//                        .pattern("EUUPRPUUF")
//                        .pattern("ASUOPOUSA")
//                        .pattern("ATUUUUUTA")
//                        .pattern("ATTSISTTA")
//                        .pattern("GAAAHAAAA")
//
//                        .define('A', ModItems.infinity_ingot.get())
//                        .define('B', Static.getIngredient("botania", "rune_envy"))
//                        .define('C', Static.getIngredient("botania", "rune_gluttony"))
//                        .define('D', Static.getIngredient("botania", "rune_winter"))
//                        .define('E', Static.getIngredient("botania", "rune_lust"))
//                        .define('F', Static.getIngredient("botania", "rune_pride"))
//                        .define('G', Static.getIngredient("botania", "rune_wrath"))
//                        .define('H', Static.getIngredient("botania", "rune_greed"))
//                        .define('I', Static.getIngredient("botania", "rune_sloth"))
//                        .define('J', Static.getIngredient("botania", "infinite_fruit"))
//                        .define('K', Static.getIngredient("botania", "flight_tiara"))
//                        .define('L', Static.getIngredient("botania", "king_key"))
//                        .define('M', Static.getIngredient("botania", "flugel_eye"))
//                        .define('N', Static.getIngredient("botania", "odin_ring"))
//                        .define('O', Static.getIngredient("botania", "spawner_mover"))
//                        .define('P', Static.getIngredient("botania", "mana_mirror"))
//                        .define('Q', Static.getIngredient("botania", "thor_ring"))
//                        .define('R', Static.getIngredient("botania", "mana_tablet"))
//                        .define('S', Static.getIngredient("botania", "dice"))
//                        .define('T', Static.getIngredient("botania", "fabulous_pool"))
//                        .define('U', Static.getIngredient("botania", "terrasteel_block"))
//                        .unlockedBy("has_item", has(Static.getItem("botania", "terrasteel_block")))::save
//        ).build(consumer, Static.rl("botania_mana_tablet"));
//
//        consumer.withConditions(modLoaded("botania")).addRecipe(
//                ModShapedRecipeBuilder.shaped(RecipeCategory.TOOLS, ResourceLocation.tryBuild("botania", "creative_pool"))
//                        .pattern("NNNNNNNNN")
//                        .pattern("NXCXYXCXN")
//                        .pattern("NCXEYEXCN")
//                        .pattern("NXEEYEEXN")
//                        .pattern("YYYYFYYYY")
//                        .pattern("NXEEYEEXN")
//                        .pattern("NCXEYEXCN")
//                        .pattern("NXCXYXCXN")
//                        .pattern("NNNNNNNNN")
//
//                        .define('X', ModItems.infinity_catalyst.get())
//                        .define('N', ModItems.neutron_ingot.get())
//                        .define('C', Static.getIngredient("botania", "mana_pool"))
//                        .define('Y', Static.getIngredient("botania", "fabulous_pool"))
//                        .define('E', Static.getIngredient("botania", "dragonstone_block"))
//                        .define('F', Static.getIngredient("botania", "mana_tablet"))
//                        .unlockedBy("has_item", has(Static.getItem("botania", "mana_tablet")))::save
//        ).build(consumer, Static.rl("botania_creative_pool"));

//        consumer.withConditions(modLoaded("draconicevolution")).addRecipe(
//                ModShapedRecipeBuilder.shaped(RecipeCategory.TOOLS, ResourceLocation.tryBuild("draconicevolution", "creative_capacitor"))
//                        .pattern("AAAACAAAA")
//                        .pattern("AEEBCBEEA")
//                        .pattern("AEBFCFBEA")
//                        .pattern("ABFFCFFBA")
//                        .pattern("CCCCDCCCC")
//                        .pattern("ABFFCFFBA")
//                        .pattern("AEBFCFBEA")
//                        .pattern("AEEBCBEEA")
//                        .pattern("AAAACAAAA")
//
//                        .define('A', ModItems.infinity_ingot.get())
//                        .define('B', Static.getIngredient("draconicevolution", "chaotic_crafting_injector"))
//                        .define('C', Static.getIngredient("draconicevolution", "reactor_stabilizer"))
//                        .define('D', Static.getIngredient("draconicevolution", "reactor_core"))
//                        .define('E', Static.getIngredient("draconicevolution", "chaotic_core"))
//                        .define('F', Static.getIngredient("draconicevolution", "chaotic_capacitor"))
//                        .unlockedBy("has_item", has(Static.getItem("draconicevolution", "chaotic_capacitor")))::save
//        ).build(consumer, Static.rl("de_creative_capacitor"));
//
//        consumer.withConditions(modLoaded("draconicevolution")).addRecipe(
//                ModShapedRecipeBuilder.shaped(RecipeCategory.TOOLS, ResourceLocation.tryBuild("draconicevolution", "creative_op_capacitor"))
//                        .pattern("BBCCCCCBB")
//                        .pattern("BBBBBBBBB")
//                        .pattern("CBAAAAABC")
//                        .pattern("CBACECABC")
//                        .pattern("CBAEDEABC")
//                        .pattern("CBACECABC")
//                        .pattern("CBAAAAABC")
//                        .pattern("BBBBBBBBB")
//                        .pattern("BBCCCCCBB")
//
//                        .define('A', ModItems.infinity_ingot.get())
//                        .define('B', ModBlocks.infinity.get())
//                        .define('C', Static.getIngredient("draconicevolution", "reactor_stabilizer"))
//                        .define('D', Static.getIngredient("draconicevolution", "reactor_core"))
//                        .define('E', Static.getIngredient("draconicevolution", "creative_capacitor"))
//                        .unlockedBy("has_item", has(Static.getItem("draconicevolution", "creative_capacitor")))::save
//        ).build(consumer, Static.rl("de_creative_op_capacitor"));
        ModShapedRecipeBuilder.shaped(RecipeCategory.TOOLS, com.refinedmods.refinedstorage.common.content.Blocks.INSTANCE.getCreativeController().getDefault())
                .conditions(modLoaded("refinedstorage"))
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
                .define('F', ItemTags.create(ResourceLocation.tryBuild("refinedstorage", "controller")))
                .unlockedBy("has_item", has(ItemTags.create(ResourceLocation.tryBuild("refinedstorage", "controller"))))
                .save(consumer, Static.rl("rs_creative_controller"));

        ModShapedRecipeBuilder.shaped(RecipeCategory.TOOLS, com.refinedmods.refinedstorage.common.content.Items.INSTANCE.getFluidStorageDisk(FluidStorageVariant.CREATIVE))
                .conditions(modLoaded("refinedstorage"))
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
                .unlockedBy("has_item", has(Static.getItem("refinedstorage", "creative_controller")))
                .save(consumer, Static.rl("rs_creative_fluid_storage_disk"));

        ModShapedRecipeBuilder.shaped(RecipeCategory.TOOLS, com.refinedmods.refinedstorage.common.content.Items.INSTANCE.getItemStorageDisk(ItemStorageVariant.CREATIVE))
                .conditions(modLoaded("refinedstorage"))
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
                .unlockedBy("has_item", has(Static.getItem("refinedstorage", "creative_controller")))
                .save(consumer, Static.rl("rs_creative_storage_disk"));

        ModShapedRecipeBuilder.shaped(RecipeCategory.TOOLS, com.refinedmods.refinedstorage.common.content.Items.INSTANCE.getCreativeWirelessGrid())
                .conditions(modLoaded("refinedstorage"))
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
                .unlockedBy("has_item", has(Static.getItem("refinedstorage", "wireless_grid")))
                .save(consumer, Static.rl("rs_creative_wireless_grid"));

//        var creative_slot_abilities = new CompoundTag();
//        creative_slot_abilities.putString("slot", "abilities");

//        consumer.withConditions(modLoaded("tconstruct")).addRecipe(
//                ModShapedRecipeBuilder.shaped(RecipeCategory.TOOLS, ResourceLocation.tryBuild("tconstruct", "creative_slot"), creative_slot_abilities)
//                        .pattern("GGBHHHBGG")
//                        .pattern("GCCCCCCCG")
//                        .pattern("BCBFFFBCB")
//                        .pattern("HCFFEFFCH")
//                        .pattern("HCFEAEFCH")
//                        .pattern("HCFFEFFCH")
//                        .pattern("BCBFFFBCB")
//                        .pattern("GCCCCCCCG")
//                        .pattern("GGBHHHBGG")
//
//                        .define('A', ModItems.infinity_catalyst.get())
//                        .define('B', Static.getIngredient("tconstruct", "iron_reinforcement"))
//                        .define('C', Static.getIngredient("tconstruct", "knightslime_ingot"))
//                        .define('E', Static.getIngredient("tconstruct", "manyullyn_block"))
//                        .define('F', Static.getIngredient("tconstruct", "jeweled_apple"))
//                        .define('G', Static.getIngredient("tconstruct", "iron_reinforcement"))
//                        .define('H', Static.getIngredient("tconstruct", "ichor_slime_crystal"))
//                        .unlockedBy("has_item", has(Static.getItem("tconstruct", "ichor_slime_crystal")))::save
//        ).build(consumer, Static.rl("tc3_creative_slot_ability"));
//
//        var creative_slot_defense = new CompoundTag();
//        creative_slot_defense.putString("slot", "defense");
//
//        consumer.withConditions(modLoaded("tconstruct")).addRecipe(
//                ModShapedRecipeBuilder.shaped(RecipeCategory.TOOLS, ResourceLocation.tryBuild("tconstruct", "creative_slot"), creative_slot_defense)
//                        .pattern("GGBHHHBGG")
//                        .pattern("GCCCCCCCG")
//                        .pattern("BCBFFFBCB")
//                        .pattern("HCFFEFFCH")
//                        .pattern("HCFEAEFCH")
//                        .pattern("HCFFEFFCH")
//                        .pattern("BCBFFFBCB")
//                        .pattern("GCCCCCCCG")
//                        .pattern("GGBHHHBGG")
//
//                        .define('A', ModItems.infinity_catalyst.get())
//                        .define('B', Static.getIngredient("tconstruct", "iron_reinforcement"))
//                        .define('C', Static.getIngredient("tconstruct", "knightslime_ingot"))
//                        .define('E', Static.getIngredient("tconstruct", "manyullyn_block"))
//                        .define('F', Static.getIngredient("tconstruct", "jeweled_apple"))
//                        .define('G', Static.getIngredient("tconstruct", "iron_reinforcement"))
//                        .define('H', Static.getIngredient("tconstruct", "earth_slime_crystal"))
//                        .unlockedBy("has_item", has(Static.getItem("tconstruct", "earth_slime_crystal")))::save
//        ).build(consumer, Static.rl("tc3_creative_slot_defense"));
//
//
//        var creative_slot_souls = new CompoundTag();
//        creative_slot_souls.putString("slot", "souls");
//
//        consumer.withConditions(modLoaded("tconstruct")).addRecipe(
//                ModShapedRecipeBuilder.shaped(RecipeCategory.TOOLS, ResourceLocation.tryBuild("tconstruct", "creative_slot"), creative_slot_souls)
//                        .pattern("GGBHHHBGG")
//                        .pattern("GCCCCCCCG")
//                        .pattern("BCBFFFBCB")
//                        .pattern("HCFFEFFCH")
//                        .pattern("HCFEAEFCH")
//                        .pattern("HCFFEFFCH")
//                        .pattern("BCBFFFBCB")
//                        .pattern("GCCCCCCCG")
//                        .pattern("GGBHHHBGG")
//
//                        .define('A', ModItems.infinity_catalyst.get())
//                        .define('B', Static.getIngredient("tconstruct", "iron_reinforcement"))
//                        .define('C', Static.getIngredient("tconstruct", "knightslime_ingot"))
//                        .define('E', Static.getIngredient("tconstruct", "manyullyn_block"))
//                        .define('F', Static.getIngredient("tconstruct", "jeweled_apple"))
//                        .define('G', Static.getIngredient("tconstruct", "iron_reinforcement"))
//                        .define('H', Static.getIngredient("tconstruct", "sky_slime_crystal"))
//                        .unlockedBy("has_item", has(Static.getItem("tconstruct", "sky_slime_crystal")))::save
//        ).build(consumer, Static.rl("tc3_creative_slot_souls"));
//
//        var creative_slot_upgrades = new CompoundTag();
//        creative_slot_upgrades.putString("slot", "upgrades");
//
//        consumer.withConditions(modLoaded("tconstruct")).addRecipe(
//                ModShapedRecipeBuilder.shaped(RecipeCategory.TOOLS, ResourceLocation.tryBuild("tconstruct", "creative_slot"), creative_slot_upgrades)
//                        .pattern("GGBHHHBGG")
//                        .pattern("GCCCCCCCG")
//                        .pattern("BCBFFFBCB")
//                        .pattern("HCFFEFFCH")
//                        .pattern("HCFEAEFCH")
//                        .pattern("HCFFEFFCH")
//                        .pattern("BCBFFFBCB")
//                        .pattern("GCCCCCCCG")
//                        .pattern("GGBHHHBGG")
//
//                        .define('A', ModItems.infinity_catalyst.get())
//                        .define('B', Static.getIngredient("tconstruct", "iron_reinforcement"))
//                        .define('C', Static.getIngredient("tconstruct", "knightslime_ingot"))
//                        .define('E', Static.getIngredient("tconstruct", "manyullyn_block"))
//                        .define('F', Static.getIngredient("tconstruct", "jeweled_apple"))
//                        .define('G', Static.getIngredient("tconstruct", "iron_reinforcement"))
//                        .define('H', Static.getIngredient("tconstruct", "ender_slime_crystal"))
//                        .unlockedBy("has_item", has(Static.getItem("tconstruct", "ender_slime_crystal")))::save
//        ).build(consumer, Static.rl("tc3_creative_slot_upgrades"));

        ModShapedRecipeBuilder.shaped(RecipeCategory.TOOLS, com.jaquadro.minecraft.storagedrawers.core.ModItems.CREATIVE_STORAGE_UPGRADE.get())
                .conditions(modLoaded("storagedrawers"))
                .pattern("    S    ")
                .pattern(" NDDDDDN ")
                .pattern(" DNDDDND ")
                .pattern(" DDNDNDD ")
                .pattern("SDDDEDDDS")
                .pattern(" DDNDNDD ")
                .pattern(" DNDDDND ")
                .pattern(" NDDDDDN ")
                .pattern("    S    ")
                .define('N', ModItems.neutron_nugget.get())
                .define('S', Items.NETHER_STAR)
                .define('D', ModTags.DRAWERS)
                .define('E', Static.getIngredient("storagedrawers", "emerald_storage_upgrade"))
                .unlockedBy("has_item", has(Static.getItem("storagedrawers", "emerald_storage_upgrade")))
                .save(consumer, Static.rl("creative_storage_upgrade"));

//        var creative_energy_cube_main = new CompoundTag();
//        var energyContainers = new ListTag();
//        var stored = new CompoundTag();
//        var mekData = new CompoundTag();
//        stored.putString("stored", "18446744073709551615.9999");
//        stored.putBoolean("Container", false);
//        energyContainers.add(stored);
//        mekData.put("EnergyContainers", energyContainers);
//        creative_energy_cube_main.put("mekData", mekData);


        ModShapedRecipeBuilder.shaped(RecipeCategory.TOOLS, MekanismBlocks.CREATIVE_ENERGY_CUBE)
                .conditions(modLoaded("mekanism"))
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
                .unlockedBy("has_item", has(Static.getItem("mekanism", "ultimate_energy_cube")))
                .save(consumer, Static.rl("mek_creative_energy_cube"));

        ModShapedRecipeBuilder.shaped(RecipeCategory.TOOLS, MekanismBlocks.CREATIVE_FLUID_TANK)
                .conditions(modLoaded("mekanism"))
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
                .unlockedBy("has_item", has(Static.getItem("mekanism", "ultimate_fluid_tank")))
                .save(consumer, Static.rl("mek_creative_fluid_tank"));


        ModShapedRecipeBuilder.shaped(RecipeCategory.TOOLS, MekanismBlocks.CREATIVE_CHEMICAL_TANK)
                .conditions(modLoaded("mekanism"))
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
                .unlockedBy("has_item", has(Static.getItem("mekanism", "ultimate_chemical_tank")))
                .save(consumer, Static.rl("mek_creative_chemical_tank"));


        ModShapedRecipeBuilder.shaped(RecipeCategory.TOOLS, MekanismBlocks.CREATIVE_BIN)
                .conditions(modLoaded("mekanism"), modLoaded("mekanismgenerators"))
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
                .unlockedBy("has_item", has(Static.getItem("mekanism", "ultimate_bin")))
                .save(consumer, Static.rl("mek_creative_bin"));


        ModShapedRecipeBuilder.shaped(RecipeCategory.TOOLS, MachineBlocks.CREATIVE_POWER)
                .conditions(modLoaded("enderio"))
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
                .unlockedBy("has_item", has(ModItems.infinity_ingot.get()))
                .save(consumer, Static.rl("eio_creative_power"));

    }
}
