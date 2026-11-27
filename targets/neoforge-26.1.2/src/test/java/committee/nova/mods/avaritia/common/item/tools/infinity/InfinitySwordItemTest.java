package committee.nova.mods.avaritia.common.item.tools.infinity;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class InfinitySwordItemTest {
    private static final Path MAIN_SOURCES = Path.of("src/main/java/committee/nova/mods/avaritia");
    private static final Path GENERATED_DATA = Path.of("src/generated/resources/data");

    @Test
    void directAttackResolvesMultipartTargetsAndEnforcesTerminalDamage() throws IOException {
        String sword = compact(readSource("common/item/tools/infinity/InfinitySwordItem.java"));
        String infinityDamage = compact(readSource("util/InfinityDamageUtils.java"));

        assertAll(
                () -> assertTrue(sword.contains("InfinityDamageUtils.resolveLivingTarget(entity)")),
                () -> assertTrue(sword.contains("InfinityDamageUtils.forceKill(serverLevel,victim,damageSource)")),
                () -> assertTrue(sword.contains("returntrue;"), "a handled custom attack must suppress vanilla fallback damage"),
                () -> assertTrue(infinityDamage.contains("part.getParent()instanceofLivingEntity")),
                () -> assertTrue(infinityDamage.contains("dragon.hurt(level,dragon.head,source,Float.MAX_VALUE)")),
                () -> assertTrue(infinityDamage.contains("victim.hurtServer(level,source,Float.MAX_VALUE)")),
                () -> assertTrue(infinityDamage.contains("if(!victim.isRemoved()&&!victim.dead)")),
                () -> assertTrue(infinityDamage.contains("victim.setHealth(0.0F)")),
                () -> assertTrue(infinityDamage.contains("forceDie(victim,source)"))
        );
    }

    @Test
    void rangedAttackExpandsItsBoxAndUsesTerminalDamageWhenConfigured() throws IOException {
        String toolUtils = compact(readSource("util/ToolUtils.java"));
        String sword = compact(readSource("common/item/tools/infinity/InfinitySwordItem.java"));

        assertAll(
                () -> assertTrue(toolUtils.contains("player.getBoundingBox().inflate(range)")),
                () -> assertFalse(toolUtils.contains("player.getBoundingBox().deflate(range)")),
                () -> assertTrue(toolUtils.contains("InfinityDamageUtils.resolveLivingTarget(entity)")),
                () -> assertTrue(toolUtils.contains("InfinityDamageUtils.forceKill(serverLevel,livingEntity,src)")),
                () -> assertTrue(sword.contains("ModConfig.isSwordAttackEndless.get()"))
        );
    }

    @Test
    void infinityDamageKeepsEnderDragonAndInvulnerabilityBypassTags() throws IOException {
        String dragonTag = Files.readString(GENERATED_DATA.resolve(
                "minecraft/tags/damage_type/always_hurts_ender_dragons.json"));
        String invulnerabilityTag = Files.readString(GENERATED_DATA.resolve(
                "minecraft/tags/damage_type/bypasses_invulnerability.json"));

        assertAll(
                () -> assertTrue(dragonTag.contains("\"avaritia:infinity\"")),
                () -> assertTrue(invulnerabilityTag.contains("\"avaritia:infinity\""))
        );
    }

    private static String readSource(String relativePath) throws IOException {
        return Files.readString(MAIN_SOURCES.resolve(relativePath));
    }

    private static String compact(String value) {
        return value.replaceAll("\\s+", "");
    }
}
