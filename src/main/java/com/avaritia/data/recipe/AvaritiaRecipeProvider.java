package com.avaritia.data.recipe;

import com.avaritia.Avaritia;
import com.avaritia.common.crafting.recipe.CompressorRecipe;
import com.avaritia.common.crafting.recipe.EternalSingularityCraftRecipe;
import com.avaritia.common.crafting.recipe.ExtremeSmithingRecipe;
import com.avaritia.common.crafting.recipe.FullMatterClusterRecipe;
import com.avaritia.common.crafting.recipe.InfinityCatalystCraftRecipe;
import com.avaritia.common.crafting.recipe.NoConsumeCatalystShapedRecipe;
import com.avaritia.common.crafting.recipe.ShapedTableCraftingRecipe;
import com.avaritia.common.crafting.recipe.ShapelessTableCraftingRecipe;
import com.avaritia.init.registry.ModBlocks;
import com.avaritia.init.registry.ModItems;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.advancements.AdvancementRequirements;
import net.minecraft.advancements.AdvancementRewards;
import net.minecraft.advancements.Criterion;
import net.minecraft.advancements.critereon.InventoryChangeTrigger;
import net.minecraft.advancements.critereon.ItemPredicate;
import net.minecraft.advancements.critereon.RecipeUnlockedTrigger;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.data.recipes.RecipeProvider;
import net.minecraft.data.recipes.ShapedRecipeBuilder;
import net.minecraft.data.recipes.ShapelessRecipeBuilder;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.ShapedRecipePattern;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.neoforge.common.crafting.CompoundIngredient;
import org.jetbrains.annotations.NotNull;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * Avaritia 配方数据生成器。
 * <p>
 * 负责通过 datagen 输出 {@code data/avaritia/recipe/} 下的核心配方，
 * 包含原版 3x3 合成、极限合成台合成、中子压缩机配方与极限锻造配方。
 * 本类只声明配方生成逻辑，不手写 JSON 文件。
 */
public class AvaritiaRecipeProvider extends RecipeProvider {

    protected AvaritiaRecipeProvider(HolderLookup.Provider registries, RecipeOutput output) {
        super(registries, output);
    }

    @Override
    protected void buildRecipes() {
        buildRecipes(this.output);
    }

    /**
     * 使用 26.1.2 的 {@link RecipeOutput} 生成 Avaritia 配方。
     *
     * @param output 配方输出
     */
    protected void buildRecipes(RecipeOutput output) {
        vanillaRecipes(output);
        craftingTableRecipes(output);
        extremeSmithingRecipes(output);
        singularityRecipes(output);
        compressorRecipes(output);
    }

    private void vanillaRecipes(RecipeOutput output) {
        storage(output, ModItems.neutron_pile.get(), ModItems.neutron_nugget.get(), "neutron_nugget_from_piles", "neutron_pile_from_nuggets");
        storage(output, ModItems.neutron_nugget.get(), ModItems.neutron_ingot.get(), "neutron_ingot_from_nuggets", "neutron_nugget_from_ingots");
        storage(output, ModItems.infinity_nugget.get(), ModItems.infinity_ingot.get(), "infinity_ingot_from_nuggets", "infinity_nugget_from_ingots");
        storage(output, ModItems.crystal_matrix_ingot.get(), ModBlocks.crystal_matrix.get(), "crystal_matrix", "crystal_matrix_ingot_normal");
        storage(output, ModItems.blaze_cube.get(), ModBlocks.blaze_cube_block.get(), "blaze_cube_block", "blaze_cube");
        storage(output, ModItems.diamond_lattice.get(), ModBlocks.diamond_lattice_block.get(), "diamond_lattice_block", "diamond_lattice_normal");
        storage(output, ModItems.star_fuel.get(), ModBlocks.star_fuel_block.get(), "star_fuel_block", "star_fuel_alternate");
        storage(output, ModItems.refined_coal.get(), ModBlocks.refined_coal_block.get(), "refined_coal_block", "refined_coal");

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModItems.neutron_gear.get())
                .pattern(" n ")
                .pattern("ncn")
                .pattern(" n ")
                .define('n', ModItems.neutron_ingot.get())
                .define('c', ModItems.crystal_matrix_ingot.get())
                .unlockedBy("has_neutron_ingot", has(ModItems.neutron_ingot.get()))
                .save(output, key("neutron_gear"));

