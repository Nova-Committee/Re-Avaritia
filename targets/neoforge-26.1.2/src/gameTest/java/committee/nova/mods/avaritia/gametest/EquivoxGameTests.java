package committee.nova.mods.avaritia.gametest;

import committee.nova.mods.avaritia.Const;
import committee.nova.mods.avaritia.common.crafting.recipe.CompressorRecipe;
import committee.nova.mods.avaritia.common.crafting.recipe.ShapelessTableCraftingRecipe;
import committee.nova.mods.avaritia.core.singularity.Singularity;
import committee.nova.mods.avaritia.init.config.ModConfig;
import committee.nova.mods.avaritia.init.registry.ModDataComponents;
import committee.nova.mods.avaritia.init.registry.ModItems;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.FunctionGameTestInstance;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.gametest.framework.TestData;
import net.minecraft.gametest.framework.TestEnvironmentDefinition;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Recipe;
import net.neoforged.neoforge.common.crafting.DataComponentIngredient;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModList;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.RegisterGameTestsEvent;
import net.neoforged.neoforge.registries.RegisterEvent;

import java.util.function.Consumer;

/** Tests the actual post-reload Equivox EMC table, not a mock mapping collector. */
@EventBusSubscriber(modid = "avaritia_gametest")
public final class EquivoxGameTests {
    private static final Identifier TEST_ID = Const.rl("equivox_optional_integration");
    private static final ResourceKey<Consumer<GameTestHelper>> TEST = ResourceKey.create(Registries.TEST_FUNCTION, TEST_ID);

    private EquivoxGameTests() {
    }

    @SubscribeEvent
    public static void registerFunctions(RegisterEvent event) {
        // Keep all optional API references in the nested class. Absence never loads it.
        if (ModList.get().isLoaded("equivox")) {
            event.register(Registries.TEST_FUNCTION, TEST_ID, () -> Presence::verify);
        } else {
            event.register(Registries.TEST_FUNCTION, TEST_ID, () -> EquivoxGameTests::verifyAbsence);
        }
    }

    @SubscribeEvent
    public static void register(RegisterGameTestsEvent event) {
        var environment = event.registerEnvironment(TEST_ID, new TestEnvironmentDefinition.AllOf());
        event.registerTest(TEST_ID, new FunctionGameTestInstance(TEST,
                new TestData<>(environment, Identifier.withDefaultNamespace("empty"), 200, 0, true)));
    }

    private static void verifyAbsence(GameTestHelper helper) {
        helper.assertTrue(!ModList.get().isLoaded("equivox"), "Absence branch requires no Equivox");
        verifyComponentFixture(helper);
        verifyCounts(helper, ModList.get().isLoaded("projecte"));
        helper.succeed();
    }

    private static void verifyCounts(GameTestHelper helper, boolean emcModLoaded) {
        boolean previous = ModConfig.enableProjectESingularityCountBoost.get();
        try {
            var small = new Singularity(Const.rl("equivox_count_fixture")).setCount(1_000);
            var large = new Singularity(Const.rl("equivox_large_count_fixture")).setCount(12_000);
            ModConfig.enableProjectESingularityCountBoost.set(true);
            helper.assertValueEqual(small.getCount(), emcModLoaded ? 10_000 : 1_000, "Enabled singularity count");
            helper.assertValueEqual(large.getCount(), 12_000, "Larger singularity counts remain unchanged");
            ModConfig.enableProjectESingularityCountBoost.set(false);
            helper.assertValueEqual(small.getCount(), 1_000, "Disabled singularity count boost");
        } finally {
            ModConfig.enableProjectESingularityCountBoost.set(previous);
        }
    }

    private static ItemStack verifyComponentFixture(GameTestHelper helper) {
        var recipes = helper.getLevel().getServer().getRecipeManager();
        var ingredientRecipe = recipes.byKey(recipeKey("equivox_components")).orElseThrow().value();
        helper.assertTrue(ingredientRecipe instanceof ShapelessTableCraftingRecipe,
                "Component ingredient fixture must load as an Avaritia shapeless recipe");
        var ingredient = ((ShapelessTableCraftingRecipe) ingredientRecipe).getIngredients().getFirst();
        helper.assertTrue(ingredient.getCustomIngredient() instanceof DataComponentIngredient,
                "Component ingredient fixture must decode as DataComponentIngredient, not a bare holder set");
        var producerRecipe = recipes.byKey(recipeKey("equivox_component_singularity")).orElseThrow().value();
        helper.assertTrue(producerRecipe instanceof CompressorRecipe,
                "Component singularity fixture must load as an Avaritia compressor recipe");
        var singularity = ((CompressorRecipe) producerRecipe).getResultItem();
        helper.assertTrue(singularity.is(ModItems.singularity.get()), "Compressor must produce a singularity");
        helper.assertTrue(Identifier.fromNamespaceAndPath("avaritia_gametest", "emc_fixture")
                        .equals(singularity.get(ModDataComponents.SINGULARITY_ID.get())),
                "Compressor output must retain the fixture singularity ID");
        helper.assertTrue(ingredient.test(singularity), "Component ingredient must accept the matching singularity");
        helper.assertTrue(!ingredient.test(new ItemStack(ModItems.singularity.get())),
                "Component ingredient must reject a bare singularity");
        var other = singularity.copy();
        other.set(ModDataComponents.SINGULARITY_ID.get(),
                Identifier.fromNamespaceAndPath("avaritia_gametest", "other_fixture"));
        helper.assertTrue(!ingredient.test(other), "Component ingredient must reject a different singularity ID");
        return singularity;
    }

