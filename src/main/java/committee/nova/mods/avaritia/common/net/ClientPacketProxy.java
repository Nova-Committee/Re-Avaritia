package committee.nova.mods.avaritia.common.net;

import java.util.function.Consumer;

/** Client-assigned packet sinks. Dedicated server keeps the no-op defaults. */
public final class ClientPacketProxy {
    public static Consumer<S2CNeutronRingOpenPack> neutronRingOpen = packet -> {
    };
    public static Consumer<S2CNeutronRingPreviewPack> neutronRingPreview = packet -> {
    };
    public static Consumer<S2CInfinityRingOpenPack> infinityRingOpen = packet -> {
    };

    private ClientPacketProxy() {
    }
}
