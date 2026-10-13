package committee.nova.mods.avaritia.api.common.net;

import net.minecraft.util.thread.ReentrantBlockableEventLoop;
import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.jupiter.api.Assertions.assertTrue;

class PacketContextTest {
    @Test
    void openingDataRunsBeforeFollowingVanillaScreenTask() {
        // 26.3 的 execute() 在同线程直接运行；强制排队以复现游戏线程的任务语义
        TestLoop loop = new TestLoop();
        PacketContext context = new PacketContext(null, loop);
        AtomicBoolean openingData = new AtomicBoolean();
        loop.execute(() -> context.enqueueWork(() -> openingData.set(true)));
        loop.execute(() -> assertTrue(openingData.get(), "vanilla screen construction requires the preceding opening data"));
        loop.pump();
        assertTrue(openingData.get(), "packet work must complete inside its original game-thread task");
        loop.pump();
    }

    private static final class TestLoop extends ReentrantBlockableEventLoop<Runnable> {
        private final Thread owner = Thread.currentThread();

        private TestLoop() {
            super("packet-order-test", false);
        }

        @Override
        public Runnable wrapRunnable(Runnable runnable) { return runnable; }

        @Override
        protected boolean shouldRun(Runnable runnable) { return true; }

        @Override
        protected boolean scheduleExecutables() { return true; }

        @Override
        protected Thread getRunningThread() { return owner; }

        void pump() { pollTask(); }
    }
}
