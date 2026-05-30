package com.avaritia.init.data.provider;

import com.avaritia.init.registry.ModBlocks;
import com.avaritia.init.registry.ModItems;
import com.avaritia.init.registry.ModRecipeSerializers;
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
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.PlacementInfo;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeBookCategories;
import net.minecraft.world.item.crafting.RecipeBookCategory;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.ShapedRecipePattern;
import net.minecraft.world.item.crafting.display.RecipeDisplay;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * Avaritia 配方数据生成入口。
 * <p>
 * Minecraft 26.1.2 的 {@link RecipeProvider} 使用 Runner 接收异步注册表查找；
 * 本类负责注册内部 Provider，并通过 Builder API / 自定义 Recipe 实例输出
 * 标准合成、极限合成和压缩机配方，避免维护手写 JSON。
 * </p>
 */
public class AvaritiaRecipeProvider extends RecipeProvider.Runner {

    /**
     * 创建 Avaritia 配方数据生成器。
     *
     * @param output     数据生成输出目录
     * @param registries 注册表查找 Future
     */
    public AvaritiaRecipeProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> registries) {
        super(output, registries);
    }

    @Override
    public String getName() {
        return "Avaritia Recipes";
    }

    @Override
    protected RecipeProvider createRecipeProvider(HolderLookup.Provider registries, RecipeOutput output) {
        return new Provider(registries, output);
    }

    /**
     * 实际写出配方的 Provider。
     */
    private static class Provider extends RecipeProvider {
        protected Provider(HolderLookup.Provider registries, RecipeOutput output) {
            super(registries, output);
        }

        @Override
        protected void buildRecipes() {
            this.standardCraftingRecipes();
            this.extremeCraftingRecipes();
            this.compressorRecipes();
        }

        /**
         * 生成原版工作台合成配方。
         */
        private void standardCraftingRecipes() {
            ShapedRecipeBuilder.shaped(this.items, RecipeCategory.MISC, ModItems.neutron_ingot.get())
                    .pattern("PPP")
                    .pattern("PPP")
                    .pattern("PPP")
                    .define('P', ModItems.neutron_pile.get())
                    .unlockedBy("has_neutron_pile", this.has(ModItems.neutron_pile.get()))
                    .save(this.output, recipeKey("neutron_ingot"));

            ShapedRecipeBuilder.shaped(this.items, RecipeCategory.MISC, ModBlocks.neutron.get())
                    .pattern("III")
                    .pattern("III")
                    .pattern("III")
                    .define('I', ModItems.neutron_ingot.get())
                    .unlockedBy("has_neutron_ingot", this.has(ModItems.neutron_ingot.get()))
                    .save(this.output, recipeKey("neutron"));
            ShapelessRecipeBuilder.shapeless(this.items, RecipeCategory.MISC, ModItems.neutron_ingot.get(), 9)
                    .requires(ModBlocks.neutron.get())
                    .unlockedBy("has_neutron_block", this.has(ModBlocks.neutron.get()))
                    .save(this.output, recipeKey("neutron_ingot_from_block"));

            ShapedRecipeBuilder.shaped(this.items, RecipeCategory.MISC, ModBlocks.crystal_matrix.get())
                    .pattern("III")
                    .pattern("III")
                    .pattern("III")
                    .define('I', ModItems.crystal_matrix_ingot.get())
                    .unlockedBy("has_crystal_matrix_ingot", this.has(ModItems.crystal_matrix_ingot.get()))
                    .save(this.output, recipeKey("crystal_matrix"));
            ShapelessRecipeBuilder.shapeless(this.items, RecipeCategory.MISC, ModItems.crystal_matrix_ingot.get(), 9)
                    .requires(ModBlocks.crystal_matrix.get())
                    .unlockedBy("has_crystal_matrix_block", this.has(ModBlocks.crystal_matrix.get()))
                    .save(this.output, recipeKey("crystal_matrix_ingot_from_block"));

            ShapedRecipeBuilder.shaped(this.items, RecipeCategory.MISC, ModBlocks.infinity.get())
                    .pattern("III")
                    .pattern("III")
                    .pattern("III")
                    .define('I', ModItems.infinity_ingot.get())
                    .unlockedBy("has_infinity_ingot", this.has(ModItems.infinity_ingot.get()))
                    .save(this.output, recipeKey("infinity"));
            ShapelessRecipeBuilder.shapeless(this.items, RecipeCategory.MISC, ModItems.infinity_ingot.get(), 9)
                    .requires(ModBlocks.infinity.get())
                    .unlockedBy("has_infinity_block", this.has(ModBlocks.infinity.get()))
                    .save(this.output, recipeKey("infinity_ingot_from_block"));

            ShapedRecipeBuilder.shaped(this.items, RecipeCategory.MISC, ModBlocks.diamond_lattice_block.get())
                    .pattern("LLL")
                    .pattern("LLL")
                    .pattern("LLL")
                    .define('L', ModItems.diamond_lattice.get())
                    .unlockedBy("has_diamond_lattice", this.has(ModItems.diamond_lattice.get()))
                    .save(this.output, recipeKey("diamond_lattice_block"));
            ShapelessRecipeBuilder.shapeless(this.items, RecipeCategory.MISC, ModItems.diamond_lattice.get(), 9)
                    .requires(ModBlocks.diamond_lattice_block.get())
                    .unlockedBy("has_diamond_lattice_block", this.has(ModBlocks.diamond_lattice_block.get()))
                    .save(this.output, recipeKey("diamond_lattice_from_block"));

            ShapedRecipeBuilder.shaped(this.items, RecipeCategory.MISC, ModBlocks.star_fuel_block.get())
                    .pattern("FFF")
                    .pattern("FFF")
                    .pattern("FFF")
                    .define('F', ModItems.star_fuel.get())
                    .unlockedBy("has_star_fuel", this.has(ModItems.star_fuel.get()))
                    .save(this.output, recipeKey("star_fuel_block"));
            ShapelessRecipeBuilder.shapeless(this.items, RecipeCategory.MISC, ModItems.star_fuel.get(), 9)
                    .requires(ModBlocks.star_fuel_block.get())
                    .unlockedBy("has_star_fuel_block", this.has(ModBlocks.star_fuel_block.get()))
                    .save(this.output, recipeKey("star_fuel_from_block"));

            ShapedRecipeBuilder.shaped(this.items, RecipeCategory.MISC, ModBlocks.refined_coal_block.get())
                    .pattern("CCC")
                    .pattern("CCC")
                    .pattern("CCC")
                    .define('C', ModItems.refined_coal.get())
                    .unlockedBy("has_refined_coal", this.has(ModItems.refined_coal.get()))
                    .save(this.output, recipeKey("refined_coal_block"));
            ShapelessRecipeBuilder.shapeless(this.items, RecipeCategory.MISC, ModItems.refined_coal.get(), 9)
                    .requires(ModBlocks.refined_coal_block.get())
                    .unlockedBy("has_refined_coal_block", this.has(ModBlocks.refined_coal_block.get()))
                    .save(this.output, recipeKey("refined_coal_from_block"));

            ShapedRecipeBuilder.shaped(this.items, RecipeCategory.MISC, ModBlocks.compressed_crafting_table.get())
                    .pattern("TTT")
                    .pattern("TTT")
                    .pattern("TTT")
                    .define('T', Blocks.CRAFTING_TABLE)
                    .unlockedBy("has_crafting_table", this.has(Blocks.CRAFTING_TABLE))
                    .save(this.output, recipeKey("compressed_crafting_table"));

            ShapedRecipeBuilder.shaped(this.items, RecipeCategory.MISC, ModBlocks.double_compressed_crafting_table.get())
                    .pattern("TTT")
                    .pattern("TTT")
                    .pattern("TTT")
                    .define('T', ModBlocks.compressed_crafting_table.get())
                    .unlockedBy("has_compressed_crafting_table", this.has(ModBlocks.compressed_crafting_table.get()))
                    .save(this.output, recipeKey("double_compressed_crafting_table"));

            ShapedRecipeBuilder.shaped(this.items, RecipeCategory.MISC, ModItems.neutron_gear.get())
                    .pattern(" N ")
                    .pattern("NCN")
                    .pattern(" N ")
                    .define('N', ModItems.neutron_ingot.get())
                    .define('C', ModItems.crystal_matrix_ingot.get())
                    .unlockedBy("has_neutron_ingot", this.has(ModItems.neutron_ingot.get()))
                    .save(this.output, recipeKey("neutron_gear"));

            ShapedRecipeBuilder.shaped(this.items, RecipeCategory.MISC, ModBlocks.sculk_crafting_table.get())
                    .pattern("ABA")
                    .pattern("CXC")
                    .pattern("ADA")
                    .define('A', Items.ECHO_SHARD)
                    .define('B', Blocks.SCULK_SHRIEKER)
                    .define('C', Blocks.SCULK)
                    .define('D', Blocks.SCULK_CATALYST)
                    .define('X', ModBlocks.double_compressed_crafting_table.get())
                    .unlockedBy("has_double_compressed_crafting_table", this.has(ModBlocks.double_compressed_crafting_table.get()))
                    .save(this.output, recipeKey("sculk_crafting_table"));
        }

        /**
         * 生成极限工作台、极限锻造和动态奇点相关配方。
         */
        private void extremeCraftingRecipes() {
            this.extremeShaped("diamond_lattice", ModItems.diamond_lattice.get(), 1, 1,
                    keys(entry('x', Items.DIAMOND), entry('y', Items.NETHERITE_SCRAP)),
                    "x x",
                    " y ",
                    "x x");

            this.extremeShaped("crystal_matrix_ingot", ModItems.crystal_matrix_ingot.get(), 1, 1,
                    keys(entry('x', ModItems.diamond_lattice.get()), entry('y', Items.NETHER_STAR)),
                    "xyx",
                    "xyx");

            this.extremeShaped("blaze_cube", ModItems.blaze_cube.get(), 1, 2,
                    keys(
                            entry('a', Items.ANCIENT_DEBRIS),
                            entry('b', Items.BLAZE_POWDER),
                            entry('c', Items.FIRE_CHARGE),
                            entry('x', Items.BLAZE_ROD),
                            entry('y', Items.BONE)
                    ),
                    " bcb ",
                    "byxyb",
                    "cxaxc",
                    "byxyb",
                    " bcb ");

            this.extremeShaped("refined_coal", ModItems.refined_coal.get(), 1, 2,
                    keys(entry('a', Items.COAL), entry('b', ModItems.neutron_nugget.get())),
                    "     ",
                    " aaa ",
                    " aba ",
                    " aaa ",
                    "     ");

            this.extremeShaped("record_fragment", ModItems.record_fragment.get(), 4, 3,
                    keys(entry('a', ModItems.neutron_pile.get()), entry('b', this.tag(ItemTags.create(Identifier.parse("c:music_discs"))))),
                    "       ",
                    "       ",
                    "   a   ",
                    "  aba  ",
                    "   a   ",
                    "       ",
                    "       ");

            this.extremeShaped("nether_crafting_table", ModBlocks.nether_crafting_table.get(), 1, 1,
                    keys(
                            entry('a', ModBlocks.double_compressed_crafting_table.get()),
                            entry('b', Blocks.RESPAWN_ANCHOR),
                            entry('c', Blocks.WITHER_SKELETON_SKULL),
                            entry('d', Blocks.NETHERRACK),
                            entry('e', Items.NETHERITE_INGOT),
                            entry('f', Items.NETHER_STAR)
                    ),
                    "cbc",
                    "dad",
                    "efe");

            this.extremeShaped("end_crafting_table", ModBlocks.end_crafting_table.get(), 1, 2,
                    keys(
                            entry('a', ModBlocks.double_compressed_crafting_table.get()),
                            entry('b', Items.END_CRYSTAL),
                            entry('c', Blocks.END_PORTAL_FRAME),
                            entry('d', Blocks.OBSIDIAN),
                            entry('e', Items.DRAGON_BREATH),
                            entry('f', Blocks.PURPUR_PILLAR),
                            entry('g', Blocks.END_STONE_BRICKS),
                            entry('h', Blocks.END_STONE),
                            entry('i', Items.ENDER_EYE),
                            entry('j', Blocks.ENDER_CHEST)
                    ),
                    "bcccb",
                    "dfifd",
                    "dgagd",
                    "dhjhd",
                    "beeeb");

            this.extremeShaped("extreme_crafting_table", ModBlocks.extreme_crafting_table.get(), 1, 3,
                    keys(
                            entry('a', ModBlocks.double_compressed_crafting_table.get()),
                            entry('b', Blocks.LODESTONE),
                            entry('c', ModItems.diamond_lattice.get()),
                            entry('d', ModItems.crystal_matrix_ingot.get()),
                            entry('e', ModBlocks.crystal_matrix.get()),
                            entry('f', Items.RECOVERY_COMPASS),
                            entry('g', Blocks.DRAGON_EGG),
                            entry('h', Blocks.BEACON),
                            entry('i', Blocks.REINFORCED_DEEPSLATE),
                            entry('j', Blocks.NETHERITE_BLOCK),
                            entry('k', Items.HEART_OF_THE_SEA)
                    ),
                    "bccfccb",
                    "cddgddc",
                    "cdihidc",
                    "cdiaidc",
                    "cdjkjdc",
                    "cdddddc",
                    "beeeeeb");

            this.extremeShaped("neutron_collector", ModBlocks.neutron_collector.get(), 1, 4,
                    keys(
                            entry('C', ModItems.crystal_matrix_ingot.get()),
                            entry('I', Blocks.IRON_BLOCK),
                            entry('Q', Blocks.QUARTZ_BLOCK),
                            entry('R', Blocks.REDSTONE_BLOCK)
                    ),
                    "IIQQQQQII",
                    "I QQQQQ I",
                    "I  RRR  I",
                    "C RRRRR C",
                    "I RRCRR I",
                    "C RRRRR C",
                    "I  RRR  I",
                    "I       I",
                    "IIICICIII");

            this.extremeShaped("neutron_compressor", ModBlocks.neutron_compressor.get(), 1, 4,
                    keys(
                            entry('C', ModItems.crystal_matrix_ingot.get()),
                            entry('H', Blocks.HOPPER),
                            entry('I', Blocks.IRON_BLOCK),
                            entry('N', ModItems.neutron_ingot.get()),
                            entry('O', ModBlocks.neutron.get()),
                            entry('R', Blocks.REDSTONE_BLOCK)
                    ),
                    "IIIHHHIII",
                    "C N   N C",
                    "I N   N I",
                    "C N   N C",
                    "RNN O NNR",
                    "C N   N C",
                    "I N   N I",
                    "C N   N C",
                    "IIICICIII");

            this.extremeShaped("star_fuel", ModItems.star_fuel.get(), 1, 4,
                    keys(
                            entry('a', Blocks.COAL_BLOCK),
                            entry('b', Blocks.MAGMA_BLOCK),
                            entry('c', Items.LAVA_BUCKET),
                            entry('d', ModItems.eternal_singularity.get())
                    ),
                    "         ",
                    "  aaaaa  ",
                    " abbcbba ",
                    " abaaaba ",
                    " acadaca ",
                    " abaaaba ",
                    " abbcbba ",
                    "  aaaaa  ",
                    "         ");

            this.extremeShaped("extreme_smithing_table", ModBlocks.extreme_smithing_table.get(), 1, 4,
                    keys(
                            entry('a', ModBlocks.neutron.get()),
                            entry('b', ModItems.neutron_ingot.get()),
                            entry('c', ModItems.diamond_lattice.get()),
                            entry('d', ModItems.blaze_cube.get()),
                            entry('e', ModBlocks.crystal_matrix.get()),
                            entry('f', ModItems.infinity_nugget.get()),
                            entry('g', ModItems.infinity_ingot.get()),
                            entry('h', ModItems.neutron_gear.get()),
                            entry('i', ModItems.infinity_catalyst.get()),
                            entry('j', Blocks.SMITHING_TABLE),
                            entry('k', ModBlocks.extreme_crafting_table.get()),
                            entry('l', ModItems.crystal_matrix_ingot.get())
                    ),
                    "aaaaaaaaa",
                    "bccfgfccb",
                    "bcdhhhdcb",
                    "lfhijihfl",
                    "eghjkjhge",
                    "lfhijihfl",
                    "bcdhhhdcb",
                    "bccfgfccb",
                    "bleeeeelb");

            this.infinityCatalyst("infinity_catalyst", "default", false);
            this.infinityCatalyst("infinity_catalyst_eternal", "eternal_singularity", true);
            this.eternalSingularity("eternal_singularity");
            this.extremeAnvil();
        }

        /**
         * 生成中子压缩机配方。
         */
        private void compressorRecipes() {
            this.compressor("compressor_matter_cluster", Ingredient.of(ModItems.neutron_ingot.get()), ModItems.full_matter_cluster.get(), 1, 4096, 240);
            this.compressor("bedrock_from_deepslate", Ingredient.of(Blocks.DEEPSLATE), Blocks.BEDROCK, 1, 10000, 240);
        }

        private void extremeShaped(String name, ItemLike result, int count, int tier, Map<Character, Ingredient> key, String... pattern) {
            ShapedRecipePattern shapedPattern = ShapedRecipePattern.of(key, pattern);
            this.output.accept(recipeKey(name), new ShapedTableDatagenRecipe(shapedPattern, stack(result, count), tier, ModRecipeSerializers.SHAPED_CRAFT_SERIALIZER.get()), null);
        }

        private void infinityCatalyst(String name, String group, boolean includeEternalSingularity) {
            NonNullList<Ingredient> ingredients = ingredients(
                    Ingredient.of(Blocks.BEDROCK),
                    Ingredient.of(ModItems.crystal_matrix_ingot.get()),
                    Ingredient.of(ModItems.neutron_ingot.get()),
                    Ingredient.of(ModItems.cosmic_meatballs.get()),
                    Ingredient.of(ModItems.ultimate_stew.get()),
                    Ingredient.of(ModItems.endest_pearl.get()),
                    Ingredient.of(ModItems.record_fragment.get())
            );
            if (includeEternalSingularity) {
                ingredients.add(Ingredient.of(ModItems.eternal_singularity.get()));
            }
            this.output.accept(recipeKey(name), new ShapelessTableDatagenRecipe(ingredients, stack(ModItems.infinity_catalyst.get(), 1), 4, group, ModRecipeSerializers.INFINITY_CATALYST_CRAFT_SERIALIZER.get()), null);
        }

        private void eternalSingularity(String name) {
            this.output.accept(recipeKey(name), new ShapelessTableDatagenRecipe(NonNullList.create(), stack(ModItems.eternal_singularity.get(), 1), 4, "", ModRecipeSerializers.ETERNAL_SINGULARITY_CRAFT_SERIALIZER.get()), null);
        }

        private void extremeAnvil() {
            Ingredient addition = Ingredient.of(
                    ModItems.full_matter_cluster.get(),
                    ModItems.enhancement_core.get(),
                    ModBlocks.neutron.get()
            );
            this.output.accept(recipeKey("extreme_anvil"), new ExtremeSmithingDatagenRecipe(
                    Ingredient.of(ModItems.upgrade_smithing_template.get()),
                    Ingredient.of(Blocks.ANVIL),
                    addition,
                    stack(ModBlocks.extreme_anvil.get(), 1)
            ), null);
        }

        private void compressor(String name, Ingredient input, ItemLike result, int count, int inputCount, int timeCost) {
            this.output.accept(recipeKey(name), new CompressorDatagenRecipe(input, stack(result, count), inputCount, timeCost), null);
        }

        private static ResourceKey<Recipe<?>> recipeKey(String name) {
            return ResourceKey.create(Registries.RECIPE, Identifier.fromNamespaceAndPath(Const.MOD_ID, name));
        }

        private static ItemStack stack(ItemLike item, int count) {
            return new ItemStack(item.asItem(), count);
        }

        private static NonNullList<Ingredient> ingredients(Ingredient... ingredients) {
            return NonNullList.of(Ingredient.of(), ingredients);
        }

        @SafeVarargs
        private static Map<Character, Ingredient> keys(Map.Entry<Character, Ingredient>... entries) {
            Map<Character, Ingredient> key = new LinkedHashMap<>();
            for (Map.Entry<Character, Ingredient> entry : entries) {
                key.put(entry.getKey(), entry.getValue());
            }
            return key;
        }

        private static Map.Entry<Character, Ingredient> entry(char symbol, ItemLike item) {
            return Map.entry(symbol, Ingredient.of(item));
        }

        private static Map.Entry<Character, Ingredient> entry(char symbol, Ingredient ingredient) {
            return Map.entry(symbol, ingredient);
        }
    }

    /**
     * 仅用于 datagen 序列化的自定义配方基类。
     */
    private interface SerializerBackedRecipe<T extends net.minecraft.world.item.crafting.RecipeInput> extends Recipe<T> {
        @Override
        default boolean matches(T input, Level level) {
            return false;
        }

        @Override
        default ItemStack assemble(T input) {
            return ItemStack.EMPTY;
        }

        @Override
        default boolean showNotification() {
            return true;
        }

        @Override
        default String group() {
            return "";
        }

        @Override
        default RecipeType<? extends Recipe<T>> getType() {
            return RecipeType.CRAFTING;
        }

        @Override
        default PlacementInfo placementInfo() {
            return PlacementInfo.NOT_PLACEABLE;
        }

        @Override
        default List<RecipeDisplay> display() {
            return List.of();
        }

        @Override
        default RecipeBookCategory recipeBookCategory() {
            return RecipeBookCategories.CRAFTING_MISC;
        }
    }

    private record ShapedTableDatagenRecipe(ShapedRecipePattern pattern, ItemStack result, int tier, RecipeSerializer<?> serializer) implements SerializerBackedRecipe<CraftingInput> {
        @Override
        public RecipeSerializer<? extends Recipe<CraftingInput>> getSerializer() {
            @SuppressWarnings("unchecked")
            RecipeSerializer<? extends Recipe<CraftingInput>> typed = (RecipeSerializer<? extends Recipe<CraftingInput>>) serializer;
            return typed;
        }
    }

    private record ShapelessTableDatagenRecipe(NonNullList<Ingredient> ingredients, ItemStack result, int tier, String group, RecipeSerializer<?> serializer) implements SerializerBackedRecipe<CraftingInput> {
        @Override
        public RecipeSerializer<? extends Recipe<CraftingInput>> getSerializer() {
            @SuppressWarnings("unchecked")
            RecipeSerializer<? extends Recipe<CraftingInput>> typed = (RecipeSerializer<? extends Recipe<CraftingInput>>) serializer;
            return typed;
        }
    }

    private record CompressorDatagenRecipe(Ingredient ingredient, ItemStack result, int inputCount, int timeCost) implements SerializerBackedRecipe<CraftingInput> {
        @Override
        public RecipeSerializer<? extends Recipe<CraftingInput>> getSerializer() {
            @SuppressWarnings("unchecked")
            RecipeSerializer<? extends Recipe<CraftingInput>> typed = (RecipeSerializer<? extends Recipe<CraftingInput>>) ModRecipeSerializers.COMPRESSOR_SERIALIZER.get();
            return typed;
        }
    }

    private record ExtremeSmithingDatagenRecipe(Ingredient template, Ingredient base, Ingredient addition, ItemStack result) implements SerializerBackedRecipe<CraftingInput> {
        @Override
        public RecipeSerializer<? extends Recipe<CraftingInput>> getSerializer() {
            @SuppressWarnings("unchecked")
            RecipeSerializer<? extends Recipe<CraftingInput>> typed = (RecipeSerializer<? extends Recipe<CraftingInput>>) ModRecipeSerializers.EXTREME_SMITHING_SERIALIZER.get();
            return typed;
        }
    }
}
