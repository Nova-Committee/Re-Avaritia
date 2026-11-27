package committee.nova.mods.avaritia.gametest;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;

/** Loader entrypoint for the isolated gameplay regression source set. */
@Mod("avaritia_gametest")
public final class AvaritiaGameTestMod {
    // Equivox registration is gone: it has no 1.21.11 build, so its compat package is
    // excluded from main and its test items cannot be registered.
    public AvaritiaGameTestMod(IEventBus modBus) {
    }
}
