package committee.nova.mods.avaritia.gametest;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;

/** Loader entrypoint for the isolated gameplay regression source set. */
@Mod("avaritia_gametest")
public final class AvaritiaGameTestMod {
    public AvaritiaGameTestMod(IEventBus modBus) {
        EquivoxGameTestItems.ITEMS.register(modBus);
    }
}
