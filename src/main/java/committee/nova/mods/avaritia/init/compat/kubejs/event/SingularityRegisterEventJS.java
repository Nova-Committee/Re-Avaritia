package committee.nova.mods.avaritia.init.compat.kubejs.event;

import committee.nova.mods.avaritia.Const;
import committee.nova.mods.avaritia.core.singularity.Singularity;
import committee.nova.mods.avaritia.core.singularity.SingularityReloadListener;
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
        try {
            Singularity singularity = new Singularity(key);
            consumer.accept(singularity);
            SingularityReloadListener.INSTANCE.registerScriptSingularity(singularity);
        } catch (IllegalArgumentException exception) {
            Const.LOGGER.error("Singularity: Invalid KubeJS singularity {}; skipping this entry", key, exception);
        }
    }

    public void removeAll() {
        SingularityReloadListener.INSTANCE.setRemoveAll(true);
    }

    public void removeAllRecipe() {
        SingularityReloadListener.INSTANCE.setRemoveAllRecipes(true);
    }


    public void remove(ResourceLocation key) {
        SingularityReloadListener.INSTANCE.removeSingularity(key);
    }

    public void removeRecipe(ResourceLocation key) {
        SingularityReloadListener.INSTANCE.removeSingularityRecipe(key);
    }
}
