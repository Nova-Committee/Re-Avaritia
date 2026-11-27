package committee.nova.mods.avaritia.init.mixins;

import committee.nova.mods.avaritia.client.AvaritiaForgeClient;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Options;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Append controls before vanilla reads saved key bindings; controls remain fully configurable. */
@Mixin(Options.class)
public abstract class ClientOptionsMixin {
    @Shadow @Final @Mutable public KeyMapping[] keyMappings;
    @Inject(method="<init>", at=@At(value="INVOKE",target="Lnet/minecraft/client/Options;load()V"))
    private void avaritia$keys(CallbackInfo ci) {
        int length = keyMappings.length;
        keyMappings = java.util.Arrays.copyOf(keyMappings, length + 3);
        keyMappings[length] = AvaritiaForgeClient.FILTER_KEY;
        keyMappings[length + 1] = AvaritiaForgeClient.RING_KEY;
        keyMappings[length + 2] = AvaritiaForgeClient.CONFIG_KEY;
    }
}
