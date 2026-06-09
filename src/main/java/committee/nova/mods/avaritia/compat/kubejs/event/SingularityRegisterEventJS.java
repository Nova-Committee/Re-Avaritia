package committee.nova.mods.avaritia.compat.kubejs.event;

import committee.nova.mods.avaritia.Const;
import committee.nova.mods.avaritia.core.singularity.Singularity;
import committee.nova.mods.avaritia.core.singularity.SingularityReloadListener;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import dev.latvian.mods.kubejs.event.KubeEvent;
import dev.latvian.mods.kubejs.recipe.RecipesKubeEvent;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.RegistryOps;
import net.neoforged.neoforge.common.conditions.ConditionalOps;

import java.util.function.Consumer;

/**
 * 允许 KubeJS 在服务端脚本加载配方前增删运行时奇点。
 */
public class SingularityRegisterEventJS implements KubeEvent {
    private final RecipesKubeEvent event;

    public SingularityRegisterEventJS(RecipesKubeEvent event) {
        this.event = event;
    }

    public void register(Identifier key) {
        this.register(key, singularity -> {
        });
    }

    public void register(Identifier key, Consumer<Singularity> consumer) {
        Singularity singularity = new Singularity(key);
        consumer.accept(singularity);

        RegistryOps<JsonElement> ops = new ConditionalOps<>(this.event.ops.json(), this.event.registries);
        var decoded = Singularity.CONDITIONAL_CODEC.parse(ops,
                Singularity.CODEC.encodeStart(this.event.ops.json(), singularity).getOrThrow()
        ).getOrThrow(JsonParseException::new);

        decoded.ifPresentOrElse(
                conditional -> SingularityReloadListener.INSTANCE.registerSingularity(conditional.carrier()),
                () -> Const.LOGGER.debug("Singularity: Skipping loading singularity {} as its conditions were not met", key)
        );
    }

    public void removeAll() {
        SingularityReloadListener.INSTANCE.setRemoveAll(true);
    }

    public void removeAllRecipe() {
        SingularityReloadListener.INSTANCE.setRemoveAllRecipes(true);
    }

    public void remove(Identifier key) {
        SingularityReloadListener.INSTANCE.removeSingularity(key);
    }

    public void removeRecipe(Identifier key) {
        SingularityReloadListener.INSTANCE.removeSingularityRecipe(key);
    }
}
