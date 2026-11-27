package committee.nova.mods.avaritia.client;

import committee.nova.mods.avaritia.Const;
import committee.nova.mods.avaritia.client.screen.AvaritiaConfigScreen;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;

/**
 * 注册仅客户端可用的模组入口。
 */
@Mod(value = Const.MOD_ID, dist = Dist.CLIENT)
public final class AvaritiaClientMod {
    public AvaritiaClientMod(ModContainer modContainer) {
        modContainer.registerExtensionPoint(IConfigScreenFactory.class,
                (container, parent) -> new AvaritiaConfigScreen(parent));
    }
}
