package committee.nova.mods.avaritia.common.component;

import net.minecraft.core.UUIDUtil;
import net.minecraft.nbt.CompoundTag;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("无尽箱通道引用")
class InfinityChestReferenceTest {
    @Test
    @DisplayName("锁定通道仅允许所有者，公开通道允许其他玩家")
    void accessPolicyUsesOwnerAndLock() {
        UUID owner = UUID.randomUUID();
        UUID stranger = UUID.randomUUID();
        assertAll(
                () -> assertTrue(InfinityChestReference.canModify(owner, true, owner)),
                () -> assertFalse(InfinityChestReference.canModify(owner, true, stranger)),
                () -> assertTrue(InfinityChestReference.canModify(owner, false, stranger))
        );
    }

    @Test
    @DisplayName("旧 chestID 别名能恢复为稳定引用")
    void legacyChestIdAliasDecodes() {
        UUID owner = UUID.randomUUID();
        UUID channel = UUID.randomUUID();
        CompoundTag tag = new CompoundTag();
        tag.store("owner", UUIDUtil.CODEC, owner);
        tag.putBoolean("locked", true);
        tag.putString("filter", "diamond");
        tag.putByte("sortType", (byte) 7);
        tag.store("chestID", UUIDUtil.CODEC, channel);

        InfinityChestReference reference = InfinityChestReference.readLegacyTag(tag);

        assertEquals(new InfinityChestReference(owner, true, "diamond", (byte) 7, channel), reference);
    }
}
