package committee.nova.mods.avaritia.common.container;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("Tesseract virtual resource projection")
class DummyChannelContainerTest {
    private static final Path SOURCE = Path.of(
            "src/main/java/committee/nova/mods/avaritia/common/container/DummyChannelContainer.java");

    @Test
    @DisplayName("the projection exposes 99 slots and all three resource kinds")
    void projectsEveryResourceKind() throws Exception {
        String source = compact(Files.readString(SOURCE));
        assertAll(
                () -> assertTrue(source.contains("publicstaticfinalintSIZE=99")),
                () -> assertTrue(source.contains("enumKind{EMPTY,ITEM,FLUID,ENERGY}")),
                () -> assertTrue(source.contains("data.items().stream().map(Entry::item)")),
                () -> assertTrue(source.contains("data.fluids().stream().map(Entry::fluid)")),
                () -> assertTrue(source.contains("Entryenergy=Entry.energy(data.energy())")),
                () -> assertTrue(source.contains("entries.stream().limit(SIZE)"))
        );
    }

    private static String compact(String value) { return value.replaceAll("\\s+", ""); }
}
