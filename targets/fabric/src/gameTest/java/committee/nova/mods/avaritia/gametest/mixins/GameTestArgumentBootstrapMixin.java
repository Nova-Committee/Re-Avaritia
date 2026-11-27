package committee.nova.mods.avaritia.gametest.mixins;

import net.minecraft.commands.synchronization.ArgumentTypeInfos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.objectweb.asm.Opcodes;

/** Test commands and their network serializers must be enabled in the same bootstrap. */
@Mixin(ArgumentTypeInfos.class)
public abstract class GameTestArgumentBootstrapMixin {
    @Redirect(method = "bootstrap", at = @At(value = "FIELD",
            target = "Lnet/minecraft/SharedConstants;IS_RUNNING_IN_IDE:Z", opcode = Opcodes.GETSTATIC))
    private static boolean avaritia$registerNativeGameTestArguments() {
        return true;
    }
}
