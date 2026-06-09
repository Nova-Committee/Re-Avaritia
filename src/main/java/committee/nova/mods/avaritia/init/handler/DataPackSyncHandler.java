package committee.nova.mods.avaritia.init.handler;

import committee.nova.mods.avaritia.Const;
import committee.nova.mods.avaritia.common.net.S2CSingularitiesPacket;
import committee.nova.mods.avaritia.core.singularity.SingularityReloadListener;
import committee.nova.mods.avaritia.init.registry.ModRecipeTypes;
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
        // JEI/客户端只会收到这里声明的自定义配方类型；不发送会导致自定义配方分类为空。
        event.sendRecipes(
                ModRecipeTypes.CRAFTING_TABLE_RECIPE.get(),
                ModRecipeTypes.COMPRESSOR_RECIPE.get(),
                ModRecipeTypes.EXTREME_SMITHING_RECIPE.get()
        );

        ServerPlayer player = event.getPlayer();
        var message = new S2CSingularitiesPacket(SingularityReloadListener.INSTANCE.getDataSingularities().values(),
                SingularityReloadListener.INSTANCE.getRunSingularities().values());
        if (player != null) {
            PacketDistributor.sendToPlayer(player, message);
        } else {
            PacketDistributor.sendToAllPlayers(message);
        }
    }
}
