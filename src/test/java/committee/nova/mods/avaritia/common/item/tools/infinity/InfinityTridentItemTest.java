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
    private static final Path ENTITY_SOURCE = Path.of("src/main/java/committee/nova/mods/avaritia/common/entity/InfinityThrownTrident.java");
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

    @Test
    void loyaltyModeKeepsOffhandProjectileAndOriginalSlotReturnContracts() throws IOException {
        String itemSource = compact(Files.readString(ITEM_SOURCE));
        String entitySource = compact(Files.readString(ENTITY_SOURCE));

        assertAll(
                () -> assertTrue(itemSource.contains("ProjectileItemUtils.createLaunchProjectile(level,player,player.getOffhandItem())")),
                () -> assertTrue(itemSource.contains("throwntrident.setReturnSlot(findSourceSlot(player,itemStack))")),
                () -> assertTrue(entitySource.contains("returnSlot=input.getIntOr(\"ReturnSlot\",NO_RETURN_SLOT)")),
                () -> assertTrue(entitySource.contains("output.putInt(\"ReturnSlot\",returnSlot)")),
                () -> assertTrue(entitySource.contains("player.getInventory().setItem(returnSlot,stack)"))
        );
    }

    private static String compact(String value) {
        return value.replaceAll("\\s+", "");
    }
}
