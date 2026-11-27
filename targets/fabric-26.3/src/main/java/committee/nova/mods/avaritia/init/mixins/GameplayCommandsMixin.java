package committee.nova.mods.avaritia.init.mixins;

import com.mojang.brigadier.CommandDispatcher;
import committee.nova.mods.avaritia.init.handler.CmdRegistryHandler;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Commands.class)
public abstract class GameplayCommandsMixin {
    @Shadow @Final private CommandDispatcher<CommandSourceStack> dispatcher;
    @Inject(method = "<init>", at = @At("TAIL"))
    private void avaritia$commands(Commands.CommandSelection selection, CommandBuildContext context, CallbackInfo ci) {
        CmdRegistryHandler.register(dispatcher);
    }
}
