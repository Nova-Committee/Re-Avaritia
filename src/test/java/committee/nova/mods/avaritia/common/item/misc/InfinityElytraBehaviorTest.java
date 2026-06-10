package committee.nova.mods.avaritia.common.item.misc;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertTrue;

class InfinityElytraBehaviorTest {
    private static final Path MAIN_SOURCES = Path.of("src/main/java/committee/nova/mods/avaritia");

    @Test
    void infinityElytraFlightKeepsInfinityDamageAndFallProtectionContracts() {
        assertAll(
                contains("common/item/misc/InfinityElytraItem.java",
                        "target.hurt(ModDamageTypes.source(entity), ModConfig.infinityElytraFlyingRangeDamage.get().floatValue())"),
                contains("common/item/misc/InfinityElytraItem.java", "entity.resetFallDistance()"),
                contains("common/item/misc/InfinityElytraItem.java", "canDamageInFlight(entity, target)"),
                contains("init/handler/InfinityHandler.java", "onInfinityElytraFall(LivingFallEvent event)"),
                contains("init/handler/InfinityHandler.java",
                        "(ToolUtils.isInfinite(player) || isUsingInfinityElytra(player)) && !damageSource.is(ModDamageTypes.INFINITY)"),
                contains("init/handler/InfinityHandler.java",
                        "return isWearingInfinityElytra(player) && (player.isFallFlying() || !player.onGround())")
        );
    }

    private static Executable contains(String relativePath, String expected) {
        return () -> assertTrue(
                compact(read(relativePath)).contains(compact(expected)),
                () -> relativePath + " should keep infinity elytra behavior contract: " + expected
        );
    }

    private static String read(String relativePath) throws IOException {
        return Files.readString(MAIN_SOURCES.resolve(relativePath));
    }

    private static String compact(String value) {
        return value.replaceAll("\\s+", "");
    }
}
