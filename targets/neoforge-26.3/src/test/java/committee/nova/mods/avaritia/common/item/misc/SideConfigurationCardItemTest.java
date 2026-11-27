package committee.nova.mods.avaritia.common.item.misc;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SideConfigurationCardItemTest {
    private static final Path SOURCE = Path.of(
            "src/main/java/committee/nova/mods/avaritia/common/item/misc/SideConfigurationCardItem.java"
    );

    @Test
    void machineInteractionKeepsTheBaselineSaveAndApplyDirection() throws IOException {
        String source = compact(Files.readString(SOURCE));
        int machineBranch = source.indexOf("if(blockEntityinstanceofITileIOtileIO)");
        int shiftSave = source.indexOf("if(player.isShiftKeyDown())", machineBranch);
        int save = source.indexOf("saveConfigToItem(stack,config);", shiftSave);
        int missingConfig = source.indexOf("if(!hasSavedConfig(stack))", save);
        int apply = source.indexOf("tileIO.setSideConfiguration(loadConfigFromItem(stack));", missingConfig);

        assertAll(
                () -> assertTrue(machineBranch >= 0),
                () -> assertTrue(machineBranch < shiftSave && shiftSave < save,
                        "shift-right-click on a machine must save its side configuration"),
                () -> assertTrue(save < missingConfig && missingConfig < apply,
                        "normal right-click must validate and then apply the saved configuration"),
                () -> assertTrue(source.contains("side_config_card.no_config_to_apply"))
        );
    }

    @Test
    void shiftRightClickInAirOrOnAnotherBlockClearsTheCard() throws IOException {
        String source = compact(Files.readString(SOURCE));

        assertAll(
                () -> assertTrue(source.contains("InteractionResultuse(@NotNullLevellevel,@NotNullPlayerplayer,@NotNullInteractionHandhand)")),
                () -> assertTrue(source.contains("if(!player.isShiftKeyDown()){returnInteractionResult.PASS;}")),
                () -> assertTrue(source.contains("clearConfig(stack,player);")),
                () -> assertTrue(source.contains("tag.remove(TAG_SIDE_CONFIG)"))
        );
    }

    private static String compact(String value) {
        return value.replaceAll("\\s+", "");
    }
}
