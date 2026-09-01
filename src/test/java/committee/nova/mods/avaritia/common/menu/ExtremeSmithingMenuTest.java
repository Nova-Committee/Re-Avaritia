package committee.nova.mods.avaritia.common.menu;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ExtremeSmithingMenuTest {
    private static final Path MAIN_SOURCES = Path.of("src/main/java/committee/nova/mods/avaritia");

    @Test
    void loadedRecipesDriveEveryInputSlotRule() throws IOException {
        String menu = compact(read("common/menu/ExtremeSmithingMenu.java"));

        assertAll(
                () -> assertTrue(menu.contains("recipe.isTemplateIngredient(stack)")),
                () -> assertTrue(menu.contains("recipe.isBaseIngredient(stack)")),
                () -> assertTrue(menu.contains("recipe.isAdditionIngredient(stack)")),
                () -> assertTrue(menu.contains("serverLevel.recipeAccess().recipeMap().byType(")),
                () -> assertTrue(!menu.contains("RecipePropertySet.SMITHING_TEMPLATE")),
                () -> assertTrue(!menu.contains("RecipePropertySet.SMITHING_BASE")),
                () -> assertTrue(!menu.contains("RecipePropertySet.SMITHING_ADDITION"))
        );
    }

    @Test
    void clientWithoutCustomRecipeAccessKeepsSlotsInteractive() throws IOException {
        String menu = compact(read("common/menu/ExtremeSmithingMenu.java"));

        assertAll(
                () -> assertTrue(menu.contains("returnrecipes.isEmpty()||recipes.stream().anyMatch(")),
                () -> assertTrue(menu.contains("if(levelinstanceofServerLevelserverLevel)")),
                () -> assertTrue(menu.contains("returnList.of();"))
        );
    }

    @Test
    void transferLayoutCoversAllFiveInputsAndSkipsTheResult() {
        assertAll(
                () -> assertEquals(0, ExtremeSmithingMenu.TEMPLATE_SLOT),
                () -> assertEquals(5, ExtremeSmithingMenu.INPUT_SLOT_COUNT),
                () -> assertEquals(5, ExtremeSmithingMenu.RESULT_SLOT),
                () -> assertEquals(6, ExtremeSmithingMenu.PLAYER_INVENTORY_SLOT_START),
                () -> assertEquals(36, ExtremeSmithingMenu.PLAYER_INVENTORY_SLOT_COUNT),
                () -> assertEquals(ExtremeSmithingMenu.RESULT_SLOT,
                        ExtremeSmithingMenu.TEMPLATE_SLOT + ExtremeSmithingMenu.INPUT_SLOT_COUNT)
        );
    }

    @Test
    void jeiAndScreenUseTheCorrectTemplateSlotContracts() throws IOException {
        String plugin = compact(read("compat/jei/AvaritiaJeiPlugin.java"));
        String category = compact(read("compat/jei/category/ExtremeSmithingRecipeCategory.java"));
        String screen = compact(read("client/screen/ExtremeSmithingScreen.java"));

        int rightAddition = category.indexOf("addSlot(RecipeIngredientRole.INPUT,63,23).add(additions.get(1))");
        int topAddition = category.indexOf("addSlot(RecipeIngredientRole.INPUT,45,5).add(additions.get(0))");

        assertAll(
                () -> assertTrue(plugin.contains("ExtremeSmithingMenu.TEMPLATE_SLOT,"
                        + "ExtremeSmithingMenu.INPUT_SLOT_COUNT,"
                        + "ExtremeSmithingMenu.PLAYER_INVENTORY_SLOT_START,"
                        + "ExtremeSmithingMenu.PLAYER_INVENTORY_SLOT_COUNT")),
                () -> assertTrue(rightAddition >= 0 && topAddition > rightAddition,
                        "JEI recipe input order must follow menu slots 2 (right) then 3 (top)"),
                () -> assertTrue(screen.contains("container/slot/smithing_template_netherite_upgrade")),
                () -> assertTrue(screen.contains("newCyclingSlotBackground(0)")),
                () -> assertTrue(screen.contains("templateIcon.extractRenderState(")),
                () -> assertTrue(!screen.contains("getBaseSlotEmptyIcons")),
                () -> assertTrue(!screen.contains("getAdditionalSlotEmptyIcons")),
                () -> assertTrue(!screen.contains("newCyclingSlotBackground(1)")),
                () -> assertTrue(!screen.contains("newCyclingSlotBackground(2)")),
                () -> assertTrue(!screen.contains("newCyclingSlotBackground(3)")),
                () -> assertTrue(!screen.contains("newCyclingSlotBackground(4)"))
        );
    }

    private static String read(String relativePath) throws IOException {
        return Files.readString(MAIN_SOURCES.resolve(relativePath));
    }

    private static String compact(String value) {
        return value.replaceAll("\\s+", "");
    }
}
