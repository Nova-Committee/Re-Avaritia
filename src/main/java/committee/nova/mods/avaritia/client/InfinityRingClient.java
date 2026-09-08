package committee.nova.mods.avaritia.client;

import committee.nova.mods.avaritia.common.net.S2CUpdateDimensionsPack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;

import java.util.Set;
import java.util.function.Consumer;

/** Client-only Infinity Ring dimension-list updates. */
public final class InfinityRingClient {
    private InfinityRingClient() {
    }

    public static void applyDimensions(S2CUpdateDimensionsPack packet) {
        applyDimensions(packet.keys(), packet.add());
    }

    public static void applyDimensions(Set<ResourceKey<Level>> keys, boolean add) {
        LocalPlayer player = Minecraft.getInstance().player;
        if (player == null) {
            return;
        }
        Set<ResourceKey<Level>> levels = player.connection.levels();
        if (levels == null) {
            return;
        }
        Consumer<ResourceKey<Level>> op = add ? levels::add : levels::remove;
        keys.forEach(op);
    }
}
