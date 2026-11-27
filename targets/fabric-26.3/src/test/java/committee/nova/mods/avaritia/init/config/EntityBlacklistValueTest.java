package committee.nova.mods.avaritia.init.config;

import com.google.gson.JsonParser;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class EntityBlacklistValueTest {
    @Test
    void invalidEntryRejectsWholeReplacementAndKeepsPriorList() {
        var value = new ModConfig.ListValue("test", List.of());
        value.load(JsonParser.parseString("[\"minecraft:zombie\"]"));
        for (String json : List.of("[\"minecraft:cow\",\"zombie\"]", "[\"Minecraft:zombie\"]",
                "[\"minecraft:\"]", "[\":zombie\"]", "[\"minecraft:cow\",42]", "{}")) {
            assertThrows(IllegalArgumentException.class, () -> value.load(JsonParser.parseString(json)));
            assertEquals(List.of("minecraft:zombie"), value.get());
        }
    }

    @Test
    void callerCannotMutateConfiguredListWithoutValidation() {
        var value = new ModConfig.ListValue("test", List.of());
        var input = new ArrayList<>(List.of("example:loot/cart"));
        value.set(input);
        input.add("invalid");
        assertEquals(List.of("example:loot/cart"), value.get());
        assertThrows(UnsupportedOperationException.class, () -> value.get().add("invalid"));
        value.load(JsonParser.parseString("[]"));
        assertEquals(List.of(), value.get());
    }
}
