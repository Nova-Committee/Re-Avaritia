package committee.nova.mods.avaritia.init.compat.kubejs.event;

import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import committee.nova.mods.avaritia.Const;
import committee.nova.mods.avaritia.core.singularity.Singularity;
import committee.nova.mods.avaritia.core.singularity.SingularityReloadListener;
import dev.latvian.mods.kubejs.event.KubeEvent;
import dev.latvian.mods.kubejs.recipe.RecipesKubeEvent;
import net.minecraft.resources.RegistryOps;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.common.conditions.ConditionalOps;
import net.neoforged.neoforge.common.conditions.WithConditions;

import java.util.Optional;
import java.util.function.Consumer;

/**
 * @author cnlimiter
 */
public class SingularityRegisterEventJS implements KubeEvent {
    private final RecipesKubeEvent event;
    public SingularityRegisterEventJS(RecipesKubeEvent event) {
        this.event = event;
    }
    public void register(ResourceLocation key) {
        this.register(key,singularity -> {});
    }

    public void register(ResourceLocation key, Consumer<Singularity> consumer) {
        try {
            Singularity singularity = new Singularity(key);
            consumer.accept(singularity);
            if (singularity.getConditions().isEmpty()) {
                SingularityReloadListener.INSTANCE.registerScriptSingularity(singularity);
                return;
            }
            RegistryOps<JsonElement> registryOps = new ConditionalOps<>(event.ops.json(), event.registries);
            JsonElement encoded = Singularity.CONDITIONAL_CODEC.encodeStart(
                    event.ops.json(), Optional.of(new WithConditions<>(singularity.getConditions(), singularity)))
                    .getOrThrow(JsonParseException::new);
            var decoded = Singularity.CONDITIONAL_CODEC.parse(registryOps, encoded)
                    .getOrThrow(JsonParseException::new);
            decoded.ifPresentOrElse(withConditions ->
                            SingularityReloadListener.INSTANCE.registerScriptSingularity(withConditions.carrier()),
                    () -> Const.LOGGER.debug(
                            "Singularity: Skipping KubeJS singularity {} because its conditions were not met", key));
        } catch (IllegalArgumentException | JsonParseException exception) {
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
