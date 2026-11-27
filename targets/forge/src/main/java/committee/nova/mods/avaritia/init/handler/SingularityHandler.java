package committee.nova.mods.avaritia.init.handler;

import committee.nova.mods.avaritia.core.singularity.SingularityEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * @author cnlimiter
 */
@Mod.EventBusSubscriber
public class SingularityHandler {

    /**
     * 重新生成压缩机配方
     * 在奇点数据重载完成后调用，确保压缩机配方使用最新的奇点数据
     */
    @SubscribeEvent
    public static void onReloadSingularity(SingularityEvent.Reload event) {
    }
}
