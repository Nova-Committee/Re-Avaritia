package committee.nova.mods.avaritia.init.handler;

import committee.nova.mods.avaritia.Const;
import committee.nova.mods.avaritia.init.test.TestCmd;
import net.minecraft.commands.Commands;
import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.commands.CommandSourceStack;

/**
 * @author cnlimiter
 */
public class CmdRegistryHandler {
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(
                Commands.literal(Const.MOD_ID)
                        .then(
                                Commands.literal("test").executes(TestCmd::run)
                        )
        );
    }
}
