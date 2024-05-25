package committee.nova.mods.avaritia.api.init.event;

import com.google.common.collect.ImmutableList;
import io.github.fabricators_of_create.porting_lib.core.event.BaseEvent;
import lombok.Getter;
import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.core.RegistryAccess;
import net.minecraft.server.ReloadableServerResources;
import net.minecraft.server.packs.resources.PreparableReloadListener;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.profiling.ProfilerFiller;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

/**
 * AddReloadListenerEvent
 *
 * @author cnlimiter
 * @version 1.0
 * @description
 * @date 2024/5/24 上午5:03
 */
public class AddReloadListenerEvent extends BaseEvent {
    public static Event<AddReloadListenerEvent.Reload> RELOAD = EventFactory.createArrayBacked(AddReloadListenerEvent.Reload.class, callbacks -> ((event) -> {
        for (AddReloadListenerEvent.Reload e : callbacks){
            e.post(event);
        }
    }));

    public interface Reload {
        void post(AddReloadListenerEvent event);
    }

    private final List<PreparableReloadListener> listeners = new ArrayList<>();
    @Getter
    private final ReloadableServerResources serverResources;

    public AddReloadListenerEvent(ReloadableServerResources serverResources)
    {
        this.serverResources = serverResources;
    }

    public void addListener(PreparableReloadListener listener)
    {
        listeners.add(new WrappedStateAwareListener(listener));
    }

    public List<PreparableReloadListener> getListeners()
    {
        return ImmutableList.copyOf(listeners);
    }


    private record WrappedStateAwareListener(PreparableReloadListener wrapped) implements PreparableReloadListener {

        @Override
            public @NotNull CompletableFuture<Void> reload(final @NotNull PreparationBarrier stage, final @NotNull ResourceManager resourceManager,
                                                           final @NotNull ProfilerFiller preparationsProfiler, final @NotNull ProfilerFiller reloadProfiler,
                                                           final @NotNull Executor backgroundExecutor, final @NotNull Executor gameExecutor) {
                if (FabricLoader.getInstance().isModLoaded("avaritia")) return wrapped.reload(stage, resourceManager, preparationsProfiler, reloadProfiler, backgroundExecutor, gameExecutor);
                else return CompletableFuture.completedFuture(null);
            }
        }

    @Override
    public void sendEvent() {
        RELOAD.invoker().post(this);
    }
}
