package committee.nova.mods.avaritia.common.menu;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TierCraftMenuTest {
    private static final Path MAIN_SOURCES = Path.of("src/main/java/committee/nova/mods/avaritia");

    @Test
    void resultValidationRequiresANonEmptyExactCountAndComponentMatch() {
        assertAll(
                () -> assertTrue(TierCraftMenu.matchesDisplayedResult(2, 2, true)),
                () -> assertFalse(TierCraftMenu.matchesDisplayedResult(0, 0, true),
                        "an empty current result cannot be taken"),
                () -> assertFalse(TierCraftMenu.matchesDisplayedResult(2, 1, true),
                        "the displayed count must match the newly assembled result"),
                () -> assertFalse(TierCraftMenu.matchesDisplayedResult(2, 2, false),
                        "the displayed item and components must match the newly assembled result")
        );
    }

    @Test
    void currentResultAssemblyRejectsInvalidTilesEmptyInputsAndMissingRecipes() throws IOException {
        String menu = compact(read("common/menu/TierCraftMenu.java"));

        assertAll(
                () -> assertTrue(menu.contains("!this.craftTile.isRemoved()")),
                () -> assertTrue(menu.contains("this.world.getBlockEntity(this.getBlockPos())==this.craftTile")),
                () -> assertTrue(menu.contains("this.craftContainer.isEmpty()"),
                        "an empty input grid must assemble to an empty result"),
                () -> assertTrue(menu.contains("serverLevel.recipeAccess().getRecipeFor(")),
                () -> assertTrue(menu.contains("recipe.value().assemble(inventory)")),
                () -> assertTrue(menu.contains(".orElse(ItemStack.EMPTY)"),
                        "a grid with no matching recipe must assemble to an empty result")
        );
    }

    @Test
    void everyResultTakePathUsesServerAuthoritativeValidationAndRefreshesStaleOutput() throws IOException {
        String menu = compact(read("common/menu/TierCraftMenu.java"));
        String resultSlot = compact(read("common/container/slot/ModCraftResultSlot.java"));
        int refresh = menu.indexOf("this.result.setItem(0,currentResult);");
        int broadcast = menu.indexOf("this.broadcastChanges();", refresh);

        assertAll(
                () -> assertTrue(menu.contains("if(!(this.worldinstanceofServerLevel)){returntrue;}"),
                        "client prediction must not reject an otherwise normal result interaction"),
                () -> assertTrue(menu.contains("ItemStack.isSameItemSameComponents(currentResult,displayedResult)")),
                () -> assertTrue(refresh >= 0 && broadcast > refresh,
                        "a rejected stale result must be replaced and synchronized to the client"),
                () -> assertTrue(menu.contains("if(slotId==0&&!this.canTakeCraftingResult()){return;}")),
                () -> assertTrue(menu.contains("if(slotNumber==0&&!this.canTakeCraftingResult()){returnItemStack.EMPTY;}")),
                () -> assertTrue(resultSlot.contains("publicbooleanmayPickup(@NotNullPlayerplayer)")),
                () -> assertTrue(resultSlot.contains("menu.canTakeCraftingResult()")),
                () -> assertTrue(resultSlot.contains("if(!this.mayPickup(this.player)){returnItemStack.EMPTY;}"))
        );
    }

    private static String read(String relativePath) throws IOException {
        return Files.readString(MAIN_SOURCES.resolve(relativePath));
    }

    private static String compact(String value) {
        return value.replaceAll("\\s+", "");
    }
}
