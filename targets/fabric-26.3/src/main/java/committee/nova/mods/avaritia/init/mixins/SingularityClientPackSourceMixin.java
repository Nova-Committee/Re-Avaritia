package committee.nova.mods.avaritia.init.mixins;

import committee.nova.mods.avaritia.init.handler.PackResourceHandler;
import net.minecraft.client.resources.ClientPackSource;
import net.minecraft.server.packs.repository.Pack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Function;

@Mixin(ClientPackSource.class)
public abstract class SingularityClientPackSourceMixin {
    @Inject(method = "populatePackList", at = @At("TAIL"))
    private void avaritia$optionalArtPack(BiConsumer<String, Function<String, Pack>> consumer, CallbackInfo callback) {
        Map<String, Pack> packs = new LinkedHashMap<>();
        PackResourceHandler.addPack(packs);
        packs.forEach((id, pack) -> consumer.accept(id, ignored -> pack));
    }
}
