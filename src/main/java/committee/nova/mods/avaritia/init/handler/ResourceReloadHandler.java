package committee.nova.mods.avaritia.init.handler;

import committee.nova.mods.avaritia.Static;
import committee.nova.mods.avaritia.api.init.event.RegisterRecipeCallback;
import committee.nova.mods.avaritia.util.RecipeUtil;
import net.fabricmc.fabric.api.resource.IdentifiableResourceReloadListener;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.ResourceManagerReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

/**
 * Description:
 * Author: cnlimiter
 * Date: 2022/5/15 11:40
 * Version: 1.0
 */
public class ResourceReloadHandler {


    public static void init() {
        ResourceManagerHelper.get(PackType.SERVER_DATA).registerReloadListener(new SingularityResourceReload());
        ResourceManagerHelper.get(PackType.SERVER_DATA).registerReloadListener(new RegisterRecipesReload());
    }


    private static class SingularityResourceReloadListener implements ResourceManagerReloadListener {
        @Override
        public void onResourceManagerReload(@NotNull ResourceManager manager) {
            SingularityRegistryHandler.onResourceManagerReload();
        }
    }

    private static class SingularityResourceReload implements IdentifiableResourceReloadListener {
        @Override
        public ResourceLocation getFabricId() {
            return Static.rl("singularity_reload");
        }

        @Override
        public @NotNull CompletableFuture<Void> reload(@NotNull PreparationBarrier preparationBarrier, @NotNull ResourceManager resourceManager,
                                                       @NotNull ProfilerFiller profilerFiller, @NotNull ProfilerFiller profilerFiller2,
                                                       @NotNull Executor executor, @NotNull Executor executor2) {
            return new SingularityResourceReload().reload(preparationBarrier, resourceManager, profilerFiller, profilerFiller2, executor, executor2);
        }
    }

    private static class RegisterRecipesReloadListener implements ResourceManagerReloadListener {
        @Override
        public void onResourceManagerReload(@NotNull ResourceManager manager) {
            RegisterRecipeCallback.EVENT.invoker().register(RecipeUtil.getRecipeManager());
        }
    }

    private static class RegisterRecipesReload implements IdentifiableResourceReloadListener {
        @Override
        public ResourceLocation getFabricId() {
            return Static.rl("recipes_reload");
        }

        @Override
        public @NotNull CompletableFuture<Void> reload(@NotNull PreparationBarrier preparationBarrier, @NotNull ResourceManager resourceManager,
                                                       @NotNull ProfilerFiller profilerFiller, @NotNull ProfilerFiller profilerFiller2,
                                                       @NotNull Executor executor, @NotNull Executor executor2) {
            return new RegisterRecipesReloadListener().reload(preparationBarrier, resourceManager, profilerFiller, profilerFiller2, executor, executor2);
        }
    }

}