        ShapedRecipeBuilder.shaped(RecipeCategory.TOOLS, ModBlocks.compressed_chest.get())
                .pattern("aaa")
                .pattern("aba")
                .pattern("aaa")
                .define('a', Blocks.CHEST)
                .define('b', ModItems.crystal_matrix_ingot.get())
                .unlockedBy("has_crystal_matrix_ingot", has(ModItems.crystal_matrix_ingot.get()))
                .save(output, key("compressed_chest"));

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModItems.crystal_matrix_ingot.get())
                .pattern("xyx")
                .pattern("xyx")
                .define('x', ModItems.diamond_lattice.get())
                .define('y', Items.NETHER_STAR)
                .unlockedBy("has_diamond_lattice", has(ModItems.diamond_lattice.get()))
                .save(output, key("crystal_matrix_ingot_normal"));

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModItems.diamond_lattice.get())
                .pattern("aba")
                .pattern("bab")
                .pattern("aba")
                .define('a', Items.DIAMOND)
                .define('b', Items.GOLD_INGOT)
                .unlockedBy("has_diamond", has(Items.DIAMOND))
                .save(output, key("diamond_lattice"));
    }

    private void craftingTableRecipes(RecipeOutput output) {
        extremeShaped(output, RecipeCategory.MISC, ModBlocks.sculk_crafting_table.get(), 1,
                new String[]{"aba", "cxc", "ada"},
                keyMap()
                        .put('a', Items.ECHO_SHARD)
                        .put('b', Blocks.SCULK_SHRIEKER)
                        .put('c', Blocks.SCULK)
                        .put('d', Blocks.SCULK_CATALYST)
                        .put('x', ModBlocks.double_compressed_crafting_table.get()),
                "has_double_compressed_crafting_table", has(ModBlocks.double_compressed_crafting_table.get()), "sculk_crafting_table");

        extremeShaped(output, RecipeCategory.MISC, ModBlocks.nether_crafting_table.get(), 1,
                new String[]{"cbc", "dad", "efe"},
                keyMap()
                        .put('a', ModBlocks.double_compressed_crafting_table.get())
                        .put('b', Blocks.RESPAWN_ANCHOR)
                        .put('c', Blocks.WITHER_SKELETON_SKULL)
                        .put('d', Blocks.NETHERRACK)
                        .put('e', Items.NETHERITE_INGOT)
                        .put('f', Items.NETHER_STAR),
                "has_sculk_crafting_table", has(ModBlocks.sculk_crafting_table.get()), "nether_crafting_table");

        extremeShaped(output, RecipeCategory.MISC, ModBlocks.end_crafting_table.get(), 2,
                new String[]{"bcccb", "dfifd", "dgagd", "dhjhd", "beeeb"},
                keyMap()
                        .put('a', ModBlocks.double_compressed_crafting_table.get())
                        .put('b', Items.END_CRYSTAL)
                        .put('c', Items.END_PORTAL_FRAME)
                        .put('d', Items.OBSIDIAN)
                        .put('e', Items.DRAGON_BREATH)
                        .put('f', Items.PURPUR_PILLAR)
                        .put('g', Items.END_STONE_BRICKS)
                        .put('h', Items.END_STONE)
                        .put('i', Items.ENDER_EYE)
                        .put('j', Items.ENDER_CHEST),
                "has_nether_crafting_table", has(ModBlocks.nether_crafting_table.get()), "end_crafting_table");

        extremeShaped(output, RecipeCategory.MISC, ModBlocks.extreme_crafting_table.get(), 3,
                new String[]{"bccfccb", "cddgddc", "cdihidc", "cdiaidc", "cdjkjdc", "cdddddc", "beeeeeb"},
                keyMap()
                        .put('a', ModBlocks.double_compressed_crafting_table.get())
                        .put('b', Blocks.LODESTONE)
                        .put('c', ModItems.diamond_lattice.get())
                        .put('d', ModItems.crystal_matrix_ingot.get())
                        .put('e', ModBlocks.crystal_matrix.get())
                        .put('f', Items.RECOVERY_COMPASS)
                        .put('g', Items.DRAGON_EGG)
                        .put('h', Items.BEACON)
                        .put('i', Items.REINFORCED_DEEPSLATE)
                        .put('j', Blocks.NETHERITE_BLOCK)
                        .put('k', Items.HEART_OF_THE_SEA),
                "has_end_crafting_table", has(ModBlocks.end_crafting_table.get()), "extreme_crafting_table");

        extremeShaped(output, RecipeCategory.MISC, ModItems.infinity_upgrade.get(), 4,
                new String[]{"  aba  ", " cdfdc ", "acdecea", "bfegefb", "acdecea", " cdfdc ", "  aba  "},
                keyMap()
                        .put('a', ModItems.crystal_matrix_ingot.get())
                        .put('b', ModItems.neutron_ingot.get())
                        .put('c', ModItems.neutron_pile.get())
                        .put('d', ModItems.infinity_nugget.get())
                        .put('e', ModItems.infinity_ingot.get())
                        .put('f', ModItems.infinity_catalyst.get())
                        .put('g', Items.NETHERITE_UPGRADE_SMITHING_TEMPLATE),
                "has_infinity_ingot", has(ModItems.infinity_ingot.get()), "infinity_upgrade");

        noConsumeExtremeShaped(output, RecipeCategory.TOOLS, ModItems.upgrade_smithing_template.get(), 2, 4,
                new String[]{"         ", " abbbbba ", " bdcccdb ", " bceeecb ", " bcefecb ", " bceeecb ", " bdcccdb ", " abbbbba ", "         "},
                keyMap()
                        .put('a', ModBlocks.crystal_matrix.get())
                        .put('b', ModItems.crystal_matrix_ingot.get())
                        .put('c', ModItems.neutron_ingot.get())
                        .put('d', ModItems.neutron_pile.get())
                        .put('e', ModItems.infinity_catalyst.get())
                        .put('f', ModItems.upgrade_smithing_template.get()),
                "has_neutron_ingot", has(ModItems.neutron_ingot.get()), "upgrade_smithing_template_too");

        extremeShaped(output, RecipeCategory.TOOLS, ModBlocks.extreme_smithing_table.get(), 4,
                new String[]{"aaaaaaaaa", "bccfgfccb", "bcdhhhdcb", "lfhijihfl", "eghjkjhge", "lfhijihfl", "bcdhhhdcb", "bccfgfccb", "bleeeeelb"},
                keyMap()
                        .put('a', ModBlocks.neutron.get())
                        .put('b', ModItems.neutron_ingot.get())
                        .put('c', ModItems.diamond_lattice.get())
                        .put('d', ModItems.blaze_cube.get())
                        .put('e', ModBlocks.crystal_matrix.get())
                        .put('f', ModItems.infinity_nugget.get())
                        .put('g', ModItems.infinity_ingot.get())
                        .put('h', ModItems.neutron_gear.get())
                        .put('i', ModItems.infinity_catalyst.get())
                        .put('j', Items.SMITHING_TABLE)
                        .put('k', ModBlocks.extreme_crafting_table.get())
                        .put('l', ModItems.crystal_matrix_ingot.get()),
                "has_extreme_crafting_table", has(ModBlocks.extreme_crafting_table.get()), "extreme_smithing_table");

        toolRecipes(output);
        foodAndMiscRecipes(output);
    }

    private void toolRecipes(RecipeOutput output) {
        extremeShaped(output, RecipeCategory.TOOLS, ModItems.blaze_sword.get(), 2,
                new String[]{"    CA", "   CCA", "  CCCA", " CCAC ", "CAC  ", " B   ", "A    "},
                keyMap().put('A', Blocks.BONE_BLOCK).put('B', ModItems.diamond_lattice.get()).put('C', ModItems.blaze_cube.get()),
                "has_blaze_cube", has(ModItems.blaze_cube.get()), "blaze_sword");
        extremeShaped(output, RecipeCategory.TOOLS, ModItems.blaze_pickaxe.get(), 2,
                new String[]{"CCCCCC ", "   CCA ", "    A C", "   B  C", "  B    ", " B     ", "A      "},
                keyMap().put('A', Blocks.BONE_BLOCK).put('B', ModItems.diamond_lattice.get()).put('C', ModItems.blaze_cube.get()),
                "has_blaze_cube", has(ModItems.blaze_cube.get()), "blaze_pickaxe");
        extremeShaped(output, RecipeCategory.TOOLS, ModItems.crystal_sword.get(), 3,
                new String[]{"     CA", "    CAC", " C CAC ", " CCAC  ", "CAAC   ", " BACC  ", "A C    "},
                keyMap().put('A', ModBlocks.crystal_matrix.get()).put('B', ModBlocks.neutron.get()).put('C', ModItems.crystal_matrix_ingot.get()),
                "has_crystal_matrix_ingot", has(ModItems.crystal_matrix_ingot.get()), "crystal_sword");
        extremeShaped(output, RecipeCategory.TOOLS, ModItems.crystal_pickaxe.get(), 3,
                new String[]{"CCCCCCA", "C CC  C", "C  A   ", "   B   ", "  B    ", " B     ", "A      "},
                keyMap().put('A', ModBlocks.crystal_matrix.get()).put('B', ModItems.neutron_ingot.get()).put('C', ModItems.crystal_matrix_ingot.get()),
                "has_crystal_matrix_ingot", has(ModItems.crystal_matrix_ingot.get()), "crystal_pickaxe");

        infinityTool(output, ModItems.infinity_axe.get(), "infinity_axe", new String[]{"     III", "    IIII", "   CIIII", "  CAXA I", "   NAA I", "  N    I", " N     I", "N      I", "A       "});
        infinityTool(output, ModItems.infinity_shovel.get(), "infinity_shovel", new String[]{"      III", "     IIII", "    CIIII", "    ACII ", "   AXAC  ", "   NA    ", "  N      ", " N       ", "A        "});
        infinityTool(output, ModItems.infinity_pickaxe.get(), "infinity_pickaxe", new String[]{" IIIIII B", "    IIAA ", "     AXAI", "     AAII", "    N  II", "   N    I", "  N     I", " N      I", "A        "});
        infinityTool(output, ModItems.infinity_hoe.get(), "infinity_hoe", new String[]{" IIIIIIAA", "  IIIIAXA", "      AAI", "     N II", "    N    ", "   N     ", "  N      ", " N       ", "A        "});
        infinityTool(output, ModItems.infinity_sword.get(), "infinity_sword", new String[]{"       II", "      III", "     III ", "  C III  ", "CCAIII   ", " CAXI    ", "  NAAC   ", " N CC    ", "A   C    "});
        infinityTool(output, ModItems.infinity_bow.get(), "infinity_bow", new String[]{"      III", "  AAIINNP", " AXA   C ", " AA   C  ", " I   C   ", " I  C    ", "IN C     ", "INC      ", "IP       "});
        infinityTool(output, ModItems.infinity_crossbow.get(), "infinity_crossbow", new String[]{"   IIIIIP", " AC N  C ", " CXN  C  ", "I NIPC   ", "IN PCN   ", "I  CNIN  ", "I C  NNA ", "IC    AAN", "P      NN"});
        infinityTool(output, ModItems.infinity_trident.get(), "infinity_trident", new String[]{"     I  I", "    I  I ", "   CAAI  ", "    AXA I", "    PAAI ", "   N  C  ", "  N      ", " C       ", "A        "});
    }

    private void foodAndMiscRecipes(RecipeOutput output) {
        extremeShapeless(output, RecipeCategory.TOOLS, ModItems.cosmic_meatballs.get(), 4,
                list(Items.PORKCHOP, Items.BEEF, Items.MUTTON, Items.COD, Items.SALMON, Items.TROPICAL_FISH, Items.PUFFERFISH, Items.RABBIT, Items.CHICKEN, Items.ROTTEN_FLESH, Items.SPIDER_EYE, ModItems.neutron_nugget.get()),
                "has_neutron_nugget", has(ModItems.neutron_nugget.get()), "cosmic_meatballs");
        extremeShapeless(output, RecipeCategory.TOOLS, ModItems.ultimate_stew.get(), 4,
                list(Items.APPLE, Items.GOLDEN_APPLE, Items.MELON_SLICE, Items.GLISTERING_MELON_SLICE, Items.SWEET_BERRIES, Items.CHORUS_FRUIT, Items.CARROT, Items.GOLDEN_CARROT, Items.POTATO, Items.POISONOUS_POTATO, Items.BEETROOT, Items.KELP, Items.NETHER_WART, Items.COCOA_BEANS, Items.PITCHER_POD, Items.HONEY_BOTTLE, Items.CACTUS, Items.BAMBOO, Items.SUGAR_CANE, Items.SEA_PICKLE, Items.BROWN_MUSHROOM, Items.RED_MUSHROOM, Items.CRIMSON_FUNGUS, Items.WARPED_FUNGUS, Items.WHEAT, Items.PUMPKIN, ModItems.neutron_nugget.get()),
                "has_neutron_nugget", has(ModItems.neutron_nugget.get()), "ultimate_stew");
    }

    private void extremeSmithingRecipes(RecipeOutput output) {
        extremeSmithing(output, Items.NETHERITE_SWORD, ModItems.infinity_sword.get(), "infinity_sword_smithing");
        extremeSmithing(output, Items.NETHERITE_PICKAXE, ModItems.infinity_pickaxe.get(), "infinity_pickaxe_smithing");
        extremeSmithing(output, Items.NETHERITE_AXE, ModItems.infinity_axe.get(), "infinity_axe_smithing");
        extremeSmithing(output, Items.NETHERITE_SHOVEL, ModItems.infinity_shovel.get(), "infinity_shovel_smithing");
        extremeSmithing(output, Items.NETHERITE_HOE, ModItems.infinity_hoe.get(), "infinity_hoe_smithing");
        extremeSmithing(output, Items.NETHERITE_CHESTPLATE, ModItems.infinity_chestplate.get(), "infinity_chestplate");
        extremeSmithing(output, Items.NETHERITE_LEGGINGS, ModItems.infinity_pants.get(), "infinity_pants");
        extremeSmithing(output, Items.NETHERITE_BOOTS, ModItems.infinity_boots.get(), "infinity_boots");
        extremeSmithing(output, Items.NETHERITE_HELMET, ModItems.infinity_helmet.get(), "infinity_helmet");

        save(output, "infinity_elytra", new ExtremeSmithingRecipe(
                Ingredient.of(ModItems.upgrade_smithing_template.get()),
                Ingredient.of(Items.ELYTRA),
                CompoundIngredient.of(Ingredient.of(ModBlocks.crystal_matrix.get()), Ingredient.of(ModItems.enhancement_core.get()), Ingredient.of(ModBlocks.neutron.get())),
                ModItems.infinity_elytra.get().getDefaultInstance()), RecipeCategory.MISC,
                "has_upgrade_smithing_template", has(ModItems.upgrade_smithing_template.get()));

        save(output, "infinity_chest", new ExtremeSmithingRecipe(
                Ingredient.of(ModItems.upgrade_smithing_template.get()),
                Ingredient.of(ModBlocks.compressed_chest.get()),
                CompoundIngredient.of(Ingredient.of(ModBlocks.neutron.get()), Ingredient.of(ModItems.enhancement_core.get()), Ingredient.of(ModBlocks.infinity.get())),
                ModBlocks.infinity_chest.get().asItem().getDefaultInstance()), RecipeCategory.MISC,
                "has_upgrade_smithing_template", has(ModItems.upgrade_smithing_template.get()));

        save(output, "extreme_anvil", new ExtremeSmithingRecipe(
                Ingredient.of(ModItems.upgrade_smithing_template.get()),
                Ingredient.of(Items.ANVIL),
                CompoundIngredient.of(Ingredient.of(ModItems.full_matter_cluster.get()), Ingredient.of(ModItems.enhancement_core.get()), Ingredient.of(ModBlocks.neutron.get())),
                ModBlocks.extreme_anvil.get().asItem().getDefaultInstance()), RecipeCategory.MISC,
                "has_upgrade_smithing_template", has(ModItems.upgrade_smithing_template.get()));
    }

    private void singularityRecipes(RecipeOutput output) {
        save(output, "infinity_catalyst", new InfinityCatalystCraftRecipe("default",
                ingredients(Items.BEDROCK, ModItems.crystal_matrix_ingot.get(), ModItems.neutron_ingot.get(), ModItems.cosmic_meatballs.get(), ModItems.ultimate_stew.get(), ModItems.endest_pearl.get(), ModItems.record_fragment.get()), 1),
                RecipeCategory.MISC, "has_neutron_ingot", has(ModItems.neutron_ingot.get()));
        save(output, "infinity_catalyst_eternal", new InfinityCatalystCraftRecipe("eternal_singularity",
                ingredients(Items.BEDROCK, ModItems.crystal_matrix_ingot.get(), ModItems.neutron_ingot.get(), ModItems.cosmic_meatballs.get(), ModItems.ultimate_stew.get(), ModItems.endest_pearl.get(), ModItems.record_fragment.get(), ModItems.eternal_singularity.get()), 1),
                RecipeCategory.MISC, "has_eternal_singularity", has(ModItems.eternal_singularity.get()));
        save(output, "full_matter_cluster", new FullMatterClusterRecipe("matter_cluster", ingredients(ModItems.matter_cluster.get()), 1),
                RecipeCategory.MISC, "has_matter_cluster", has(ModItems.matter_cluster.get()));
        save(output, "eternal_singularity", new EternalSingularityCraftRecipe(NonNullList.create(), 1),
                RecipeCategory.MISC, "has_singularity", has(ModItems.singularity.get()));
    }

    private void compressorRecipes(RecipeOutput output) {
        save(output, "bedrock_from_deepslate", new CompressorRecipe(Ingredient.of(Blocks.DEEPSLATE), new ItemStack(Blocks.BEDROCK), 10000, 240),
                RecipeCategory.MISC, "has_deepslate", has(Blocks.DEEPSLATE));
        save(output, "compressor_matter_cluster", new CompressorRecipe(Ingredient.of(ModItems.neutron_ingot.get()), new ItemStack(ModItems.full_matter_cluster.get()), 4096, 240),
                RecipeCategory.MISC, "has_neutron_ingot", has(ModItems.neutron_ingot.get()));
    }

    private void storage(RecipeOutput output, ItemLike small, ItemLike large, String packingName, String unpackingName) {
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, large)
                .pattern("###")
                .pattern("###")
                .pattern("###")
                .define('#', small)
                .unlockedBy("has_" + name(small), has(small))
                .save(output, key(packingName));
        ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, small, 9)
                .requires(large)
                .unlockedBy("has_" + name(large), has(large))
                .save(output, key(unpackingName));
    }

    private void infinityTool(RecipeOutput output, ItemLike result, String name, String[] pattern) {
        extremeShaped(output, RecipeCategory.TOOLS, result, 4, pattern,
                keyMap()
                        .put('I', ModItems.infinity_ingot.get())
                        .put('N', ModItems.neutron_ingot.get())
                        .put('X', ModItems.infinity_catalyst.get())
                        .put('A', ModBlocks.crystal_matrix.get())
                        .put('B', ModBlocks.neutron.get())
                        .put('C', ModItems.crystal_matrix_ingot.get())
                        .put('P', ModItems.neutron_pile.get()),
                "has_infinity_ingot", has(ModItems.infinity_ingot.get()), name);
    }

    private void extremeSmithing(RecipeOutput output, ItemLike base, ItemLike result, String name) {
        save(output, name, new ExtremeSmithingRecipe(
                Ingredient.of(ModItems.upgrade_smithing_template.get()),
                Ingredient.of(base),
                CompoundIngredient.of(Ingredient.of(ModItems.infinity_ingot.get()), Ingredient.of(ModItems.infinity_catalyst.get()), Ingredient.of(ModItems.enhancement_core.get())),
                new ItemStack(result)), RecipeCategory.TOOLS,
                "has_upgrade_smithing_template", has(ModItems.upgrade_smithing_template.get()));
    }

    private void extremeShaped(RecipeOutput output, RecipeCategory category, ItemLike result, int tier, String[] pattern, RecipeKeyMap keys,
                               String criterionName, Criterion<?> criterion, String name) {
        ShapedRecipePattern shapedPattern = ShapedRecipePattern.of(keys.ingredients, List.of(pattern));
        save(output, name, new ShapedTableCraftingRecipe(shapedPattern, new ItemStack(result), tier, false), category, criterionName, criterion);
    }

    private void noConsumeExtremeShaped(RecipeOutput output, RecipeCategory category, ItemLike result, int count, int tier, String[] pattern, RecipeKeyMap keys,
                                        String criterionName, Criterion<?> criterion, String name) {
        ShapedRecipePattern shapedPattern = ShapedRecipePattern.of(keys.ingredients, List.of(pattern));
        save(output, name, new NoConsumeCatalystShapedRecipe(shapedPattern, new ItemStack(result, count), tier), category, criterionName, criterion);
    }

    private void extremeShapeless(RecipeOutput output, RecipeCategory category, ItemLike result, int tier, NonNullList<Ingredient> ingredients,
                                  String criterionName, Criterion<?> criterion, String name) {
        save(output, name, new ShapelessTableCraftingRecipe(ingredients, new ItemStack(result), tier), category, criterionName, criterion);
    }

    private void save(RecipeOutput output, String name, Recipe<?> recipe, RecipeCategory category, String criterionName, Criterion<?> criterion) {
        ResourceKey<Recipe<?>> key = key(name);
        Advancement.Builder advancementBuilder = output.advancement()
                .addCriterion("has_the_recipe", RecipeUnlockedTrigger.unlocked(key))
                .rewards(AdvancementRewards.Builder.recipe(key))
                .requirements(AdvancementRequirements.Strategy.OR)
                .addCriterion(criterionName, criterion);
        AdvancementHolder advancement = advancementBuilder.build(key.location().withPrefix("recipes/" + category.getFolderName() + "/"));
        output.accept(key, recipe, advancement);
    }

    private static ResourceKey<Recipe<?>> key(String name) {
        return ResourceKey.create(Registries.RECIPE, Identifier.fromNamespaceAndPath(Avaritia.MOD_ID, name));
    }

    private static Criterion<InventoryChangeTrigger.TriggerInstance> has(ItemLike item) {
        return RecipeProvider.inventoryTrigger(ItemPredicate.Builder.item().of(item.asItem().builtInRegistryHolder()).build());
    }

    private static String name(ItemLike item) {
        return item.asItem().builtInRegistryHolder().key().location().getPath();
    }

    private static NonNullList<Ingredient> list(ItemLike... items) {
        return ingredients(items);
    }

    private static NonNullList<Ingredient> ingredients(ItemLike... items) {
        NonNullList<Ingredient> list = NonNullList.create();
        for (ItemLike item : items) {
            list.add(Ingredient.of(item));
        }
        return list;
    }

    private static RecipeKeyMap keyMap() {
        return new RecipeKeyMap();
    }

    private static class RecipeKeyMap {
        private final java.util.Map<Character, Ingredient> ingredients = new LinkedHashMap<>();

        private RecipeKeyMap put(char key, ItemLike item) {
            this.ingredients.put(key, Ingredient.of(item));
            return this;
        }
    }

    /**
     * 26.1.2 数据生成入口。原版 {@link RecipeProvider} 不再直接实现 {@code DataProvider}，
     * 需要通过 Runner 延迟拿到注册表与 {@link RecipeOutput} 后创建实际 provider。
     */
    public static class Runner extends RecipeProvider.Runner {
        public Runner(PackOutput output, CompletableFuture<HolderLookup.Provider> registries) {
            super(output, registries);
        }

        @Override
        protected RecipeProvider createRecipeProvider(HolderLookup.Provider registries, RecipeOutput output) {
            return new AvaritiaRecipeProvider(registries, output);
        }

        @Override
        public @NotNull String getName() {
            return "Avaritia Recipes";
        }
    }
}
