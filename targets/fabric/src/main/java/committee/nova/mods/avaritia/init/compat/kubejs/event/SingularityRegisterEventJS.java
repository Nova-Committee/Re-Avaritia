package committee.nova.mods.avaritia.init.compat.kubejs.event;

import committee.nova.mods.avaritia.Const;
import committee.nova.mods.avaritia.core.singularity.Singularity;
import committee.nova.mods.avaritia.core.singularity.SingularityReloadListener;
import committee.nova.mods.avaritia.core.singularity.SingularityValidationException;
import dev.latvian.mods.kubejs.event.EventJS;
import net.minecraft.resources.ResourceLocation;

import java.util.function.Consumer;

/**
 * @author cnlimiter
 */
public class SingularityRegisterEventJS extends EventJS {
    public void register(Object key) {
        this.register(key,singularity -> {});
    }

    public void register(Object key, Consumer<Singularity> consumer) {
        ResourceLocation id = parseId(key);
        if (id == null) {
            return;
        }
        try {
            Singularity singularity = new Singularity(id);
            consumer.accept(singularity);
            SingularityReloadListener.INSTANCE.registerScriptSingularity(
                    SingularityReloadListener.ScriptSource.KUBE_JS, singularity);
        } catch (RuntimeException exception) {
            Throwable validationFailure = findValidationFailure(exception);
            if (validationFailure == null) {
                throw exception;
            }
            Const.LOGGER.error("Singularity: Invalid KubeJS singularity {}; skipping this entry", id,
                    validationFailure);
        }
    }

    public void removeAll() {
        SingularityReloadListener.INSTANCE.setRemoveAll(
                SingularityReloadListener.ScriptSource.KUBE_JS, true);
    }

    public void removeAllRecipe() {
        SingularityReloadListener.INSTANCE.setRemoveAllRecipes(
                SingularityReloadListener.ScriptSource.KUBE_JS, true);
    }


    public void remove(Object key) {
        ResourceLocation id = parseId(key);
        if (id != null) {
            SingularityReloadListener.INSTANCE.removeSingularity(
                    SingularityReloadListener.ScriptSource.KUBE_JS, id);
        }
    }

    public void removeRecipe(Object key) {
        ResourceLocation id = parseId(key);
        if (id != null) {
            SingularityReloadListener.INSTANCE.removeSingularityRecipe(
                    SingularityReloadListener.ScriptSource.KUBE_JS, id);
        }
    }

    private static ResourceLocation parseId(Object key) {
        ResourceLocation id = key instanceof ResourceLocation resourceLocation
                ? resourceLocation
                : key == null ? null : ResourceLocation.tryParse(key.toString());
        if (id == null) {
            Const.LOGGER.error("Singularity: Invalid KubeJS singularity id {}; skipping this operation", key);
        }
        return id;
    }

    private static Throwable findValidationFailure(Throwable exception) {
        Throwable current = exception;
        while (current != null) {
            if (current instanceof SingularityValidationException) {
                return current;
            }
            current = current.getCause();
        }
        return null;
    }
}
