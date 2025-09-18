package committee.nova.mods.avaritia.init.test;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.commands.CommandSourceStack;

/**
 * @author: cnlimiter
 */
public class TestCmd {
    public static int run(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        return Command.SINGLE_SUCCESS;
    }
}
