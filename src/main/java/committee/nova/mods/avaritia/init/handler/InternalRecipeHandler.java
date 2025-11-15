package committee.nova.mods.avaritia.init.handler;

import committee.nova.mods.avaritia.Const;
import committee.nova.mods.avaritia.api.init.event.RegisterRecipesEvent;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;

/**
 * Description:
 * Author: cnlimiter
 * Date: 2022/5/15 20:34
 * Version: 1.0
 */
@EventBusSubscriber(modid = Const.MOD_ID)
public class InternalRecipeHandler {
    @SubscribeEvent
    public static void onRegisterRecipes(RegisterRecipesEvent event) {
    }
}
