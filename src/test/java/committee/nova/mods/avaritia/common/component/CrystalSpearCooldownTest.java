package committee.nova.mods.avaritia.common.component;

import io.netty.buffer.Unpooled;
import net.minecraft.nbt.NbtOps;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CrystalSpearCooldownTest {
    @Test
    void cooldownLastsExactlySixtySeconds() {
        CrystalSpearCooldown cooldown = CrystalSpearCooldown.start(400L);

        assertEquals(1_600L, cooldown.endsAt());
        assertTrue(cooldown.isActive(1_599L));
        assertFalse(cooldown.isActive(1_600L));
        assertEquals(60L, cooldown.remainingSeconds(400L));
        assertEquals(1L, cooldown.remainingSeconds(1_599L));
        assertEquals(0L, cooldown.remainingSeconds(1_600L));
    }

    @Test
    void persistentCodecPreservesCooldownEnd() {
        CrystalSpearCooldown expected = CrystalSpearCooldown.start(20_000L);

        var encoded = CrystalSpearCooldown.CODEC.encodeStart(NbtOps.INSTANCE, expected).getOrThrow();
        CrystalSpearCooldown decoded = CrystalSpearCooldown.CODEC.parse(NbtOps.INSTANCE, encoded).getOrThrow();

        assertEquals(expected, decoded);
    }

    @Test
    void networkCodecPreservesCooldownEnd() {
        CrystalSpearCooldown expected = CrystalSpearCooldown.start(20_000L);
        var buffer = Unpooled.buffer();
        try {
            CrystalSpearCooldown.STREAM_CODEC.encode(buffer, expected);
            assertEquals(expected, CrystalSpearCooldown.STREAM_CODEC.decode(buffer));
        } finally {
            buffer.release();
        }
    }
}
