package committee.nova.mods.avaritia.compat.jade;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertTrue;

class JadeCompatContractTest {
    private static final Path SOURCE = Path.of(
            "src/main/java/committee/nova/mods/avaritia/compat/jade/JadeCompat.java");

    @Test
    void livingEntityTooltipReadsTheSyncedMarkAndShowsRemainingSeconds() throws IOException {
        String source = Files.readString(SOURCE).replaceAll("\\s+", "");

        assertAll(
                () -> assertTrue(source.contains(
                        "registerEntityComponent(SpearMarkComponentProvider.INSTANCE,LivingEntity.class)")),
                () -> assertTrue(source.contains("SpearMarkmark=SpearMarkUtils.getActiveMark(target)")),
                () -> assertTrue(source.contains("mark.remainingTicks(target.level().getGameTime())")),
                () -> assertTrue(source.contains("tooltip.avaritia.jade.spear_mark")),
                () -> assertTrue(source.contains("returnConst.rl(\"spear_mark\")"))
        );
    }
}
