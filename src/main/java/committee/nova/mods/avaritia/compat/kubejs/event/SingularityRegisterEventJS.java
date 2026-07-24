package committee.nova.mods.avaritia.compat.kubejs.event;

import committee.nova.mods.avaritia.Const;
import committee.nova.mods.avaritia.core.singularity.Singularity;
import committee.nova.mods.avaritia.core.singularity.SingularityReloadListener;
import committee.nova.mods.avaritia.core.singularity.SingularityValidationException;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import dev.latvian.mods.kubejs.event.KubeEvent;
import dev.latvian.mods.kubejs.recipe.RecipesKubeEvent;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.RegistryOps;
import net.neoforged.neoforge.common.conditions.ConditionalOps;
import net.neoforged.neoforge.common.conditions.WithConditions;

import java.util.Optional;
import java.util.function.Consumer;

/** 允许 KubeJS 在配方加载期间以事务方式增删奇点。 */
public class SingularityRegisterEventJS implements KubeEvent {
    private final RecipesKubeEvent event;

    public SingularityRegisterEventJS(RecipesKubeEvent event) {
        this.event = event;
    }

    public void register(Object key) {
        register(key, singularity -> {
        });
    }

    public void register(Object key, Consumer<Singularity> consumer) {
        Identifier id = parseId(key);
        if (id == null) {
            return;
        }

        Singularity singularity = new Singularity(id);
        try {
            consumer.accept(singularity);
        } catch (RuntimeException exception) {
            Throwable validationFailure = findValidationFailure(exception);
            if (validationFailure == null) {
                throw exception;
            }
            Const.LOGGER.error("Singularity: Invalid KubeJS singularity {}; skipping this entry", id,
                    validationFailure);
            return;
        }

        try {
            if (singularity.getConditions().isEmpty()) {
                SingularityReloadListener.INSTANCE.registerScriptSingularity(
                        SingularityReloadListener.ScriptSource.KUBE_JS, singularity);
                return;
            }
            RegistryOps<JsonElement> registryOps = new ConditionalOps<>(this.event.ops.json(), this.event.registries);
            JsonElement encoded = Singularity.CONDITIONAL_CODEC.encodeStart(
                            this.event.ops.json(),
                            Optional.of(new WithConditions<>(singularity.getConditions(), singularity)))
                    .getOrThrow(JsonParseException::new);
            var decoded = Singularity.CONDITIONAL_CODEC.parse(registryOps, encoded)
                    .getOrThrow(JsonParseException::new);
            decoded.ifPresentOrElse(withConditions ->
                            SingularityReloadListener.INSTANCE.registerScriptSingularity(
                                    SingularityReloadListener.ScriptSource.KUBE_JS, withConditions.carrier()),
                    () -> Const.LOGGER.debug(
                            "Singularity: Skipping KubeJS singularity {} because its conditions were not met", id));
        } catch (JsonParseException | SingularityValidationException exception) {
            Const.LOGGER.error("Singularity: Invalid KubeJS singularity {}; skipping this entry", id, exception);
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
        Identifier id = parseId(key);
        if (id != null) {
            SingularityReloadListener.INSTANCE.removeSingularity(
                    SingularityReloadListener.ScriptSource.KUBE_JS, id);
        }
    }

    public void removeRecipe(Object key) {
        Identifier id = parseId(key);
        if (id != null) {
            SingularityReloadListener.INSTANCE.removeSingularityRecipe(
                    SingularityReloadListener.ScriptSource.KUBE_JS, id);
        }
    }

    private static Identifier parseId(Object key) {
        Identifier id = key instanceof Identifier identifier
                ? identifier
                : key == null ? null : Identifier.tryParse(key.toString());
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
