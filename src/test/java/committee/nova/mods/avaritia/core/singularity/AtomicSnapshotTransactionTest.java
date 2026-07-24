package committee.nova.mods.avaritia.core.singularity;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AtomicSnapshotTransactionTest {
    @Test
    void failedDerivedStateBuildRestoresPreviousSnapshotAndAllowsRetry() {
        AtomicSnapshotTransaction<String> transaction = new AtomicSnapshotTransaction<>("committed");
        transaction.begin();

        assertThrows(IllegalStateException.class, () -> transaction.commit("candidate", () -> {
            assertEquals("candidate", transaction.current(), "派生构建期间必须能看到候选快照");
            throw new IllegalStateException("derived recipe rebuild failed");
        }));

        assertEquals("committed", transaction.current());
        assertTrue(transaction.commit("retry", () -> assertEquals("retry", transaction.current())));
        assertEquals("retry", transaction.current());
        assertFalse(transaction.commit("ignored", () -> {
            throw new AssertionError("同一轮事务不得重复构建派生状态");
        }));
    }

    @Test
    void beginningNextReloadAllowsOneNewCommit() {
        AtomicSnapshotTransaction<Integer> transaction = new AtomicSnapshotTransaction<>(1);
        transaction.begin();
        assertTrue(transaction.commit(2, () -> {
        }));
        transaction.begin();
        assertTrue(transaction.commit(3, () -> {
        }));
        assertEquals(3, transaction.current());
    }
}
