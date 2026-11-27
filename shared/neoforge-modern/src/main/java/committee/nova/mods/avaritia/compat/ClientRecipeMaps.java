package committee.nova.mods.avaritia.compat;

import committee.nova.mods.avaritia.Const;
import net.minecraft.client.Minecraft;
import net.minecraft.world.item.crafting.RecipeMap;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
import net.neoforged.neoforge.client.event.RecipesReceivedEvent;

import java.lang.reflect.Method;

@EventBusSubscriber(modid = Const.MOD_ID, value = Dist.CLIENT)
public final class ClientRecipeMaps {
    private static RecipeMap syncedRecipes = RecipeMap.EMPTY;

    private ClientRecipeMaps() {
    }

    public static RecipeMap get() {
        if (!syncedRecipes.values().isEmpty()) {
            return syncedRecipes;
        }

        RecipeMap jeiRecipes = getJeiClientSyncedRecipes();
        if (!jeiRecipes.values().isEmpty()) {
            return jeiRecipes;
        }

        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.hasSingleplayerServer() && minecraft.getSingleplayerServer() != null) {
            return minecraft.getSingleplayerServer().getRecipeManager().recipeMap();
        }

        return RecipeMap.EMPTY;
    }

    @SubscribeEvent
    public static void onRecipesReceived(RecipesReceivedEvent event) {
        // 缓存 NeoForge 同步到客户端的自定义配方，供 JEI、Jade 等客户端集成共用。
        syncedRecipes = event.getRecipeMap();
    }

    @SubscribeEvent
    public static void onClientPlayerLoggedOut(ClientPlayerNetworkEvent.LoggingOut event) {
        syncedRecipes = RecipeMap.EMPTY;
    }

    private static RecipeMap getJeiClientSyncedRecipes() {
        try {
            Class<?> internal = Class.forName("mezz.jei.common.Internal");
            Method method = internal.getMethod("getClientSyncedRecipes");
            Object recipes = method.invoke(null);
            if (recipes instanceof RecipeMap recipeMap) {
                return recipeMap;
            }
        } catch (ReflectiveOperationException | LinkageError ignored) {
        }
        return RecipeMap.EMPTY;
    }
}
