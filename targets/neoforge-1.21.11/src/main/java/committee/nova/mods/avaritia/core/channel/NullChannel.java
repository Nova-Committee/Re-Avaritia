package committee.nova.mods.avaritia.core.channel;

/** Immutable sentinel returned for absent or deleted channels. */
public final class NullChannel extends ServerChannel {
    public static final NullChannel INSTANCE = new NullChannel();

    private NullChannel() {
        super(new Data("Removed channel", java.util.List.of(), java.util.List.of(), 0));
        setRemoved();
    }
}
