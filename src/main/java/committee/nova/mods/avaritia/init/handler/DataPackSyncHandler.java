package committee.nova.mods.avaritia.init.handler;

import committee.nova.mods.avaritia.Const;
import committee.nova.mods.avaritia.common.net.S2CSingularitiesPack;
import committee.nova.mods.avaritia.core.singularity.SingularityReloadListener;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.OnDatapackSyncEvent;
import net.neoforged.neoforge.network.PacketDistributor;

/**
 * @author cnlimiter
 */
@EventBusSubscriber(modid = Const.MOD_ID)
public class DataPackSyncHandler {
    @SubscribeEvent
    public static void onDatapackSync(OnDatapackSyncEvent event) {
        ServerPlayer player = event.getPlayer();
        var message = new S2CSingularitiesPack(SingularityReloadListener.INSTANCE.getDataSingularities().values(),
                SingularityReloadListener.INSTANCE.getRunSingularities().values());
        if (player != null) {
            PacketDistributor.sendToPlayer(player, message);
        } else {
            PacketDistributor.sendToAllPlayers(message);
        }
    }
}
