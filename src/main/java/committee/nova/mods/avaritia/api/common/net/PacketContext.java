package committee.nova.mods.avaritia.api.common.net;

import net.minecraft.server.level.ServerPlayer;
import java.util.concurrent.Executor;

/** Side-specific packet context. All game mutations are scheduled on the owning game thread. */
public final class PacketContext {
    private final ServerPlayer sender;
    private final Executor executor;
    private boolean handled;

    public PacketContext(ServerPlayer sender, Executor executor) {
        this.sender = sender;
        this.executor = executor;
    }

    public ServerPlayer getSender() { return sender; }
    public boolean isServerbound() { return sender != null; }
    public void enqueueWork(Runnable work) { executor.execute(work); }
    public void setPacketHandled(boolean handled) { this.handled = handled; }
    public boolean isPacketHandled() { return handled; }
}
