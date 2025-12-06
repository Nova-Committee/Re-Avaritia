package committee.nova.mods.avaritia.init.compat.kubejs.event;

import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.mojang.serialization.JsonOps;
import committee.nova.mods.avaritia.Const;
import committee.nova.mods.avaritia.core.singularity.Singularity;
import committee.nova.mods.avaritia.init.data.listener.SingularityReloadListener;
import dev.latvian.mods.kubejs.event.KubeEvent;
import dev.latvian.mods.kubejs.recipe.RecipesKubeEvent;
import net.minecraft.resources.RegistryOps;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.common.conditions.ConditionalOps;

import java.util.Map;
import java.util.function.Consumer;

/**
 * @author cnlimiter
 */
public class SingularityRegisterEventJS implements KubeEvent {
    private final RecipesKubeEvent event;
    private final Map<ResourceLocation, JsonElement> recipeJsons;
    public SingularityRegisterEventJS(RecipesKubeEvent event, Map<ResourceLocation, JsonElement> recipeJsons) {
        this.recipeJsons = recipeJsons;
        this.event = event;
    }
    public void register(ResourceLocation key) {
        this.register(key,singularity -> {});
    }

    public void register(ResourceLocation key, Consumer<Singularity> consumer) {
        Singularity singularity = new Singularity(key);
        consumer.accept(singularity);
        RegistryOps<JsonElement> registryops = new ConditionalOps<>(event.ops.json(), event.registries);
        var decoded = Singularity.CONDITIONAL_CODEC.parse(registryops,
                Singularity.CODEC.encodeStart(event.ops.json(), singularity).getOrThrow()
                ).getOrThrow(JsonParseException::new);
        decoded.ifPresentOrElse(r -> {
            var carrier = r.carrier();
            SingularityReloadListener.INSTANCE.registerSingularity(carrier);
        }, () -> {
            Const.LOGGER.debug("Singularity: Skipping loading singularity {} as its conditions were not met", key);
        });

    }
}
