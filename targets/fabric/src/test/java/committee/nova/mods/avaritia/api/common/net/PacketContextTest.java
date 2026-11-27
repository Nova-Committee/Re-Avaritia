package committee.nova.mods.avaritia.api.common.net;

import net.minecraft.util.thread.ReentrantBlockableEventLoop;
import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.jupiter.api.Assertions.assertTrue;

class PacketContextTest {
    @Test
    void openingDataRunsBeforeFollowingVanillaScreenTask() {
        ReentrantBlockableEventLoop<Runnable> loop = new ReentrantBlockableEventLoop<>("packet-order-test") {
            private final Thread owner = Thread.currentThread();

            @Override
            protected Runnable wrapRunnable(Runnable runnable) { return runnable; }

            @Override
            protected boolean shouldRun(Runnable runnable) { return true; }

            @Override
            protected Thread getRunningThread() { return owner; }
        };
        PacketContext context = new PacketContext(null, loop);
        AtomicBoolean openingData = new AtomicBoolean();
        loop.tell(() -> context.enqueueWork(() -> openingData.set(true)));
        loop.tell(() -> assertTrue(openingData.get(), "vanilla screen construction requires the preceding opening data"));
        loop.pollTask();
        assertTrue(openingData.get(), "packet work must complete inside its original game-thread task");
        loop.pollTask();
    }
}
