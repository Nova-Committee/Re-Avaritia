package committee.nova.mods.avaritia.common.item.tools.infinity;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class InfinityTridentItemTest {
    private static final Path ITEM_SOURCE = Path.of("src/main/java/committee/nova/mods/avaritia/common/item/tools/infinity/InfinityTridentItem.java");
    private static final Path MODEL_JSON = Path.of("src/generated/resources/assets/avaritia/items/infinity_trident.json");

    @Test
    void longUseKeepsVanillaTridentAnimationContract() throws IOException {
        String itemSource = compact(Files.readString(ITEM_SOURCE));
        String modelJson = Files.readString(MODEL_JSON);

        assertAll(
                () -> assertTrue(itemSource.contains("returnItemUseAnimation.TRIDENT;")),
                () -> assertFalse(itemSource.contains("returnItemUseAnimation.SPEAR;")),
                () -> assertTrue(itemSource.contains("player.startUsingItem(hand);")),
                () -> assertTrue(itemSource.contains("returnInteractionResult.CONSUME;")),
                () -> assertTrue(modelJson.contains("\"property\": \"minecraft:using_item\"")),
                () -> assertTrue(modelJson.contains("\"model\": \"avaritia:item/infinity_trident_throwing\""))
        );
    }

    private static String compact(String value) {
        return value.replaceAll("\\s+", "");
    }
}
