package com.avaritia.init.data.provider.recipe;

import com.avaritia.Const;

import com.avaritia.common.crafting.recipe.CompressorRecipe;
import com.avaritia.common.crafting.recipe.EternalSingularityCraftRecipe;
import com.avaritia.common.crafting.recipe.ExtremeSmithingRecipe;
import com.avaritia.common.crafting.recipe.FullMatterClusterRecipe;
import com.avaritia.common.crafting.recipe.InfinityCatalystCraftRecipe;
import com.avaritia.common.crafting.recipe.NoConsumeCatalystShapedRecipe;
import com.avaritia.common.crafting.recipe.ShapedTableCraftingRecipe;
import com.avaritia.common.crafting.recipe.ShapelessTableCraftingRecipe;
import com.avaritia.common.ingredient.TagIngredient;
import com.avaritia.core.singularity.Singularity;
import com.avaritia.init.registry.ModBlocks;
import com.avaritia.init.registry.ModDataComponents;
import com.avaritia.init.registry.ModItems;
import com.avaritia.init.registry.ModSingularities;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.advancements.AdvancementRequirements;
import net.minecraft.advancements.AdvancementRewards;
import net.minecraft.advancements.Criterion;
import net.minecraft.advancements.criterion.RecipeUnlockedTrigger;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.data.recipes.RecipeProvider;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.ShapedRecipePattern;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.neoforge.common.crafting.CompoundIngredient;
import net.neoforged.neoforge.common.crafting.DataComponentIngredient;
import net.neoforged.neoforge.common.conditions.ICondition;
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
        legacyVanillaRecipes(output);
        craftingTableRecipes(output);
        legacyCoreRecipes(output);
        extremeSmithingRecipes(output);
        singularityRecipes(output);
        compressorRecipes(output);
    }

    private void vanillaRecipes(RecipeOutput output) {
        storage(output, ModItems.neutron_pile.get(), ModItems.neutron_nugget.get(), "neutron_nugget_from_piles", "neutron_pile_from_nuggets");
        storage(output, ModItems.neutron_nugget.get(), ModItems.neutron_ingot.get(), "neutron_ingot_from_nuggets", "neutron_nugget_from_ingots");
        storage(output, ModItems.infinity_nugget.get(), ModItems.infinity_ingot.get(), "infinity_ingot_from_nuggets", "infinity_nugget_from_ingots");
        storage(output, ModItems.crystal_matrix_ingot.get(), ModBlocks.crystal_matrix.get(), "crystal_matrix", "crystal_matrix_ingot");
        storage(output, ModItems.blaze_cube.get(), ModBlocks.blaze_cube_block.get(), "blaze_cube_block", "blaze_cube");
        storage(output, ModItems.diamond_lattice.get(), ModBlocks.diamond_lattice_block.get(), "diamond_lattice_block", "diamond_lattice_normal");
        storage(output, ModItems.star_fuel.get(), ModBlocks.star_fuel_block.get(), "star_fuel_block", "star_fuel_alternate");
        storage(output, ModItems.refined_coal.get(), ModBlocks.refined_coal_block.get(), "refined_coal_block", "refined_coal");

        this.shaped(RecipeCategory.MISC, ModItems.neutron_gear.get())
                .pattern(" n ")
                .pattern("ncn")
                .pattern(" n ")
                .define('n', ModItems.neutron_ingot.get())
                .define('c', ModItems.crystal_matrix_ingot.get())
                .unlockedBy("has_neutron_ingot", has(ModItems.neutron_ingot.get()))
                .save(output, key("neutron_gear"));

        this.shaped(RecipeCategory.TOOLS, ModBlocks.compressed_chest.get())
                .pattern("aaa")
                .pattern("aba")
                .pattern("aaa")
                .define('a', Blocks.CHEST)
                .define('b', ModItems.crystal_matrix_ingot.get())
                .unlockedBy("has_crystal_matrix_ingot", has(ModItems.crystal_matrix_ingot.get()))
                .save(output, key("compressed_chest"));

        this.shaped(RecipeCategory.MISC, ModItems.crystal_matrix_ingot.get())
                .pattern("xyx")
                .pattern("xyx")
                .define('x', ModItems.diamond_lattice.get())
                .define('y', Items.NETHER_STAR)
                .unlockedBy("has_diamond_lattice", has(ModItems.diamond_lattice.get()))
                .save(output, key("crystal_matrix_ingot_normal"));

        this.shaped(RecipeCategory.MISC, ModItems.diamond_lattice.get())
                .pattern("aba")
                .pattern("bab")
                .pattern("aba")
                .define('a', Items.DIAMOND)
                .define('b', Items.GOLD_INGOT)
                .unlockedBy("has_diamond", has(Items.DIAMOND))
                .save(output, key("diamond_lattice"));
    }

    private void legacyVanillaRecipes(RecipeOutput output) {
        storage(output, ModItems.neutron_ingot.get(), ModBlocks.neutron.get(), minecraftKey("neutron"), minecraftKey("neutron_ingot_from_neutron_block"));
        storage(output, ModItems.neutron_pile.get(), ModItems.neutron_nugget.get(), minecraftKey("neutron_pile_from_ingots"), minecraftKey("neutron_pile"));
        this.shapeless(RecipeCategory.MISC, ModItems.neutron_nugget.get(), 9)
                .requires(ModItems.neutron_ingot.get())
                .unlockedBy("has_neutron_ingot", has(ModItems.neutron_ingot.get()))
                .save(output, minecraftKey("neutron_nugget"));
        storage(output, ModItems.infinity_ingot.get(), ModBlocks.infinity.get(), minecraftKey("infinity_block_from_infinity_ingot"), minecraftKey("infinity_ingot"));
        storage(output, ModItems.infinity_nugget.get(), ModItems.infinity_ingot.get(), minecraftKey("infinity_ingot_from_infinity_nugget"), minecraftKey("infinity_nugget"));
        storage(output, Blocks.CRAFTING_TABLE, ModBlocks.compressed_crafting_table.get(), minecraftKey("compressed_crafting_table"), minecraftKey("crafting_table_from_compressed_crafting_table"));
        storage(output, ModBlocks.compressed_crafting_table.get(), ModBlocks.double_compressed_crafting_table.get(), minecraftKey("double_compressed_crafting_table"), minecraftKey("compressed_crafting_table_from_double_compressed_crafting_table"));

        extremeShaped(output, minecraftKey("reinforced_deepslate"), RecipeCategory.MISC, Blocks.REINFORCED_DEEPSLATE, 1,
                new String[]{"ada", "cbc", "aba"},
                keyMap().put('a', Blocks.OBSIDIAN).put('b', Blocks.DEEPSLATE).put('c', Blocks.DRIPSTONE_BLOCK).put('d', Blocks.SCULK_CATALYST),
                "has_sculk_catalyst", has(Blocks.SCULK_CATALYST));
        extremeShaped(output, minecraftKey("crying_obsidian"), RecipeCategory.MISC, Blocks.CRYING_OBSIDIAN, 1,
                new String[]{"bab", "aba", "bab"},
                keyMap().put('a', Blocks.AMETHYST_BLOCK).put('b', Blocks.OBSIDIAN),
                "has_obsidian", has(Blocks.OBSIDIAN));
        extremeShaped(output, minecraftKey("budding_amethyst"), RecipeCategory.MISC, Blocks.BUDDING_AMETHYST, 1,
                new String[]{"cac", "aba", "cac"},
                keyMap().put('a', ModBlocks.soul_farmland.get()).put('b', Blocks.AMETHYST_BLOCK).put('c', Items.AMETHYST_CLUSTER),
                "has_amethyst_cluster", has(Items.AMETHYST_CLUSTER));
        extremeShaped(output, minecraftKey("gilded_blackstone"), RecipeCategory.MISC, Blocks.GILDED_BLACKSTONE, 1,
                new String[]{" a ", "aba", " a "},
                keyMap().put('a', Items.GOLD_NUGGET).put('b', Blocks.BLACKSTONE),
                "has_blackstone", has(Blocks.BLACKSTONE));
        extremeShaped(output, minecraftKey("cobweb"), RecipeCategory.MISC, Blocks.COBWEB, 1,
                new String[]{"a a", " a ", "a a"},
                keyMap().put('a', Items.STRING),
                "has_string", has(Items.STRING));
        extremeShaped(output, minecraftKey("ancient_debris"), RecipeCategory.MISC, Blocks.ANCIENT_DEBRIS, 1,
                new String[]{" a ", "aba", " a "},
                keyMap().put('a', Items.NETHERITE_SCRAP).put('b', Blocks.CRYING_OBSIDIAN),
                "has_netherite_scrap", has(Items.NETHERITE_SCRAP));
        extremeShaped(output, minecraftKey("dragon_breath"), RecipeCategory.MISC, Items.DRAGON_BREATH, 1,
                new String[]{" a ", "a a", " a "},
                keyMap().put('a', Items.END_CRYSTAL),
                "has_end_crystal", has(Items.END_CRYSTAL));
        extremeShaped(output, minecraftKey("spore_blossom"), RecipeCategory.MISC, Blocks.SPORE_BLOSSOM, 1,
                new String[]{"aba", "bcb", "aba"},
                keyMap().put('a', Items.BONE_MEAL).put('b', Items.PINK_PETALS).put('c', Items.TORCHFLOWER_SEEDS),
                "has_torchflower_seeds", has(Items.TORCHFLOWER_SEEDS));
        extremeShaped(output, minecraftKey("dragon_head"), RecipeCategory.MISC, Items.DRAGON_HEAD, 1,
                new String[]{" a ", "bcb", " b "},
                keyMap().put('a', Items.WITHER_SKELETON_SKULL).put('b', Items.END_CRYSTAL).put('c', Items.DRAGON_EGG),
                "has_dragon_egg", has(Items.DRAGON_EGG));
        extremeShaped(output, minecraftKey("trident"), RecipeCategory.MISC, Items.TRIDENT, 1,
                new String[]{" ba", " cb", "d  "},
                keyMap().put('a', Items.NAUTILUS_SHELL).put('b', Items.PRISMARINE_CRYSTALS).put('c', Items.HEART_OF_THE_SEA).put('d', Items.PRISMARINE_SHARD),
                "has_heart_of_the_sea", has(Items.HEART_OF_THE_SEA));
        extremeShaped(output, minecraftKey("heart_of_the_sea"), RecipeCategory.MISC, Items.HEART_OF_THE_SEA, 1,
                new String[]{"bdb", "dcd", "bdb"},
                keyMap().put('b', Items.PRISMARINE_SHARD).put('c', Items.ENDER_EYE).put('d', Items.NAUTILUS_SHELL),
                "has_prismarine_shard", has(Items.PRISMARINE_SHARD));
        extremeShaped(output, minecraftKey("soul_sand"), RecipeCategory.MISC, Blocks.SOUL_SAND, 4, 1,
                new String[]{"ab ", "ba "},
                keyMap().put('a', Blocks.SOUL_SOIL).put('b', Blocks.SAND),
                "has_soul_soil", has(Blocks.SOUL_SOIL));
        extremeShapeless(output, minecraftKey("echo_shard"), RecipeCategory.MISC, Items.ECHO_SHARD, 1, 1,
                ingredients(Blocks.SCULK), "has_sculk", has(Blocks.SCULK));
        extremeShaped(output, minecraftKey("sculk_sensor"), RecipeCategory.MISC, Blocks.SCULK_SENSOR, 1,
                new String[]{"a a"},
                keyMap().put('a', Items.ECHO_SHARD),
                "has_echo_shard", has(Items.ECHO_SHARD));
        extremeShaped(output, minecraftKey("sculk_shrieker"), RecipeCategory.MISC, Blocks.SCULK_SHRIEKER, 1,
                new String[]{"cac", " b "},
                keyMap().put('a', Blocks.SCULK_CATALYST).put('b', Blocks.SCULK_SENSOR).put('c', Blocks.BONE_BLOCK),
                "has_sculk_sensor", has(Blocks.SCULK_SENSOR));
        extremeShaped(output, minecraftKey("sculk_catalyst"), RecipeCategory.MISC, Blocks.SCULK_CATALYST, 1,
                new String[]{"b", "c"},
                keyMap().put('b', Blocks.SCULK).put('c', Blocks.BONE_BLOCK),
                "has_sculk", has(Blocks.SCULK));
        extremeShaped(output, minecraftKey("sculk"), RecipeCategory.MISC, Blocks.SCULK, 1,
                new String[]{"aa", "aa"},
                keyMap().put('a', Items.ECHO_SHARD),
                "has_echo_shard", has(Items.ECHO_SHARD));
        extremeShapeless(output, minecraftKey("torchflower_seeds"), RecipeCategory.MISC, Items.TORCHFLOWER_SEEDS, 2, 1,
                ingredients(Items.TORCHFLOWER), "has_torchflower", has(Items.TORCHFLOWER));
        extremeShapeless(output, minecraftKey("pitcher_pod"), RecipeCategory.MISC, Items.PITCHER_POD, 2, 1,
                ingredients(Items.PITCHER_PLANT), "has_pitcher_plant", has(Items.PITCHER_PLANT));
        extremeShaped(output, minecraftKey("sniffer_egg"), RecipeCategory.MISC, Items.SNIFFER_EGG, 1,
                new String[]{"aaa", "dbd", "ccc"},
                keyMap().put('a', Items.BRICK).put('b', Items.EGG).put('c', Items.GRAVEL).put('d', Items.SAND),
                "has_egg", has(Items.EGG));
        extremeShaped(output, minecraftKey("end_portal_frame"), RecipeCategory.MISC, Blocks.END_PORTAL_FRAME, 2, 1,
                new String[]{"     ", "fghgf", "ecace", "dcccd", "bbbbb"},
                keyMap()
                        .put('a', Items.END_CRYSTAL)
                        .put('b', Blocks.END_STONE_BRICKS)
                        .put('c', Blocks.END_STONE)
                        .put('d', Blocks.END_STONE_BRICK_WALL)
                        .put('e', Blocks.EMERALD_BLOCK)
                        .put('f', ModItems.crystal_matrix_ingot.get())
                        .put('g', Items.ENDER_EYE)
                        .put('h', Blocks.SCULK_SHRIEKER),
                "has_crystal_matrix_ingot", has(ModItems.crystal_matrix_ingot.get()));
        extremeShaped(output, minecraftKey("dragon_egg"), RecipeCategory.MISC, Items.DRAGON_EGG, 3, 1,
                new String[]{"  ggg  ", " gfefg ", "gfbdbfg", "gecaceg", "gfbcbfg", "ggfefgg", " ggggg "},
                keyMap()
                        .put('a', Items.EGG)
                        .put('b', Items.DRAGON_BREATH)
                        .put('c', Items.EXPERIENCE_BOTTLE)
                        .put('d', Items.DRAGON_HEAD)
                        .put('e', Blocks.ENDER_CHEST)
                        .put('f', Items.END_CRYSTAL)
                        .put('g', ModItems.neutron_pile.get()),
                "has_neutron_pile", has(ModItems.neutron_pile.get()));
        musicDisc(output, Items.MUSIC_DISC_13, Items.YELLOW_DYE, "music_disc_13");
        musicDisc(output, Items.MUSIC_DISC_CAT, Items.GREEN_DYE, "music_disc_cat");
        musicDisc(output, Items.MUSIC_DISC_BLOCKS, Items.ORANGE_DYE, "music_disc_blocks");
        musicDisc(output, Items.MUSIC_DISC_CHIRP, Items.RED_DYE, "music_disc_chirp");
        musicDisc(output, Items.MUSIC_DISC_FAR, Items.LIME_DYE, "music_disc_far");
        musicDisc(output, Items.MUSIC_DISC_MALL, Items.PURPLE_DYE, "music_disc_mall");
        musicDisc(output, Items.MUSIC_DISC_MELLOHI, Items.MAGENTA_DYE, "music_disc_mellohi");
        musicDisc(output, Items.MUSIC_DISC_STAL, Items.BLACK_DYE, "music_disc_stal");
        musicDisc(output, Items.MUSIC_DISC_STRAD, Items.WHITE_DYE, "music_disc_strad");
        musicDisc(output, Items.MUSIC_DISC_WARD, Items.CYAN_DYE, "music_disc_ward");
        extremeShaped(output, minecraftKey("music_disc_11"), RecipeCategory.MISC, Items.MUSIC_DISC_11, 1,
                new String[]{"a a", " a ", "a a"},
                keyMap().put('a', ModItems.record_fragment.get()),
                "has_record_fragment", has(ModItems.record_fragment.get()));
        musicDisc(output, Items.MUSIC_DISC_WAIT, Items.LIGHT_BLUE_DYE, "music_disc_wait");
        musicDisc(output, Items.MUSIC_DISC_PIGSTEP, Items.NETHER_GOLD_ORE, "music_disc_pigstep");
        musicDisc(output, Items.MUSIC_DISC_OTHERSIDE, Items.GRASS_BLOCK, "music_disc_otherside");
        musicDisc(output, Items.MUSIC_DISC_5, Items.ECHO_SHARD, "music_disc_5");
        musicDisc(output, Items.MUSIC_DISC_RELIC, Blocks.WAXED_WEATHERED_COPPER, "music_disc_relic");
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

    private void legacyCoreRecipes(RecipeOutput output) {
        noConsumeExtremeShaped(output, RecipeCategory.MISC, Blocks.ANCIENT_DEBRIS, 8, 1,
                new String[]{"cbc", "bab", "cbc"},
                keyMap().put('a', ModItems.infinity_catalyst.get()).put('b', Items.NETHERITE_SCRAP).put('c', Items.DIAMOND),
                "has_infinity_catalyst", has(ModItems.infinity_catalyst.get()), "ancient_debris_eight");

        extremeShaped(output, RecipeCategory.MISC, ModItems.neutron_ring.get(), 3,
                new String[]{"  aaa  ", " cbbbc ", "ab d ba", "abdedba", "ab d ba", " cbbbc ", "  aaa  "},
                keyMap()
                        .put('a', ModItems.neutron_ingot.get())
                        .put('b', ModItems.crystal_matrix_ingot.get())
                        .put('c', ModBlocks.diamond_lattice_block.get())
                        .put('d', ModItems.endest_pearl.get())
                        .put('e', ModItems.infinity_catalyst.get()),
                "has_end_crafting_table", has(ModBlocks.end_crafting_table.get()), "neutron_ring");
        extremeShaped(output, RecipeCategory.MISC, ModItems.record_fragment.get(), 4, 3,
                new String[]{"       ", "       ", "   a   ", "  aba  ", "   a   ", "       ", "       "},
                keyMap().put('a', ModItems.neutron_pile.get()).put('b', tagIngredient("music_discs")),
                "has_music_disc", has(itemTag("music_discs")), "record_fragment");
        extremeShaped(output, RecipeCategory.MISC, ModItems.side_config_card.get(), 1,
                new String[]{"iii", "iai", " b "},
                keyMap().put('i', Items.IRON_INGOT).put('a', ModItems.diamond_lattice.get()).put('b', Items.GOLD_INGOT),
                "has_diamond_lattice", has(ModItems.diamond_lattice.get()), "side_config_card");
        extremeShaped(output, RecipeCategory.TOOLS, ModItems.upgrade_smithing_template.get(), 1, 4,
                new String[]{" aaabaaa ", " accecca ", " acefeca ", "dijkelmnd", "dogphqgrd", "dstufvwxd", " acdfdca ", " accecca ", " aaabaaa "},
                keyMap()
                        .put('a', ModItems.crystal_matrix_ingot.get())
                        .put('b', ModBlocks.crystal_matrix.get())
                        .put('c', ModItems.neutron_ingot.get())
                        .put('d', ModItems.neutron_pile.get())
                        .put('e', ModItems.infinity_nugget.get())
                        .put('f', ModItems.infinity_ingot.get())
                        .put('g', ModItems.infinity_catalyst.get())
                        .put('h', Items.NETHERITE_UPGRADE_SMITHING_TEMPLATE)
                        .put('i', Items.SENTRY_ARMOR_TRIM_SMITHING_TEMPLATE)
                        .put('j', Items.DUNE_ARMOR_TRIM_SMITHING_TEMPLATE)
                        .put('k', Items.COAST_ARMOR_TRIM_SMITHING_TEMPLATE)
                        .put('l', Items.WILD_ARMOR_TRIM_SMITHING_TEMPLATE)
                        .put('m', Items.WARD_ARMOR_TRIM_SMITHING_TEMPLATE)
                        .put('n', Items.EYE_ARMOR_TRIM_SMITHING_TEMPLATE)
                        .put('o', Items.VEX_ARMOR_TRIM_SMITHING_TEMPLATE)
                        .put('p', Items.TIDE_ARMOR_TRIM_SMITHING_TEMPLATE)
                        .put('q', Items.SNOUT_ARMOR_TRIM_SMITHING_TEMPLATE)
                        .put('r', Items.RIB_ARMOR_TRIM_SMITHING_TEMPLATE)
                        .put('s', Items.SPIRE_ARMOR_TRIM_SMITHING_TEMPLATE)
                        .put('t', Items.WAYFINDER_ARMOR_TRIM_SMITHING_TEMPLATE)
                        .put('u', Items.SHAPER_ARMOR_TRIM_SMITHING_TEMPLATE)
                        .put('v', Items.SILENCE_ARMOR_TRIM_SMITHING_TEMPLATE)
                        .put('w', Items.RAISER_ARMOR_TRIM_SMITHING_TEMPLATE)
                        .put('x', Items.HOST_ARMOR_TRIM_SMITHING_TEMPLATE),
                "has_neutron_ingot", has(ModItems.neutron_ingot.get()), "upgrade_smithing_template");
        extremeShapeless(output, key("netherite_ingot_too"), RecipeCategory.MISC, Items.NETHERITE_INGOT, 1, 2,
                ingredients(Items.NETHERITE_SCRAP, Items.NETHERITE_SCRAP, Items.GOLD_INGOT, Items.GOLD_INGOT),
                "has_netherite_scrap", has(Items.NETHERITE_SCRAP));

        extremeShaped(output, RecipeCategory.TOOLS, ModItems.endest_pearl.get(), 1, 4,
                new String[]{"   EEE   ", " EEPPPEE ", " EPPPPPE ", "EPPPNPPPE", "EPPNSNPPE", "EPPPNPPPE", " EPPPPPE ", " EEPPPEE ", "   EEE   "},
                keyMap()
                        .put('E', tagIngredient("end_stones"))
                        .put('P', tagIngredient("ender_pearls"))
                        .put('S', tagIngredient("nether_stars"))
                        .put('N', ModItems.neutron_ingot.get()),
                "has_neutron_ingot", has(ModItems.neutron_ingot.get()), "endest_pearl");
        extremeShaped(output, RecipeCategory.TOOLS, ModItems.enhancement_core.get(), 1, 4,
                new String[]{"   PPP   ", " NPCCCPN ", " PABBBAP ", "PCBBXBBCP", "PCBXEXBCP", "PCBBXBBCP", " PABBBAP ", " NPCCCPN ", "   PPP   "},
                keyMap()
                        .put('E', ModItems.endest_pearl.get())
                        .put('B', ModItems.infinity_nugget.get())
                        .put('C', ModItems.crystal_matrix_ingot.get())
                        .put('X', ModItems.infinity_catalyst.get())
                        .put('N', ModItems.neutron_ingot.get())
                        .put('P', ModItems.neutron_pile.get())
                        .put('A', ModBlocks.crystal_matrix.get()),
                "has_endest_pearl", has(ModItems.endest_pearl.get()), "enhancement_core");
        extremeShaped(output, RecipeCategory.MISC, ModItems.star_fuel.get(), 4, 4,
                new String[]{"         ", "  aaaaa  ", " abbcbba ", " abaaaba ", " acadaca ", " abaaaba ", " abbcbba ", "  aaaaa  ", "         "},
                keyMap().put('a', Blocks.COAL_BLOCK).put('b', Blocks.MAGMA_BLOCK).put('c', Items.LAVA_BUCKET).put('d', ModItems.eternal_singularity.get()),
                "has_eternal_singularity", has(ModItems.eternal_singularity.get()), "star_fuel");
        extremeShaped(output, RecipeCategory.TOOLS, ModItems.infinity_ingot.get(), 1, 4,
                new String[]{"NNNNNNNNN", "NCXXCXXCN", "NXCCXCCXN", "NCXXCXXCN", "NNNNNNNNN"},
                keyMap().put('N', ModItems.neutron_ingot.get()).put('C', ModItems.crystal_matrix_ingot.get()).put('X', ModItems.infinity_catalyst.get()),
                "has_neutron_ingot", has(ModItems.neutron_ingot.get()), "infinity_ingot");

        missingToolRecipes(output);
        machineUpgradeRecipes(output);
        extraInfinityRecipes(output);
    }

    private void missingToolRecipes(RecipeOutput output) {
        RecipeKeyMap blazeKeys = keyMap()
                .put('A', Blocks.BONE_BLOCK)
                .put('B', ModItems.diamond_lattice.get())
                .put('C', ModItems.blaze_cube.get())
                .put('D', Items.BLAZE_POWDER)
                .put('E', Blocks.SOUL_SOIL);
        extremeShaped(output, RecipeCategory.TOOLS, ModItems.blaze_axe.get(), 2,
                new String[]{"  DDA", " DCA ", " DACD", " E DD", "B    "},
                blazeKeys, "has_blaze_cube", has(ModItems.blaze_cube.get()), "blaze_axe");
        extremeShaped(output, RecipeCategory.TOOLS, ModItems.blaze_hoe.get(), 2,
                new String[]{"DDCCA", " DDAC", "  A D", " E   ", "B    "},
                blazeKeys, "has_blaze_cube", has(ModItems.blaze_cube.get()), "blaze_hoe");
        extremeShaped(output, RecipeCategory.TOOLS, ModItems.blaze_shovel.get(), 2,
                new String[]{"   DC", "  DCD", "  AD ", " E   ", "B    "},
                blazeKeys, "has_blaze_cube", has(ModItems.blaze_cube.get()), "blaze_shovel");
        extremeShaped(output, RecipeCategory.TOOLS, ModItems.blaze_bow.get(), 2,
                new String[]{" ABBA", "ACDDC", "BD E ", "BDE  ", "AC   "},
                blazeKeys, "has_blaze_cube", has(ModItems.blaze_cube.get()), "blaze_bow");

        extremeShaped(output, RecipeCategory.TOOLS, ModItems.crystal_hoe.get(), 3,
                new String[]{"CAAAAA ", " CCCA A", "     AA", "   B CA", "  B   C", " B     ", "A      "},
                keyMap().put('A', ModBlocks.crystal_matrix.get()).put('B', ModItems.neutron_ingot.get()).put('C', ModItems.crystal_matrix_ingot.get()),
                "has_crystal_matrix_ingot", has(ModItems.crystal_matrix_ingot.get()), "crystal_hoe");
        extremeShaped(output, RecipeCategory.TOOLS, ModItems.crystal_axe.get(), 3,
                new String[]{"  CCC  ", " CAA D ", " CAA   ", " C  AC ", "  B CC ", " B     ", "A      "},
                keyMap().put('A', ModBlocks.crystal_matrix.get()).put('B', ModItems.neutron_ingot.get()).put('C', ModItems.crystal_matrix_ingot.get()).put('D', ModBlocks.neutron.get()),
                "has_crystal_matrix_ingot", has(ModItems.crystal_matrix_ingot.get()), "crystal_axe");
        extremeShaped(output, RecipeCategory.TOOLS, ModItems.crystal_shovel.get(), 3,
                new String[]{"    CCC", "   CCCC", "    CCC", "   B C ", "  B    ", " B     ", "A      "},
                keyMap().put('A', ModBlocks.crystal_matrix.get()).put('B', ModItems.neutron_ingot.get()).put('C', ModItems.crystal_matrix_ingot.get()),
                "has_crystal_matrix_ingot", has(ModItems.crystal_matrix_ingot.get()), "crystal_shovel");
        extremeShaped(output, RecipeCategory.TOOLS, ModItems.crystal_bow.get(), 3,
                new String[]{"  CDAAC", "   CCCD", "C A  B ", "DC  B  ", "AC B   ", "ACB    ", "CD     "},
                keyMap().put('A', ModBlocks.crystal_matrix.get()).put('B', ModItems.neutron_ingot.get()).put('C', ModItems.crystal_matrix_ingot.get()).put('D', ModItems.diamond_lattice.get()),
                "has_crystal_matrix_ingot", has(ModItems.crystal_matrix_ingot.get()), "crystal_bow");
    }

    private void machineUpgradeRecipes(RecipeOutput output) {
        extremeShaped(output, RecipeCategory.TOOLS, ModBlocks.neutron_collector.get(), 1, 4,
                new String[]{"IIQQQQQII", "I QQQQQ I", "I  RRR  I", "C RRRRR C", "I RRCRR I", "C RRRRR C", "I  RRR  I", "I       I", "IIICICIII"},
                keyMap().put('I', Blocks.IRON_BLOCK).put('R', Blocks.REDSTONE_BLOCK).put('C', ModItems.crystal_matrix_ingot.get()).put('Q', Blocks.QUARTZ_BLOCK),
                "has_infinity_catalyst", has(ModItems.infinity_catalyst.get()), "neutron_collector");
        extremeShaped(output, RecipeCategory.TOOLS, ModBlocks.neutron_compressor.get(), 1, 4,
                new String[]{"IIIHHHIII", "C N   N C", "I N   N I", "C N   N C", "RNN O NNR", "C N   N C", "I N   N I", "C N   N C", "IIICICIII"},
                keyMap().put('I', Blocks.IRON_BLOCK).put('R', Blocks.REDSTONE_BLOCK).put('C', ModItems.crystal_matrix_ingot.get()).put('O', ModBlocks.neutron.get()).put('H', Blocks.HOPPER).put('N', ModItems.neutron_ingot.get()),
                "has_neutron_ingot", has(ModItems.neutron_ingot.get()), "neutron_compressor");

        extremeShaped(output, RecipeCategory.TOOLS, ModBlocks.dense_neutron_collector.get(), 1, 4,
                new String[]{"AAC   CAA", "AB     BA", "C DEEED C", "  EGGGE  ", "  EGFGE  ", "  EGGGE  ", "C DEEED C", "AB     BA", "AAC   CAA"},
                denseMachineKeys(ModBlocks.neutron_collector.get()), "has_neutron_collector", has(ModBlocks.neutron_collector.get()), "dense_neutron_collector");
        extremeShaped(output, RecipeCategory.TOOLS, ModBlocks.dense_neutron_compressor.get(), 1, 4,
                new String[]{"AAC   CAA", "AB     BA", "C DEEED C", "  EGGGE  ", "  EGFGE  ", "  EGGGE  ", "C DEEED C", "AB     BA", "AAC   CAA"},
                denseMachineKeys(ModBlocks.neutron_compressor.get()), "has_neutron_compressor", has(ModBlocks.neutron_compressor.get()), "dense_neutron_compressor");

        extremeShaped(output, RecipeCategory.TOOLS, ModBlocks.denser_neutron_collector.get(), 1, 4,
                new String[]{"ABB F BBA", "BCC   CCB", "BCDEEEDCB", "  EGGGE  ", "F EGEGE F", "  EGGGE  ", "BCDEEEDCB", "BCC   CCB", "ABB F BBA"},
                denserMachineKeys(ModBlocks.dense_neutron_collector.get()), "has_dense_neutron_collector", has(ModBlocks.dense_neutron_collector.get()), "denser_neutron_collector");
        extremeShaped(output, RecipeCategory.TOOLS, ModBlocks.denser_neutron_compressor.get(), 1, 4,
                new String[]{"ABB F BBA", "BCC   CCB", "BCDEEEDCB", "  EGGGE  ", "F EGEGE F", "  EGGGE  ", "BCDEEEDCB", "BCC   CCB", "ABB F BBA"},
                denserMachineKeys(ModBlocks.dense_neutron_compressor.get()), "has_dense_neutron_compressor", has(ModBlocks.dense_neutron_compressor.get()), "denser_neutron_compressor");

        extremeShaped(output, RecipeCategory.TOOLS, ModBlocks.densest_neutron_collector.get(), 1, 4,
                new String[]{"CC     CC", "C  BBB  C", "  AAAAA  ", " BAXXXAB ", " BAXYXAB ", " BAXXXAB ", "  AAAAA  ", "C  BBB  C", "CC     CC"},
                densestMachineKeys(ModBlocks.denser_neutron_collector.get()), "has_denser_neutron_collector", has(ModBlocks.denser_neutron_collector.get()), "densest_neutron_collector");
        extremeShaped(output, RecipeCategory.TOOLS, ModBlocks.densest_neutron_compressor.get(), 1, 4,
                new String[]{"CC     CC", "C  BBB  C", "  AAAAA  ", " BAXXXAB ", " BAXYXAB ", " BAXXXAB ", "  AAAAA  ", "C  BBB  C", "CC     CC"},
                densestMachineKeys(ModBlocks.denser_neutron_compressor.get()), "has_denser_neutron_compressor", has(ModBlocks.denser_neutron_compressor.get()), "densest_neutron_compressor");
    }

    private void extraInfinityRecipes(RecipeOutput output) {
        extremeShaped(output, RecipeCategory.TOOLS, ModItems.infinity_umbrella.get(), 1, 4,
                new String[]{" IBNND  C", " INENNNA ", " XINNNBN ", "  INNFNND", "   INNNNN", "   NINNGN", "  N  IINB", " B    XII", "ACC      "},
                keyMap()
                        .put('I', ModItems.infinity_ingot.get())
                        .put('N', ModItems.neutron_ingot.get())
                        .put('X', ModItems.infinity_nugget.get())
                        .put('A', ModBlocks.crystal_matrix.get())
                        .put('B', ModBlocks.neutron.get())
                        .put('C', ModItems.crystal_matrix_ingot.get())
                        .put('D', ModItems.neutron_nugget.get())
                        .put('E', Items.FLINT_AND_STEEL)
                        .put('F', Items.WATER_BUCKET)
                        .put('G', Items.TRIDENT),
                "has_infinity_ingot", has(ModItems.infinity_ingot.get()), "infinity_umbrella");
        extremeShaped(output, RecipeCategory.TOOLS, ModItems.infinity_shield.get(), 1, 4,
                new String[]{" MNNCNNM ", " NCCDCCN ", " NSIIISN ", " NIAAAIN ", " NIAXAIN ", " NIAAAIN ", " NSIIISN ", " NCCDCCN ", " MNNCNNM "},
                keyMap()
                        .put('I', ModItems.infinity_ingot.get())
                        .put('C', ModItems.crystal_matrix_ingot.get())
                        .put('X', ModItems.infinity_catalyst.get())
                        .put('A', ModBlocks.crystal_matrix.get())
                        .put('D', ModItems.diamond_lattice.get())
                        .put('M', ModBlocks.neutron.get())
                        .put('N', ModItems.neutron_ingot.get())
                        .put('S', Items.SHIELD),
                "has_infinity_ingot", has(ModItems.infinity_ingot.get()), "infinity_shield");
        extremeShaped(output, RecipeCategory.TOOLS, ModItems.infinity_mace.get(), 1, 4,
                new String[]{"     CC C", "    CIIC ", "    CIMIC", "     CIIC", "    N CC ", "   D     ", "  N      ", " N       ", "C        "},
                keyMap()
                        .put('I', ModItems.infinity_nugget.get())
                        .put('C', ModItems.crystal_matrix_ingot.get())
                        .put('D', Items.BREEZE_ROD)
                        .put('M', Items.HEAVY_CORE)
                        .put('N', ModItems.neutron_ingot.get()),
                "has_infinity_ingot", has(ModItems.infinity_ingot.get()), "infinity_mace");

        extremeSmithing(output, "infinity_totem", Items.TOTEM_OF_UNDYING, ModItems.infinity_totem.get(),
                Ingredient.of(Items.EXPERIENCE_BOTTLE), Ingredient.of(ModItems.enhancement_core.get()), Ingredient.of(Items.BEACON));
        extremeSmithing(output, "endless_cake", Items.CAKE, ModBlocks.endless_cake.get(),
                Ingredient.of(Items.GOLDEN_CARROT), Ingredient.of(ModItems.enhancement_core.get()), Ingredient.of(Items.DRAGON_EGG));
        extremeSmithing(output, "neutron_horse_armor", Items.DIAMOND_HORSE_ARMOR, ModItems.neutron_horse_armor.get(),
                DataComponentIngredient.of(false, DataComponents.POTION_CONTENTS, new PotionContents(Potions.SWIFTNESS), Items.POTION),
                Ingredient.of(ModItems.enhancement_core.get()),
                Ingredient.of(Items.BLUE_ICE));
        extremeSmithing(output, "infinity_bucket", Items.BUCKET, ModItems.infinity_bucket.get(),
                Ingredient.of(Items.LAVA_BUCKET), Ingredient.of(ModItems.enhancement_core.get()), Ingredient.of(Items.POWDER_SNOW_BUCKET));
        extremeSmithing(output, "infinity_clock", Items.CLOCK, ModItems.infinity_clock.get(),
                Ingredient.of(Items.ENCHANTED_GOLDEN_APPLE), Ingredient.of(ModItems.enhancement_core.get()), Ingredient.of(ModItems.eternal_singularity.get()));
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
                template(ModItems.infinity_elytra.get())), RecipeCategory.MISC,
                "has_upgrade_smithing_template", has(ModItems.upgrade_smithing_template.get()));

        save(output, "infinity_chest", new ExtremeSmithingRecipe(
                Ingredient.of(ModItems.upgrade_smithing_template.get()),
                Ingredient.of(ModBlocks.compressed_chest.get()),
                CompoundIngredient.of(Ingredient.of(ModBlocks.neutron.get()), Ingredient.of(ModItems.enhancement_core.get()), Ingredient.of(ModBlocks.infinity.get())),
                template(ModBlocks.infinity_chest.get())), RecipeCategory.MISC,
                "has_upgrade_smithing_template", has(ModItems.upgrade_smithing_template.get()));

        save(output, "extreme_anvil", new ExtremeSmithingRecipe(
                Ingredient.of(ModItems.upgrade_smithing_template.get()),
                Ingredient.of(Items.ANVIL),
                CompoundIngredient.of(Ingredient.of(ModItems.full_matter_cluster.get()), Ingredient.of(ModItems.enhancement_core.get()), Ingredient.of(ModBlocks.neutron.get())),
                template(ModBlocks.extreme_anvil.get())), RecipeCategory.MISC,
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
        save(output, "bedrock_from_deepslate", new CompressorRecipe(Ingredient.of(Blocks.DEEPSLATE), template(Blocks.BEDROCK), 10000, 240),
                RecipeCategory.MISC, "has_deepslate", has(Blocks.DEEPSLATE));
        save(output, "compressor_matter_cluster", new CompressorRecipe(Ingredient.of(ModItems.neutron_ingot.get()), template(ModItems.full_matter_cluster.get()), 4096, 240),
                RecipeCategory.MISC, "has_neutron_ingot", has(ModItems.neutron_ingot.get()));
        for (Singularity singularity : ModSingularities.getDefaults()) {
            // 条件标签奇点在 datagen 阶段可能判定为空，但仍需要输出带条件的配方 JSON。
            if (!singularity.isEnabled() || !singularity.isRecipeEnabled() || singularity.getIngredient() == null) {
                continue;
            }
            saveWithConditions(output, key(singularity.getRegistryName().getPath() + "_singularity"),
                    new CompressorRecipe(singularity.getIngredient(), singularityTemplate(singularity),
                            singularity.getCount(), singularity.getTimeCost()),
                    RecipeCategory.MISC, "has_neutron_compressor", has(ModBlocks.neutron_compressor.get()),
                    singularity.getConditions().toArray(ICondition[]::new));
        }
    }

    private void storage(RecipeOutput output, ItemLike small, ItemLike large, String packingName, String unpackingName) {
        storage(output, small, large, key(packingName), key(unpackingName));
    }

    private void storage(RecipeOutput output, ItemLike small, ItemLike large, ResourceKey<Recipe<?>> packingKey, ResourceKey<Recipe<?>> unpackingKey) {
        this.shaped(RecipeCategory.MISC, large)
                .pattern("###")
                .pattern("###")
                .pattern("###")
                .define('#', small)
                .unlockedBy("has_" + name(small), has(small))
                .save(output, packingKey);
        this.shapeless(RecipeCategory.MISC, small, 9)
                .requires(large)
                .unlockedBy("has_" + name(large), has(large))
                .save(output, unpackingKey);
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
                template(result)), RecipeCategory.TOOLS,
                "has_upgrade_smithing_template", has(ModItems.upgrade_smithing_template.get()));
    }

    private void extremeSmithing(RecipeOutput output, String name, ItemLike base, ItemLike result, Ingredient... additions) {
        save(output, name, new ExtremeSmithingRecipe(
                Ingredient.of(ModItems.upgrade_smithing_template.get()),
                Ingredient.of(base),
                CompoundIngredient.of(additions),
                template(result)), RecipeCategory.MISC,
                "has_upgrade_smithing_template", has(ModItems.upgrade_smithing_template.get()));
    }

    private void extremeShaped(RecipeOutput output, RecipeCategory category, ItemLike result, int tier, String[] pattern, RecipeKeyMap keys,
                               String criterionName, Criterion<?> criterion, String name) {
        extremeShaped(output, key(name), category, result, 1, tier, pattern, keys, criterionName, criterion);
    }

    private void extremeShaped(RecipeOutput output, RecipeCategory category, ItemLike result, int count, int tier, String[] pattern, RecipeKeyMap keys,
                               String criterionName, Criterion<?> criterion, String name) {
        extremeShaped(output, key(name), category, result, count, tier, pattern, keys, criterionName, criterion);
    }

    private void extremeShaped(RecipeOutput output, ResourceKey<Recipe<?>> key, RecipeCategory category, ItemLike result, int tier, String[] pattern, RecipeKeyMap keys,
                               String criterionName, Criterion<?> criterion) {
        extremeShaped(output, key, category, result, 1, tier, pattern, keys, criterionName, criterion);
    }

    private void extremeShaped(RecipeOutput output, ResourceKey<Recipe<?>> key, RecipeCategory category, ItemLike result, int count, int tier, String[] pattern, RecipeKeyMap keys,
                               String criterionName, Criterion<?> criterion) {
        ShapedRecipePattern shapedPattern = ShapedRecipePattern.of(usedIngredients(pattern, keys), normalizedPattern(pattern));
        save(output, key, new ShapedTableCraftingRecipe(shapedPattern, template(result, count), tier, false), category, criterionName, criterion);
    }

    private void noConsumeExtremeShaped(RecipeOutput output, RecipeCategory category, ItemLike result, int count, int tier, String[] pattern, RecipeKeyMap keys,
                                        String criterionName, Criterion<?> criterion, String name) {
        ShapedRecipePattern shapedPattern = ShapedRecipePattern.of(usedIngredients(pattern, keys), normalizedPattern(pattern));
        save(output, name, new NoConsumeCatalystShapedRecipe(shapedPattern, template(result, count), tier), category, criterionName, criterion);
    }

    private void extremeShapeless(RecipeOutput output, RecipeCategory category, ItemLike result, int tier, NonNullList<Ingredient> ingredients,
                                  String criterionName, Criterion<?> criterion, String name) {
        extremeShapeless(output, key(name), category, result, 1, tier, ingredients, criterionName, criterion);
    }

    private void extremeShapeless(RecipeOutput output, ResourceKey<Recipe<?>> key, RecipeCategory category, ItemLike result, int count, int tier, NonNullList<Ingredient> ingredients,
                                  String criterionName, Criterion<?> criterion) {
        save(output, key, new ShapelessTableCraftingRecipe(ingredients, template(result, count), tier), category, criterionName, criterion);
    }

    private void save(RecipeOutput output, String name, Recipe<?> recipe, RecipeCategory category, String criterionName, Criterion<?> criterion) {
        save(output, key(name), recipe, category, criterionName, criterion);
    }

    private void save(RecipeOutput output, ResourceKey<Recipe<?>> key, Recipe<?> recipe, RecipeCategory category, String criterionName, Criterion<?> criterion) {
        saveWithConditions(output, key, recipe, category, criterionName, criterion);
    }

    private void saveWithConditions(RecipeOutput output, ResourceKey<Recipe<?>> key, Recipe<?> recipe, RecipeCategory category,
                                    String criterionName, Criterion<?> criterion, ICondition... conditions) {
        Advancement.Builder advancementBuilder = output.advancement()
                .addCriterion("has_the_recipe", RecipeUnlockedTrigger.unlocked(key))
                .rewards(AdvancementRewards.Builder.recipe(key))
                .requirements(AdvancementRequirements.Strategy.OR)
                .addCriterion(criterionName, criterion);
        AdvancementHolder advancement = advancementBuilder.build(key.identifier().withPrefix("recipes/" + category.getFolderName() + "/"));
        output.accept(key, recipe, advancement, conditions);
    }

    private static ResourceKey<Recipe<?>> key(String name) {
        return ResourceKey.create(Registries.RECIPE, Identifier.fromNamespaceAndPath(Const.MOD_ID, name));
    }

    private static ResourceKey<Recipe<?>> minecraftKey(String name) {
        return ResourceKey.create(Registries.RECIPE, Identifier.fromNamespaceAndPath("minecraft", name));
    }

    private static String name(ItemLike item) {
        return item.asItem().builtInRegistryHolder().key().identifier().getPath();
    }

    private static ItemStackTemplate template(ItemLike item) {
        return template(item, 1);
    }

    private static ItemStackTemplate template(ItemLike item, int count) {
        return new ItemStackTemplate(item.asItem(), count);
    }

    /**
     * 直接写出带组件的物品模板，避免 datagen 阶段构造真实 ItemStack 时组件尚未绑定。
     */
    private static ItemStackTemplate singularityTemplate(Singularity singularity) {
        DataComponentPatch components = DataComponentPatch.builder()
                .set(ModDataComponents.SINGULARITY_ID.get(), singularity.getRegistryName())
                .build();
        return new ItemStackTemplate(ModItems.singularity.get(), components);
    }

    private static List<String> normalizedPattern(String[] pattern) {
        int width = 0;
        for (String row : pattern) {
            width = Math.max(width, row.length());
        }

        List<String> rows = new java.util.ArrayList<>(pattern.length);
        for (String row : pattern) {
            rows.add(row.length() == width ? row : row + " ".repeat(width - row.length()));
        }
        return rows;
    }

    private static java.util.Map<Character, Ingredient> usedIngredients(String[] pattern, RecipeKeyMap keys) {
        java.util.Map<Character, Ingredient> ingredients = new LinkedHashMap<>();
        keys.ingredients.forEach((key, ingredient) -> {
            if (usesKey(pattern, key)) {
                ingredients.put(key, ingredient);
            }
        });
        return ingredients;
    }

    private static boolean usesKey(String[] pattern, char key) {
        for (String row : pattern) {
            if (row.indexOf(key) >= 0) {
                return true;
            }
        }
        return false;
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

    private static NonNullList<Ingredient> ingredients(Ingredient... ingredients) {
        NonNullList<Ingredient> list = NonNullList.create();
        for (Ingredient ingredient : ingredients) {
            list.add(ingredient);
        }
        return list;
    }

    private static RecipeKeyMap keyMap() {
        return new RecipeKeyMap();
    }

    private static TagKey<Item> itemTag(String path) {
        return ItemTags.create(Identifier.fromNamespaceAndPath("c", path));
    }

    private static Ingredient tagIngredient(String path) {
        return new TagIngredient(itemTag(path)).toVanilla();
    }

    private static RecipeKeyMap denseMachineKeys(ItemLike baseMachine) {
        return keyMap()
                .put('A', Items.ENDER_PEARL)
                .put('B', Items.NETHER_STAR)
                .put('C', ModItems.diamond_lattice.get())
                .put('D', ModItems.neutron_ingot.get())
                .put('E', Blocks.EMERALD_BLOCK)
                .put('F', ModItems.endest_pearl.get())
                .put('G', baseMachine);
    }

    private static RecipeKeyMap denserMachineKeys(ItemLike baseMachine) {
        return keyMap()
                .put('A', ModItems.neutron_gear.get())
                .put('B', ModItems.neutron_pile.get())
                .put('C', ModItems.blaze_cube.get())
                .put('D', singularityIngredient(ModSingularities.GOLD))
                .put('E', ModBlocks.blaze_cube_block.get())
                .put('F', Blocks.GOLD_BLOCK)
                .put('G', baseMachine);
    }

    private static RecipeKeyMap densestMachineKeys(ItemLike baseMachine) {
        return keyMap()
                .put('A', Blocks.REDSTONE_BLOCK)
                .put('B', ModItems.neutron_ingot.get())
                .put('C', ModItems.neutron_gear.get())
                .put('X', baseMachine)
                .put('Y', singularityIngredient(ModSingularities.REDSTONE));
    }

    private static Ingredient singularityIngredient(Singularity singularity) {
        return DataComponentIngredient.of(false, ModDataComponents.SINGULARITY_ID, singularity.getRegistryName(), ModItems.singularity.get());
    }

    private void musicDisc(RecipeOutput output, ItemLike result, ItemLike center, String name) {
        extremeShaped(output, minecraftKey(name), RecipeCategory.MISC, result, 1,
                new String[]{"a a", " b ", "a a"},
                keyMap().put('a', ModItems.record_fragment.get()).put('b', center),
                "has_record_fragment", has(ModItems.record_fragment.get()));
    }

    private static class RecipeKeyMap {
        private final java.util.Map<Character, Ingredient> ingredients = new LinkedHashMap<>();

        private RecipeKeyMap put(char key, ItemLike item) {
            this.ingredients.put(key, Ingredient.of(item));
            return this;
        }

        private RecipeKeyMap put(char key, Ingredient ingredient) {
            this.ingredients.put(key, ingredient);
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
