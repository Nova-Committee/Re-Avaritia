package committee.nova.mods.avaritia.init.mixins;

import com.google.gson.JsonElement;
import committee.nova.mods.avaritia.api.init.data.LootResourceConditions;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.storage.loot.LootDataType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import java.util.Optional;

@Mixin(LootDataType.class)
public abstract class RegistryLootTypeConditionsMixin {
    @Inject(method = "deserialize", at = @At("HEAD"), cancellable = true)
    private void avaritia$modConditions(ResourceLocation id, JsonElement resource,
                                        CallbackInfoReturnable<Optional<?>> cir) {
        if (resource.isJsonObject() && !LootResourceConditions.prepare(
                (LootDataType<?>) (Object) this, id, resource.getAsJsonObject())) {
            cir.setReturnValue(Optional.empty());
        }
    }
}
