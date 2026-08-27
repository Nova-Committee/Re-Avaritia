package committee.nova.mods.avaritia.common.component;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SpearMarkIntegrationContractTest {
    @Test
    void attachmentIsPersistentSyncedRegisteredAndVisiblyExpired() throws IOException {
        String registry = compact(Files.readString(Path.of(
                "src/main/java/committee/nova/mods/avaritia/init/registry/ModDataAttachments.java")));
        String mod = compact(Files.readString(Path.of(
                "src/main/java/committee/nova/mods/avaritia/Avaritia.java")));
        String handler = compact(Files.readString(Path.of(
                "src/main/java/committee/nova/mods/avaritia/init/handler/SpearMarkHandler.java")));

        assertAll(
                () -> assertTrue(registry.contains(".serialize(SpearMark.CODEC).sync(SpearMark.STREAM_CODEC)")),
                () -> assertTrue(mod.contains("ModDataAttachments.ATTACHMENT_TYPES.register(modEventBus)")),
                () -> assertTrue(handler.contains("if(!mark.isActive(gameTime))")),
                () -> assertTrue(handler.contains("target.removeData(ModDataAttachments.SPEAR_MARK)")),
                () -> assertTrue(handler.contains("ParticleTypes.DAMAGE_INDICATOR")),
                () -> assertTrue(handler.contains("target.level().isClientSide()"))
        );
    }

    private static String compact(String value) {
        return value.replaceAll("\\s+", "");
    }
}
