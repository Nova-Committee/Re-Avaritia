package committee.nova.mods.avaritia.init.handler;

import committee.nova.mods.avaritia.common.net.S2CSingularitiesPack;
import committee.nova.mods.avaritia.core.singularity.SingularityDataManager;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.OnDatapackSyncEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.network.PacketDistributor;

/**
 * @author cnlimiter
 */
@Mod.EventBusSubscriber
public class DataPackSyncHandler {
    @SubscribeEvent
    public static void onDatapackSync(OnDatapackSyncEvent event) {
        ServerPlayer player = event.getPlayer();
        var cacheMsg = new S2CSingularitiesPack(SingularityDataManager.getInstance().getCachedSingularities().values());
        var runtimeMsg = new S2CSingularitiesPack(SingularityDataManager.getInstance().getRuntimeSingularities().values());

        if (player != null) {
            NetworkHandler.CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), cacheMsg);
            NetworkHandler.CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), runtimeMsg);
        } else {
            NetworkHandler.CHANNEL.send(PacketDistributor.ALL.noArg(), cacheMsg);
            NetworkHandler.CHANNEL.send(PacketDistributor.ALL.noArg(), runtimeMsg);
        }
    }
}
