package committee.nova.mods.avaritia.core.singularity;

import java.util.Objects;

/**
 * 将一个候选快照临时发布给派生状态构建器；构建失败时恢复上次提交值并允许重试。
 */
final class AtomicSnapshotTransaction<T> {
    private volatile T committed;
    private boolean finalized;

    AtomicSnapshotTransaction(T initialValue) {
        this.committed = Objects.requireNonNull(initialValue);
    }

    synchronized void begin() {
        this.finalized = false;
    }

    synchronized boolean commit(T candidate, Runnable derivedStateBuilder) {
        Objects.requireNonNull(candidate);
        Objects.requireNonNull(derivedStateBuilder);
        if (this.finalized) {
            return false;
        }

        T previous = this.committed;
        this.committed = candidate;
        this.finalized = true;
        try {
            derivedStateBuilder.run();
        } catch (RuntimeException | Error exception) {
            this.committed = previous;
            this.finalized = false;
            throw exception;
        }
        return true;
    }

    synchronized void replaceCommitted(T replacement) {
        this.committed = Objects.requireNonNull(replacement);
        this.finalized = true;
    }

    T current() {
        return this.committed;
    }
}