    private static ResourceKey<Recipe<?>> recipeKey(String path) {
        return ResourceKey.create(Registries.RECIPE, Identifier.fromNamespaceAndPath("avaritia_gametest", path));
    }

    private static final class Presence {
        private static void verify(GameTestHelper helper) {
            // Equivox publishes its EMC table on datapack sync. The playerless
            // GameTest startup has no initial sync, so exercise the real reload path.
            var server = helper.getLevel().getServer();
            var reload = server.reloadResources(server.getPackRepository().getSelectedIds());
            helper.succeedWhen(() -> {
                helper.assertTrue(reload.isDone(), "Server resource reload has not completed");
                reload.join();
                verifyMappedValues(helper);
            });
        }

        private static void verifyMappedValues(GameTestHelper helper) {
            var emc = com.yaskulsky.equivox.api.proxy.IEMCProxy.INSTANCE;
            long pile = ModConfig.neutronPileEmc.get();
            long blaze = ModConfig.blazeCubeEmc.get();
            helper.assertValueEqual(emc.getValue(ModItems.neutron_pile.get()), pile, "Configured neutron pile EMC");
            helper.assertValueEqual(emc.getValue(ModItems.blaze_cube.get()), blaze, "Configured blaze cube EMC");
            helper.assertValueEqual(emc.getValue(Items.TOTEM_OF_UNDYING), (long) ModConfig.vanillaTotemEmc.get(), "Configured totem EMC");
            helper.assertValueEqual(emc.getValue(ModItems.full_matter_cluster.get()), 0L, "Full matter cluster must not gain EMC");
            helper.assertValueEqual(emc.getValue(EquivoxGameTestItems.SHAPED.get()), (pile * 2 + blaze) / 2, "Shaped input multiplicity and output count");
            helper.assertValueEqual(emc.getValue(EquivoxGameTestItems.SHAPELESS.get()), pile + blaze, "Shapeless input sum");
            helper.assertValueEqual(emc.getValue(EquivoxGameTestItems.COMPRESSOR.get()), pile * 6_000, "Compressor 1000 inputs x 1440 / 240 time policy");
            helper.assertValueEqual(emc.getValue(EquivoxGameTestItems.SMITHING.get()), pile * 4 + blaze, "Smithing template, base and all three additions");
            helper.assertValueEqual(emc.getValue(EquivoxGameTestItems.ALTERNATIVES.get()), Math.min(pile, blaze), "Ingredient alternatives choose the cheaper available value");
            long milk = emc.getValue(Items.MILK_BUCKET);
            long bucket = emc.getValue(Items.BUCKET);
            helper.assertTrue(milk > bucket, "Milk and bucket fixture inputs require mapped positive EMC");
            helper.assertValueEqual(emc.getValue(EquivoxGameTestItems.REMAINDER.get()), milk - bucket, "Crafting remainder is not charged as consumed input");
            var singularity = verifyComponentFixture(helper);
            var info = com.yaskulsky.equivox.api.ItemInfo.fromStack(singularity);
            helper.assertTrue(!info.equals(info.itemOnly()), "Component singularity identity must differ from the bare item");
            helper.assertTrue(info.equals(com.yaskulsky.equivox.api.ItemInfo.fromNSS(
                            com.yaskulsky.equivox.api.nss.NSSItem.createItem(singularity))),
                    "NSSItem and ItemInfo must preserve the same singularity component identity");
            helper.assertTrue(info.equals(emc.getPersistentInfo(info)),
                    "Real Equivox normalization must retain the mapped singularity component");
            helper.assertValueEqual(pile * 2_000, emc.getValue(singularity),
                    "Component-specific compressor output retains its own EMC");
            helper.assertValueEqual(pile * 2_000, emc.getValue(EquivoxGameTestItems.COMPONENTS.get()),
                    "Component-specific singularity ingredients retain their own EMC");
            verifyCounts(helper, true);
        }
    }
}
