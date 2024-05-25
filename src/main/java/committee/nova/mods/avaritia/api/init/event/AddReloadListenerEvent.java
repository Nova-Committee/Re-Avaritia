package committee.nova.mods.avaritia.api.init.event;

import com.google.common.collect.ImmutableList;
import io.github.fabricators_of_create.porting_lib.core.event.BaseEvent;
import lombok.Getter;
import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.core.RegistryAccess;
import net.minecraft.server.ReloadableServerResources;
import net.minecraft.server.packs.resources.PreparableReloadListener;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.profiling.ProfilerFiller;

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
    private final RegistryAccess registryAccess;

    public AddReloadListenerEvent(ReloadableServerResources serverResources, RegistryAccess registryAccess)
    {
        this.serverResources = serverResources;
        this.registryAccess = registryAccess;
    }

    public void addListener(PreparableReloadListener listener)
    {
        listeners.add(new WrappedStateAwareListener(listener));
    }

    public List<PreparableReloadListener> getListeners()
    {
        return ImmutableList.copyOf(listeners);
    }

    public RegistryAccess getRegistryAccess()
    {
        return registryAccess;
    }

    private static class WrappedStateAwareListener implements PreparableReloadListener {
        private final PreparableReloadListener wrapped;

        private WrappedStateAwareListener(final PreparableReloadListener wrapped) {
            this.wrapped = wrapped;
        }

        @Override
        public CompletableFuture<Void> reload(final PreparationBarrier stage, final ResourceManager resourceManager, final ProfilerFiller preparationsProfiler, final ProfilerFiller reloadProfiler, final Executor backgroundExecutor, final Executor gameExecutor) {
                return wrapped.reload(stage, resourceManager, preparationsProfiler, reloadProfiler, backgroundExecutor, gameExecutor);
        }
    }

    @Override
    public void sendEvent() {
        RELOAD.invoker().post(this);
    }
}
