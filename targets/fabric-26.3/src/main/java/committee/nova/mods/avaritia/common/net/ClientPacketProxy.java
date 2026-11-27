package committee.nova.mods.avaritia.common.net;

import java.util.function.Consumer;

/** Client-assigned packet sinks; no client classes are resolved on the dedicated server. */
public final class ClientPacketProxy {
    private static Consumer<Object> handler;

    public static void initialize(Consumer<Object> clientHandler) {
        handler = java.util.Objects.requireNonNull(clientHandler);
    }

    public static void handle(Object packet) {
        if (handler == null) throw new IllegalStateException("Client packet handlers are not initialized");
        handler.accept(packet);
    }
    public static Consumer<S2CInfinityRingOpenPack> infinityRingOpen = ClientPacketProxy::uninitialized;
    public static Consumer<S2CUpdateDimensionsPack> updateDimensions = ClientPacketProxy::uninitialized;
    public static Consumer<S2CNeutronRingOpenPack> neutronRingOpen = ClientPacketProxy::uninitialized;
    public static Consumer<S2CNeutronRingPreviewPack> neutronRingPreview = ClientPacketProxy::uninitialized;

    private static void uninitialized(Object packet) {
        throw new IllegalStateException("Client packet handler is not initialized: " + packet.getClass().getName());
    }

    private ClientPacketProxy() {
    }
}
