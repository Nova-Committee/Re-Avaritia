package committee.nova.mods.avaritia.common.menu;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("Tesseract selector authorization contract")
class TesseractSelectorContractTest {
    private static final Path GUARD = Path.of(
            "src/main/java/committee/nova/mods/avaritia/common/net/channel/ChannelPayloadGuard.java");
    private static final Path MANAGER = Path.of(
            "src/main/java/committee/nova/mods/avaritia/core/channel/ServerChannelManager.java");
    private static final Path MENU = Path.of(
            "src/main/java/committee/nova/mods/avaritia/common/menu/TesseractChannelMenu.java");
    private static final Path MAIN_MENU = Path.of(
            "src/main/java/committee/nova/mods/avaritia/common/menu/TesseractMenu.java");
    private static final Path SYNC = Path.of(
            "src/main/java/committee/nova/mods/avaritia/core/channel/ServerChannel.java");

    @Test
    @DisplayName("payloads bind menu, distance, permission and active selector session")
    void selectorPayloadsAreServerBound() throws Exception {
        String guard = compact(Files.readString(GUARD));
        assertAll(
                () -> assertTrue(guard.contains("menu.containerId!=containerId")),
                () -> assertTrue(guard.contains("!menu.stillValid(player)")),
                () -> assertTrue(guard.contains("!menu.canPlayerModify(player)")),
                () -> assertTrue(guard.contains("!manager.isSelecting(player,menu.getTerminalOwner())"))
        );
    }

    @Test
    @DisplayName("public management is permission gated and selecting returns to the main menu")
    void publicManagementAndOpenAreValidated() throws Exception {
        String manager = compact(Files.readString(MANAGER));
        String menu = compact(Files.readString(MENU));
        assertAll(
                () -> assertTrue(manager.contains("permissions().hasPermission(Permissions.COMMANDS_GAMEMASTER)")),
                () -> assertTrue(manager.contains("ModConfig.MAX_PUBLIC_CHANNELS.get()")),
                () -> assertTrue(manager.contains("ModConfig.MAX_CHANNELS_PRE_PLAYER.get()")),
                () -> assertTrue(menu.contains("terminal.openMainMenu(player)"))
        );
    }

    @Test
    @DisplayName("oversized transaction deltas fall back to paged full synchronization")
    void oversizedDeltaUsesPagedFullSync() throws Exception {
        String sync = compact(Files.readString(SYNC));
        assertTrue(sync.contains("changedItems.size()+changedFluids.size()>SYNC_PAGE_SIZE){sendFullToListeners()"));
    }

    @Test
    @DisplayName("main menu accepts carried items in empty virtual slots and guards ordinary inventory paths")
    void mainMenuGuardsAndEmptySlotDepositArePreserved() throws Exception {
        String menu = compact(Files.readString(MAIN_MENU));
        assertAll(
                () -> assertTrue(menu.contains("caseEMPTY->{ItemStackcarried=getCarried();if(!carried.isEmpty())")),
                () -> assertTrue(menu.contains("clickItem(ItemResource.of(carried),button,clickType,serverPlayer)")),
                () -> assertTrue(menu.contains("if(playerinstanceofServerPlayer&&(!stillValid(player)||!canPlayerModify(player)))return;")),
                () -> assertTrue(menu.contains("if(!(playerinstanceofServerPlayer)||index<0||index>=CHANNEL_START||!stillValid(player)||!canPlayerModify(player))returnItemStack.EMPTY;"))
        );
    }

    private static String compact(String value) {
        return value.replaceAll("\\s+", "");
    }
}
