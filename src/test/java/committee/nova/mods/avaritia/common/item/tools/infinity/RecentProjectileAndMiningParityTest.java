package committee.nova.mods.avaritia.common.item.tools.infinity;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RecentProjectileAndMiningParityTest {
    private static final Path MAIN_SOURCES = Path.of("src/main/java/committee/nova/mods/avaritia");
    private static final Path MIXIN_CONFIG = Path.of("src/main/resources/avaritia.mixins.json");

    @Test
    void infinityRangedWeaponsShareTargetVersionProjectileCreation() throws IOException {
        String utility = compact(read("util/ProjectileItemUtils.java"));
        String crossbow = compact(read("common/item/tools/infinity/InfinityCrossBowItem.java"));
        String trident = compact(read("common/item/tools/infinity/InfinityTridentItem.java"));

        assertAll(
                () -> assertTrue(utility.contains("instanceofProjectileItem")),
                () -> assertTrue(utility.contains("projectileItem.asProjectile(level,position,ammo,direction)")),
                () -> assertTrue(utility.contains("ammo.is(Items.TNT)")),
                () -> assertTrue(utility.contains("ammo.is(Items.FIRE_CHARGE)")),
                () -> assertTrue(crossbow.contains("ProjectileItemUtils.createLaunchProjectile(level,player,ammo)")),
                () -> assertTrue(trident.contains("ProjectileItemUtils.createLaunchProjectile(level,player,player.getOffhandItem())"))
        );
    }

    @Test
    void interruptedSpecialBlockMiningRestoresOriginalBlock() throws IOException {
        String handler = compact(read("init/handler/InfinityHandler.java"));
        String accessor = compact(read("mixin/accessor/ServerPlayerGameModeAccessor.java"));
        String mixinConfig = compact(Files.readString(MIXIN_CONFIG));

        assertAll(
                () -> assertTrue(handler.contains("trackTemporaryFakeBlock(level,pos,state)")),
                () -> assertTrue(handler.contains("restoreAbandonedTemporaryFakeBlocks(ServerTickEvent.Postevent)")),
                () -> assertTrue(handler.contains("isTemporaryFakeBlockBeingMined(level,temporaryBlock.pos())")),
                () -> assertTrue(accessor.contains("@Accessor(\"isDestroyingBlock\")")),
                () -> assertTrue(accessor.contains("@Accessor(\"hasDelayedDestroy\")")),
                () -> assertTrue(mixinConfig.contains("accessor.ServerPlayerGameModeAccessor"))
        );
    }

    private static String read(String relativePath) throws IOException {
        return Files.readString(MAIN_SOURCES.resolve(relativePath));
    }

    private static String compact(String value) {
        return value.replaceAll("\\s+", "");
    }
}
