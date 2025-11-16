package committee.nova.mods.avaritia.init.compat.kubejs.event;

import committee.nova.mods.avaritia.core.singularity.Singularity;
import committee.nova.mods.avaritia.core.singularity.SingularityDataManager;
import dev.latvian.mods.kubejs.event.EventJS;
import net.minecraft.resources.ResourceLocation;

import java.util.function.Consumer;

/**
 * @author cnlimiter
 */
public class SingularityRegisterEventJS extends EventJS {
    public void register(ResourceLocation key) {
        this.register(key,singularity -> {});
    }

    public void register(ResourceLocation key, Consumer<Singularity> consumer) {
        Singularity singularity = new Singularity(key);
        consumer.accept(singularity);
        SingularityDataManager.getInstance().registerRuntimeSingularity(singularity);
    }
}
